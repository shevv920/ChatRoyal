package io.chatroyal.actors.tank

import com.badlogic.gdx.{Gdx, math}
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.msg.{MessageDispatcher, Telegram}
import com.badlogic.gdx.graphics.g2d.{
  ParticleEffect,
  ParticleEffectPool,
  SpriteBatch,
  TextureRegion
}
import com.badlogic.gdx.math.{Circle, MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d.{Fixture, RayCastCallback, World}
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions._
import com.badlogic.gdx.scenes.scene2d.actions.{DelayAction, RotateByAction}
import com.badlogic.gdx.utils.{Align, Pool}
import io.chatroyal.ChatRoyal
import io.chatroyal.actors.{Character, CharacterState, JustSpawned, PlayZone}
import io.chatroyal.ai.AStarPathFinding

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
class Tank(val world: World,
           val pathFinding: AStarPathFinding,
           val messageDispatcher: MessageDispatcher,
           sx: Float,
           sy: Float,
           name: String,
           val baseTexture: TextureRegion,
           val turretTexture: TextureRegion)
    extends Character(name) {

  import Tank._
  setPosition(sx, sy)
  setWidth(baseTexture.getRegionWidth)
  setHeight(baseTexture.getRegionHeight)
  setOrigin(Align.center)
  setScale(Tank.scale)

  val visionCheckInterval: Float = random.nextFloat() * 5f + 1f
  var lastVisionCheck: Long = 0
  val tempVector = new Vector2()
  val posTempVec = new Vector2(getX, getY)
  val knownEnemies = new mutable.HashSet[Character]()
  private var lastShotTime = 0L
  private var health = 100
  private var lastTargetVisibleCheckTime = 0L
  def getHealth: Int = health

  val oX: Float = getOriginX
  val oY: Float = getOriginY

  val turret: Turret = new Turret(this)
  turret.setWidth(turretTexture.getRegionWidth)
  turret.setHeight(turretTexture.getRegionHeight)
  turret.setOriginX(turret.getWidth / 2f)
  turret.setOriginY(turret.getHeight / 4.3f)

  val effects: com.badlogic.gdx.utils.Array[ParticleEffectPool#PooledEffect] =
    new com.badlogic.gdx.utils.Array()

  override val stateMachine =
    new DefaultStateMachine[Character, CharacterState](this, JustSpawned)

  def ifTargetVisibleCallback(target: Character): RayCastCallback =
    (fixture: Fixture, point1: Vector2, point2: Vector2, fraction: Float) => {
      val body = fixture.getBody
      body.getUserData match {
        case t: Tank if t.getName == target.getName =>
        //visible
        case _ =>
          //not visible
          messageDispatcher.dispatchMessage(
            this,
            this,
            CharacterState.TARGET_LOST
          )
      }
      0
    }

  def shootCallback: RayCastCallback =
    (fixture: Fixture, point1: Vector2, point2: Vector2, fraction: Float) => {
      val body = fixture.getBody
      body.getUserData match {
        case tank: Tank =>
          messageDispatcher.dispatchMessage(
            this,
            tank,
            CharacterState.BEING_HIT,
            this
          )
          0
        case a =>
          0
      }
    }

  def regularVisionCheckCallback(): RayCastCallback =
    (fixture: Fixture, _: Vector2, _: Vector2, _: Float) => {
      val body = fixture.getBody
      body.getUserData match {
        case tank: Tank =>
          if (knownEnemies.contains(tank)) {
            //
          } else {
            messageDispatcher.dispatchMessage(
              this,
              this,
              CharacterState.ENEMY_DETECTED,
              tank
            )
          }
          0
        case _ =>
          0
      }
    }

  override def setTarget(t: Character): Unit = {
    super.setTarget(t)
  }

  def aimAt(t: Character): Unit = {
    val angle = ChatRoyal.calcAngleTo(
      posTempVec.set(getX, getY),
      tempVector.set(t.getX, t.getY)
    )
    if (Math.abs(turret.getRotation - angle) >= 5) {
      val a = rotateToActionPool.obtain()
      a.setActor(turret)
      a.setTargetPos(t.getX, t.getY)
      turret.clearActions()
      turret.addAction(sequence(a, turret.onAimFinishAction))
    }
  }

  override def doTick(delta: Float): Unit = this.act(delta)

  override def draw(batch: SpriteBatch): Unit = {
    val x = getX
    val y = getY
    val width = getWidth
    val height = getHeight
    val scaleX = getScaleX
    val scaleY = getScaleY
    val rotation = getRotation
    batch.draw(
      baseTexture,
      x - oX,
      y - oY,
      oX,
      oY,
      width,
      height,
      pixelsToMeters(scaleX),
      pixelsToMeters(scaleY),
      rotation
    )
    batch.draw(
      turretTexture,
      x - turret.getOriginX,
      y - turret.getOriginY,
      turret.getOriginX,
      turret.getOriginY,
      turret.getWidth,
      turret.getHeight,
      turretScale,
      turretScale,
      turret.getRotation
    )
    drawEffects(batch)
  }

  private def drawEffects(batch: SpriteBatch): Unit = {
    @scala.annotation.tailrec
    def loop(i: Int): Unit = {
      if(i >= 0 ) {
        val effect = effects.get(i)
        effect.draw(batch)
        if (effect.isComplete) {
          effect.free()
          effects.removeIndex(i)
        }
        loop(i-1)
      }
    }
    loop(effects.size - 1)
  }

  override def act(delta: Float): Unit = {
    stateMachine.update()
    turret.act(delta)
    effects.forEach(_.update(delta))
    RegularVisionCheck()
    super.act(delta)
  }

  private def RegularVisionCheck():Unit = {
    if (ChatRoyal.currentTimeMillis - lastVisionCheck >= visionCheckInterval) {
      for (angle <- visionCheckRange) {
        ChatRoyal.angleToVector(tempVector, angle * MathUtils.degreesToRadians)
        posTempVec.set(getX, getY).add(tempVector.scl(visionDistance))
        world.rayCast(
          regularVisionCheckCallback(),
          getX,
          getY,
          posTempVec.x,
          posTempVec.y
        )
      }
      lastVisionCheck = ChatRoyal.currentTimeMillis
    }
  }

  def onAimFinished(): Unit = {
    if (ChatRoyal.currentTimeMillis - lastShotTime >= shootCooldown) {
      lastShotTime = ChatRoyal.currentTimeMillis
      val eff: ParticleEffectPool#PooledEffect = shootEffectPool.obtain()
      val (x, y) = turretTipPosition()
      eff.setPosition(x, y)
      eff.allowCompletion()
      eff.scaleEffect(0.01f)
      eff.start()
      effects.add(eff)
      ChatRoyal.angleToVector(
        tempVector,
        turret.getRotation * MathUtils.degreesToRadians
      )
      posTempVec.set(getX, getY).add(tempVector.scl(visionDistance))
      world.rayCast(shootCallback, getX, getY, posTempVec.x, posTempVec.y)
    } else {
      val delayAction = delayActionPool.obtain()
      delayAction.setDuration(
        (ChatRoyal.currentTimeMillis - lastShotTime) / 1000f
      )
      turret.addAction(sequence(delayAction, turret.onAimFinishAction))
    }
  }

  def turretTipPosition(): (Float, Float) = {
    val angle = turret.getRotation
    val vector =
      ChatRoyal.angleToVector(tempVector, angle * MathUtils.degreesToRadians)
    val posVector = new Vector2(getX, getY)
    val res = posVector.add(vector.scl(0.9f))
    (res.x, res.y)
  }

  override def handleMessage(msg: Telegram): Boolean =
    stateMachine.handleMessage(msg)

  override def onHit(attacker:Character): Unit = {
    health -= 25
    if(health <= 0) {
      messageDispatcher.dispatchMessage(this, attacker, CharacterState.FRAG_SCORED, this)
    }
  }

  override def dispose(): Unit = {
    clear()
    effects.clear()
  }

  override def checkIfTargetVisible(): Unit = {
    if (ChatRoyal.currentTimeMillis - lastTargetVisibleCheckTime >= targetVisibleCheckInterval) {
      for {
        target <- getTarget
        cb = ifTargetVisibleCallback(target)
        _ = {
          ChatRoyal.angleToVector(
            tempVector,
            turret.getRotation * MathUtils.degreesToRadians
          )
          posTempVec.set(getX, getY).add(tempVector.scl(visionDistance))
          world.rayCast(cb, getX, getY, posTempVec.x, posTempVec.y)
        }
      } yield ()
      lastTargetVisibleCheckTime = ChatRoyal.currentTimeMillis
    }
  }

  override def moveToPlayZone(): Unit = {
    val (rX, rY) =
      pathFinding.getRandomPos(PlayZone.getZoneX, PlayZone.getZoneY, PlayZone.getZoneRadius)
    for {
      path <- moveToLocation(tempVector.set(rX, rY))
      _ = this.clearActions()
      _ = this.addAction(sequence(path: _*))
    } yield ()
  }

  def moveToLocation(loc: Vector2): Option[Array[Action]] =
    Tank.moveToLocation(pathFinding, this, loc)
}
object Tank {
  val scale = 0.4f
  val turretScale: Float = pixelsToMeters(scale * 0.9f)
  val RADAR_DISTANCE = 15f
  val TURN_SPEED = 190f
  val MOVE_SPEED = 2f //tile(1m)/sec
  val random = new Random()
  val visionDistance = 8
  val visionCheckRange: Range = 0 to 360 by 15
  val targetVisibleCheckInterval = 1000L
  val shootCooldown = 3000L

  val moveToActionPool: Pool[TankMoveToAction] = () => new TankMoveToAction
  val rotateToActionPool: Pool[TankRotateAction] = () => new TankRotateAction
  val rotateByActionPool: Pool[RotateByAction] = () => new RotateByAction
  val delayActionPool: Pool[DelayAction] = () => new DelayAction
  val shootEffect = new ParticleEffect()
  shootEffect.load(
    Gdx.files.internal("effects/Particle Park Muzzle Flash.p"),
    Gdx.files.internal("effects")
  )

  val shootEffectPool: ParticleEffectPool =
    new ParticleEffectPool(shootEffect, 100, 200) //todo

  def moveToLocation(pathFinding: AStarPathFinding,
                     tank: Tank,
                     loc: Vector2): Option[Array[Action]] = {
    val nodePath =
      pathFinding.findPathNodes(new Vector2(tank.getX, tank.getY), loc)
    nodePath match {
      case Some(array) =>
        val arrayBuffer = new ArrayBuffer[Action]()
        array.forEach { node =>
          val rotateAction = rotateToActionPool.obtain()
          rotateAction.setTargetPos(node.x + 0.5f, node.y + 0.5f)
          val moveAction = moveToActionPool.obtain()
          moveAction.setPosition(node.x + 0.5f, node.y + 0.5f)
          arrayBuffer += rotateAction
          arrayBuffer += moveAction
        }
        Some(arrayBuffer.toArray)
      case _ => None
    }
  }

  def pixelsToMeters(pixelValue: Float): Float =
    pixelValue * ChatRoyal.UNIT_SCALE

}

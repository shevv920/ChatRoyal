package io.chatroyal.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.ai.msg.MessageManager
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.{SpriteBatch, TextureAtlas}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.{BodyDef, FixtureDef, PolygonShape, World}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, Screen}
import io.chatroyal.ChatRoyal.{PPM, UNIT_SCALE}
import io.chatroyal.actors.{CharacterState, PlayZone}
import io.chatroyal.actors.tank.Tank
import io.chatroyal.ai.AStarPathFinding
import io.chatroyal.components.{B2DBody, CharacterC, Control}
import io.chatroyal.systems._

class TheGame(val assets: AssetManager,
              val names: List[String],
              val world: World,
              val map: TiledMap,
              val pathFinding: AStarPathFinding)
    extends Screen {

  val viewport = new ScreenViewport()
  val spriteBatch = new SpriteBatch()
  val mapRenderer =
    new OrthogonalTiledMapRenderer(map, UNIT_SCALE, spriteBatch)

  //ashley entity engine
  val engine = new PooledEngine()

  val stage = new Stage(viewport)
  val shapeRenderer = new ShapeRenderer()

  val mapWidth: Int =
    map.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
  val mapHeight: Int =
    map.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight

  val messageDispatcher: MessageManager = MessageManager.getInstance()
  val renderingSystem =
    new Rendering(world, spriteBatch, mapRenderer, shapeRenderer)
  val controlSystem = new Controller
  val physicsSystem = new Physics(world)
  val charSystem = new Characters
  val keys = Map(
    Keys.W -> { () =>
      renderingSystem.cam.translate(0, 0.2f)
    },
    Keys.S -> { () =>
      renderingSystem.cam.translate(0, -0.2f)
    },
    Keys.A -> { () =>
      renderingSystem.cam.translate(-0.2f, 0f)
    },
    Keys.D -> { () =>
      renderingSystem.cam.translate(0.2f, 0f)
    },
    Keys.Q -> { () =>
      renderingSystem.cam.zoom -= 0.1f
    },
    Keys.E -> { () =>
      renderingSystem.cam.zoom += 0.1f
    },
    Keys.ESCAPE -> { () =>
      Gdx.app.exit()
    }
  )

  val visionBlockers = map.getLayers.get("VisionBlocker").getObjects
  visionBlockers.forEach { vBlock =>
    val rect = vBlock.asInstanceOf[RectangleMapObject].getRectangle
    val rectangle = new Rectangle(
      (rect.x / PPM) / 2f,
      (rect.y / PPM) / 2f,
      rect.getWidth / PPM,
      rect.getHeight / PPM
    )
    val polygonShape = new PolygonShape
    val bodyDef = new BodyDef
    bodyDef.`type` = BodyType.StaticBody
    bodyDef.position.set(rectangle.getX, rectangle.getY)
    polygonShape.set(
      Array(
        new Vector2(rectangle.getX, rectangle.getY),
        new Vector2(rectangle.getX, rectangle.getY + rectangle.height),
        new Vector2(
          rectangle.getX + rectangle.width,
          rectangle.getY + rectangle.height
        ),
        new Vector2(rectangle.getX + rectangle.width, rectangle.getY)
      )
    )
    val body = world.createBody(bodyDef)
    body.createFixture(polygonShape, 0f)
    polygonShape.dispose()
  }

  val contrEntity = engine.createEntity()
  val contrComp = engine.createComponent(classOf[Control])
  contrEntity.add(contrComp)
  engine.addEntity(contrEntity)
  contrComp.keyMap = keys

  engine.addSystem(renderingSystem)
  engine.addSystem(physicsSystem)
  engine.addSystem(controlSystem)
  engine.addSystem(charSystem)

  for (name <- names) {
    createTank(name)
  }
  PlayZone.shrinkZone()
  override def show(): Unit = {

  }

  private def createTank(name: String): Unit = {
    val entity = engine.createEntity()
    val charC = engine.createComponent(classOf[CharacterC])
    val baseTexture = assets
      .get[TextureAtlas]("images/tanks/tank.atlas")
      .findRegion("tank_base")
    val turretTexture =
      assets
        .get[TextureAtlas]("images/tanks/tank.atlas")
        .findRegion("tank_turret")
    val (startX, startY) = pathFinding.genSpawnPos
    val tank = new Tank(
      world,
      pathFinding,
      messageDispatcher,
      startX + 0.5f,
      startY + 0.5f,
      name,
      baseTexture,
      turretTexture
    )
    charC.char = Some(tank)
    entity.add(charC)
    val b2DBody = engine.createComponent(classOf[B2DBody])
    val bodyDef = new BodyDef
    val fixtureDef = new FixtureDef
    val polygonShape = new PolygonShape()
    polygonShape.setAsBox(0.5f, 0.5f)
    fixtureDef.shape = polygonShape

    bodyDef.`type` = BodyType.StaticBody
    bodyDef.position.x = tank.getX
    bodyDef.position.y = tank.getY
    val body = world.createBody(bodyDef)
    body.createFixture(fixtureDef)
    body.setUserData(tank)
    b2DBody.body = Some(body)
    entity.add(b2DBody)
    engine.addEntity(entity)
    polygonShape.dispose()

    MessageManager.getInstance().addListener(tank, CharacterState.NEW_ZONE_MSG)
  }

  override def render(delta: Float): Unit = {
    Gdx.gl.glClearColor(1f, 1f, 1f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    engine.update(delta)
  }
  override def resize(width: Int, height: Int): Unit =
    viewport.update(width, height)

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {
    engine.clearPools()
    mapRenderer.dispose()
    spriteBatch.dispose()
  }
}

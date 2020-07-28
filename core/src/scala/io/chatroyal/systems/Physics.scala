package io.chatroyal.systems
import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import io.chatroyal.components.{B2DBody, CharacterC, Mapper}
import io.chatroyal.systems.Physics._

class Physics(val world: World)
    extends IntervalIteratingSystem(
      Family.all(classOf[B2DBody], classOf[CharacterC]).get,
      MAX_STEP_TIME
    ) {

  override def updateInterval(): Unit = {
    world.step(1, 1, 1)
    getEntities.forEach(processEntity)
  }

  override def processEntity(entity: Entity): Unit = {
    val charC = Mapper.charC.get(entity)
    val b2DBodyC = Mapper.b2b.get(entity)

    for {
      char <- charC.char
      body <- b2DBodyC.body
      _ = if (char.getHealth > 0) {
        body.setTransform(
          char.getX,
          char.getY,
          char.getRotation * MathUtils.degRad
        )
      } else {
        getEngine.removeEntity(entity)
        world.destroyBody(body)
        char.dispose()
      }
    } yield ()

  }
}

object Physics {
  private val MAX_STEP_TIME = 1 / 60f

}

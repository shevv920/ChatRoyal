package io.chatroyal.systems
import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import io.chatroyal.components.{Control, Mapper}

class Controller extends IteratingSystem(Family.all(classOf[Control]).get) {

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val cm = Mapper.control
    val c = cm.get(entity)
    c.processInput()
  }
}

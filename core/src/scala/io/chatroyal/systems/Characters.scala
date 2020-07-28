package io.chatroyal.systems

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import io.chatroyal.components.{CharacterC, Mapper}

class Characters extends IteratingSystem(Family.all(classOf[CharacterC]).get){

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val charC = Mapper.charC.get(entity)
    charC.char.foreach(_.doTick(deltaTime))
  }
}

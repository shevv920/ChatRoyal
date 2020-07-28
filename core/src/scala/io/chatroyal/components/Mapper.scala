package io.chatroyal.components
import com.badlogic.ashley.core.ComponentMapper

object Mapper {
  val b2b: ComponentMapper[B2DBody]        = ComponentMapper.getFor(classOf[B2DBody])
  val control: ComponentMapper[Control]    = ComponentMapper.getFor(classOf[Control])
  val charC: ComponentMapper[CharacterC]   = ComponentMapper.getFor(classOf[CharacterC])
}

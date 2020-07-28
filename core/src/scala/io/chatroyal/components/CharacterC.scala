package io.chatroyal.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import io.chatroyal.actors.Character

class CharacterC extends Component with Poolable {
  var char: Option[Character] = None
  override def reset(): Unit = char = None
}

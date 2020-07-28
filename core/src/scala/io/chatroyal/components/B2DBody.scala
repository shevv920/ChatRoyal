package io.chatroyal.components
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Pool.Poolable

class B2DBody extends Component with Poolable{
  var body: Option[Body] = None
  override def reset(): Unit = body = null
}

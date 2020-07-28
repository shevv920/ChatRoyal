package io.chatroyal.components
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Pool.Poolable

class Control extends Component with Poolable{
  var keyMap: Map[Int, () => Unit] = Map.empty
  def processInput(): Unit = {
    keyMap.foreach { case (k, v) => if(Gdx.input.isKeyPressed(k)) v.apply
    }
  }
  override def reset(): Unit = keyMap = Map.empty
}

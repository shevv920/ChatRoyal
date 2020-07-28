package io.chatroyal.actors.tank

import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction

class TankMoveToAction extends MoveToAction {

  override def begin(): Unit = {
    val distance = Math.abs(getX - actor.getX) + Math.abs(getY - actor.getY)
    setDuration(distance / Tank.MOVE_SPEED)
    super.begin()
  }
}

package io.chatroyal.actors.tank

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.scenes.scene2d.actions.RotateToAction
import io.chatroyal.ChatRoyal

class TankRotateAction extends RotateToAction {
  setUseShortestDirection(true)
  private var targetX = 0f
  private var targetY = 0f


  def setTargetPos(x: Float, y: Float): Unit = {
    targetX = x
    targetY = y
  }

  override def begin(): Unit = {
    val mV = new Vector2(actor.getX, actor.getY)
    val tV = new Vector2(targetX, targetY)
    val angle = (ChatRoyal.vectorToAngle(tV.sub(mV)) * MathUtils.radDeg) + 360

    val diff = this.getRotation - angle

    setDuration(0.5f)
    setRotation(angle)
    super.begin()
  }

  override def reset(): Unit = {
    targetX = 0f
    targetY = 0f
    super.reset()
  }

}

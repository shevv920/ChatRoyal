package io.chatroyal.actors

import com.badlogic.gdx.ai.msg.MessageManager
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.scenes.scene2d.Actor

object PlayZone extends Actor {
  private val playCircle = new Circle(35f, 35f, 15f)
  val segments = 64

  def getZoneX: Float  = playCircle.x
  def getZoneY: Float  = playCircle.y
  def getZoneRadius: Float = playCircle.radius

  def shrinkZone(): Unit = {
    playCircle.set(playCircle.x, playCircle.y, playCircle.radius / 2f) //todo
    MessageManager.getInstance().dispatchMessage(CharacterState.NEW_ZONE_MSG)
  }

  def ifInZone(x: Float, y: Float): Boolean = {
    playCircle.contains(x, y)
  }

  override def act(delta: Float): Unit = {
    super.act(delta)
  }

}


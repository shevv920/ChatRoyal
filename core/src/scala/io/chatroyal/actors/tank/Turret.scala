package io.chatroyal.actors.tank

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction
class Turret(val tank: Tank) extends Actor {
  setName(tank.getName + "'s turret")
  override def getX: Float = tank.getX
  override def getY: Float = tank.getY

  val onAimFinishAction: RunnableAction = new RunnableAction {
    override def run(): Unit = {
      tank.onAimFinished()
    }
  }

}

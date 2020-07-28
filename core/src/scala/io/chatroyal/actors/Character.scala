package io.chatroyal.actors

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.msg.Telegraph
import com.badlogic.gdx.graphics.g2d.{ParticleEffect, SpriteBatch}
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.scenes.scene2d.Actor

import scala.collection.mutable

abstract class Character(name: String) extends Actor with Telegraph {
  setName(name)
  val stateMachine: DefaultStateMachine[Character, CharacterState]
  private var target: Character = _
  def getTarget = Option(target)
  def setTarget(t: Character):Unit = target = t
  def aimAt(t: Character): Unit
  def removeTarget(): Unit = target = null
  def doTick(delta: Float): Unit
  def draw(batch: SpriteBatch): Unit
  def onHit(attacker:Character): Unit
  def getHealth: Int
  def dispose(): Unit
  def moveToPlayZone(): Unit
  def checkIfTargetVisible(): Unit
  def knownEnemies: mutable.Set[Character]
}

package io.chatroyal.actors

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import io.chatroyal.actors.CharacterState.{NEW_ZONE_MSG, _}

import scala.util.Random

sealed abstract class CharacterState extends State[Character] {
  override def update(entity: Character): Unit = ()
  override def exit(entity: Character): Unit = ()
  def telegramToState(msg: Telegram, entity: Character): CharacterState

  override def onMessage(entity: Character, telegram: Telegram): Boolean = {
    val newState = telegramToState(telegram, entity)
    if (newState == this) true
    else {
      entity.stateMachine.changeState(newState)
      true
    }
  }

  def randomly(state: CharacterState): CharacterState =
    if (Random.nextBoolean) state else this

}

case object JustSpawned extends CharacterState {
  override def enter(entity: Character): Unit = ()

  override def telegramToState(telegram: Telegram, entity:Character): CharacterState =
    telegram.message match {
      case NEW_ZONE_MSG =>
        Chilling randomly MovingToPlayZone
      case _ =>
        this
    }
}

case class Fighting(target: Character) extends CharacterState {
  override def enter(entity: Character): Unit = {
    entity.setTarget(target)
    entity.aimAt(target)
    entity.clearActions()
  }

  override def update(entity: Character): Unit = {
    if (entity.getTarget.nonEmpty) {
      entity.checkIfTargetVisible()
    }
  }

  override def telegramToState(telegram: Telegram, entity:Character): CharacterState =
    telegram.message match {
      case NEW_ZONE_MSG =>
        this randomly MovingToPlayZone randomly ForceMovingToPlayZone
      case ENEMY_DETECTED =>
        entity.knownEnemies += telegram.extraInfo.asInstanceOf[Character]
        this
      case BEING_HIT =>
        val attacker = telegram.extraInfo.asInstanceOf[Character]
        entity.onHit(attacker)
        this randomly Fighting(attacker) randomly MovingToPlayZone
      case TARGET_LOST =>
        entity.knownEnemies -= telegram.extraInfo.asInstanceOf[Character]
        if(entity.knownEnemies.isEmpty) {
          entity.removeTarget()
          Chilling randomly MovingToPlayZone
        } else {
          Fighting(entity.knownEnemies.head) randomly MovingToPlayZone
        }
      case FRAG_SCORED =>
        val victim = telegram.extraInfo.asInstanceOf[Character]
        entity.knownEnemies -= victim
        for {
          target <- entity.getTarget
          _ = entity.knownEnemies -= target
        } yield ()
        if(entity.knownEnemies.isEmpty) {
          Chilling randomly MovingToPlayZone
        } else {
          Fighting(entity.knownEnemies.head) randomly MovingToPlayZone
        }
    }

  override def exit(entity: Character): Unit = {}
}

case object MovingToPlayZone extends CharacterState {
  override def enter(entity: Character): Unit = {
    entity.moveToPlayZone()
  }
  override def telegramToState(telegram: Telegram, entity:Character): CharacterState =
    telegram.message match {
      case NEW_ZONE_MSG =>
        this
      case ENEMY_DETECTED =>
        this randomly Fighting(telegram.extraInfo.asInstanceOf[Character])
      case BEING_HIT =>
        entity.onHit(telegram.extraInfo.asInstanceOf[Character])
        this randomly Fighting(telegram.extraInfo.asInstanceOf[Character]) randomly ForceMovingToPlayZone
      case ARRIVED =>
        Chilling
    }
}

case object ForceMovingToPlayZone extends CharacterState {
  override def enter(entity: Character): Unit = ()
  override def telegramToState(telegram: Telegram, entity:Character): CharacterState = this
}
case object Chilling extends CharacterState {
  override def enter(entity: Character): Unit = ()
  override def telegramToState(telegram: Telegram, entity:Character): CharacterState =
    telegram.message match {
      case ENEMY_DETECTED =>
        Fighting(telegram.extraInfo.asInstanceOf[Character])
      case NEW_ZONE_MSG =>
        MovingToPlayZone
      case BEING_HIT =>
        val attacker = telegram.extraInfo.asInstanceOf[Character]
        entity.onHit(attacker)
        Fighting(attacker)
    }
}

object CharacterState {
  val NEW_ZONE_MSG = 0
  val ENEMY_DETECTED = 1
  val BEING_HIT = 2
  val TARGET_LOST = 3
  val FRAG_SCORED = 4
  val ARRIVED = 5
}

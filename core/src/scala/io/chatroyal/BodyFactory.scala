package io.chatroyal

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._

object BodyFactory {
  val STEEL = 0
  val WOOD = 1
  val RUBBER = 2
  val STONE = 3
  val BULLET = 4

  def makeSensorBody(posX: Float, posY: Float)(implicit world: World): Body = {
    val bodyDef = new BodyDef
    bodyDef.`type` = BodyType.DynamicBody
    bodyDef.position.x = posX
    bodyDef.position.y = posY
    val body = world.createBody(bodyDef)
    val circleShape = new CircleShape()
    circleShape.setRadius(5f)
    body.createFixture(makeFixture(5, circleShape))
    circleShape.dispose()
    body
  }

  def makeBulletBody(posX: Float, posY: Float)(implicit world: World): Body = {
    val bodyDef = new BodyDef
    bodyDef.`type` = BodyType.DynamicBody
    bodyDef.position.x = posX
    bodyDef.position.y = posY
    val body =world.createBody(bodyDef)
    val circleShape = new CircleShape()
    circleShape.setRadius(1f)
    body.createFixture(makeFixture(4, circleShape))
    circleShape.dispose()
    body
  }

  def makeCirclePolyBody(posX: Float,
                         posY: Float,
                         radius: Float,
                         material: Int,
                         bodyType: BodyType,
                         fixedRotation: Boolean)(implicit world: World): Body = {
    val boxBodyDef = new BodyDef
    boxBodyDef.`type` = bodyType
    boxBodyDef.position.x = posX
    boxBodyDef.position.y = posY
    boxBodyDef.fixedRotation = fixedRotation

    val boxBody = world.createBody(boxBodyDef)
    val circleShape = new CircleShape
    circleShape.setRadius(radius / 2f)
    boxBody.createFixture(makeFixture(material, circleShape))
    circleShape.dispose()
    boxBody
  }



  def makeFixture(material: Int, shape: Shape): FixtureDef = {
    val fixtureDef = new FixtureDef
    fixtureDef.shape = shape
    material match {
      case STEEL =>
        fixtureDef.density = 1f
        fixtureDef.friction = 0.3f
        fixtureDef.restitution = 0.1f
      case WOOD =>
        fixtureDef.density = 0.5f
        fixtureDef.friction = 0.7f
        fixtureDef.restitution = 0.3f
      case RUBBER =>
        fixtureDef.density = 1f
        fixtureDef.friction = 0f
        fixtureDef.restitution = 1f
      case STONE =>
        fixtureDef.density = 1f
        fixtureDef.friction = 0.5f
        fixtureDef.restitution = 0f
      case BULLET =>
        fixtureDef.isSensor = true
        fixtureDef.density = 1f
        fixtureDef.friction = 0
        fixtureDef.restitution = 0
      case _ =>
        fixtureDef.density = 0f
        fixtureDef.friction = 0f
        fixtureDef.restitution = 0f
    }
    fixtureDef
  }
}

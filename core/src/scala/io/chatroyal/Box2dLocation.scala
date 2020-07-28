package io.chatroyal

import com.badlogic.gdx.ai.utils.Location
import com.badlogic.gdx.math.Vector2
class Box2dLocation extends Location[Vector2] {
  var position: Vector2                                 = new Vector2()
  var orientation = 0f
  override val getPosition: Vector2                     = position
  override val getOrientation: Float                    = orientation
  override def setOrientation(orientation: Float): Unit = this.orientation = orientation
  override def newLocation() = new Box2dLocation
  override def vectorToAngle(vector: Vector2): Float    = ChatRoyal.vectorToAngle(vector)
  override def angleToVector(outVector: Vector2, angle: Float): Vector2 =
    ChatRoyal.angleToVector(outVector, angle)

  def random(maxX: Int, maxY: Int): Box2dLocation = {
    import scala.util.Random
    this.position.set(Random.nextInt(maxX), Random.nextInt(maxY))
    this
  }

  def withPosition(x: Float, y: Float) : Box2dLocation = {
    this.position.set(x, y)
    this
  }
}
package io.chatroyal
import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.{MathUtils, Vector2}

class ChatRoyal extends Game {

  override def create(): Unit = {
    setScreen(new MainMenuScreen(this))
  }

  override def render(): Unit = super.render()

  override def dispose(): Unit = {
    Assets.dispose()
  }
}

object ChatRoyal {
  val testPlayersList: List[String] =
    "@swine _mos_ redeyedman norm_nick outZoNe Bga3 JustPerson Sasha_LV freepy humanist_ @anon516 kukuruku Tiez dvasibul @Spoofing PERDOLIKS writed Bga5 @vertexua cetjs2 +pztrn Qubick R2AMO buggytom jircbot asibul nikitosiusis dulo_t-34 O01eg zvezdochiot \\[-_-] deniska xand_real @Pinkbyte Mugichka1 Mike2_ mniip e2 mandala zogagent @quax @dim13 verm1n squirrel ovf TwisteR bvn13 @KronoZ rulo_t-34 postno dulo_t-34_ bezik ilbelkyr"
      .split(" ")
      .toList
  val sqrt2: Float      = Math.sqrt(2).toFloat
  val WORLD_HEIGHT      = 100
  val WORLD_WIDTH       = 100
  val PPM               = 32f
  val UNIT_SCALE: Float = 1f / PPM
  val zoomSpeed         = 0.02f
  val cameraMoveSpeed   = 0.05f
  //bits to represent different physic things
  val NOTHING_BIT: Short                    = 0
  val VISION_BLOCKED_BIT: Short             = (1 << 1).toShort
  val MOVE_BLOCKED_BIT: Short               = (1 << 2).toShort
  val CHARACTER_BIT: Short                  = (1 << 3).toShort
  def vectorToAngle(vector: Vector2): Float = MathUtils.atan2(-vector.x, vector.y)
  def angleToVector(outVector: Vector2, angle: Float): Vector2 = {
    outVector.x = -Math.sin(angle).toFloat
    outVector.y = Math.cos(angle).toFloat
    outVector
  }

  def currentTimeMillis: Long = System.currentTimeMillis()

  def currentTimeSeconds: Long = System.currentTimeMillis() / 1000L

  def calcAngleTo(from: Vector2, to: Vector2):Float = {
    (ChatRoyal.vectorToAngle(to.sub(from)) * MathUtils.radDeg) + 360
  }
}

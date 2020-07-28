package io.chatroyal.systems
import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Box2DDebugRenderer, World}
import com.badlogic.gdx.{Gdx, utils}
import io.chatroyal.ChatRoyal
import io.chatroyal.actors.PlayZone
import io.chatroyal.components.{CharacterC, Mapper}

class Rendering(val world:World,
                 val batch: SpriteBatch,
                val mapRenderer: OrthogonalTiledMapRenderer,
                val shapeRenderer: ShapeRenderer,
                val comparator: ZComparator = new ZComparator)
    extends SortedIteratingSystem(Family.all(classOf[CharacterC]).get, comparator) {
  val PPM                     = ChatRoyal.PPM
  val FRUSTUM_WIDTH: Float    = Gdx.graphics.getWidth / PPM
  val FRUSTUM_HEIGHT: Float   = Gdx.graphics.getHeight / PPM
  val PIXELS_TO_METRES: Float = ChatRoyal.UNIT_SCALE

  val renderQueue: utils.Array[Entity] = new utils.Array[Entity]()

  val cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT)
  cam.position.set(FRUSTUM_WIDTH / 2f, FRUSTUM_HEIGHT / 2f, 0)
  val debugRenderer = new Box2DDebugRenderer()

  private val meterDimensions = new Vector2
  private val pixelDimensions = new Vector2

  def getScreenSizeInMeters: Vector2 =
    meterDimensions.set(Gdx.graphics.getWidth * PIXELS_TO_METRES,
                        Gdx.graphics.getHeight * PIXELS_TO_METRES)

  // static method to get screen size in pixels
  def getScreenSizeInPixels: Vector2 =
    pixelDimensions.set(Gdx.graphics.getWidth, Gdx.graphics.getHeight)

  // convenience method to convert pixels to meters
  def pixelsToMeters(pixelValue: Float): Float = pixelValue * PIXELS_TO_METRES

  override def processEntity(entity: Entity, deltaTime: Float): Unit = renderQueue.add(entity)

  override def update(deltaTime: Float): Unit = {
    super.update(deltaTime)

    renderQueue.sort(comparator)
    val selected = renderQueue.select((entity: Entity) => (for {
      char    <- Mapper.charC.get(entity).char
      visible = cam.frustum.pointInFrustum(char.getX, char.getY, char.getZIndex)
    } yield visible).getOrElse(false))
    cam.update()

    batch.setProjectionMatrix(cam.combined)
    batch.enableBlending()

    mapRenderer.setView(cam)
    mapRenderer.render()
//  Gdx.app.log("current fps: ", " " + Gdx.graphics.getFramesPerSecond)
    batch.begin()
    shapeRenderer.setProjectionMatrix(cam.combined)
    shapeRenderer.setAutoShapeType(false)
    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(Color.GREEN)

    selected.forEach { entity =>
      val charC = Mapper.charC.get(entity)
      for {
        char <- charC.char
        _ = char.draw(batch)
        _ = shapeRenderer.setColor(if(char.getHealth <= 25) Color.RED else if(char.getHealth <= 50)Color.ORANGE else Color.GREEN)
        _ = shapeRenderer.circle(char.getX, char.getY, 0.1f, 32)
        //todo draw namE?
      } yield ()
    }
    batch.end()
    shapeRenderer.end()
    val debugMatrix = cam.combined
    debugRenderer.render(world, debugMatrix)

    shapeRenderer.begin(ShapeType.Line)
    shapeRenderer.setColor(Color.BLUE)
    shapeRenderer.circle(PlayZone.getZoneX + 1.5f, //fixme
      PlayZone.getZoneY + 1.5f,
      PlayZone.getZoneRadius,
      PlayZone.segments)
    shapeRenderer.end()

    renderQueue.clear()
  }

}

import java.util.Comparator
class ZComparator() extends Comparator[Entity] {
  override def compare(entityA: Entity, entityB: Entity): Int = {
    val tankA = Mapper.charC.get(entityA)
    val tankB = Mapper.charC.get(entityB)
    val res = for {
      a <- tankA.char
      b <- tankB.char
    } yield {
       if(a.getZIndex < b.getZIndex) -1 else if (a.getZIndex == b.getZIndex) 0 else 1
    }
    res.getOrElse(0)
  }
}

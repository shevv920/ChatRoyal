package io.chatroyal

import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import com.badlogic.gdx.math.Rectangle
import io.chatroyal.ai.{AStarMap, AStarPathFinding}
import scala.collection.mutable.ArrayBuffer

object PathFinding {

  def createPathFinding(map: TiledMap): AStarPathFinding = {
    val mapWidth = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
    val mapHeight =
      map.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight

    val pitLayer = map.getLayers.get("Pits").asInstanceOf[TiledMapTileLayer]
    val waterLayer = map.getLayers.get("Water0").asInstanceOf[TiledMapTileLayer]
    //    val groundLayer = map.getLayers.get("Ground0").asInstanceOf[TiledMapTileLayer]
    //    val grassLayer  = map.getLayers.get("Grass0").asInstanceOf[TiledMapTileLayer]
    val wallLayer = map.getLayers.get("Walls").asInstanceOf[TiledMapTileLayer]

    //everything from pit layer is a wall for pathFinding
    val walls = new ArrayBuffer[(Int, Int)]()
    for (x <- 0 until mapWidth; y <- 0 until mapHeight) {
      if (wallLayer.getCell(x, y) != null
        || pitLayer.getCell(x, y) != null
        || waterLayer.getCell(x, y) != null)
        walls += ((x, y))
    }

    val aStarMap = new AStarMap(mapWidth, mapHeight, walls.toSeq)
    new AStarPathFinding(aStarMap)
  }

  def correctRectangle(rect: Rectangle): Unit = {
    rect.x = rect.x / ChatRoyal.PPM
    rect.y = rect.y / ChatRoyal.PPM
    rect.width = rect.width / ChatRoyal.PPM
    rect.height = rect.height / ChatRoyal.PPM
  }

}

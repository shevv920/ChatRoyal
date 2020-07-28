package io.chatroyal.ai

import com.badlogic.gdx.ai.pfa._
import com.badlogic.gdx.ai.pfa.indexed.{IndexedAStarPathFinder, IndexedGraph}
import com.badlogic.gdx.math.{Circle, MathUtils, Vector2}
import com.badlogic.gdx.utils
import io.chatroyal.actors.tank.Tank.random

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

class AStarPathFinding(val map: AStarMap) {
  import AStarPathFinding._
  val pathFinder = new IndexedAStarPathFinder[Node](createGraph(map))
  val heuristic: Heuristic[Node] =
    (node: Node, endNode: Node) => { // Manhattan distance
      Math.abs(endNode.x - node.x) + Math.abs(endNode.y - node.y)
    }
  val connectionPath = new DefaultGraphPath[Node]()

  def findRandomStep(source: Vector2): Option[Node] = {
    val sourceX = MathUtils.floor(source.x)
    val sourceY = MathUtils.floor(source.y)
    if (map == null
        || sourceX < 0 || sourceX >= map.width
        || sourceY < 0 || sourceY >= map.height)
       None
    else {
      val sourceNode = map.getNodeAt(sourceX, sourceY)
      Option(sourceNode.connections.random()).map(_.getToNode)
    }
  }

  def findPathNodes(source: Vector2,
                    target: Vector2): Option[utils.Array[Node]] = {
    val sourceX = MathUtils.floor(source.x)
    val sourceY = MathUtils.floor(source.y)
    val targetX = MathUtils.floor(target.x)
    val targetY = MathUtils.floor(target.y)
    if (map == null
        || sourceX < 0 || sourceX >= map.width
        || sourceY < 0 || sourceY >= map.height
        || targetX < 0 || targetX >= map.width
        || targetY < 0 || targetY >= map.height)
      None
    else {
      val sourceNode = map.getNodeAt(sourceX, sourceY)
      val targetNode = map.getNodeAt(targetX, targetY)
      connectionPath.clear()
      val found = pathFinder.searchNodePath(
        sourceNode,
        targetNode,
        heuristic,
        connectionPath
      )
      if (found)
        Some(correctPath(connectionPath.nodes))
      else
        None
    }
  }

  @tailrec
  final def genSpawnPos: (Float, Float) = {
    val randomX = random.nextInt(this.map.width)
    val randomY = random.nextInt(this.map.height)
    if (this.map.getNodeAt(randomX, randomY).isWall) {
      genSpawnPos
    } else {
      (randomX, randomY)
    }
  }

  final def getRandomPos(x:Float, y:Float, radius:Float): (Float, Float) = {

    @tailrec
    def loop(x: Float, y: Float): (Float, Float) = {
      val r: Float = radius * Math.sqrt(random.nextFloat).toFloat
      val theta = random.nextFloat * 2 * MathUtils.PI
      val randomX = MathUtils.ceil(x + MathUtils.cos(theta) * r)
      val randomY = MathUtils.ceil(y + MathUtils.sin(theta) * r)
      if (this.map.getNodeAt(randomX, randomY).isWall) {
        loop(x, y)
      } else {
        (randomX, randomY)
      }
    }

    loop(x, y)
  }

}

object AStarPathFinding {
  private val NEIGHBORHOOD =
    Array[(Int, Int)]((-1, 0), (0, -1), (0, 1), (1, 0))

  def correctPath(path: utils.Array[Node]): utils.Array[Node] = {
    val buf = new ArrayBuffer[Node](path.size)
    if (path.size <= 1) path
    else {
      var isByX = path.get(0).x != path.get(1).x
      for (i <- 1 until path.size - 2) {
        if (path.get(i).x == path.get(i + 1).x && isByX) {
          buf += path.get(i)
          isByX = !isByX
        } else if (path.get(i).y == path.get(i + 1).y && !isByX) {
          buf += path.get(i)
          isByX = !isByX
        }
      }
      buf += path.get(path.size - 1)

      new utils.Array[Node](buf.toArray.distinct)
    }
  }

  def createGraph(map: AStarMap): Graph = {
    val graph = new Graph(map)
    for {
      y <- 0 until map.height
      x <- 0 until map.width
      node = map.getNodeAt(x, y)
      _ = createConnections(map, node) if !node.isWall
    } yield ()
    graph
  }

  def createConnections(map: AStarMap, node: Node): Unit = {
    for (off <- NEIGHBORHOOD.indices) {
      val x = node.x + NEIGHBORHOOD(off)._1
      val y = node.y + NEIGHBORHOOD(off)._2
      if (x >= 0 && x < map.width && y >= 0 && y < map.height) {
        val neighbor = map.getNodeAt(x, y)
        if (!neighbor.isWall) {
          node.connections.add(new DefaultConnection[Node](node, neighbor))
        }
      }
    }
  }

  class Graph(val map: AStarMap) extends IndexedGraph[Node] {
    override def getIndex(node: Node): Int = node.index
    override def getNodeCount: Int = map.height * map.width

    override def getConnections(fromNode: Node): utils.Array[Connection[Node]] =
      fromNode.connections
  }

}

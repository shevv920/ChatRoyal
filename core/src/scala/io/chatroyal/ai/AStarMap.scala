package io.chatroyal.ai

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.utils

class AStarMap(val width: Int, val height: Int, walls: Seq[(Int, Int)]) {

  private val map: Array[Array[Node]] =
    Array.tabulate(height, width) { (x, y) =>
      Node(x, y, (x * height) + y, isWall = false, new utils.Array[Connection[Node]]())
    }

  walls.foreach { xy =>
    getNodeAt(xy._1, xy._2).isWall = true
  }

  def getNodeAt(x: Int, y: Int): Node = map(x)(y)

  override def toString: String = {
    val stringBuilder = new StringBuilder
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        stringBuilder.append(if (map(x)(y).isWall) "#" else ".")
      }
      stringBuilder.append("\n")
    }
    stringBuilder.mkString
  }
}

case class Node(x: Int,
                y: Int,
                index: Int,
                var isWall: Boolean,
                connections: utils.Array[Connection[Node]])
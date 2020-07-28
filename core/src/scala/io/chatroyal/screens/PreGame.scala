package io.chatroyal.screens

import com.badlogic.gdx.{Game, Gdx, Screen}
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}
import com.badlogic.gdx.scenes.scene2d.ui.{SelectBox, Skin, Table, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import io.chatroyal.{ChatRoyal, PathFinding}

class PreGame(val game: Game) extends Screen {

  val assets = new AssetManager()
  assets.setLoader(classOf[TiledMap], new TmxMapLoader(new InternalFileHandleResolver))
  assets.load("images/tanks/tank.atlas", classOf[TextureAtlas])
  assets.load("skin/uiskin.json", classOf[Skin])
  assets.load("map3.tmx", classOf[TiledMap])

  val stage = new Stage()
  override def show(): Unit = {
    assets.finishLoading()
    val skin = assets.get[Skin]("skin/uiskin.json")

    val physWorld   = new World(new Vector2(0, 0), true)

    val table = new Table()
    table.setFillParent(true)
    val mapChooser = new SelectBox[String](skin)
    table.add(mapChooser)
    mapChooser.setItems("map3.tmx")
    val map         = assets.get[TiledMap](mapChooser.getSelected)
    val pathFinding = PathFinding.createPathFinding(map)

    val startGameButton = new TextButton("Start", skin)
    table.add(startGameButton)

    startGameButton.addListener {
      new ChangeListener {
        override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
          val gameScreen = new TheGame(assets, ChatRoyal.testPlayersList, physWorld, map, pathFinding)
          game.setScreen(gameScreen)
        }
      }
    }
    stage.addActor(table)
    Gdx.input.setInputProcessor(stage)
  }

  override def render(delta: Float): Unit = {
    import com.badlogic.gdx.Gdx
    import com.badlogic.gdx.graphics.GL20
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    stage.act(delta)
    stage.draw()
  }
  override def resize(width: Int, height: Int): Unit = stage.getViewport.update(width, height, true)
  override def pause(): Unit                         = ()
  override def resume(): Unit                        = ()
  override def hide(): Unit                          = ()
  override def dispose(): Unit = {
    stage.dispose()
    assets.dispose()
  }
}

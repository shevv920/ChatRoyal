package io.chatroyal
import com.badlogic.gdx.scenes.scene2d.ui.{Table, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}
import com.badlogic.gdx.{Game, Gdx, Screen}
import io.chatroyal.screens.PreGame

class MainMenuScreen(val game: Game) extends Screen {
  val stage = new Stage()

  override def show(): Unit = {
    Gdx.input.setInputProcessor(stage)
    val table = new Table()
    table.setFillParent(true)
    val startGameButton = new TextButton("Start", Assets.skin)
    startGameButton.addListener {val lst: ChangeListener =
      (_: ChangeEvent, _: Actor) => game.setScreen(new PreGame(game))
      lst
    }

    table.add(startGameButton)
    val quitButton = new TextButton("Quit", Assets.skin)
    quitButton.addListener {
      new ChangeListener {
        override def changed(event: ChangeEvent, actor: Actor): Unit = Gdx.app.exit()
      }
    }
    table.add(quitButton)
    stage.addActor(table)
  }
  override def render(delta: Float): Unit = {
    import com.badlogic.gdx.Gdx
    import com.badlogic.gdx.graphics.GL20
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    stage.act(delta)
    stage.draw()
  }
  override def resize(width: Int, height: Int): Unit = {
    stage.getViewport.update(width, height, true)
  }
  override def pause(): Unit  = ()
  override def resume(): Unit = ()
  override def hide(): Unit   = this.dispose()
  override def dispose(): Unit = {
    stage.dispose()
  }
}

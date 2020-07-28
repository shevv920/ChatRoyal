package io.chatroyal
import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}

object DesktopLauncher extends App {

  val config = new LwjglApplicationConfiguration()
  config.vSyncEnabled = false
  config.foregroundFPS = 60
  config.backgroundFPS = 60
  config.title = "Chat Royal"
  config.width = 1024
  config.height = 768
  config.useGL30 = false
  config.forceExit = false
  val cr  = new ChatRoyal
  val app = new LwjglApplication(cr, config)

}

package io.chatroyal
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}

object DesktopLauncher extends App {

  val config = new Lwjgl3ApplicationConfiguration()
  // config.setVSyncEnabled(false)
  // config.setForegroundFPS(60)
  // config.setBackgroundFPS(60)
  // config.setTitle("Chat Royal")
  // config.setWidth(1024)
  // config.setHeight(768)
  // config.setUseGL30(false)
  // config.setForceExit(false)
  val cr  = new ChatRoyal
  val app = new Lwjgl3Application(cr, config)

}

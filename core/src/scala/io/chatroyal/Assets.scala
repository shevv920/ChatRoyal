package io.chatroyal
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.{Sprite, TextureAtlas, TextureRegion}
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader

object Assets {
  val assetManager = new AssetManager()

  assetManager.setLoader(classOf[TiledMap], new TmxMapLoader(new InternalFileHandleResolver))
  assetManager.load("images/tanks/tank.atlas", classOf[TextureAtlas])
  assetManager.load("skin/uiskin.json", classOf[Skin])
  assetManager.load("map.tmx", classOf[TiledMap])
  assetManager.load("map3.tmx", classOf[TiledMap])
  assetManager.finishLoading()
  val tanksAtlas: TextureAtlas = assetManager.get("images/tanks/tank.atlas", classOf[TextureAtlas])

  val tankBase        = new Sprite(new TextureRegion(tanksAtlas.findRegion("tank_base")))
  val tankTurret      = new Sprite(new TextureRegion(tanksAtlas.findRegion("tank_turret")))
  val skin: Skin      = assetManager.get("skin/uiskin.json").asInstanceOf[Skin]
  def dispose(): Unit = assetManager.dispose()
}

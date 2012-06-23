package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._
import play.api.cache._
import play.api.Play.current
import services._
import play.api.libs.json._
import scala.collection.mutable._

object CommandsController extends Controller {

    def clearQueue() = Action {
        Player.songsQueue.clear
        Application.updateClients( )
        Ok( "cleared" )
    }

    def playSong() = Action {
        Player.play()
        Ok("playing")
    }

    def nextSong() = Action {
        Player.stop()
        Player.play()
        Ok("playing")
    }

    def stopSong() = Action {
        Player.stop()
        Ok("stopped")
    }
}
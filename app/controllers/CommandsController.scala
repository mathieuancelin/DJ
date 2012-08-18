package controllers

import play.api._
import mvc._
import play.api.data.Forms._
import play.api.data._
import models._
import play.api.cache._
import play.api.Play.current
import services._
import play.api.libs.json._
import scala.collection.mutable._
import util.Constants

object CommandsController extends Controller {

    val idForm = Form( "id" -> text )

    def clearQueue() = Action { implicit request =>
        Player.songsQueue.clear
        Application.pushNotification( "The queue has been cleared by " + request.remoteAddress  )
        Ok
    }

    def playSong() = Action {
        Player.play()
        Ok
    }

    def nextSong() = Action {
        Player.stop()
        Player.play()
        Ok
    }

    def stopSong() = Action {
        Player.stop()
        Ok
    }

    def updatePlaying( ) = Action {
        Application.updateClientPlaying()
        Ok
    }

    def updateLibraryList( ) = Action {
        Application.updateClientLibrary()
        Ok
    }

    val form = Form( "message" -> text )

    def say( ) = Action { implicit request =>
        form.bindFromRequest().fold(
            formWithErrors => Ok,
            { messageValue =>
                Application.pushNotification( "[MESSAGE] " + messageValue )
                Ok
            }
        )
    }

    def volumeUp() = Action { implicit request =>
        Player.volumeUp()
        Ok
    }

    def volumeDown() = Action { implicit request =>
        Player.volumeDown()
        Ok
    }

    def enqueue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id = Option.apply( maybeIdValue ).map( toL( _ ) ).getOrElse( toL( -1 ) )
                Player.enqueue( id )
                Application.pushNotification( "'" + Song.findById( id ).map( _.name ).getOrElse("Undefined")
                    + "' has been added to the queue by "
                    + request.remoteAddress )
                Ok
            }
        )
    }

    def prequeue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id = Option.apply( maybeIdValue ).map( toL( _ ) ).getOrElse( toL( -1 ) )
                Player.prequeue( id )
                Application.pushNotification( "'" + Song.findById( id ).map( _.name ).getOrElse("Undefined")
                    + "' has been added on top of the queue by "
                    + request.remoteAddress )
                Ok
            }
        )
    }

    def deletequeue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id = Option.apply( maybeIdValue ).map( toL( _ ) ).getOrElse( toL( -1 ) )
                val newQueue = Player.songsQueue.filter( _.id != id )
                Player.songsQueue.clear
                newQueue.foreach { Player.songsQueue.enqueue( _ ) }
                Application.pushNotification( "'" + Song.findById( id ).map( _.name ).getOrElse("Undefined")
                    + "' has been removed from the queue by "
                    + request.remoteAddress )
                Ok
            }
        )
    }

    def reindexLibraryAsync() = Action {
        reindexLibrary()
        Ok
    }

    def reindexLibrary() = {
        var queue = Queue[Song]()
        Player.songsQueue.foreach { queue.enqueue( _ ) }
        Player.songsQueue.clear
        MusicLibraryScanner.scan( Constants.musicBase )
        queue.foreach { oldsong =>
            Song.findByArtistAndAlbumAndName(oldsong.artist, oldsong.album, oldsong.name).foreach { song =>
                Player.songsQueue.enqueue( song )
            }
        }
    }

    def toL( value: String ) = {
        java.lang.Long.valueOf( value )
    }

    def toL( value: Int ) = {
        java.lang.Long.valueOf( value )
    }
}
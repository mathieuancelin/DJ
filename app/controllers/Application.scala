package controllers

import play.api._
import libs.iteratee.{Concurrent, Enumerator, Enumeratee}
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._
import play.api.cache._
import play.api.Play.current
import services._
import play.api.libs.json._
import scala.collection.mutable._
import util.{LastFM, Constants}

object Application extends Controller {

    val idForm = Form( "id" -> text )

    def index() = Action {
        val songs = Song.findAll().sortWith { (song1, song2) =>
            if (song1.artist.toLowerCase.equals(song2.artist.toLowerCase) && song1.album.toLowerCase.equals(song2.album.toLowerCase)) {
                song1.name.toLowerCase.compareTo(song2.name.toLowerCase) < 0
            } else {
                if (song1.artist.toLowerCase.equals(song2.artist.toLowerCase)) {
                    song1.album.toLowerCase.compareTo(song2.album.toLowerCase) < 0
                } else {
                    song1.artist.toLowerCase.compareTo(song2.artist.toLowerCase) < 0
                }
            }
        }
        Ok( views.html.index( songs ) )
    }

    def enqueue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id = Option.apply( maybeIdValue ).map( toL( _ ) ).getOrElse( toL( -1 ) )
                Player.enqueue( id )
                updateClients( "'" + Song.findById( id ).map( _.name ).getOrElse("Undefined")
                    + "' has been added to the queue by "
                    + request.headers.get(play.api.http.HeaderNames.FROM).getOrElse("Unknown") )
                Ok( "enqueued" )
            }
        )
    }

    def prequeue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id = Option.apply( maybeIdValue ).map( toL( _ ) ).getOrElse( toL( -1 ) )
                Player.prequeue( id )
                updateClients( "'" + Song.findById( id ).map( _.name ).getOrElse("Undefined")
                    + "' has been added on top of the queue by "
                    + request.headers.get(play.api.http.HeaderNames.FROM).getOrElse("Unknown") )
                Ok( "prequeued " )
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
                updateClients( "'" + Song.findById( id ).map( _.name ).getOrElse("Undefined")
                    + "' has been removed from the queue by "
                    + request.headers.get(play.api.http.HeaderNames.FROM).getOrElse("Unknown") )
                Ok( "deleted" )
            }
        )
    }

    def playing() = Action {
        Ok( playingDataJson() )
    }

    val toEventSource = Enumeratee.map[JsValue] { msg => "data: " + msg.toString() + "\n\n" }

    val hubEnumerator = Enumerator.imperative[JsValue]()

    val hub = Concurrent.hub[JsValue]( hubEnumerator )

    def updateClients( notification: String = "", command: String = "" ): Unit = {
        hubEnumerator.push( playingDataJson( notification, command ) )
    }

    def playingSSE() = Action {
        SimpleResult(
            header = ResponseHeader(
                OK,
                scala.collection.immutable.Map(
                    CONTENT_LENGTH -> "-1",
                    CONTENT_TYPE -> "text/event-stream"
                )
            ),
            hub.getPatchCord().through( toEventSource )
        )
    }

    def playingDataJson(notification: String = "", command: String = "") = {
        Player.currentSong.map { song =>
            Json.toJson(
                JsObject(
                    List(
                        "name" -> JsString( song.name ),
                        "album" -> JsString( song.album ),
                        "artist" -> JsString( song.artist ),
                        "img" -> JsString( currentPict() ),
                        "notification" -> JsString( notification ),
                        "command" -> JsString( command ),
                        "queue" -> queue()
                    )
                )
            )
        } getOrElse(
            Json.toJson(
                JsObject(
                    List(
                        "name" -> JsString( "Nothing" ),
                        "album" -> JsString( "" ),
                        "artist" -> JsString( "" ),
                        "img" -> JsString( LastFM.emptyCover ),
                        "notification" -> JsString( notification ),
                        "command" -> JsString( command ),
                        "queue" -> queue()
                    )
                )
            )
        )
    }

    def updateLibrary() = Action {
        var queue = Queue[Song]()
        Player.songsQueue.foreach { queue.enqueue( _ ) }
        Player.songsQueue.clear
        MusicLibraryScanner.scan( Constants.musicBase )
        queue.foreach { oldsong =>
            Song.findByArtistAndAlbumAndName(oldsong.artist, oldsong.album, oldsong.name).foreach { song =>
                Player.songsQueue.enqueue( song )
            }
        }
        Redirect( routes.Application.index() )
    }

    def updateLibraryAsync() = Action {
        var queue = Queue[Song]()
        Player.songsQueue.foreach { queue.enqueue( _ ) }
        Player.songsQueue.clear
        MusicLibraryScanner.scan( Constants.musicBase )
        queue.foreach { oldsong =>
            Song.findByArtistAndAlbumAndName(oldsong.artist, oldsong.album, oldsong.name).foreach { song =>
                Player.songsQueue.enqueue( song )
            }
        }
        Ok
    }

    ///////  ------ No more Actions, util methods -------- ///////

    def currentPict() = {
        Player.currentSong match {
            case Some( song ) => {
                val maybeImg:Option[String] = Cache.getAs[String](song.artist + song.album)
                maybeImg.getOrElse {
                    LastFM.retrieveCoverArtFromLastFM( song )
                    LastFM.emptyCover
                }
            }
            case _ => LastFM.emptyCover
        }
    }

    def queue() = {
        var l = List[JsObject]()
        Player.songsQueue.foreach { song =>
            l = l :+ JsObject(
                List(
                    "id" -> JsString( "" + song.id ),
                    "name" -> JsString( song.name )
                )
            )
        }
        JsArray( l )
    }

    def toL( value: String ) = {
        java.lang.Long.valueOf( value )
    }

    def toL( value: Int ) = {
        java.lang.Long.valueOf( value )
    }
}
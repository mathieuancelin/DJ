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

object Application extends Controller {

    val idForm = Form( "id" -> text )

    def index() = Action {
        Ok( views.html.index( Player.songsList ) )
    }

    def enqueue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id = Option.apply( maybeIdValue ).map( toL( _ ) ).getOrElse( toL( -1 ) )
                Player.enqueue( id )
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
                Ok( "deleted" )
            }
        )
    }

    def clearQueue() = Action {
        Player.songsQueue.clear
        Ok( "cleared" )
    }

    def queue() = Action {
        var l = List[JsObject]()
        Player.songsQueue.foreach { song =>
            l = l :+ JsObject(
                List(
                    "id" -> JsString( "" + song.id ),
                    "name" -> JsString( song.name )
                )
            )
        }
        Ok( Json.toJson( JsObject ( List ( "songs" -> JsArray( l ) ) ) ) )
    }

    def playing() = Action {
        Ok( 
            Player.currentSong.map { song => 
                Json.toJson(
                    JsObject(
                        List(
                            "name" -> JsString( song.name ),
                            "album" -> JsString( song.album ),
                            "artist" -> JsString( song.artist )
                        )
                    )
                )
            } getOrElse( 
                Json.toJson(
                    JsObject(
                        List(
                            "name" -> JsString( "Nothing" ),
                            "album" -> JsString( "" ),
                            "artist" -> JsString( "" )
                        )
                    )
                )
            ) 
        )
    }

    def currentPict() = Action {
        Player.currentSong match {
            case Some( song ) => {
                val maybeImg:Option[String] = Cache.getAs[String](song.artist + song.album)
                Ok( maybeImg.getOrElse {
                    LastFM.retrieveCoverArtFromLastFM( song )
                    ""
                })
            }
            case _ => Ok( "" )
        }
    }

    def playSong() = Action {
        Player.play()
        Ok("playing")
    }

    def stopSong() = Action {
        Player.stop()
        Ok("stopped")
    }

    def toL( value: String ) = {
        java.lang.Long.valueOf( value )
    }

    def toL( value: Int ) = {
        java.lang.Long.valueOf( value )
    }
}
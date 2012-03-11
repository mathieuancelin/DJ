package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._
import play.api.cache._
import play.api.Play.current
import services._

import scala.collection.mutable.Queue

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
        Ok(
            Player.songsQueue.foldLeft("") { ( acc, n ) =>
                acc + "<tr><td>" + n.name + "</td><td style=\"width: 70px\"><a href=\"" + n.id + "\" class=\"copy-queue btn btn-mini btn-success\"><i class=\"icon-play icon-white\"></i></a>&nbsp;<a href=\"" + n.id + "\" class=\"delete-queue btn btn-mini btn-danger\"><i class=\"icon-remove icon-white\"></i></a></td></tr>"
            }
        )
    }

    def playing() = Action {
        Ok( 
            Player.currentSong.map { song => 
                "<strong>" + song.name + "</strong><br/>from: " + song.album + "<br/>by: " + song.artist + "<br/>" 
            } getOrElse( "<strong>Nothing</strong>" ) 
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
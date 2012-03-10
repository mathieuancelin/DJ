package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._
import akka.actor._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.ws._
import play.api.cache._
import play.api.Play.current
import util._

import scala.collection.mutable.Queue

object Application extends Controller {

    var songsList = IndexedSeq[Song]()

    val idForm = Form( "id" -> text )

    def index() = Action {
        Ok( views.html.index( songsList ) )
    }

    def enqueue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id: java.lang.Long = Option.apply( maybeIdValue ).map( java.lang.Long.valueOf( _ ) ).getOrElse( java.lang.Long.valueOf( -1 ) )
                Player.enqueue( id )
                Ok("enqueued")
            }
        )
    }

    def prequeue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id: java.lang.Long = Option.apply( maybeIdValue ).map( java.lang.Long.valueOf( _ ) ).getOrElse( java.lang.Long.valueOf( -1 ) )
                Player.prequeue( id )
                Ok("prequeued " + id)
            }
        )
    }

    def deletequeue() = Action { implicit request =>
        idForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'id' value!" ),
            { maybeIdValue =>
                val id: java.lang.Long = Option.apply( maybeIdValue ).map( java.lang.Long.valueOf( _ ) ).getOrElse( java.lang.Long.valueOf( -1 ) )
                val newQueue = Player.songsQueue.filter( _.id != id )
                Player.songsQueue.clear
                newQueue.foreach {Player.songsQueue.enqueue(_)}
            }
        )
        Ok("deleted")
    }

    def clearQueue() = Action {
        Player.songsQueue.clear
        Ok("cleared")
    }

    def queue() = Action {
        Ok(Player.songsQueue.foldLeft("") { (acc, n) =>
            acc + "<tr><td>" + n.name + "</td><td><a href=\"" + n.id + "\" class=\"copy-queue btn btn-small btn-success\">►</a>" + "&nbsp;<a href=\"" + n.id + "\" class=\"delete-queue btn btn-small btn-danger\">x</a></td></tr>"
        })
    }

    def playing() = Action {
        Ok(Player.current.map { song => "<strong>" + song.name + "</strong><br>" + song.album + "<br>" + song.artist + "<br>" } getOrElse("<strong>Nothing</strong>") )
    }

    def currentPict() = Action {
        if (Player.current.isDefined) {
            val song = Player.current.get
            val maybeImg:Option[String] = Cache.getAs[String](song.artist + song.album)
            Ok(maybeImg.getOrElse {
                LastFM.getCoverArt(song)
                ""
            })
        } else {
            Ok("")
        }
    }

    def playSong() = Action {
        Player.play()
        Ok("playing")
    }

    def stopSong() = Action {
        Player.stop()
        Ok("stopping")
    }
}

case class Done()

case class Play()

case class Stop()

object Player {

    var process: java.lang.Process = null

    var current:Option[Song] = None

    var songsQueue = Queue[Song]()

    val system = ActorSystem("PlayerSystem")

    val queuer = system.actorOf(Props[PlayQueueActor], name = "queuer")

    val player = system.actorOf(Props[PlayerActor], name = "player")

    def enqueue( id: Long ) = {        
        val song: Song = Application.songsList.filter( _.id == id ).head
        queuer ! song
    }

    def prequeue( id: Long ) = {        
        val song: Song = Application.songsList.filter( _.id == id ).head
        var tmp = Queue[Song]()
        tmp.enqueue(song)
        Player.songsQueue.foreach {tmp.enqueue(_)}
        Player.songsQueue.clear
        tmp.foreach {Player.songsQueue.enqueue(_)}
    }

    def play() = {
        player ! Play()
    }

    def stop() = {
        queuer ! Stop()
    }
}

class PlayQueueActor extends Actor with ActorLogging {

    def receive = {
        case s: Song => {
            Player.songsQueue.enqueue( s )
        }
        case d: Done => {
            if (Player.songsQueue.nonEmpty) {
                Player.player ! Play()
            }
        }
        case s: Stop => {
            if (Player.process != null) {
                Player.process.destroy()
            }
        }
    }
}

class PlayerActor extends Actor with ActorLogging {

    def receive = {
        case p: Play => {
            val song = Player.songsQueue.dequeue()
            Player.current = Option.apply( song.copy() )
            val command = Array[String]("/usr/bin/afplay", song.path)
            Player.process = Runtime.getRuntime().exec( command, null, new java.io.File("/usr/bin"));
            val ret = Player.process.waitFor()
            if (ret == 0) {
                Player.queuer ! Done()
            }
            Player.current = None
        }
    }
}
 

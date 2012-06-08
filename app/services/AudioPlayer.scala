package services

import play.api._
import models._
import akka.actor._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.ws._
import play.api.cache._
import play.api.Play.current
import controllers._
import scala.collection.mutable._

case class Done()

case class Play()

case class Stop()

object Player {

    var songsList = IndexedSeq[Song]()

    val playerExec = current.configuration.getString( "player.exec" ).getOrElse( "afplay" )

    var currentSong:Option[Song] = None

    var songsQueue = Queue[Song]()

    val system = ActorSystem( "PlayerSystem" )

    val queuer = system.actorOf( Props[PlayQueueActor], name = "queuer" )

    val player = system.actorOf( Props[PlayerActor], name = "player" )

    def enqueue( id: Long ) = {        
        val song: Song = songsList.filter( _.id == id ).head
        queuer ! song
    }

    def prequeue( id: Long ) = {        
        val song: Song = songsList.filter( _.id == id ).head
        var tmp = Queue[Song]()
        tmp.enqueue( song )
        Player.songsQueue.foreach { tmp.enqueue( _ ) }
        Player.songsQueue.clear
        tmp.foreach { Player.songsQueue.enqueue( _ ) }
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
            if ( Player.songsQueue.nonEmpty ) {
                Player.player ! Play()
            }
        }
        case s: Stop => {
            Runtime.getRuntime().exec( "killall " + Player.playerExec + " > /dev/null 2>&1" )
        }
    }
}

class PlayerActor extends Actor with ActorLogging {

    def receive = {
        case p: Play => {
            if ( Player.songsQueue.nonEmpty ) {
                val song = Player.songsQueue.dequeue()
                Player.currentSong = Option.apply( song.copy() )
                val command = Array[String]( Player.playerExec, song.path )
                var process = Runtime.getRuntime().exec( command )
                val ret = process.waitFor()
                if ( ret == 0 ) {
                    Player.queuer ! Done()
                }
                Player.currentSong = None
            }
        }
    }
}
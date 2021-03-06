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
import util.Constants

case class Done()

case class Play()

case class Stop()

object Player {

    var currentSong:Option[Song] = None

    var songsQueue = Queue[Song]()

    val system = ActorSystem( "PlayerSystem" )

    val queuer = system.actorOf( Props[PlayQueueActor], name = "queuer" )

    val player = system.actorOf( Props[PlayerActor], name = "player" )

    def enqueue( id: Long ) = {        
        Song.findById( id ).foreach { song =>
            queuer ! song
        }
        Application.updateClientPlaying()
    }

    def prequeue( id: Long ) = {        
        Song.findById( id ).foreach { song =>
            var tmp = Queue[Song]()
            tmp.enqueue( song )
            Player.songsQueue.foreach { tmp.enqueue( _ ) }
            Player.songsQueue.clear
            tmp.foreach { Player.songsQueue.enqueue( _ ) }
        }
        Application.updateClientPlaying( )
    }

    def play() = {
        player ! Play()
    }

    def stop() = {
        queuer ! Stop()
    }

    var volume = 100

    def volumeUp() = {
        if (volume < 100) {
            volume = volume + 5
            Application.pushNotification( "Volume is " + volume )
        }
        var command = Array[String]( "/usr/bin/amixer", "set", "Headphone", volume + "%" )
        Runtime.getRuntime().exec( command ).waitFor()
    }

    def volumeDown() = {
        if (volume > 0) {
            volume = volume - 5
            Application.pushNotification( "Volume is " + volume )
        }
        var command = Array[String]( "/usr/bin/amixer", "set", "Headphone", volume + "%" )
        Runtime.getRuntime().exec( command ).waitFor()
    }
}

class PlayQueueActor extends Actor with ActorLogging {

    def receive = {
        case s: Song => {
            Player.songsQueue.enqueue( s )
            Application.updateClientPlaying( )
        }
        case d: Done => {
            if ( Player.songsQueue.nonEmpty ) {
                Player.player ! Play()
            }
            Application.updateClientPlaying( )
        }
        case s: Stop => {
            Runtime.getRuntime().exec( "killall " + Constants.playerExec + " > /dev/null 2>&1" )
            Application.pushNotification( "Stopping ..." )
        }
    }
}

class PlayerActor extends Actor with ActorLogging {

    def receive = {
        case p: Play => {
            if ( Player.songsQueue.nonEmpty ) {
                val song = Player.songsQueue.dequeue()
                song.copy(played = song.played + 1L).save()
                Player.currentSong = Option.apply( song.copy() )
                val command = Array[String]( Constants.playerExec, song.path )
                Application.pushNotification( "Playing '" + song.name + "' by " + song.artist + " from " + song.album)
                val process = Runtime.getRuntime().exec( command )
                val ret = process.waitFor()
                if ( ret == 0 ) {
                    Player.queuer ! Done()
                }
                Player.currentSong = None
                Application.updateClientPlaying( )
            }
        }
    }
}
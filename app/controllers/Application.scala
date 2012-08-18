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

    val toEventSource = Enumeratee.map[JsValue] { msg => "data: " + msg.toString() + "\n\n" }

    val hubEnumerator = Enumerator.imperative[JsValue]()

    val hub = Concurrent.hub[JsValue]( hubEnumerator )

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

    def pushNotification(notification: String): Unit = {
        hubEnumerator.push( playingDataJson( notification, "" ) )
    }

    def updateClientLibrary(): Unit = {
        hubEnumerator.push( playingDataJson( "", "updatelib" ) )
    }

    def updateClientPlaying(): Unit = {
        hubEnumerator.push( playingDataJson( ) )
    }

    def pushNotificationAndUpdateClientLib(notification: String): Unit = {
        hubEnumerator.push( playingDataJson( notification, "updatelib" ) )
    }

    def playingSSE() = Action {
        Ok.feed( hub.getPatchCord().through( toEventSource ) ).as( "text/event-stream" )
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

    def reindexLibrary() = Action {
        CommandsController.reindexLibrary()
        Redirect( routes.Application.index() )
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
}
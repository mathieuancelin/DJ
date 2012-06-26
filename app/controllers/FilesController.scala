package controllers

import play.api._
import mvc._
import play.api.data.Forms._
import play.api.data._
import models._
import services._

import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._
import java.io.File
import util.{Constants, ZipUtils}
import java.util


object FilesController extends Controller {

    def createTmpRootIfNotExists() = {
        val root = new File( Constants.tmpDownloadDir )
        if (!root.exists()) {
            root.mkdirs()
        }
    }

    def erase( file: java.io.File ): Unit = {
        if (file.isDirectory) {
            Option.apply( file.list() ).foreach { files => files.foreach { f => erase( new java.io.File( f ) ) } }
            file.delete()
        } else {
            file.delete()
        }
    }

    def runZipCleaner() = {
        Akka.system.scheduler.schedule( 0 millisecond, 120 minutes ) {
            val rootFiles = new java.io.File( Constants.tmpDownloadDir )
            if ( rootFiles.exists ) {
                erase( rootFiles )
                rootFiles.mkdirs()
            }
        }
    }

    val uploadForm = Form( tuple( "inputArtist" -> text, "inputAlbum" -> text ) )

    def upload() = Action(parse.anyContent) { implicit request =>
        uploadForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post an 'artist' and 'album' value!" ),
            { value =>
                val artist = value._1
                val album = value._2
                if (!artist.isEmpty && !album.isEmpty) {
                    request.body.asMultipartFormData.headOption.map { m =>
                        m.files.map { file =>
                            import java.io.File
                            val filename = file.filename
                            val contentType = file.contentType
                            val artistDir = new File(Constants.musicBase, artist)
                            if (!artistDir.exists()) {
                                artistDir.mkdirs()
                            }
                            val albumDir = new File(artistDir, album)
                            if (!albumDir.exists()) {
                                albumDir.mkdirs()
                            }
                            val song = new File(albumDir, filename)
                            if (!song.exists()) {
                                if (filename.toLowerCase.endsWith("mp3")) {
                                    file.ref.moveTo(song)
                                    val s = Song(0L, song.getAbsolutePath(), filename, artist, album, 0L, 0L, 0L)
                                    s.createIfNotExistByPath().foreach { s =>
                                        println("Persist'" + s.path + "' to database")
                                    }
                                    Application.pushNotification( "New MP3 '" + song.getName + "' has been uploaded to the library" )
                                }
                            }
                        }
                        Application.updateClientLibrary()
                    }
                }
                Redirect( routes.Application.index() )
            }
        )
    }

    def file(id: Long) = Action { implicit request =>
        createTmpRootIfNotExists()
        Song.findById( id ).map { song =>
            Ok.sendFile( new java.io.File( song.path ) )    
        }.getOrElse {
            BadRequest( "Song with id " + id + " doesn't exists." )
        }
    }

    def currentSong() = Action { implicit request =>
        createTmpRootIfNotExists()
        Player.currentSong.map { song =>
            Ok.sendFile( new java.io.File( song.path ) )
        }.getOrElse {
            BadRequest( "Song doesn't exists." )
        }
    }

    def currentQueue() = Action { implicit request =>
        createTmpRootIfNotExists()
        val promise = Akka.future {
            val tmpFileName = Constants.tmpDownloadDir + "/" + System.currentTimeMillis() + ".zip"
            ZipUtils.zip(tmpFileName, Player.songsQueue.map { _.path } )
            Ok.sendFile( new java.io.File( tmpFileName ) )
        }
        Async {
            promise.map { result => result }
        }
    }
    
    def artist(name: String) = Action { implicit request =>
       createTmpRootIfNotExists()
       val promise = Akka.future { 
            val tmpFileName = Constants.tmpDownloadDir + "/" + name.replace(" ", "") + ".zip"
            if ( !new java.io.File( tmpFileName ).exists ) {
                val possibleArtistFolder = Constants.musicBase + "/" + name.replace(" ", "")
                ZipUtils.zip(tmpFileName, Song.findByArtist( name ).map { _.path } )
            }
            Ok.sendFile( new java.io.File( tmpFileName ) )
        }
        Async {
            promise.map { result => result }
        }    
    }
    def album(artist: String, name: String) = Action { implicit request =>
        createTmpRootIfNotExists()
        val promise = Akka.future { 
            val tmpFileName = Constants.tmpDownloadDir + "/" + artist.replace(" ", "") + name.replace(" ", "") + ".zip"
            if ( !new java.io.File( tmpFileName ).exists ) {
                val possibleArtistFolder = Constants.musicBase + "/" + artist.replace(" ", "") + "/" + name.replace(" ", "")
                ZipUtils.zip(tmpFileName, Song.findByArtistAndAlbum( artist, name ).map { _.path } )
            }
            Ok.sendFile( new java.io.File( tmpFileName ) )
        }
        Async {
            promise.map { result => result }
        }    
    }
    def song(artist: String, album: String, name: String) = Action { implicit request =>
        Ok.sendFile( new java.io.File( Song.findByArtistAndAlbumAndName( artist, album, name ).head.path ) )
    }
}
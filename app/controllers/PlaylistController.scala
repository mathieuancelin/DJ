package controllers

import play.api.mvc._
import models._
import play.api.cache._
import play.api.Play.current
import services._
import play.api.libs.json._
import util.LastFM
import play.api.data.Forms._
import play.api.data._

object PlaylistController extends Controller {

    val idForm = Form( "id" -> number )
    val nameForm = Form( "inputName" -> text )

    def index() = Action {
        val playlists = Playlist.findAll().sortWith {
            (pl1, pl2) => pl1.name.compareTo(pl2.name) < 0
        }
        Ok(views.html.playlists( playlists ))
    }

    def addPlaylist() = Action { implicit request =>
        nameForm.bindFromRequest().fold(
            formWithErrors => BadRequest( "You have to post a 'name' value" ),
            { name =>
                Playlist(-1L, name, 0L, 0L, 0l).save()
                Redirect( routes.PlaylistController.index() )
            }
        )
    }

    def showPlaylist(id: Long) = Action {
        Ok( views.html.playlist( Playlist.findById( id ).map { _.songs() } getOrElse( List[Song]() ) ) )
    }

    def enqueuePlaylist() = Action { implicit request =>
        idForm.bindFromRequest().fold(
            formWithErrors => BadRequest( "You have to post an 'id' value" ),
            { id =>
                Playlist.findById(id).map { pl =>
                     pl.songs().foreach { song => Player.enqueue( song.id ) }
                }
                Ok
            }
        )
    }

    def deletePlaylist() = Action { implicit request =>
        idForm.bindFromRequest().fold(
            formWithErrors => BadRequest( "You have to post an 'id' value" ),
            { id =>
                Playlist.findById(id).foreach { _.delete() }
                Ok
            }
        )
    }

    def likePlaylist() = Action { implicit request =>
        idForm.bindFromRequest().fold(
            formWithErrors => BadRequest( "You have to post an 'id' value" ),
            { id =>
                Playlist.findById(id).foreach { pl => Playlist(pl.id, pl.name, pl.likeit + 1, pl.dontlikeit, pl.played).save() }
                Ok( Playlist.findById(id).map { pl => pl.likeit + " / " + pl.dontlikeit }.getOrElse("0 / 0") )
            }
        )
    }

    def dontlikePlaylist() = Action { implicit request =>
        idForm.bindFromRequest().fold(
            formWithErrors => BadRequest( "You have to post an 'id' value" ),
            { id =>
                Playlist.findById(id).foreach { pl => Playlist(pl.id, pl.name, pl.likeit, pl.dontlikeit + 1, pl.played).save() }
                Ok( Playlist.findById(id).map { pl => pl.likeit + " / " + pl.dontlikeit }.getOrElse("0 / 0") )
            }
        )
    }

    def addSongTo(playListId: Long) = Action { implicit request =>
        idForm.bindFromRequest().fold(
            formWithErrors => BadRequest( "You have to post an 'id' value" ),
            { id =>
                PlaylistSongs(-1L, playListId, id).save()
                Ok
            }
        )
    }
    def deleteSongFrom(playListId: Long) = Action { implicit request =>
        idForm.bindFromRequest().fold(
            formWithErrors => BadRequest( "You have to post an 'id' value" ),
            { id =>
                PlaylistSongs.findByPlaylistAndSong(playListId, id).foreach { pls =>
                    pls.delete()
                }
                Ok
            }
        )
    }
}
package models

import anorm._ 
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

case class PlaylistSongs( id: Long = -1L, playlistId: Long, songId: Long ) {

    def save() = PlaylistSongs.save( this )

    def delete() = PlaylistSongs.delete( id )

    def exists() = PlaylistSongs.exists( this )

    def song() =  PlaylistSongs.loadSong( this )
}

object PlaylistSongs {

    val simple = {
        get[Long]( "playlistsongs.id" ) ~
        get[Long]( "playlistsongs.id_playlist" ) ~
        get[Long]( "playlistsongs.id_song" ) map {
            case id ~ playlistId ~ songId => PlaylistSongs( id, playlistId, songId )
        }
    }

    def findAll() = DB.withConnection { implicit connection =>
        SQL( "select * from playlistsongs" ).as( PlaylistSongs.simple * )
    }

    def findById( id:Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from playlistsongs s where s.id = {id}" ).on( "id" -> id ).as( PlaylistSongs.simple.singleOpt )
    }

    def findByPlaylistId( playlistId: Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from playlistsongs s where s.id_playlist = {id}" ).on( "id" -> playlistId ).as( PlaylistSongs.simple * )
    }

    def findByPlaylistAndSong( playlistId: Long, songId: Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from playlistsongs s where s.id_playlist = {id_playlist} and s.id_song = {id_song}" ).on( "id_song" -> songId, "id_playlist" -> playlistId ).as( PlaylistSongs.simple * )
    }

    def loadSong( pls: PlaylistSongs ) = {
        Song.findById( pls.songId )
    }

    def create( model: PlaylistSongs ) = DB.withConnection { implicit connection =>
        val id: Long = PlaylistSongs.nextId()
        SQL( "insert into playlistsongs values ( {id}, {id_playlist}, {id_song} )" ).on( "id" -> id, "id_playlist" -> model.playlistId, "id_song" -> model.songId ).executeUpdate()
        ( id, PlaylistSongs( id, model.playlistId, model.songId ) )
    }

    def save( model:PlaylistSongs ) = {
        if ( PlaylistSongs.findById( model.id ).isDefined ) {
            PlaylistSongs.update( model.id, model )
        } else {
            PlaylistSongs.create( model )._2
        }
    }

    def delete( id: Long ) = DB.withConnection { implicit connection =>
        SQL( "delete from playlistsongs where id = {id}" ).on( "id" -> id ).executeUpdate()
    }

    def deleteAll() = DB.withConnection { implicit connection =>
        SQL( "delete from playlistsongs" ).executeUpdate()
    }

    def update( id: Long, model: PlaylistSongs ) = DB.withConnection { implicit connection =>
        SQL( "update playlistsongs set id_playlist = {id_playlist}, id_song = {id_song} where id = {id}" ).on( "id"-> id, "id_playlist" -> model.playlistId, "id_song" -> model.songId ).executeUpdate()
        PlaylistSongs( id, model.playlistId, model.songId )
    }

    def count() = DB.withConnection { implicit connection => 
        val firstRow = SQL( "select count(*) as s from playlistsongs" ).apply().head 
        firstRow[Long]( "s" )
    }

    def nextId() = DB.withConnection { implicit connection =>
        SQL( "select next value for playlistsongs_seq" ).as( scalar[Long].single )
    }

    def exists( id: Long ) = PlaylistSongs.findById( id ).isDefined
    def exists( model: PlaylistSongs ) = PlaylistSongs.findById( model.id ).isDefined
}
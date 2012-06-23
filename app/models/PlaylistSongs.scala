package models

import anorm._ 
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

case class PlaylistSongs( id: Long = -1L, songId: Long ) {

    def save() = PlaylistSongs.save( this )

    def delete() = PlaylistSongs.delete( id )

    def exists() = PlaylistSongs.exists( this ) 
}

object PlaylistSongs {

    val simple = {
        get[Long]( "playlistsongs.id_playlist" ) ~ get[Long]( "playlistsongs.id_song" ) map {
            case id ~ songId => PlaylistSongs( id, songId )
        }
    }

    def findAll() = DB.withConnection { implicit connection =>
        SQL( "select * from playlistsongs" ).as( PlaylistSongs.simple * )
    }

    def findById( id:Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from playlistsongs s where s.id = {id}" ).on( "id" -> id ).as( PlaylistSongs.simple.singleOpt )
    } 

    def create( model: PlaylistSongs ) = DB.withConnection { implicit connection =>
        val id: Long = PlaylistSongs.nextId()
        SQL( "insert into playlistsongs values ( {id_playlist}, {id_song} )" ).on( "id_playlist" -> id, "id_song" -> model.songId ).executeUpdate()
        ( id, PlaylistSongs( id, model.songId ) )
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
        SQL( "update playlistsongs set id_song = {id_song} where id = {id}" ).on( "id"-> id, "id_song" -> model.songId ).executeUpdate()
        PlaylistSongs( id, model.songId )
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
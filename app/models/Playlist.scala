package models

import anorm._ 
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

case class Playlist( id: Long = -1L, name: String, likeit: Long, dontlikeit: Long, played: Long ) {

    def save() = Playlist.save( this )

    def delete() = Playlist.delete( id )

    def exists() = Playlist.exists( this )

    def songs() = Playlist.songs( this )
}

object Playlist {

    val simple = {
        get[Long]( "playlist.id" ) ~
        get[String]( "playlist.name" ) ~
        get[Long]( "playlist.likeit" ) ~
        get[Long]( "playlist.dontlikeit" ) ~
        get[Long]( "playlist.played" ) map {
            case id ~ name ~ likeit ~ dontlikeit ~ played => Playlist( id, name, likeit, dontlikeit, played )
        }
    }

    def findAll() = DB.withConnection { implicit connection =>
        SQL( "select * from playlist" ).as( Playlist.simple * )
    }

    def findById( id:Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from playlist s where s.id = {id}" ).on( "id" -> id ).as( Playlist.simple.singleOpt )
    } 

    def create( model: Playlist ) = DB.withConnection { implicit connection =>
        val id: Long = Playlist.nextId()
        SQL( "insert into playlist values ( {id}, {name}, {likeit}, {dontlikeit}, {played} )" )
            .on( "id" -> id, "name" -> model.name,
            "likeit" -> model.likeit, "dontlikeit" -> model.dontlikeit, "played" -> model.played ).executeUpdate()
        ( id, Playlist( id, model.name, model.likeit, model.dontlikeit, model.played ) )
    }

    def save( model:Playlist ) = {
        if ( Playlist.findById( model.id ).isDefined ) {
            Playlist.update( model.id, model )
        } else {
            Playlist.create( model )._2
        }
    }

    def delete( id: Long ) = DB.withConnection { implicit connection =>
        SQL( "delete from playlist where id = {id}" ).on( "id" -> id ).executeUpdate()
    }

    def deleteAll() = DB.withConnection { implicit connection =>
        SQL( "delete from playlist" ).executeUpdate()
    }

    def update( id: Long, model: Playlist ) = DB.withConnection { implicit connection =>
        SQL( "update playlist set name = {name}, likeit = {likeit}, dontlikeit = {dontlikeit}, played = {played} where id = {id}" )
            .on( "id" -> id, "name" -> model.name,
            "likeit" -> model.likeit,
            "dontlikeit" -> model.dontlikeit, "played" -> model.played  ).executeUpdate()
        Playlist( id, model.name, model.likeit, model.dontlikeit, model.played )
    }

    def count() = DB.withConnection { implicit connection => 
        val firstRow = SQL( "select count(*) as s from playlist" ).apply().head 
        firstRow[Long]( "s" )
    }

    def nextId() = DB.withConnection { implicit connection =>
        SQL( "select next value for playlist_seq" ).as( scalar[Long].single )
    }

    def songs( pl: Playlist ) = {
        PlaylistSongs.findByPlaylistId( pl.id ).map { _.song() }.filter { _.isDefined }.map { _.get }
    }

    def exists( id: Long ) = Playlist.findById( id ).isDefined
    def exists( model: Playlist ) = Playlist.findById( model.id ).isDefined
}
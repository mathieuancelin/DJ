package models

import anorm._ 
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

case class Playlist( id: Long = -1L ) {

    def save() = Playlist.save( this )

    def delete() = Playlist.delete( id )

    def exists() = Playlist.exists( this ) 
}

object Playlist {

    val simple = {
        get[Long]( "playlist.id" ) map {  // use ~ get[String]("playlist.stuff")
            case id => Playlist( id ) // use case id ~ stuff => Playlist( id, stuff ) 
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
        /** TODO : update the SQL req below to match your case class structure **/
        SQL( "insert into playlist values ( {id} )" ).on( "id" -> id ).executeUpdate()
        ( id, Playlist( id ) )
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
        /** TODO : update the SQL req below to match your case class structure **/
        //SQL( "update playlist set stuff = {stuffvalue} where id = {id}" ).on( "id"-> id, "stuffvalue" -> "value" ).executeUpdate()
        Playlist( id )
    }

    def count() = DB.withConnection { implicit connection => 
        val firstRow = SQL( "select count(*) as s from playlist" ).apply().head 
        firstRow[Long]( "s" )
    }

    def nextId() = DB.withConnection { implicit connection =>
        SQL( "select next value for playlist_seq" ).as( scalar[Long].single )
    }

    def exists( id: Long ) = Playlist.findById( id ).isDefined
    def exists( model: Playlist ) = Playlist.findById( model.id ).isDefined
}
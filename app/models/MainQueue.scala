package models

import anorm._ 
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

case class MainQueue( id: Long = -1L, songId: Long ) {

    def save() = MainQueue.save( this )

    def delete() = MainQueue.delete( id )

    def exists() = MainQueue.exists( this ) 
}

object MainQueue {

    val simple = {
        get[Long]( "queue.id" ) ~ get[Long]( "queue.id_song" ) map {
            case id ~ songId => MainQueue( id, songId ) 
        }
    }

    def findAll() = DB.withConnection { implicit connection =>
        SQL( "select * from queue" ).as( MainQueue.simple * )
    }

    def findById( id:Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from queue s where s.id = {id}" ).on( "id" -> id ).as( MainQueue.simple.singleOpt )
    } 

    def create( model: MainQueue ) = DB.withConnection { implicit connection =>
        val id: Long = MainQueue.nextId()
        SQL( "insert into queue values ( {id}, {id_song} )" ).on( "id" -> id, "id_song" -> model.songId ).executeUpdate()
        ( id, MainQueue( id, model.songId ) )
    }

    def save( model:MainQueue ) = {
        if ( MainQueue.findById( model.id ).isDefined ) {
            MainQueue.update( model.id, model )
        } else {
            MainQueue.create( model )._2
        }
    }

    def delete( id: Long ) = DB.withConnection { implicit connection =>
        SQL( "delete from queue where id = {id}" ).on( "id" -> id ).executeUpdate()
    }

    def deleteAll() = DB.withConnection { implicit connection =>
        SQL( "delete from queue" ).executeUpdate()
    }

    def update( id: Long, model: MainQueue ) = DB.withConnection { implicit connection =>
        /** TODO : update the SQL req below to match your case class structure **/
        SQL( "update queue set id_song = {id_song} where id = {id}" ).on( "id"-> id, "id_song" -> model.songId ).executeUpdate()
        MainQueue( id, model.songId )
    }

    def count() = DB.withConnection { implicit connection => 
        val firstRow = SQL( "select count(*) as s from queue" ).apply().head 
        firstRow[Long]( "s" )
    }

    def nextId() = DB.withConnection { implicit connection =>
        SQL( "select next value for queue_seq" ).as( scalar[Long].single )
    }

    def exists( id: Long ) = MainQueue.findById( id ).isDefined
    def exists( model: MainQueue ) = MainQueue.findById( model.id ).isDefined
}
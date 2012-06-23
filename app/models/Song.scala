package models

import anorm._ 
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

case class Song( id: Long = -1L, path: String, name: String, artist: String, album: String, likeit: Long, dontlikeit: Long, played: Long ) {

    def save() = Song.save( this )

    def delete() = Song.delete( id )

    def exists() = Song.exists( this ) 

    def createIfNotExistByPath() = Song.createIfNotExistByPath( this )
}

object Song {

    val simple = {
        get[Long]( "song.id" ) ~ 
        get[String]( "song.filepath" ) ~ 
        get[String]( "song.name" ) ~ 
        get[String]( "song.artist" ) ~  
        get[String]( "song.album" ) ~   
        get[Long]( "song.likeit" ) ~  
        get[Long]( "song.dontlikeit" ) ~  
        get[Long]( "song.played" ) map {
            case id ~ path ~ name ~ artist ~ album ~ likeit ~ dontlikeit ~ played => Song( id, path, name, artist, album, likeit, dontlikeit, played )
        }
    }

    def findAll() = DB.withConnection { implicit connection =>
        SQL( "select * from song" ).as( Song.simple * )
    }

    def findById( id:Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from song s where s.id = {id}" ).on( "id" -> id ).as( Song.simple.singleOpt )
    } 

    def findByPath( path: String ) = DB.withConnection { implicit connection =>
        SQL( "select * from song s where s.filepath = {filepath}" )
            .on( "filepath" -> path ).as( Song.simple * )
    }

    def findByAlbum( album: String ) = DB.withConnection { implicit connection =>
        SQL( "select * from song s where s.album = {album}" )
            .on( "album" -> album ).as( Song.simple * )
    }

    def findByArtist( artist: String ) = DB.withConnection { implicit connection =>
        SQL( "select * from song s where s.artist = {artist}" )
            .on( "artist" -> artist ).as( Song.simple * )
    }

    def findByName( name: String ) = DB.withConnection { implicit connection =>
        SQL( "select * from song s where s.name = {name}" )
            .on( "name" -> name ).as( Song.simple * )
    }

    def findByArtistAndAlbum( artist: String, album: String ) = DB.withConnection { implicit connection =>
        SQL( "select * from song s where s.album = {album} and s.artist = {artist}" )
            .on( "album" -> album, "artist" -> artist ).as( Song.simple * )
    }

    def findByArtistAndAlbumAndName( artist: String, album: String, name: String ) = DB.withConnection { implicit connection =>
        SQL( "select * from song s where s.album = {album} and s.artist = {artist} and s.name = {name}" )
            .on( "album" -> album, "artist" -> artist, "name" -> name ).as( Song.simple * )
    }

    def mostLiked() = DB.withConnection { implicit connection =>
        SQL( "select * from song s order by likeit desc limit 30" ).as( Song.simple * )
    }

    def mostUniked() = DB.withConnection { implicit connection =>
        SQL( "select * from song s order by dontlikeit desc limit 30" ).as( Song.simple * )
    }

    def mostListened() = DB.withConnection { implicit connection =>
        SQL( "select * from song s order by played desc limit 30" ).as( Song.simple * )
    }

    def create( model: Song ) = DB.withConnection { implicit connection =>
        val id: Long = Song.nextId()
        SQL( "insert into song values ( {id}, {filepath}, {name}, {artist}, {album}, {likeit}, {dontlikeit}, {played} )" )
            .on( "id" -> id, "filepath" -> model.path, "name" -> model.name, 
                 "album" -> model.album, "artist" -> model.artist, "likeit" -> model.likeit, 
                 "dontlikeit" -> model.dontlikeit, "played" -> model.played ).executeUpdate()
        val song = Song( id, model.path, model.name, model.artist, model.album, model.likeit, model.dontlikeit, model.played )
        ( id,  song )
    }

    def save( model:Song ) = {
        if ( Song.findById( model.id ).isDefined ) {
            Song.update( model.id, model )
        } else {
            Song.create( model )._2
        }
    }

    def createIfNotExistByPath( model:Song ) = {
        if ( Song.findByPath( model.path ).size <= 0 ) {
            Option(Song.create( model )._2)
        } else {
            Option.empty[Song]
        }
    }

    def delete( id: Long ) = DB.withConnection { implicit connection =>
        SQL( "delete from song where id = {id}" ).on( "id" -> id ).executeUpdate()
    }

    def deleteAll() = DB.withConnection { implicit connection =>
        SQL( "delete from song" ).executeUpdate()
    }

    def update( id: Long, model: Song ) = DB.withConnection { implicit connection =>
        SQL( "update song set filepath = {filepath}, name = {name}, album = {album}, artist = {artist}, likeit = {likeit}, dontlikeit = {dontlikeit}, played = {played} where id = {id}" )
            .on( "id" -> id, "filepath" -> model.path, "name" -> model.name, 
                 "album" -> model.album, "artist" -> model.artist, "likeit" -> model.likeit, 
                 "dontlikeit" -> model.dontlikeit, "played" -> model.played  ).executeUpdate()
        Song( id, model.path, model.name, model.artist, model.album, model.likeit, model.dontlikeit, model.played )
    }

    def count() = DB.withConnection { implicit connection => 
        val firstRow = SQL( "select count(*) as s from song" ).apply().head 
        firstRow[Long]( "s" )
    }

    def nextId() = DB.withConnection { implicit connection =>
        SQL( "select next value for song_seq" ).as( scalar[Long].single )
    }

    def exists( id: Long ) = Song.findById( id ).isDefined
    def exists( model: Song ) = Song.findById( model.id ).isDefined
}
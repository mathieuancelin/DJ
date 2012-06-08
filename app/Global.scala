import play.api._
import java.io._
import models._
import controllers._
import services._
import scala.collection.mutable._

object Global extends GlobalSettings {
  
    override def onStart( app: Application ) {
        val base = app.configuration.getString( "music.root" )
            .getOrElse( "/Users/mathieuancelin/Music/iTunes/iTunes Music" )
        MusicLibraryScanner.scan( base ) 
    }
}


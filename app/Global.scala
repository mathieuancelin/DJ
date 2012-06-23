import play.api._
import java.io._
import models._
import controllers._
import services._
import scala.collection.mutable._
import util.Constants

object Global extends GlobalSettings {
  
    override def onStart( app: Application ) {
        MusicLibraryScanner.autoScann( Constants.musicBase )
    }
}


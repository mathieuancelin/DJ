package util

import play.api.Play
import play.api.Play._

object Constants {

    val musicBase = Play.configuration.getString( "music.root" )
        .getOrElse( "/Users/mathieuancelin/Music/iTunes/iTunes Music" )

    val playerExec = Play.configuration.getString( "player.exec" ).getOrElse( "afplay" )

    val tmpDownloadDir =  Play.configuration.getString( "player.download.dir" ).getOrElse( "/tmp/dj" )

}

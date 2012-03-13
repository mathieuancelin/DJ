package services

import play.api._
import java.io._
import models._
import controllers._
import services._
import scala.collection.mutable._

object MusicLibraryScanner {

    var songsList = IndexedSeq[Song]()

    val dirFilter = new FilenameFilter() {
        def accept( f: File, name: String ) = {
            new File( f, name ).isDirectory()
        }    
    }

    val mp3Filter = new FilenameFilter() {
        def accept( f: File, name: String ) = {
            name.endsWith( ".mp3" )
        }    
    }

    def scan( base: String ) = {
        var index = 0L
        new File( base ).list( dirFilter ).foreach { artist =>
            new File( base, artist ).list( dirFilter ).foreach { album =>
                new File( base + "/" + artist, album ).list( mp3Filter ).foreach { song =>
                    val s = Song(index, new File( base + "/" + artist + "/" + album, song )
                        .getAbsolutePath(), song, artist, album)
                    songsList = songsList :+ s
                    index = index + 1
                }
            }
        }
        Player.songsList = songsList.sortWith { (a, b) =>
            a.path.compareToIgnoreCase(b.path) < 0
        }
    }
}
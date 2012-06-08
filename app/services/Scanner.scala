package services

import play.api._
import java.io._
import models._
import controllers._
import services._
import scala.collection.mutable._

object MusicLibraryScanner {

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
        Player.songsList = IndexedSeq[Song]()
        var songsList = IndexedSeq[Song]()
        var index = 0L
        println("Scan music library at '" + base + "'")
        Option.apply(new File( base ).list( dirFilter )).foreach { listart => listart.foreach { artist =>
            println("Found artist " + artist)
            Option.apply(new File( base, artist ).list( dirFilter )).foreach { listal => listal.foreach { album =>
                println("  found album " + album)
                Option.apply(new File( base + "/" + artist, album ).list( mp3Filter )).foreach { listso => listso.foreach { song =>
                    if (!song.startsWith(".")) {
                        println("    found song : " + song)
                        val s = Song(index, new File( base + "/" + artist + "/" + album, song )
                            .getAbsolutePath(), song, artist, album)
                        songsList = songsList :+ s
                        index = index + 1
                    }
                }}
            }}
        }}
        Player.songsList = songsList.sortWith { (a, b) =>
            a.path.compareToIgnoreCase(b.path) < 0
        }
    }
}
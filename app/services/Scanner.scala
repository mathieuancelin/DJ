package services

import play.api._
import java.io._
import models._
import controllers._
import services._
import scala.collection.mutable._
import akka.util.duration._
import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._

object MusicLibraryScanner {

    val dirFilter = new FilenameFilter() {
        def accept( f: File, name: String ) = {
            new File( f, name ).isDirectory()
        }    
    }

    val mp3Filter = new FilenameFilter() {
        def accept( f: File, name: String ) = {
            name.toLowerCase.endsWith( ".mp3" )
        }    
    }

    def autoScann( base: String) = {
        Akka.system.scheduler.schedule( 0 millisecond, 500 seconds ) {
            scan( base )
        }
    }

    def scan( base: String ) = {
        Akka.system.scheduler.scheduleOnce(0 millisecond) {
            var songsList = IndexedSeq[Song]()
            var index = 0L
            print("Scanning music library at '" + base + "' ... ")
            val start = System.currentTimeMillis()
            Option.apply(new File( base ).list( dirFilter )).foreach { listart => listart.foreach { artist =>
                Option.apply(new File( base, artist ).list( dirFilter )).foreach { listal => listal.foreach { album =>
                    Option.apply(new File( base + "/" + artist, album ).list( mp3Filter )).foreach { listso => listso.foreach { song =>
                        if (!song.startsWith(".")) {
                            val s = Song(index, new File( base + "/" + artist + "/" + album, song )
                                .getAbsolutePath(), song, artist, album, 0L, 0L, 0L)
                            songsList = songsList :+ s
                            index = index + 1
                        }
                    }}
                }}
            }}
            var i = 0
            songsList.foreach { song =>
                song.createIfNotExistByPath().foreach { s =>
                    println("Persist'" + s.path + "' to database")
                    i = i + 1
                }
            }
            if (i > 0) {
                Application.pushNotification( "Persisted '" + i + "' song(s) in database" )
            }
            i = 0
            Song.findAll().foreach { song =>
                if (!new File(song.path).exists) {
                    song.delete()
                    i = i + 1
                }
            }
            if (i > 0) {
                Application.pushNotification( "Deleted '" + i + "' song(s) from database" )
            }
            println("done (" + (System.currentTimeMillis() - start) + " ms.)")
            Application.pushNotificationAndUpdateClientLib( "Music library has been scanned in " + (System.currentTimeMillis() - start) + " ms." )
        }
    }
}
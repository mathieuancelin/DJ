import play.api._
import java.io._
import models._
import controllers._
import scala.collection.IndexedSeq

object Global extends GlobalSettings {
  
    override def onStart(app: Application) {
        val base = app.configuration.getString( "music.root" ).getOrElse( "/Users/mathieuancelin/Music/iTunes/iTunes Music" )
        MusicLibraryScanner.scan( base )
    }
}

object MusicLibraryScanner {

    var songsList = IndexedSeq[Song]()

    def scan( base: String ) = {
        var index = 0L
        val root = new File( base )
        val artists = root.list( new FilenameFilter() {
            def accept( f: File, name: String) = {
                if (name.equals("Podcasts")) {
                    false
                } else {
                    new File( f, name ).isDirectory()
                }
            }    
        } )
        for (artist <- artists) {
            val albums = new File( base, artist ).list( new FilenameFilter() {
                def accept( f: File, name: String) = {
                    new File( f, name ).isDirectory()
                }    
            } )
            if (albums != null) {
                for (album <- albums) {
                    val songs = new File( base + "/" + artist, album ).list( new FilenameFilter() {
                        def accept( f: File, name: String) = {
                            name.endsWith( ".mp3" )
                        }    
                    } )
                    for (song <- songs) {
                        val s = Song(index, new File( base + "/" + artist + "/" + album, song ).getAbsolutePath(), song, artist, album)
                        songsList = songsList :+ s
                        index = index + 1
                    }
                }
            }
        }
        Application.songsList = songsList
    }
}
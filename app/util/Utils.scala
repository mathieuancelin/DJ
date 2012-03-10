package util

import models._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.ws._
import play.api.cache._
import play.api.Play.current

object LastFM {
    def getCoverArt(song: Song) = {
        println("getting img from lastfm")
        WS.url("http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=b25b959554ed76058ac220b7b2e0a026&artist=" 
            + song.artist.replace(" ", "%20")
            + "&album="
            + song.album.replace(" ", "%20")).get().onRedeem { value =>
                val content: xml.Elem = value.xml
                val images = content \\ "lfm" \\ "album" \\ "image"
                var url = ""
                for (image <- images) {
                    if ((image \ "@size").text.equals("large")) {
                        url = image.text
                    }
                }
                if (!url.equals("")) {
                    val img = "<img src=\"" + url + "\">"
                    Cache.set(song.artist + song.album, img)
                } else {
                    Cache.set(song.artist + song.album, "---")
                }
        }
    }
}
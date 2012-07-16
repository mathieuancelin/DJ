package util

import play.api.templates.Html
import controllers._

object AssetHelper {

    def cssTag(name: String) = css("stylesheets/" + name)

    def css(path: String) = Html {
        """<link href="%s" type="text/css" media="screen" rel="stylesheet"/>""".format(routes.Assets.at(path))
    }

    def jsTag(name: String) = js("javascripts/" + name)

    def js(path: String) = Html {
        """<script src="%s" type="text/javascript"></script>""".format(routes.Assets.at(path))
    }

    def favicon(path: String) = Html {
        """<link rel="shortcut icon" type="image/png" href="%s">""".format(routes.Assets.at(path))
    }
}

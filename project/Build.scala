import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "DJ"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "commons-net" % "commons-net" % "3.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}

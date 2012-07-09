package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import monitoring.Reporting

/** Uncomment the following lines as needed **/
/**
import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._
**/

object MonitorController extends Controller {

    def monitorSource() = Action {

        Ok.feed( Reporting.monitoringEnumerator.through( Reporting.toEventSource ) ).as( "text/event-stream" )

        /**SimpleResult(
            header = ResponseHeader( OK, Map( CONTENT_LENGTH -> "-1", CONTENT_TYPE -> "text/event-stream") ),
            Reporting.monitoringEnumerator.through( Reporting.toEventSource )
        )**/
    }
}
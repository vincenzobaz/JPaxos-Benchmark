import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import tools.{AkkaConfig, ClientProtocol, ReplicaProtocol}

import scala.concurrent.Future
import scala.concurrent.duration._

object PuppetMaster extends App with AkkaConfig with ClientProtocol with ReplicaProtocol {

  val receptionist = new Receptionist(args.drop(1))

  private val timingsHolder: ActorRef = system.actorOf(Props[PastHolder[OperationTiming]], "timings-holder")
  private val viewsHolder: ActorRef = system.actorOf(Props[PastHolder[NewView]], "views-holder")
  private implicit val timeout: Timeout = 5 seconds

  val route = path("client") {
    post {
      entity(as[OperationTiming]) { ot =>
        timingsHolder ! ot
        complete(StatusCodes.Accepted)
      }
    } ~ get {
      val timings: Future[Timings] = (timingsHolder ? Get).mapTo[List[OperationTiming]].map(Timings)
      complete(timings)
    }
  } ~ path("replica") {
    post {
      entity(as[NewView]) { nv =>
        viewsHolder ! nv
        complete(StatusCodes.Accepted)
      }
    }
  } ~ path("leaders") {
    get {
      val viewsF: Future[Views] = (viewsHolder ? Get).mapTo[List[NewView]].map(Views)
      complete(viewsF)
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9090)
}

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import lsr.common.Configuration
import dummyservice.IntRegisterService
import tools.{AkkaConfig, NetworkStoppable}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object ReplicaManager extends App with AkkaConfig with NetworkStoppable {
  val configFile = args(1)
  val localId = args(0).toInt
  val port = args(2).toInt
  val masterAddr = {
    Try(args(3)).toOption match {
      case Some("") => None
      case Some(x) => Some(x)
      case None => None
    }
  }

  private implicit val timeout: Timeout = 1 minute
  private val replica: ActorRef = system.actorOf(
    Props(new DebuggingReplica(new Configuration(configFile), localId, new IntRegisterService(), masterAddr)),
    "remote-replica")

  val route =
    stopRoute ~ {
      path("status") {
        get {
          complete((replica ? GetStatus).mapTo[StatusResponse].map(_.msg))
       }
      } ~
        path("start") {
          get {
            complete((replica ? Start).mapTo[StartResponse].map(_.msg))
          }
        } ~
        path("kill") {
          get {
            val resp = (replica ? GetStatus).mapTo[StatusResponse].map(_.msg).map { s =>
              Try(s.toInt) match {
                case Success(_) =>
                  exit(1)
                  "Killed\n"
                case Failure(_) => "Replica was already killed\n"
              }
            }
            complete(resp)
          }
        }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", port)

  println(s"Replica control interface online at http://0.0.0.0:$port")
}

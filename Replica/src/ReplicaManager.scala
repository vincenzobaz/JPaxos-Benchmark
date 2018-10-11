import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import lpd.register.IntRegisterService
import lsr.common.Configuration

import scala.concurrent.Future


object ReplicaManager extends App {
  val configFile = args(1)
  val localId = args(0).toInt
  val port = args(2).toInt
  val masterAddr = args(3)

  var isRunning = false
  var replica: DebuggingReplica = null

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def exit(code: Int): Unit = {
    Http().shutdownAllConnectionPools() andThen {
      case _ => system.terminate()
    } onComplete { _ =>
      System.exit(code)
    }
  }

  def startReplica(): Unit = {
    replica = new DebuggingReplica(new Configuration(configFile),
                                   localId,
                                   new IntRegisterService())
    replica.start()
  }

  val route = {
      path("status") {
        get {
          complete(isRunning.toString)
        }
      } ~
      path("stop") {
        get {
          exit(0)
          complete("Goodbye")
        }
      } ~
      path("start") {
        get {
          if (!isRunning) {
            startReplica()
            isRunning = true
            complete("Replica started")
          } else {
            complete("Replica was already running")
          }
        }
      } ~
      path("kill") {
        get {
          if (isRunning) {
            exit(1)
            complete("Killed")
          } else {
            complete("Replica was already killed")
          }
        }
      }
  }


  val bindingFuture = Http().bindAndHandle(route, "localhost", port)

  val responseFuture: Future[HttpResponse] =
    Http()(system).singleRequest(HttpRequest(uri = s"$masterAddr/$localId/imUp"))
  println(s"Replica control interface online at http://0.0.0.0:$port")
}

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import lpd.register.IntRegisterService
import lsr.common.Configuration

import scala.concurrent.Future

object ReplicaManager extends App with AkkaConfig with NetworkStoppable {
  val configFile = args(1)
  val localId = args(0).toInt
  val port = args(2).toInt
  val masterAddr = args(3)

  var isRunning = false
  var replica: DebuggingReplica = null

  def startReplica(): Unit = {
    replica = new DebuggingReplica(new Configuration(configFile),
      localId,
      new IntRegisterService())
    replica.start()
  }

  val route =
    stopRoute ~ {
      path("status") {
        get {
          if (isRunning) complete(replica.leader.toString)
          else complete("Not running")
        }
      } ~
        path("start") {
          get {
            if (!isRunning) {
              startReplica()
              isRunning = true
              complete("Replica started\n")
            } else {
              complete("Replica was already running\n")
            }
          }
        } ~
        path("kill") {
          get {
            if (isRunning) {
              exit(1)
              complete("Killed\n")
            } else {
              complete("Replica was already killed\n")
            }
          }
        }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", port)

  val responseFuture: Future[HttpResponse] =
    Http()(system).singleRequest(HttpRequest(uri = s"$masterAddr/$localId/imUp"))
  println(s"Replica control interface online at http://0.0.0.0:$port")
}

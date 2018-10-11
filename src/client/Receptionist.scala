package client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor

object Receptionist {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def apply(addresses: Array[String]): Receptionist = new Receptionist(addresses)
}

class Receptionist(addresses: Array[String]) {
  import Receptionist._
  val remotes: Array[ReplicaRemote] = addresses.map(ad => new ReplicaRemote(ad))

  val route: Route = path(IntNumber / "imUp") { replicaId =>
    get {
      remotes(replicaId).isUp = true
      println(s"$replicaId is back up")
      complete("It's noted")
    }
  }
}

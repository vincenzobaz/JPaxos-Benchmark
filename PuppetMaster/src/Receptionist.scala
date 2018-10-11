import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class Receptionist(addresses: Array[String]) extends AkkaConfig {
  val remotes: Array[ReplicaRemote] = addresses.map(ad => new ReplicaRemote(ad))

  val route: Route = path(IntNumber / "imUp") { replicaId =>
    get {
      remotes(replicaId).isUp = true
      println(s"$replicaId is back up")
      complete("It's noted")
    }
  }
}

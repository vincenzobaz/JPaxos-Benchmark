import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait AkkaConfig {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
}

trait NetworkStoppable {
  def exit(code: Int)(implicit as: ActorSystem, ex: ExecutionContext): Unit = {
    Http().shutdownAllConnectionPools() andThen {
      case _ => as.terminate()
    } onComplete { _ =>
      System.exit(code)
    }
  }

  def stopRoute(implicit as: ActorSystem, ex: ExecutionContext ): Route = {
    path("stop") {
      get {
        exit(0)
        complete("Goodbye")
      }
    }
  }

}


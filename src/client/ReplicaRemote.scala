package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class ReplicaRemote(address: String)(implicit as: ActorSystem, e: ExecutionContext) {
  var isUp = true

  def get(route: String): Unit = {
    val responseFuture: Future[HttpResponse] =
      Http()(as).singleRequest(HttpRequest(uri = (address + "/" + route)))
    Try(Await.result(responseFuture, 5 seconds)) match {
      case Failure(_) => println(s"ReplicaRemote@$address was not available")
      case Success(httoResponse) => println(s"ReplicaRemote@$address got message $route and replied ${httoResponse.entity.toString()}")
    }
  }

  def start: Unit = get("start")

  def stop: Unit = {
    if (isUp) {
      isUp = false
    }
    get("stop")
  }

  def kill: Unit = {
    if (isUp) {
      isUp = false
    }
    get("kill")
  }
}

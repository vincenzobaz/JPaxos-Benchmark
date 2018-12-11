package tools

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


trait ClientProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  final case class OperationTiming(id: Int, start: Long, end: Long)
  implicit val operationTimingFormat = jsonFormat3(OperationTiming)
}

trait ReplicaProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  final case class NewView(time: Long, localId: Int, newView: Int, newLeader: Int)
  implicit val newViewFormat = jsonFormat4(NewView)
}

trait ControlProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  final case class ControlEvent(time: Long, label: String)
  implicit val controlFormat = jsonFormat2(ControlEvent)
}

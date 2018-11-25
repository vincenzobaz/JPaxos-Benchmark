import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


trait ClientProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  final case class OperationTiming(id: Int, start: Long, end: Long)
  final case class Timings(timings: List[OperationTiming])
  implicit val operationTimingFormat = jsonFormat3(OperationTiming)
  implicit val timingsFormat = jsonFormat1(Timings)
}

trait ReplicaProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  final case class NewView(time: Long, localId: Int, newView: Int, newLeader: Int)
  final case class Views(view: List[NewView])
  final case class Killed(id: Int, time: Long)
  final case class Started(id: Int, time: Long)

  implicit val newViewFormat = jsonFormat4(NewView)
  implicit val viewsFormat = jsonFormat1(Views)
  implicit val killedFormat = jsonFormat2(Killed)
  implicit val startedFormat = jsonFormat2(Started)
}

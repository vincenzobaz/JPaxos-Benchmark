import akka.actor.{Actor, ActorLogging}

import scala.collection.mutable.ListBuffer

case object Get
case object Reset

class PastHolder[T] extends Actor with ActorLogging {

  override def receive: Receive = listenAndStore(ListBuffer[T]())


  def listenAndStore(b: ListBuffer[T]): Receive = {
    case Get => sender ! b.toList
    case Reset => context become listenAndStore(ListBuffer[T]())
    case el: T => context become listenAndStore(b :+ el)
  }
}
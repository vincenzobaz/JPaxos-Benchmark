import akka.actor.{Actor, ActorRef}
import lsr.common.Configuration
import lsr.paxos.replica.Replica
import lsr.service.Service

case object Start

case object GetStatus

final case class StatusResponse(msg: String)
final case class StartResponse(msg: String)

class DebuggingReplica(conf: Configuration, id: Int, srv: Service, masterAddress: Option[String])
  extends Actor {

  private val replica = new Replica(conf, id, srv)

  var isRunning = false
  var leader = 0
  val logger: ViewLogger = {
    if (masterAddress.nonEmpty) new ReplicaSpy(masterAddress.get, id)
    else new ReplicaLogger()
  }

  def start(notify: ActorRef): Unit = {
    isRunning = true
    replica.start()
    replica.onPaxosActive(() => notify ! StartResponse("Replica started\n"))
    replica.addViewChangeListener((newView, newLeader) => {
      leader = newLeader
      logger(newView, newLeader)
    })
    //notify ! StartResponse("Replica started\n")
  }

  override def receive: Receive = {
    case Start =>
      if (!isRunning) start(sender)
      else sender ! StatusResponse("Replica was already running\n")
    case GetStatus => sender ! StatusResponse(if (isRunning) leader.toString else "Not running")
  }
}

import lsr.common.Configuration
import lsr.paxos.replica.Replica
import lsr.service.Service

class DebuggingReplica(conf: Configuration, id: Int, srv: Service, masterAddress: Option[String])
  extends Replica(conf, id, srv) with AkkaConfig with ReplicaProtocol {
  var leader = 0
  val logger: ViewLogger = {
    if (masterAddress.nonEmpty) new ReplicaSpy(masterAddress.get, id)
    else new ReplicaLogger()
  }

  override def start() {
    super.start()
    addViewChangeListener((newView, newLeader) => {
      leader = newLeader
      logger(newView, newLeader)
    })
  }
}

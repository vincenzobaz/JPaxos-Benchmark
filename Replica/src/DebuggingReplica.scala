import lsr.common.Configuration
import lsr.paxos.replica.Replica
import lsr.service.Service

import org.slf4j.{Logger, LoggerFactory}

object DebuggingReplica {
  val logger: Logger = LoggerFactory.getLogger(classOf[DebuggingReplica])

}

class DebuggingReplica(conf: Configuration, id: Int, srv: Service)
    extends Replica(conf, id, srv) {

  import DebuggingReplica._

  override def start() {
    super.start()
    /*
    addLogListener(_ => logger.info(s"Log changed! ${logView()}"))
    addViewChangeListener((newView, newLeader) =>
      logger.info(s"entered view $newView lead by $newLeader"))
      */
  }

}

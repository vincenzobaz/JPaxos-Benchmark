import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.slf4j.{Logger, LoggerFactory}
import tools.{AkkaConfig, ReplicaProtocol}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait ViewLogger extends ((Int, Int) => Unit)

class ReplicaLogger extends ViewLogger {
  private val logger: Logger = LoggerFactory.getLogger(classOf[DebuggingReplica])

  override def apply(v1: Int, v2: Int): Unit =
    logger.info(s"entered view $v1 lead by $v2")
}

class ReplicaSpy(masterAddress: String, localId: Int) extends ViewLogger with AkkaConfig with ReplicaProtocol {

  val QueueSize = 50

  private val (host, port): (String, Int) = {
    val arr = masterAddress.split(":")
    (arr(0), arr(1).toInt)
  }
  private val uri = Uri.from(scheme = "http", host = host, port = port, path = "/replica")
  private val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = host, port = port)

  private val queue = Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.dropNew)
    .via(poolClientFlow)
    .toMat(Sink.foreach({
      case (Success(resp), p) => p.success(resp)
      case (Failure(e), p) => p.failure(e)
    }))(Keep.left)
    .run()

  private def enqueueMsg(nv: NewView) = {
    val fReq: Future[HttpRequest] = Marshal(nv).to[RequestEntity] map { entity => HttpRequest(method = HttpMethods.POST, uri = uri, entity = entity) }
    fReq flatMap { req =>
      val responsePromise = Promise[HttpResponse]()
      queue.offer(req -> responsePromise) flatMap {
        case QueueOfferResult.Enqueued => responsePromise.future
        case QueueOfferResult.Dropped => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) => Future.failed(ex)
        case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
      }
    }
  }

  def notifyLeader(view: Int, leader: Int): Unit =
    enqueueMsg(NewView(System.currentTimeMillis(), localId, view, leader)) andThen {
      case Success(_) =>
      case Failure(e) => println("Request failed :( " + e)
    }

  override def apply(v1: Int, v2: Int): Unit = notifyLeader(v1, v2)
}

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Flow, Source}
import lsr.paxos.client.{Client => PaxosClient}
import dummyservice.Command
import tools.{AkkaConfig, ClientProtocol}

import scala.concurrent.Future
import scala.util.Failure

class PuppetMasterSpy(requests: Source[Command, NotUsed], paxosClient: PaxosClient, localId: Int, puppetMasterAddress: String)
  extends AkkaConfig with ClientProtocol {

  val timings: Flow[Command, OperationTiming, NotUsed] = Flow[Command] map { req =>
    val serialized = req.toByteArray
    val start = System.currentTimeMillis()
    paxosClient.execute(serialized)
    val end = System.currentTimeMillis()
    OperationTiming(localId, start, end)
  }

  val (host, port): (String, Int) = {
    val arr = puppetMasterAddress.split(":")
    (arr(0), arr(1).toInt)
  }
  private val uri = Uri.from(scheme = "http", host = host, port = port, path="/client")

  private def createReq(ot: OperationTiming): Future[(HttpRequest, OperationTiming)] =
    Marshal(ot).to[RequestEntity] map { entity =>
      HttpRequest(method = HttpMethods.POST, uri = uri, entity = entity) -> ot
    }

  private val poolClientFlow = Http().cachedHostConnectionPool[OperationTiming](host = host, port = port)

  requests
    .via(timings)
    .mapAsync(1)(createReq)
    .via(poolClientFlow)
    .runForeach {
      case (Failure(ex), timing) => println(s"Error sending $timing with $ex")
      case _ =>
    }
}

import akka.NotUsed
import lsr.paxos.client.{Client => PaxosClient}
import akka.stream.{FlowShape, ThrottleMode, scaladsl}
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source}
import lpd.register.{Command, Response}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

class LogSpammer(requests: Stream[Command], paxosClient: PaxosClient) extends AkkaConfig {
  val logger: Logger = LoggerFactory.getLogger(classOf[PaxosClient])

  val graph: Flow[Command, (Command, Response), NotUsed] = Flow.fromGraph( GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val serializedCommands = Flow[Command].map(_.toByteArray)
    val serializedResponses = Flow[Array[Byte]].map(paxosClient.execute(_))
    val responses = Flow[Array[Byte]].map(new Response(_))

    val zipper = builder.add(scaladsl.Zip[Command, Response]())
    val bcast1 = builder.add(scaladsl.Broadcast[Array[Byte]](2))
    val bcast2 = builder.add(scaladsl.Broadcast[Array[Byte]](2))
    val bcast3 = builder.add(scaladsl.Broadcast[Command](2))

    bcast3 ~> serializedCommands ~> bcast1 ~> Sink.foreach[Array[Byte]](_ => logger.info("Requested operation"))
    bcast1 ~> serializedResponses ~> bcast2 ~> Sink.foreach[Array[Byte]](_ => logger.info("Completed operation"))
    bcast2 ~> responses ~> zipper.in1
    bcast3 ~> zipper.in0

    FlowShape(bcast3.in, zipper.out)
  })

  Source(requests)
    //.via(Flow[Command].throttle(3, 1 seconds, 0, ThrottleMode.Shaping))
    .via(graph)
    .runForeach { case (cmd, resp) => logger.info(s"Request: ${cmd.toString} - Response: ${resp.toString} ")}
}

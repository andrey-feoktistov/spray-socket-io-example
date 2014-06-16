package spray.contrib.socketio.examples

import akka.io.IO
import akka.actor.{ ActorSystem, Actor, Props, ActorLogging, ActorRef }
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket.frame.Frame
import spray.contrib.socketio.SocketIOExtension
import spray.contrib.socketio.SocketIOServerConnection
import spray.contrib.socketio.packet.EventPacket
import spray.contrib.socketio.namespace.Namespace
import spray.contrib.socketio.namespace.Namespace.{OnConnect, OnData, OnEvent}
import spray.contrib.socketio.namespace.NamespaceExtension
import spray.http._
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object SimpleServer extends App {

  object SocketIOServer {
    def props(resovler: ActorRef) = Props(classOf[SocketIOServer], resolver)
  }
  class SocketIOServer(resolver: ActorRef) extends Actor with ActorLogging {
    def receive = {
      // when a new connection comes in we register a SocketIOConnection actor as the per connection handler
      case Http.Connected(remoteAddress, localAddress) =>
        val serverConnection = sender()
        val conn = context.actorOf(SocketIOWorker.props(serverConnection, resolver))
        serverConnection ! Http.Register(conn)
    }
  }

  object SocketIOWorker {
    def props(serverConnection: ActorRef, resolver: ActorRef) = Props(classOf[SocketIOWorker], serverConnection, resolver)
  }

  class SocketIOWorker(val serverConnection: ActorRef, val resolver: ActorRef) extends Actor with SocketIOServerConnection {

    def genericLogic: Receive = {
      case HttpRequest(HttpMethods.GET, Uri.Path(path), _, _, _) =>
        log.info("request for a path = " + path)
        path match {
          case "/socketio.html" =>
            val content = renderTextFile (path)
            val entity = HttpEntity (ContentType (MediaTypes.`text/html`), content)
            sender () ! HttpResponse (entity = entity)
          case _ => sender ! HttpResponse(StatusCodes.NotFound)
        }

      case x: HttpRequest =>
        log.info("Got http req uri = {}", x.uri.path.toString.split("/").toList)

      case x: Frame =>
    }

    def renderTextFile(path: String) = {
      val stream = getClass.getResourceAsStream("/serversocketiotest/" + path)
      val source = scala.io.Source.fromInputStream(stream)
      val lines = source.getLines mkString "\n"
      source.close()
      lines
    }
  }

  // --- json protocals for socketio messages:
  case class Msg(message: String)
  case class Now(time: String)
  object TheJsonProtocol extends DefaultJsonProtocol {
    implicit val msgFormat = jsonFormat1(Msg)
    implicit val nowFormat = jsonFormat1(Now)
  }
  import spray.json._
  import TheJsonProtocol._

  implicit val system = ActorSystem()
  val socketioExt = SocketIOExtension(system)
  val namespaceExt = NamespaceExtension(system)
  implicit val resolver = namespaceExt.resolver

  val observer = new Observer[OnData] {
    override def onNext(value: OnData) {
      value match {
        case connect @ OnConnect(args, context) =>
          println("observed: OnConnect: " + connect)
          system.scheduler.schedule(10 seconds, 1 second) {
            connect.reply(
              EventPacket(-1L, false, "testendpoint", "time", List(Now("Batched " + (new java.util.Date).toString)).toJson.toString))
          }
        case event @ OnEvent("Hi!", args, context) =>
          println("observed: " + "Hi!" + ", " + args)
          if (event.packet.hasAckData) {
            event.ack("[]")
          }
          event.replyEvent("welcome", List(Msg("Greeting from spray-socketio")).toJson.toString)
          event.replyEvent("time", List(Now((new java.util.Date).toString)).toJson.toString)
          // batched packets
          event.reply(
            EventPacket(-1L, false, "testendpoint", "welcome", List(Msg("Batcher Greeting from spray-socketio")).toJson.toString),
            EventPacket(-1L, false, "testendpoint", "time", List(Now("Batched " + (new java.util.Date).toString)).toJson.toString))
        case OnEvent("time", args, context) =>
          println("observed: " + "time" + ", " + args)
        case _ =>
          println("observed: " + value)
      }
    }
  }

  val channel = Subject[OnData]()
  // there is no channel.ofType method for RxScala, why?
  channel.subscribe(observer)

  namespaceExt.startNamespace("testendpoint")
  namespaceExt.namespace("testendpoint") ! Namespace.Subscribe(channel)

  val server = system.actorOf(SocketIOServer.props(resolver), name = "socketio-server")

  IO(UHttp) ! Http.Bind(server, "localhost", 8080)

  readLine("Hit ENTER to exit ...\n")
  system.shutdown()
  system.awaitTermination()
}

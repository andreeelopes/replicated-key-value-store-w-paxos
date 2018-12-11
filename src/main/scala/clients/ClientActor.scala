package clients

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import clients.test._
import rendezvous.IdentifyClient
import replicas.statemachinereplication
import replicas.statemachinereplication._
import utils.ReplicaNode

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class ClientActor(ip: String, port: Int, rendezvousIP: String, rendezvousPort: Int) extends Actor with ActorLogging {

  val OperationTimeout = 1

  var replicas = List[ReplicaNode]()
  var delivered = Set[String]()
  var appActor: ActorRef = _
  var myReplica: ActorRef = _

  var timers = Map[String, Cancellable]()

  val rendezvous = context.actorSelection {
    s"akka.tcp://RemoteService@$rendezvousIP:$rendezvousPort/user/rendezvous"
  }
  //println(s"$rendezvous")

  rendezvous ! IdentifyClient(self)


  override def receive: Receive = {

    case i: InitClient =>
      appActor = i.appActor


    case WeakGet(key) =>
      myReplica = replicas(pickRandomSmr()).smrActor
      receiveWeakOp(WeakGetRequest(key, generateMID()))

    case StrongGet(key) =>
      myReplica = replicas(pickRandomSmr()).smrActor
      receiveStrongOp(StrongGetRequest(key, generateMID()))

    case Put(key, value) =>
      myReplica = replicas(pickRandomSmr()).smrActor
      receiveStrongOp(PutRequest(key, value, generateMID()))

    case AddReplica(node) =>
      myReplica ! AddReplicaRequest(node, generateMID())

    case RemoveReplica(node) =>
      myReplica ! RemoveReplicaRequest(node, generateMID())

    case ResendOp(op) =>
      if (!delivered.contains(op.mid))
        myReplica ! op
      else {
        timers(op.mid).cancel()
        timers -= op.mid
      }

    case ResendWeakOp(op) =>
      if (!delivered.contains(op.mid))
        myReplica ! op
      else {
        timers(op.mid).cancel()
        timers -= op.mid
      }

    case Reply(event) =>
      if (!delivered.contains(event.mid)) {
        //println(s"Receive(REPLY, $event)")
        delivered += event.mid
        appActor ! ReplyDelivery(event)
      }

    case GetReply(value, mid) =>
      if (!delivered.contains(mid)) {
        delivered += mid
        appActor ! GetReply(value, mid)
      }

    case TestWeakGet(key, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      myReplica ! WeakGetRequest(key, mid)

    case TestStrongGet(key, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      receiveStrongOp(StrongGetRequest(key, mid))

    case TestPut(key, value, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      receiveStrongOp(PutRequest(key, value, mid))

    case TestAddReplica(node, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      myReplica ! AddReplicaRequest(node, mid)

    case TestRemoveReplica(node, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      myReplica ! RemoveReplicaRequest(node, mid)


    case update: UpdateReplicas =>
      println(replicas)
      replicas = update.replicas.toList
      appActor ! update
      myReplica = replicas(pickRandomSmr()).smrActor

  }

  def receiveStrongOp(op: Operation) = {
    myReplica ! op
    timers += (op.mid -> context.system.scheduler.scheduleOnce(
      Duration(OperationTimeout, TimeUnit.SECONDS),
      self, ResendOp(op)))
  }

  def receiveWeakOp(op: WeakGetRequest) = {
    myReplica ! op

    timers += (op.mid -> context.system.scheduler.scheduleOnce(
      Duration(OperationTimeout, TimeUnit.SECONDS),
      self, ResendWeakOp(op)))
  }

  private def generateMID(): String = {
    ip + port + System.nanoTime()
  }

  private def pickRandomSmr(): Int = {

    val rnd = new scala.util.Random
    rnd.nextInt(replicas.size)
  }
}


package clients

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import clients.test.{TestAddReplica, TestGet, TestPut, TestRemoveReplica}
import replicas.statemachinereplication
import replicas.statemachinereplication._
import utils.ReplicaNode

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class ClientActor(ip: String, port: Int) extends Actor with ActorLogging {

  val OperationTimeout = 1

  var replicas = List[ReplicaNode]()
  var delivered = Set[String]()
  var appActor: ActorRef = _
  var myReplica: ActorRef = _

  var timers = Map[String, Cancellable]()


  override def receive: Receive = {

    case i: InitClient =>
      appActor = i.appActor
      var myReplicaId = i.smr
      if (i.smr == -1) {
        myReplicaId = pickRandomSmr()
      }
      myReplica = replicas(myReplicaId).smrActor

    case Get(key) => // TODO adicionar timer para lidar com a possibilidade de o smr nao responder ao get
      myReplica ! GetRequest(key, generateMID())


    case Put(key, value) =>
      myReplica = replicas(pickRandomSmr()).smrActor
      receivePut(key, value, generateMID())


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

    case Reply(event) =>
      if (!delivered.contains(event.mid)) {
        delivered += event.mid
        appActor ! ReplyDelivery(event)
      }

    case GetReply(value, mid) =>
      if (!delivered.contains(mid)) {
        delivered += mid
        appActor ! GetReply(value, mid)
      }

    case TestGet(key, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      myReplica ! GetRequest(key, mid)

    case TestPut(key, value, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      receivePut(key, value, mid)

    case TestAddReplica(node, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      myReplica ! AddReplicaRequest(node, mid)

    case TestRemoveReplica(node, mid) =>
      myReplica = replicas(pickRandomSmr()).smrActor

      myReplica ! RemoveReplicaRequest(node, mid)



    case update: UpdateReplicas =>
      replicas = update.replicas.toList

  }

  def receivePut(key: String, value: String, mid: String) = {
    val op = PutRequest(key, value, mid)
    myReplica ! op
    timers += (op.mid -> context.system.scheduler.scheduleOnce(
      Duration(OperationTimeout, TimeUnit.SECONDS),
      self, ResendOp(op)))
  }


  private def generateMID(): String = {
    ip + port + System.nanoTime()
  }

  private def pickRandomSmr(): Int = {

    val rnd = new scala.util.Random
    rnd.nextInt(replicas.size)
  }
}


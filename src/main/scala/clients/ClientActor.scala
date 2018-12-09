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

  var myReplica: ActorRef = _

  var timers = Map[String, Cancellable]()




  override def receive: Receive = {
    case r: Reply =>
      log.info(s"CLIENT RECEIVED: ${r.toString}")

    case i: InitClient =>
      replicas = i.replicas.toList
      var myReplicaId = i.smr
      if (i.smr == -1) {
        myReplicaId = pickRandomSmr()
      }
      myReplica = replicas(myReplicaId).smrActor

    case clients.Get(key) => // TODO adicionar timer para lidar com a possibilidade de o smr nao responder ao get
      myReplica ! statemachinereplication.Get(key, generateMID())


    case clients.Put(key, value) =>
      receivePut(key, value, generateMID())


    case clients.AddReplica(node) =>
      myReplica ! statemachinereplication.AddReplica(node, generateMID())

    case clients.RemoveReplica(node) =>
      myReplica ! statemachinereplication.RemoveReplica(node, generateMID())

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
        ReplyDelivery(event)
      }

    case TestGet(key, mid) =>
      myReplica ! statemachinereplication.Get(key, mid)

    case TestPut(key, value, mid) =>
      receivePut(key, value, mid)

    case TestAddReplica(node, mid) =>
      myReplica ! statemachinereplication.AddReplica(node, mid)

    case TestRemoveReplica(node, mid) =>
      myReplica ! statemachinereplication.RemoveReplica(node, mid)
  }

  def receivePut(key: String, value: String, mid: String) = {
    val op = statemachinereplication.Put(key, value, mid)
    myReplica ! op
    timers += (op.mid -> context.system.scheduler.scheduleOnce(
      Duration(OperationTimeout, TimeUnit.SECONDS),
      self, ResendOp(op)))
  }


  private def generateMID(): String = {
    ip + port + System.nanoTime()
  }

  private def pickRandomSmr(): Int = {
    val start = 0
    val end: Int = replicas.size
    val rnd = new scala.util.Random
    start + rnd.nextInt((end - start) + 1)
  }
}


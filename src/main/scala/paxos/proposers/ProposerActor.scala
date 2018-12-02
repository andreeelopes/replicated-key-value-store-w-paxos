package paxos.proposers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import paxos._
import statemachinereplication.updateReplicas
import utils.{Node, SequenceNumber, Utils}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}

class ProposerActor extends Actor with ActorLogging {
  //state
  val PrepareTimeout = 1 //secs
  val AcceptTimeout = 1  //secs

  var replicas = Set[Node]()
  var snFactory : SequenceNumber = _ // sequence number of the proposed value
  var sn : Int =_
  var value = "" // value to be proposed
  var prepares = 0 // number of received prepares_ok
  var highestSna = -1 // highest sna seen so far on received prepare ok messages
  var highestVa = "-1" // value corresponding to the highestSna received
  var accepts = 0 // number of received accepts_ok
  var prepareTimer: Cancellable = _

  var myNode: Node = _
  override def receive: Receive = {
    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
      snFactory = new SequenceNumber(myNode.getNodeID)

    case Propose(v) =>
      log.info(s"Propose($v)")
      receivePropose(v)

    case PrepareOk(sna, va) =>
      log.info(s"Receive(PREPARE_OK, $sna, $va)")
      receivePrepareOk(sna, va)

    case PrepareNotOk =>
      log.info(s"Receive PrepareNotOk")
      receivePropose(value) //TODO voltar a por, foi so para debug que se desactivou -nelson

    case AcceptNotOk =>
      log.info(s"Receive AcceptNotOk")
      context.system.scheduler.scheduleOnce(Duration(PrepareTimeout, TimeUnit.SECONDS),self, Propose(value))
      //receivePropose(value) //TODO voltar a por, foi so para debug que se desactivou -nelson

    case updateReplicas(_replicas_) =>
      replicas = _replicas_

  }


  def receivePropose(v: String): Unit = {
    value = v
    sn = snFactory.getSN()
    replicas.foreach {
      r =>
        r.acceptorActor ! Prepare(sn)
        log.info(s"Send(PREPARE,$sn, to: $r)")
    }
    prepareTimer = context.system.scheduler.scheduleOnce(Duration(PrepareTimeout, TimeUnit.SECONDS),self, Propose(v))
  }

  def receivePrepareOk(sna: Int, va: String): Unit = {
    prepares += 1
    if (sna > highestSna && va != "-1") {
      highestSna = sna
      highestVa = va
    }

    if (Utils.majority(prepares, replicas)) {
      prepareTimer.cancel()

      var valueToAccept = value
      if (lockedInValue())
        valueToAccept = highestVa

      log.info(s"Send(ACCEPT, $sn, $valueToAccept) to: all")
      replicas.foreach(r => r.acceptorActor ! Accept(sn, valueToAccept))
    }
  }


  /**
    * Returns true if consensus was already reached
    *
    * @return
    */
  private def lockedInValue(): Boolean = {
    highestSna != -1
  }
}

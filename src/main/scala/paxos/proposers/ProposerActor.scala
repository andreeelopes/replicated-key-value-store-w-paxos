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
  val AcceptTimeout = 1 //secs

  var replicas = Set[Node]()
  var snFactory: SequenceNumber = _ // sequence number of the proposed value
  var sn: Int = _
  var value = "" // value to be proposed
  var prepares = 0 // number of received prepares_ok
  var highestSna = -1 // highest sna seen so far on received prepare ok messages
  var lockedValue = "-1" // value corresponding to the highestSna received
  var accepts = 0 // number of received accepts_ok

  var prepareTimer: Cancellable = _
  var acceptTimer: Cancellable = _

  var myNode: Node = _

  var prepareMajority: Boolean = false

  override def receive: Receive = {
    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
      snFactory = new SequenceNumber(myNode.getNodeID)

    case Propose(v) =>
      receivePropose(v)
      log.info(s"[${System.nanoTime()}]  Propose($v) | " +
        s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case PrepareOk(sna, va) =>
      receivePrepareOk(sna, va)
      log.info(s"[${System.nanoTime()}]  Receive(PREPARE_OK, $sna, $va) | " +
        s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case AcceptOk(sna) =>
      receiveAcceptOk(sna)
      log.info(s"[${System.nanoTime()}]  Receive(ACCEPT_OK, $sna) | " +
        s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case PrepareTimer =>
      receivePropose(value)
      log.info(s"[${System.nanoTime()}]  Prepare timer fired | " +
        s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case AcceptTimer =>
      receivePropose(value)
      log.info(s"[${System.nanoTime()}]  Accept timer fired | " +
        s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case updateReplicas(_replicas_) =>
      replicas = _replicas_
      log.info(s"[${System.nanoTime()}]  Receive(UPDATE_REPLICAS, $_replicas_) | " +
        s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")
  }


  def receivePropose(v: String): Unit = {
    resetState()
    value = v
    sn = snFactory.getSN()
    replicas.foreach(r => r.acceptorActor ! Prepare(sn))
    prepareTimer = context.system.scheduler.scheduleOnce(Duration(PrepareTimeout, TimeUnit.SECONDS), self, PrepareTimer)
    log.info(s"[${System.nanoTime()}]  Send(PREPARE,$sn) to: all acceptors")
  }

  def receivePrepareOk(sna: Int, va: String): Unit = {
    prepares += 1
    if (sna > highestSna && va != "-1") {
      highestSna = sna
      lockedValue = va
    }

    if (Utils.majority(prepares, replicas) && !prepareMajority) {
      prepareTimer.cancel()
      prepareMajority = true

      if (lockedInValue()) {
        value = lockedValue
      }

      replicas.foreach(r => r.acceptorActor ! Accept(sn, value))
      acceptTimer = context.system.scheduler.scheduleOnce(Duration(AcceptTimeout, TimeUnit.SECONDS), self, AcceptTimer)
      log.info(s"[${System.nanoTime()}]  Send(ACCEPT, $sn, $value) to: all")
    }
  }

  def receiveAcceptOk(sna: Int): Unit = {
    accepts += 1
    if (Utils.majority(accepts, replicas)) {
      acceptTimer.cancel()
      replicas.foreach(r => r.learnerActor ! LockedValue(value))
      log.info(s"[${System.nanoTime()}]  Send(LOCKED_VALUE, $value) to: all")
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

  /**
    * Resets the variables (majority, prepares and accepts) associate with Paxos
    */
  private def resetState(): Unit = {
    prepareMajority = false
    prepares = 0
    accepts = 0
  }
}

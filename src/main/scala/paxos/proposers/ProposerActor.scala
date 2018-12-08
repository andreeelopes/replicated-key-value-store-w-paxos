package paxos.proposers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import paxos._
import paxos.acceptors.AcceptorInstances
import statemachinereplication.updateReplicas
import utils.{Node, SequenceNumber, Utils}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * @param sn          current sn
  * @param value       value to be proposed
  * @param prepares    number of received prepares_ok
  * @param accepts     number of received accepts_ok
  * @param highestSna  highest sna seen so far on received prepare ok messages
  * @param lockedValue value corresponding to the highestSna received
  */
case class ProposerInstances(sn: Int, value: String = "", prepares: Int = 0,
                             accepts: Int = 0, highestSna: Int = -1,
                             lockedValue: Int = -1, majority: Boolean = false,
                             prepareTimer: Cancellable = _, acceptTimer: Cancellable = _) {

  override def toString = s"{sn=$sn, value=$value, prepares=$prepares, " +
    s"accepts=$accepts, highestSna=$highestSna, lockedValue=$lockedValue}"
}

class ProposerActor extends Actor with ActorLogging {

  //state

  var proposerInstances = Map[Long, ProposerInstances]()

  val PrepareTimeout = 1 //secs
  val AcceptTimeout = 1 //secs

  var replicas = Set[Node]()
  var snFactory: SequenceNumber = _ // sequence number of the proposed value

  var myNode: Node = _

  override def receive: Receive = {
    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
      snFactory = new SequenceNumber(myNode.getNodeID)

    case Propose(v, i) =>
      log.info(s"[${System.nanoTime()}]  Propose($v, $i)")
      receivePropose(v, i)

    case PrepareOk(sna, va, i) =>
      log.info(s"[${System.nanoTime()}]  Receive(PREPARE_OK, $sna, $va, $i) | State($i) = ${proposerInstances(i)}")

      receivePrepareOk(sna, va, i)

    case AcceptOk(sna, i) =>
      log.info(s"[${System.nanoTime()}]  Receive(ACCEPT_OK, $sna, $i)")
      receiveAcceptOk(sna, i)

    case PrepareTimer(i) =>
      log.info(s"[${System.nanoTime()}]  Prepare timer fired, i=$i")
      receivePropose(proposerInstances(i).value, i)

    case AcceptTimer(i) =>
      log.info(s"[${System.nanoTime()}]  Accept timer fired, i=$i")
      receivePropose(proposerInstances(i).value, i)

    case updateReplicas(_replicas_) =>
      replicas = _replicas_

  }


  def receivePropose(v: String, i: Int): Unit = {
    val iPropose = proposerInstances(i)

    proposerInstances += (i -> iPropose.copy(value = v))
//    proposerInstances += (i ->
//      iPropose.copy(value = v,  sn = snFactory.getSN(), va = v, majority = false, prepares = 0, accepts = 0)


    log.info(s"[${System.nanoTime()}]  Send(PREPARE,$sn) to: all acceptors")
    replicas.foreach(r => r.acceptorActor ! Prepare(sn))
    prepareTimer = context.system.scheduler.scheduleOnce(Duration(PrepareTimeout, TimeUnit.SECONDS), self, PrepareTimer)
  }

  def receivePrepareOk(sna: Int, va: String, i: Int): Unit = {
    prepares += 1
    if (sna > highestSna && va != "-1") {
      highestSna = sna
      lockedValue = va
    }

    if (Utils.majority(prepares, replicas) && !majority) {
      majority = true
      prepareTimer.cancel()

      if (lockedInValue()) {
        value = lockedValue
      }

      log.info(s"[${System.nanoTime()}]  Send(ACCEPT, $sn, $value) to: all")
      replicas.foreach(r => r.acceptorActor ! Accept(sn, value))
      acceptTimer = context.system.scheduler.scheduleOnce(Duration(AcceptTimeout, TimeUnit.SECONDS), self, AcceptTimer)
    }
  }

  def receiveAcceptOk(sna: Int, i: Int): Unit = {
    accepts += 1
    if (Utils.majority(accepts, replicas)) {
      acceptTimer.cancel()
      replicas.foreach(r => r.learnerActor ! LockedValue(value))
    }
  }


  /**
    * Returns true if consensus was already reached
    *
    * @return
    */
  private def lockedInValue(i: Int): Boolean = {
    highestSna != -1
  }

  /**
    * Resets the variables (majority, prepares and accepts) associate with Paxos
    */
  private def resetState(i: Int): Unit = {
    majority = false
    prepares = 0
    accepts = 0
  }
}

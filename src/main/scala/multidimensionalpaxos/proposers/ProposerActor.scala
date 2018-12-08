package multidimensionalpaxos.proposers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, Cancellable}
import multidimensionalpaxos._
import statemachinereplication.{Event, updateReplicas}
import utils.{Node, SequenceNumber, Utils}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * @param sn          current sn
  * @param value       value to be proposed
  * @param prepares    number of received prepares_ok
  * @param accepts     number of received accepts_ok
  * @param highestSna  highest sna seen so far on received prepare ok messages
  * @param lockedValue value corresponding to the highestSna received
  */
case class ProposerInstance(var sn: Int = -1, var value: Event = null, var prepares: Int = 0,
                            var accepts: Int = 0, var highestSna: Int = -1,
                            var lockedValue: Event = null, var prevMajority: Boolean = false,
                            var prepareTimer: Cancellable = null, var acceptTimer: Cancellable = null, var i: Long) {

  override def toString = s"{sn=$sn, value=$value, prepares=$prepares, " +
    s"accepts=$accepts, highestSna=$highestSna, lockedValue=$lockedValue}"
}

class ProposerActor extends Actor with ActorLogging {

  //state

  var proposerInstances = Map[Long, ProposerInstance]()

  val PrepareTimeout = 1 //secs
  val AcceptTimeout = 1 //secs

  var replicas = Set[Node]()
  var snFactory: SequenceNumber = _ // sequence number of the proposed value

  var myNode: Node = _

  override def receive = {
    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
      snFactory = new SequenceNumber(myNode.getNodeID)

    case Propose(v, i) =>
      log.info(s"[${System.nanoTime()}]  Propose($v, $i)")
      proposerInstances += (i -> receivePropose(proposerInstances.getOrElse(i, ProposerInstance(i = i)), v))

    case PrepareOk(sna, va, i) =>
      log.info(s"[${System.nanoTime()}]  Receive(PREPARE_OK, $sna, $va, $i) | State($i) = ${proposerInstances(i)}")
      proposerInstances += (i -> receivePrepareOk(proposerInstances.getOrElse(i, ProposerInstance(i = i)), sna, va))

    case AcceptOk(sna, i) =>
      log.info(s"[${System.nanoTime()}]  Receive(ACCEPT_OK, $sna, $i)")
      proposerInstances += (i -> receiveAcceptOk(proposerInstances.getOrElse(i, ProposerInstance(i = i)), sna))

    case PrepareTimer(i) =>
      log.info(s"[${System.nanoTime()}]  Prepare timer fired, i=$i")
      val iProposer = proposerInstances.getOrElse(i, ProposerInstance(i = i))
      proposerInstances += (i -> receivePropose(iProposer, iProposer.value))

    case AcceptTimer(i) =>
      log.info(s"[${System.nanoTime()}]  Accept timer fired, i=$i")
      val iProposer = proposerInstances.getOrElse(i, ProposerInstance(i = i))
      proposerInstances += (i -> receivePropose(iProposer, iProposer.value))

    case updateReplicas(_replicas_) =>
      replicas = _replicas_

  }


  def receivePropose(iProposer: ProposerInstance, v: Event) = {

    resetState(iProposer)
    iProposer.value = v
    iProposer.sn = snFactory.getSN()
    iProposer.prepareTimer = context.system.scheduler.scheduleOnce(Duration(PrepareTimeout, TimeUnit.SECONDS), self, PrepareTimer)

    log.info(s"[${System.nanoTime()}]  Send(PREPARE,${iProposer.sn}, ${iProposer.i}) to: all acceptors")
    replicas.foreach(r => r.acceptorActor ! Prepare(iProposer.sn, iProposer.i))

    iProposer
  }

  def receivePrepareOk(iProposer: ProposerInstance, sna: Int, va: Event) = {
    iProposer.prepares += 1
    if (sna > iProposer.highestSna && va != null) {
      iProposer.highestSna = sna
      iProposer.lockedValue = va
    }

    if (Utils.majority(iProposer.prepares, replicas) && !iProposer.prevMajority) {
      iProposer.prevMajority = true
      iProposer.prepareTimer.cancel()

      if (lockedInValue(iProposer.highestSna, iProposer.i)) {
        iProposer.value = iProposer.lockedValue
      }

      log.info(s"[${System.nanoTime()}]  Send(ACCEPT, ${iProposer.sn}, ${iProposer.value}) to: all")
      replicas.foreach(r => r.acceptorActor ! Accept(iProposer.sn, iProposer.value, iProposer.i))
      iProposer.acceptTimer = context.system.scheduler.scheduleOnce(Duration(AcceptTimeout, TimeUnit.SECONDS), self, AcceptTimer)
    }

    iProposer
  }

  def receiveAcceptOk(iProposer: ProposerInstance, sna: Int) = {
    iProposer.accepts += 1

    if (Utils.majority(iProposer.accepts, replicas)) {
      iProposer.acceptTimer.cancel()
      replicas.foreach(r => r.learnerActor ! LockedValue(iProposer.value, iProposer.i))
    }

    iProposer
  }


  /**
    * Returns true if consensus was already reached
    *
    * @return
    */
  private def lockedInValue(sna: Int, i: Long): Boolean = sna != -1


  /**
    * Resets the variables (majority, prepares and accepts) associate with Paxos
    */
  private def resetState(iProposer: ProposerInstance) = {
    iProposer.prevMajority = false
    iProposer.prepares = 0
    iProposer.accepts = 0

    iProposer
  }
}

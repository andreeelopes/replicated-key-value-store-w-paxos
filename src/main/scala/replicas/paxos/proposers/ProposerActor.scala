package replicas.paxos.proposers

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, Cancellable}
import replicas.paxos._
import replicas.statemachinereplication.UpdateReplicas
import utils.{ReplicaNode, SequenceNumber, Utils}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration}

class ProposerActor extends Actor with ActorLogging {
  //state
  val PrepareTimeout = 1 //secs
  val AcceptTimeout = 1 //secs

  var replicas = Set[ReplicaNode]()
  var snFactory: SequenceNumber = _ // sequence number of the proposed value
  var sn: Int = _
  var value = "" // value to be proposed
  var prepares = 0 // number of received prepares_ok
  var highestSna = -1 // highest sna seen so far on received prepare ok messages
  var lockedValue = "-1" // value corresponding to the highestSna received
  var accepts = 0 // number of received accepts_ok

  var prepareTimer: Cancellable = _
  var acceptTimer: Cancellable = _

  var myNode: ReplicaNode = _

  var prevMajority: Boolean = false

  override def receive: Receive = {
    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
      snFactory = new SequenceNumber(myNode.getNodeID)

    case Propose(v) =>
      receivePropose(v)
//     //println(s"  Propose($v) | " +
       // s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case PrepareOk(sna, va) =>
      receivePrepareOk(sna, va)
//     //println(s"  Receive(PREPARE_OK, $sna, $va) | " +
     //   s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case AcceptOk(sna) =>
      receiveAcceptOk(sna)
//     //println(s"  Receive(ACCEPT_OK, $sna) | " +
      //  s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case PrepareTimer =>
      receivePropose(value)
//     //println(s"  Prepare timer fired | " +
      //  s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case AcceptTimer =>
      receivePropose(value)
//     //println(s"  Accept timer fired | " +
      //  s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")

    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_
//     //println(s"  Receive(UPDATE_REPLICAS, $replicas) | " +
       // s"state={sn: $sn, value: $value, highestSna: $highestSna, lockedValue: $lockedValue, accepts: $accepts, prepares: $prepares}")
  }


  def receivePropose(v: String): Unit = {
    resetState()
    value = v
    sn = snFactory.getSN()
    replicas.foreach(r => r.acceptorActor ! Prepare(sn))
    prepareTimer = context.system.scheduler.scheduleOnce(Duration(PrepareTimeout, TimeUnit.SECONDS), self, PrepareTimer)
   //println(s"  Send(PREPARE,$sn) to: all acceptors")
  }

  def receivePrepareOk(sna: Int, va: String): Unit = {
    prepares += 1
    if (sna > highestSna && va != "-1") {
      highestSna = sna
      lockedValue = va
    }

    if (Utils.majority(prepares, replicas) && !prevMajority) {
      prepareTimer.cancel()
      prevMajority = true

      if (lockedInValue()) {
        value = lockedValue
      }

      replicas.foreach(r => r.acceptorActor ! Accept(sn, value))
      acceptTimer = context.system.scheduler.scheduleOnce(Duration(AcceptTimeout, TimeUnit.SECONDS), self, AcceptTimer)
     //println(s"  Send(ACCEPT, $sn, $value) to: all")
    }
  }

  def receiveAcceptOk(sna: Int): Unit = {
    accepts += 1
    if (Utils.majority(accepts, replicas)) {
      acceptTimer.cancel()
      replicas.foreach(r => r.learnerActor ! LockedValue(value))
     //println(s"  Send(LOCKED_VALUE, $value) to: all")
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
    prevMajority = false
    prepares = 0
    accepts = 0
  }
}

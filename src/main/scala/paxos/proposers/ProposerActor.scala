package paxos.proposers

import akka.actor.{Actor, ActorLogging, ActorRef}
import paxos._
import statemachinereplication.updateReplicas

class ProposerActor extends Actor with ActorLogging{
  //state
  var replicas = Set[ActorRef]()
  var sn = 0          // sequence number of the proposed value
  var value = ""      // value to be proposed
  var prepares = 0    // number of received prepares_ok
  var accepts = 0     // number of received accepts_ok



  override def receive: Receive = {
    case Init(_replicas_ : Set[ActorRef]) =>
      replicas = _replicas_
    case Propose(v) =>
      receivePropose(v)
    case PrepareOk(sna, va) =>
      receivePrepareOk(sna, va)
    case PrepareNotOk || AcceptNotOk =>
      self ! Propose(value)

    case updateReplicas(_replicas_ : Set[ActorRef]) =>
      replicas = _replicas_
  }

  def receivePropose(v: String): Unit = {
    acceptors.foreach(a => a ! Prepare(sn))
    sn += 1
  }

  def receivePrepareOk(sna: Int, va: String): Unit = {
    prepares += 1
    value = va
    if(prepares > membership.size()/2){

    }
  }


}

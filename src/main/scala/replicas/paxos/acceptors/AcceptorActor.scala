package replicas.paxos.acceptors

import akka.actor.{Actor, ActorLogging}
import replicas.paxos._
import replicas.statemachinereplication.UpdateReplicas
import utils.ReplicaNode

class AcceptorActor extends Actor with ActorLogging {

  var np = -1
  var na = -1
  var va = "-1"

  var replicas = Set[ReplicaNode]()
  var myNode: ReplicaNode = _

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_

    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_

    case Prepare(n) =>
      log.info(s"[${System.nanoTime()}]  Receive(PREPARE, $n) from: $sender")

      if (n > np) {
        np = n
        sender ! PrepareOk(na, va)
        log.info(s"[${System.nanoTime()}]  Send(PREPARE_OK, $na, $va) to: $sender")
      }


    case Accept(n, v) =>
      log.info(s"[${System.nanoTime()}]  Receive(Accept, $n, $v) | State = {na: $na, va: $va, np: $np}")

      if (n >= np) {
        na = n
        va = v
        sender ! AcceptOk(na)
        log.info(s"[${System.nanoTime()}]  Send(ACCEPT_OK , $na) to: $sender")
      }
  }
}

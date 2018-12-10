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
     //println(s"  Receive(PREPARE, $n) from: $sender")

      if (n > np) {
        np = n
        sender ! PrepareOk(na, va)
       //println(s"  Send(PREPARE_OK, $na, $va) to: $sender")
      }


    case Accept(n, v) =>
     //println(s"  Receive(Accept, $n, $v) | State = {na: $na, va: $va, np: $np}")

      if (n >= np) {
        na = n
        va = v
        sender ! AcceptOk(na)
       //println(s"  Send(ACCEPT_OK , $na) to: $sender")
      }
  }
}

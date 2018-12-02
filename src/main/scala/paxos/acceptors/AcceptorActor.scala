package paxos.acceptors

import akka.actor.{Actor, ActorLogging, ActorRef}
import paxos._
import statemachinereplication.updateReplicas
import utils.Node

class AcceptorActor extends Actor with ActorLogging {

  var np = -1
  var na = -1
  var va = "-1"

  var replicas = Set[Node]()
  var myNode: Node = _

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_

    case updateReplicas(_replicas_) =>
      replicas = _replicas_

    case Prepare(n) =>
      log.info(s"Receive(PREPARE, $n) from: $sender")

      if (n > np) {
        np = n
        log.info(s"Send(PREPARE_OK, $na, $va) to: $sender")
        sender ! PrepareOk(na, va)
      }
      /*else { //TODO replaced with a timer - nelson
        log.info(s"Send(PREPARE_NOT_OK) to: $sender")
        sender ! PrepareNotOk
      }*/

    case Accept(n, v) =>
      log.info(s"Receive(Accept, $n, $v) | State = {na: $na, va: $va, np: $np}")

      if (n >= np) {
        na = n
        va = v
        replicas.foreach {
          r =>
            r.learnerActor ! AcceptOk(na, va)
            log.info(s"Send(ACCEPT_OK , $na, $va) to: $r")
        }
      } else {
        sender ! AcceptNotOk
        log.info(s"Send(ACCEPT_NOT_OK) to: $sender")

      }

  }

}

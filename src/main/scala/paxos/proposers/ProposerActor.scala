package paxos.proposers

import akka.actor.{Actor, ActorLogging}
import paxos._
import statemachinereplication.updateReplicas
import utils.{Node, Utils}

class ProposerActor extends Actor with ActorLogging {
  //state
  var replicas = Set[Node]()
  var sn = 0 // sequence number of the proposed value
  var value = "" // value to be proposed
  var prepares = 0 // number of received prepares_ok
  var accepts = 0 // number of received accepts_ok


  override def receive: Receive = {
    case Init(_replicas_, _) =>
      replicas = _replicas_
    case Propose(v) =>
      log.info(s"Propose($v)")
      receivePropose(v)
    case PrepareOk(sna, va) =>
      log.info(s"Receive(PREPARE_OK, $sna, $va)")
      receivePrepareOk(sna, va)
    case PrepareNotOk | AcceptNotOk =>
      log.info(s"Receive Not Ok")
      //self ! Propose(value) //TODO voltar a por, foi so para debug que se desactivou -nelson

    case updateReplicas(_replicas_) =>
      replicas = _replicas_
  }

  def receivePropose(v: String): Unit = {
    value = v
    replicas.foreach {
      r =>
        r.acceptorActor ! Prepare(sn)
        log.info(s"Send(PREPARE,$sn, to: $r)")

    }
    sn += 1
  }

  def receivePrepareOk(sna: Int, va: String): Unit = {
    prepares += 1
    if(sna > sn){ //TODO verificar este if, foi adicionado  - nelson
      sn = sna
    }
    if (Utils.majority(prepares, replicas)) {
      log.info(s"Send(ACCEPT, $sn, $value) to: all")
      replicas.foreach(r => r.acceptorActor ! Accept(sn, value))
    }
  }
}

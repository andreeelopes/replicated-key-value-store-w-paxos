package test

import akka.actor.{Actor, ActorLogging}
import statemachinereplication.StateDelivery
import utils.Node

class testActor extends Actor with ActorLogging {

  case class Validate(replicas: Set[Node])

  var replicas: Set[Node] = _
  var states = List[StateDelivery]()

  override def receive = {//TODO put in admin

    case Validate(_replicas_) =>
      replicas = _replicas_
      replicas.foreach(r => r.smrActor ! statemachinereplication.State)

    case state: StateDelivery =>
      states ::= state

      if(states.size == replicas.size){

      }

  }


}

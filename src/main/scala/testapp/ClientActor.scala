package testapp

import akka.actor.{Actor, ActorLogging, ActorRef}
import statemachinereplication.{Get, Operation, Put, Reply}

case class TriggerGet(smr: ActorRef, key: String)
case class TriggerPut(smr: ActorRef, key: String, value: String)

class ClientActor extends Actor with ActorLogging{
  override def receive: Receive = {
    case r: Reply =>

    case TriggerGet(smr, key) =>
      smr ! Get(key, key)

    case TriggerPut(smr, key, value) =>
      smr ! Put(key, value, key)
  }
}

package statemachinereplication

import akka.actor.{Actor, ActorLogging}
import multidimensionalpaxos.{DecisionDelivery, Propose}
import utils.Node

import scala.collection.immutable.Queue

class StateMachineReplicationActor extends Actor with ActorLogging {
  //State
  val NotDefined = "NA"

  var history = Map[Long, Event]() // array that contains the history of operations [i, (op, returnValue, mid)]
  var current = 0 // current op
  var store = Map[String, String]() // key value store
  var toBeProposed = Queue[Event]() // queue with ops to be proposed (op, sender, mid)
  var proposed = Set[String]()

  var replicas = Set[Node]()

  var myNode: Node = _


  override def receive: Receive = {
    case i: Init =>
      replicas = i.replicas
      myNode = i.myNode
    case Get(key, mid) =>
      receiveGet(key, mid)

    case op: Operation => //TODO testar esta notação e experimetnar
      receiveUpdateOp(op)

    case DecisionDelivery(decision: Event, instance) =>
      receiveDecision(decision, instance)
  }


  def receiveGet(key: String, mid: String): Unit = {
    sender ! GetReply(store.getOrElse(key, NotDefined), mid)
  }

  def receiveUpdateOp(op: Operation): Unit = {
    if (!proposed.contains(op.mid))
    {
      proposed += op.mid
      toBeProposed.enqueue(Event(op, op.mid, sender))

      if(current == 0)
        myNode.proposerActor ! Propose(toBeProposed.head, current)
    }
    else
    {
      val eventOpt = history.values.find(e =>  e.mid.equals(op.mid))
      if(eventOpt.isDefined){

        sender ! Reply(eventOpt.get)
      }
    }
  }

  def receiveDecision(op: Event, i: Long): Unit = {
    current += 1
  }


  //Procedures

  private def executeOp(op: Event, index: Long) = {

  }

  private def executeHistory(history: Map[Long, Event]) = {

  }

  private def previousCompleted(index: Long): Boolean = {

    true
  }

  private def findValidIndex(): Long = {

    0
  }

}
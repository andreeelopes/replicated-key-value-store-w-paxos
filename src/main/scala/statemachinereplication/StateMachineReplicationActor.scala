package statemachinereplication

import akka.actor.{Actor, ActorLogging}
import multidimensionalpaxos.{DecisionDelivery, Propose}
import utils.Node

import scala.collection.immutable.Queue

class StateMachineReplicationActor extends Actor with ActorLogging {
  //State
  val NotDefined = "NA"

  var history = Map[Long, Event]() // array that contains the history of operations [i, (op, returnValue, mid)]
  var current: Long = 0 // current op
  var store = Map[String, String]() // key value store
  var toBeProposed = Queue[Event]() // queue with ops to be proposed (op, sender, mid)
  var proposed = Set[String]()
  var historyProcessed = false

  var replicas = Set[Node]()

  var myNode: Node = _


  override def receive: Receive = {
    case i: statemachinereplication.Init =>
      log.info(s"Init=$i")
      replicas = i.replicas
      myNode = i.myNode
    case get@Get(key, mid) =>
      log.info(get.toString)
      receiveGet(key, mid)

    case op: Operation => //TODO testar esta notação e experimetnar
      log.info(op.toString)
      receiveUpdateOp(op)

    case dd@DecisionDelivery(decision: Event, instance) =>
      log.info(dd.toString)
      receiveDecision(decision, instance)

    case h: History =>
      log.info(h.toString)
      executeHistory(h.history, h.index)
  }


  def receiveGet(key: String, mid: String): Unit = {
    log.info(s"GET($key)=${store.getOrElse(key, NotDefined)}, sender=$sender")

    myNode.testAppActor ! GetReply(store.getOrElse(key, NotDefined), mid)
  }

  def receiveUpdateOp(op: Operation): Unit = {
    if (!proposed.contains(op.mid)) {
      proposed += op.mid
      toBeProposed = toBeProposed.enqueue(Event(op, op.mid, sender))

      if (current == 0) {
        log.info(s"myNode.proposer=${myNode.proposerActor}")
        myNode.proposerActor ! Propose(toBeProposed.head, current)
      }
    }
    else {
      val eventOpt = history.values.find(e => e.mid.equals(op.mid) && e.executed)
      if (eventOpt.isDefined) {

        sender ! Reply(eventOpt.get)

      }
    }
  }

  def receiveDecision(op: Event, i: Long): Unit = { //TODO podera decidir duas vezes?
    history += (i -> op)

    if (toBeProposed.nonEmpty && toBeProposed.head.equals(op))
      toBeProposed = toBeProposed.dequeue._2

    if (toBeProposed.nonEmpty) {
      current = findValidIndex()
      myNode.proposerActor ! Propose(toBeProposed.head, current)
      log.info(Propose(toBeProposed.head, current).toString)
    }

    if (previousCompleted(i))
      executeOp(op, i)
  }


  //Procedures

  private def executeOp(event: Event, index: Long) = {
    var oldValue: String = null
    event.op match {
      case Put(key, value, _) =>
        oldValue = store.getOrElse(key, NotDefined)
        store += (key -> value)
        history += (index -> history(index).copy(executed = true, returnValue = oldValue))

      case AddReplica(replica, _) =>
        oldValue = index.toString
        replicas += replica
        updatePaxosReplicas()
        replica.smrActor ! History(history, index)
        log.info(s"${History(history, index).toString} to: $replica")


      case RemoveReplica(replica, _) =>
        oldValue = index.toString //TODO
        replicas -= replica
        updatePaxosReplicas()

    }

    history += (index -> history(index).copy(executed = true, returnValue = oldValue))
    event.sender ! Reply(event)
    log.info(s"${Reply(event).toString} to: ${event.sender}")
  }

  private def updatePaxosReplicas(): Unit = {
    myNode.acceptorActor ! UpdateReplicas(replicas)
    myNode.learnerActor ! UpdateReplicas(replicas)
    myNode.proposerActor ! UpdateReplicas(replicas)
  }

  private def executeHistory(_history_ : Map[Long, Event], index: Long): Unit = {

    if (!historyProcessed) {
      historyProcessed = true
      _history_.foreach { p =>
        val event = p._2
        var oldValue: String = null
        event.op match {
          case Put(key, value, _) =>
            oldValue = store.getOrElse(key, NotDefined)
            store += (key -> value)
            history += (index -> history(index).copy(executed = true, returnValue = oldValue))

          case AddReplica(replica, _) =>
            oldValue = index.toString
            replicas += replica
            updatePaxosReplicas()

          case RemoveReplica(replica, _) =>
            oldValue = index.toString
            replicas -= replica
            updatePaxosReplicas()
        }
        history += (index -> history(index).copy(executed = true, returnValue = oldValue))
      }
    }
  }

  /**
    *
    * @param index
    * @return
    */
  private def previousCompleted(index: Long): Boolean = {
    for (i <- 0 to index.toInt) {
      if (history.get(i).isEmpty)
        return false
      if (!history(i).executed)
        executeOp(history(i), i)
    }
    true
  }

  private def findValidIndex(): Long = {
    val maxKey = history.keys.max
    for (i <- 0 to maxKey.toInt) {
      //TODO
      if (history.get(i).isEmpty)
        return i
    }
    maxKey + 1
  }

}
package replicas.statemachinereplication

import akka.actor.{Actor, ActorLogging}
import replicas.multidimensionalpaxos.{DecisionDelivery, Propose}
import clients.test.{State, StateDelivery}
import rendezvous.IdentifySmr
import utils.ReplicaNode

import scala.collection.immutable.Queue

class StateMachineReplicationActor(rendezvousIP: String, rendezvousPort: Int) extends Actor with ActorLogging {
  //State
  val NotDefined = "NA"

  var history = Map[Long, Event]() // array that contains the history of operations [i, (op, returnValue, mid)]
  var current: Long = 0 // current op
  var store = Map[String, String]() // key value store
  var toBeProposed = Queue[Event]() // queue with ops to be proposed (op, sender, mid)
  var proposed = Set[String]()
  var historyProcessed = false

  var myReplicas = Set[ReplicaNode]()

  var myNode: ReplicaNode = _

  val rendezvous = context.actorSelection {
    s"akka.tcp://RemoteService@$rendezvousIP:$rendezvousPort/user/rendezvous"
  }


  override def receive: Receive = {
    case i: replicas.statemachinereplication.InitSmr =>
      //println(s"Init=$i")
      myNode = i.myNode
      rendezvous ! IdentifySmr(myNode)

    //Testing
    case State() =>
      sender ! StateDelivery(history, store, toBeProposed, myReplicas)

    case get@GetRequest(key, mid) =>
      //println(get.toString)
      receiveGet(key, mid)

    case op: Operation =>
      ////println(op.toString)
      receiveUpdateOp(op)

    case dd@DecisionDelivery(decision: Event, instance) =>
      //println(dd.toString)
      receiveDecision(decision, instance)

    case h: History =>
      //println(h.toString)
      executeHistory(h.history, h.index)

    case a :UpdateReplicas =>
      myReplicas = a.replicas
      //println(s"replicas: $myReplicas")
      updatePaxosReplicas()
  }


  def receiveGet(key: String, mid: String): Unit = {
    //println(s"GET($key)=${store.getOrElse(key, NotDefined)}, sender=${sender.path.name}")

    sender ! GetReply(store.getOrElse(key, NotDefined), mid)
  }

  def receiveUpdateOp(op: Operation): Unit = {
    if (!proposed.contains(op.mid)) {
      proposed += op.mid
      toBeProposed = toBeProposed.enqueue(Event(op, op.mid, sender, myNode))

      if (toBeProposed.size == 1) {
        current = findValidIndex()
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

  def receiveDecision(op: Event, i: Long): Unit = {
    //    if(!executed.contains(op.mid)) { TODO - evitar executar duas vezes
    history += (i -> op)

    if (toBeProposed.nonEmpty && toBeProposed.head.equals(op))
      toBeProposed = toBeProposed.dequeue._2


    if (toBeProposed.nonEmpty) {
      current = findValidIndex()
      myNode.proposerActor ! Propose(toBeProposed.head, current)
      //println(Propose(toBeProposed.head, current).toString)

    }

    if (previousCompleted(i)) {
      executeOp(op, i)
    }
    //    } TODO - evitar executar duas vezes
  }


  //Procedures

  private def executeOp(event: Event, index: Long) = {
    var oldValue: String = null
    event.op match {
      case PutRequest(key, value, _) =>
        oldValue = store.getOrElse(key, NotDefined)
        store += (key -> value)
        history += (index -> history(index).copy(executed = true, returnValue = oldValue))

      case AddReplicaRequest(replica, _) =>
        oldValue = index.toString
        myReplicas += replica
        updatePaxosReplicas()
        replica.smrActor ! History(history, index)
        //println(s"${History(history, index).toString} to: $replica")


      case RemoveReplicaRequest(replica, _) =>
        oldValue = index.toString
        myReplicas -= replica
        updatePaxosReplicas()

    }

    history += (index -> history(index).copy(executed = true, returnValue = oldValue))

    if (event.replica.equals(myNode) || !myReplicas.contains(event.replica)) {
      //println(s"Send(REPLY, $event) to: ${event.sender}")
      event.sender ! Reply(event)
    }

    //println(s"STORE: ${store.toString()}")
    //println(s"HISTORY: ${history.toString()}")

  }

  private def updatePaxosReplicas(): Unit = {
    myNode.acceptorActor ! UpdateReplicas(myReplicas)
    myNode.learnerActor ! UpdateReplicas(myReplicas)
    myNode.proposerActor ! UpdateReplicas(myReplicas)
  }

  private def executeHistory(_history_ : Map[Long, Event], index: Long): Unit = {

    if (!historyProcessed) {
      historyProcessed = true
      _history_.foreach { p =>
        val event = p._2
        var oldValue: String = null
        event.op match {
          case PutRequest(key, value, _) =>
            oldValue = store.getOrElse(key, NotDefined)
            store += (key -> value)
            history += (index -> history(index).copy(executed = true, returnValue = oldValue))

          case AddReplicaRequest(replica, _) =>
            oldValue = index.toString
            myReplicas += replica
            updatePaxosReplicas()

          case RemoveReplicaRequest(replica, _) =>
            oldValue = index.toString
            myReplicas -= replica
            updatePaxosReplicas()
        }
        history += (index -> history(index).copy(executed = true, returnValue = oldValue))
      }
    }
  }

  /**
    *
    * @param index event index in history
    * @return
    */
  private def previousCompleted(index: Long): Boolean = {
    for (i <- 0 until index.toInt) {
      if (history.get(i).isEmpty)
        return false
      if (!history(i).executed)
        executeOp(history(i), i)
    }
    true
  }

  private def findValidIndex(): Long = {
    if (history.isEmpty)
      return 0
    val maxKey = history.keys.max
    for (i <- 0 to maxKey.toInt) {

      if (history.get(i).isEmpty)
        return i
    }
    maxKey + 1
  }

}


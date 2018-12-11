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

  var history = Map[Long, List[Event]]() // array that contains the history of operations [i, (op, returnValue, mid)]
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

    case get@WeakGetRequest(key, mid) =>
      //println(get.toString)
      receiveGet(key, mid)

    case op: Operation =>
      //println(op.toString)
      receiveUpdateOp(op)

    case dd: DecisionDelivery =>
      //println(dd.toString)
      receiveDecision(dd.decision, dd.instance)

    case h: History =>
      //println(h.toString)
      executeHistory(h.history, h.index)

    case a: UpdateReplicas =>
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
        myNode.proposerActor ! Propose(toBeProposed.toList, current)
      }

    }
    else {
      val eventOpt = history.values.flatten.find(e => e.mid.equals(op.mid) && e.executed)
      if (eventOpt.isDefined) {
        sender ! Reply(eventOpt.get)
      }
    }
  }

  def receiveDecision(ops: List[Event], i: Long): Unit = {
    //    if(!executed.contains(op.mid)) { TODO - evitar executar duas vezes
    history += (i -> ops)

    if (toBeProposed.nonEmpty && toBeProposed.containsSlice(ops))
      toBeProposed = toBeProposed.filterNot(op => ops.contains(op))

    if (toBeProposed.nonEmpty) {
      current = findValidIndex()
      myNode.proposerActor ! Propose(toBeProposed.toList, current)
      //println(Propose(toBeProposed.head, current).toString)
    }

    if (previousCompleted(i))
      ops.foreach(op => executeOp(op, i))

    //    } TODO - evitar executar duas vezes
  }


  //Procedures

  private def executeOp(event: Event, index: Long) = {
    var oldValue: String = null
    event.op match {
      case PutRequest(key, value, _) =>
        oldValue = store.getOrElse(key, NotDefined)
        store += (key -> value)

      case StrongGetRequest(key, _) =>
        oldValue = store.getOrElse(key, NotDefined)

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

    history += (index -> history(index).map { e =>
      if (e.equals(event)) event.copy(executed = true, returnValue = oldValue) else e
    })

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

  private def executeHistory(_history_ : Map[Long, List[Event]], index: Long): Unit = {

    if (!historyProcessed) {
      historyProcessed = true
      _history_.foreach { p =>
        p._2.foreach { event =>

          var oldValue: String = null
          event.op match {
            case PutRequest(key, value, _) =>
              oldValue = store.getOrElse(key, NotDefined)
              store += (key -> value)

            case StrongGetRequest(key, _) =>
              oldValue = store.getOrElse(key, NotDefined)

            case AddReplicaRequest(replica, _) =>
              oldValue = index.toString
              myReplicas += replica
              updatePaxosReplicas()

            case RemoveReplicaRequest(replica, _) =>
              oldValue = index.toString
              myReplicas -= replica
              updatePaxosReplicas()
          }

          history += (index -> history(index).map { e =>
            if (e.equals(event)) event.copy(executed = true, returnValue = oldValue) else e
          })
        }
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
      //it is enough to check the head
      if (!history(i).head.executed)
        history(i).foreach(event => executeOp(event, i))
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


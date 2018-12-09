import akka.actor.{ActorSystem, Props}
import multidimensionalpaxos.Init
import multidimensionalpaxos.acceptors.AcceptorActor
import multidimensionalpaxos.learners.LearnerActor
import multidimensionalpaxos.proposers.ProposerActor
import statemachinereplication.{Get, Put, StateMachineReplicationActor}
import clients.{ClientActor, TriggerPut}
import utils.Node

object StateMachineReplicationMain extends App {

  override def main(args: Array[String]) = {

    val a = ActorSystem("nodeAsystem")
    val b = ActorSystem("nodeBSystem")
    val c = ActorSystem("nodeCSytstem")
    val d = ActorSystem("nodeDsystem")
    val e = ActorSystem("nodeESystem")
    val f = ActorSystem("nodeFSytstem")

    val aPaxosProposer = a.actorOf(Props[ProposerActor], "aPaxosProposer")
    val bPaxosProposer = b.actorOf(Props[ProposerActor], "bPaxosProposer")
    val cPaxosProposer = c.actorOf(Props[ProposerActor], "cPaxosProposer")
    val dPaxosProposer = d.actorOf(Props[ProposerActor], "dPaxosProposer")
    val ePaxosProposer = e.actorOf(Props[ProposerActor], "ePaxosProposer")
    val fPaxosProposer = f.actorOf(Props[ProposerActor], "fPaxosProposer")

    val aPaxosAcceptor = a.actorOf(Props[AcceptorActor], "aPaxosAcceptor")
    val bPaxosAcceptor = b.actorOf(Props[AcceptorActor], "bPaxosAcceptor")
    val cPaxosAcceptor = c.actorOf(Props[AcceptorActor], "cPaxosAcceptor")
    val dPaxosAcceptor = d.actorOf(Props[AcceptorActor], "dPaxosAcceptor")
    val ePaxosAcceptor = e.actorOf(Props[AcceptorActor], "ePaxosAcceptor")
    val fPaxosAcceptor = f.actorOf(Props[AcceptorActor], "fPaxosAcceptor")

    val aPaxosLearner = a.actorOf(Props[LearnerActor], "aPaxosLearner")
    val bPaxosLearner = b.actorOf(Props[LearnerActor], "bPaxosLearner")
    val cPaxosLearner = c.actorOf(Props[LearnerActor], "cPaxosLearner")
    val dPaxosLearner = d.actorOf(Props[LearnerActor], "dPaxosLearner")
    val ePaxosLearner = e.actorOf(Props[LearnerActor], "ePaxosLearner")
    val fPaxosLearner = f.actorOf(Props[LearnerActor], "fPaxosLearner")

    val aPaxosSmr = a.actorOf(Props[StateMachineReplicationActor], "aPaxosSmr")
    val bPaxosSmr = b.actorOf(Props[StateMachineReplicationActor], "bPaxosSmr")
    val cPaxosSmr = c.actorOf(Props[StateMachineReplicationActor], "cPaxosSmr")
    val dPaxosSmr = d.actorOf(Props[StateMachineReplicationActor], "dPaxosSmr")
    val ePaxosSmr = e.actorOf(Props[StateMachineReplicationActor], "ePaxosSmr")
    val fPaxosSmr = f.actorOf(Props[StateMachineReplicationActor], "fPaxosSmr")

    val aKeyValueClient = a.actorOf(Props[ClientActor], "aKeyValueClient")
    val bKeyValueClient = b.actorOf(Props[ClientActor], "bKeyValueClient")
    val cKeyValueClient = c.actorOf(Props[ClientActor], "cKeyValueClient")
    val dKeyValueClient = d.actorOf(Props[ClientActor], "dKeyValueClient")
    val eKeyValueClient = e.actorOf(Props[ClientActor], "eKeyValueClient")
    val fKeyValueClient = f.actorOf(Props[ClientActor], "fKeyValueClient")


    val aNode = Node("aNode", "1", aKeyValueClient, aPaxosSmr, aPaxosAcceptor, aPaxosLearner, aPaxosProposer)
    val bNode = Node("bNode", "2", bKeyValueClient, bPaxosSmr, bPaxosAcceptor, bPaxosLearner, bPaxosProposer)
    val cNode = Node("cNode", "3", cKeyValueClient, cPaxosSmr, cPaxosAcceptor, cPaxosLearner, cPaxosProposer)
    val dNode = Node("dNode", "4", dKeyValueClient, dPaxosSmr, dPaxosAcceptor, dPaxosLearner, dPaxosProposer)
    val eNode = Node("eNode", "5", eKeyValueClient, ePaxosSmr, ePaxosAcceptor, ePaxosLearner, ePaxosProposer)
    val fNode = Node("fNode", "6", fKeyValueClient, fPaxosSmr, fPaxosAcceptor, fPaxosLearner, fPaxosProposer)

    val membership = Set(aNode, bNode, cNode, dNode, eNode, fNode)
    //Init

    aPaxosSmr ! statemachinereplication.Init(membership, aNode)
    bPaxosSmr ! statemachinereplication.Init(membership, bNode)
    cPaxosSmr ! statemachinereplication.Init(membership, cNode)
    dPaxosSmr ! statemachinereplication.Init(membership, dNode)
    ePaxosSmr ! statemachinereplication.Init(membership, eNode)
    fPaxosSmr ! statemachinereplication.Init(membership, fNode)


    aPaxosProposer ! Init(membership, aNode)
    aPaxosAcceptor ! Init(membership, aNode)
    aPaxosLearner ! Init(membership, aNode)


    bPaxosProposer ! Init(membership, bNode)
    bPaxosAcceptor ! Init(membership, bNode)
    bPaxosLearner ! Init(membership, bNode)

    cPaxosProposer ! Init(membership, cNode)
    cPaxosAcceptor ! Init(membership, cNode)
    cPaxosLearner ! Init(membership, cNode)

    dPaxosProposer ! Init(membership, dNode)
    dPaxosAcceptor ! Init(membership, dNode)
    dPaxosLearner ! Init(membership, dNode)

    ePaxosProposer ! Init(membership, eNode)
    ePaxosAcceptor ! Init(membership, eNode)
    ePaxosLearner ! Init(membership, eNode)

    fPaxosProposer ! Init(membership, fNode)
    fPaxosAcceptor ! Init(membership, fNode)
    fPaxosLearner ! Init(membership, fNode)


    //    aNode.smrActor ! Get("a1", "a1mid")
    aNode.client ! TriggerPut(aNode.smrActor, "a1", "av1")
    aNode.client ! TriggerPut(aNode.smrActor, "a2", "av2")
    bNode.client ! TriggerPut(bNode.smrActor, "b1", "bv1")
    cNode.client ! TriggerPut(cNode.smrActor, "c1", "cv1")
    dNode.client ! TriggerPut(dNode.smrActor, "d1", "dv1")
    fNode.client ! TriggerPut(fNode.smrActor, "e1", "fv1")
    eNode.client ! TriggerPut(eNode.smrActor, "e1", "CHANGED_E1")





  }

}

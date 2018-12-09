import akka.actor.{ActorSystem, Props}
import clients.{ClientActor, InitClient, Put}
import clients.test.{StartTest, TestActor, Validate}
import replicas.multidimensionalpaxos.Init
import replicas.multidimensionalpaxos.acceptors.AcceptorActor
import replicas.multidimensionalpaxos.learners.LearnerActor
import replicas.multidimensionalpaxos.proposers.ProposerActor
import replicas.statemachinereplication.StateMachineReplicationActor
import utils.ReplicaNode

object TestClientMain extends App {


  override def main(args: Array[String]) = {

    val a = ActorSystem("nodeAsystem")
    val b = ActorSystem("nodeBSystem")
    val c = ActorSystem("nodeCSytstem")
    val d = ActorSystem("nodeDsystem")
    val e = ActorSystem("nodeESystem")
    val f = ActorSystem("nodeFSytstem")
    val clientSystem = ActorSystem("clientSytstem")


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

    val aKeyValueClient = a.actorOf(Props(new ClientActor("localhost", 69)), "aKeyValueClient")
    //    val bKeyValueClient = b.actorOf(Props[ClientActor], "bKeyValueClient")
    //    val cKeyValueClient = c.actorOf(Props[ClientActor], "cKeyValueClient")
    //    val dKeyValueClient = d.actorOf(Props[ClientActor], "dKeyValueClient")
    //    val eKeyValueClient = e.actorOf(Props[ClientActor], "eKeyValueClient")
    //    val fKeyValueClient = f.actorOf(Props[ClientActor], "fKeyValueClient")


    val aNode = ReplicaNode("aNode", "1", aPaxosSmr, aPaxosAcceptor, aPaxosLearner, aPaxosProposer)
    val bNode = ReplicaNode("bNode", "2", bPaxosSmr, bPaxosAcceptor, bPaxosLearner, bPaxosProposer)
    val cNode = ReplicaNode("cNode", "3", cPaxosSmr, cPaxosAcceptor, cPaxosLearner, cPaxosProposer)
    val dNode = ReplicaNode("dNode", "4", dPaxosSmr, dPaxosAcceptor, dPaxosLearner, dPaxosProposer)
    val eNode = ReplicaNode("eNode", "5", ePaxosSmr, ePaxosAcceptor, ePaxosLearner, ePaxosProposer)
    val fNode = ReplicaNode("fNode", "6", fPaxosSmr, fPaxosAcceptor, fPaxosLearner, fPaxosProposer)

    val membership = Set(aNode, bNode, cNode, dNode, eNode, fNode)
    //Init

    aPaxosSmr ! replicas.statemachinereplication.Init(membership, aNode)
    bPaxosSmr ! replicas.statemachinereplication.Init(membership, bNode)
    cPaxosSmr ! replicas.statemachinereplication.Init(membership, cNode)
    dPaxosSmr ! replicas.statemachinereplication.Init(membership, dNode)
    ePaxosSmr ! replicas.statemachinereplication.Init(membership, eNode)
    fPaxosSmr ! replicas.statemachinereplication.Init(membership, fNode)


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
//    aNode.client ! TriggerPut(aNode.smrActor, "a2", "av2")
//    bNode.client ! TriggerPut(bNode.smrActor, "b1", "bv1")
//    cNode.client ! TriggerPut(cNode.smrActor, "c1", "cv1")
//    dNode.client ! TriggerPut(dNode.smrActor, "d1", "dv1")
//    fNode.client ! TriggerPut(fNode.smrActor, "e1", "fv1")
//    eNode.client ! TriggerPut(eNode.smrActor, "e1", "CHANGED_E1")

    aKeyValueClient ! InitClient(membership, 0)

    val testActor = a.actorOf(Props[TestActor], "testActor")
    testActor ! StartTest(membership, aKeyValueClient)
    Thread.sleep(5000)
    testActor ! Validate

  }


}

//import akka.actor.{ActorSystem, Props}
//import clients.{ClientActor, InitClient, Put}
//import clients.test.{StartTest, TestActor, Validate}
//import replicas.multidimensionalpaxos.Init
//import replicas.multidimensionalpaxos.acceptors.AcceptorActor
//import replicas.multidimensionalpaxos.learners.LearnerActor
//import replicas.multidimensionalpaxos.proposers.ProposerActor
//import replicas.statemachinereplication.StateMachineReplicationActor
//import utils.ReplicaNode
//
//object TestClientMain extends App {
//
//
//  override def main(args: Array[String]) = {
//
//    val a = ActorSystem("nodeAsystem")
//    val b = ActorSystem("nodeBSystem")
//    val c = ActorSystem("nodeCSytstem")
//    val d = ActorSystem("nodeDsystem")
//    val e = ActorSystem("nodeESystem")
//    val f = ActorSystem("nodeFSytstem")
//    val g = ActorSystem("nodeGSytstem")
//    //    val h = ActorSystem("nodeHSytstem")
//    //    val i = ActorSystem("nodeISytstem")
//    //    val j = ActorSystem("nodeJSytstem")
//
//    //    val clientSystem = ActorSystem("clientSytstem")
//
//
//    val aPaxosProposer = a.actorOf(Props[ProposerActor], "aPaxosProposer")
//    val bPaxosProposer = b.actorOf(Props[ProposerActor], "bPaxosProposer")
//    val cPaxosProposer = c.actorOf(Props[ProposerActor], "cPaxosProposer")
//    val dPaxosProposer = d.actorOf(Props[ProposerActor], "dPaxosProposer")
//    val ePaxosProposer = e.actorOf(Props[ProposerActor], "ePaxosProposer")
//    val fPaxosProposer = f.actorOf(Props[ProposerActor], "fPaxosProposer")
//    val gPaxosProposer = g.actorOf(Props[ProposerActor], "gPaxosProposer")
//    //    val hPaxosProposer = h.actorOf(Props[ProposerActor], "hPaxosProposer")
//    //    val iPaxosProposer = i.actorOf(Props[ProposerActor], "iPaxosProposer")
//    //    val jPaxosProposer = j.actorOf(Props[ProposerActor], "jPaxosProposer")
//
//
//    val aPaxosAcceptor = a.actorOf(Props[AcceptorActor], "aPaxosAcceptor")
//    val bPaxosAcceptor = b.actorOf(Props[AcceptorActor], "bPaxosAcceptor")
//    val cPaxosAcceptor = c.actorOf(Props[AcceptorActor], "cPaxosAcceptor")
//    val dPaxosAcceptor = d.actorOf(Props[AcceptorActor], "dPaxosAcceptor")
//    val ePaxosAcceptor = e.actorOf(Props[AcceptorActor], "ePaxosAcceptor")
//    val fPaxosAcceptor = f.actorOf(Props[AcceptorActor], "fPaxosAcceptor")
//    val gPaxosAcceptor = g.actorOf(Props[AcceptorActor], "gPaxosAcceptor")
//
//    val aPaxosLearner = a.actorOf(Props[LearnerActor], "aPaxosLearner")
//    val bPaxosLearner = b.actorOf(Props[LearnerActor], "bPaxosLearner")
//    val cPaxosLearner = c.actorOf(Props[LearnerActor], "cPaxosLearner")
//    val dPaxosLearner = d.actorOf(Props[LearnerActor], "dPaxosLearner")
//    val ePaxosLearner = e.actorOf(Props[LearnerActor], "ePaxosLearner")
//    val fPaxosLearner = f.actorOf(Props[LearnerActor], "fPaxosLearner")
//    val gPaxosLearner = g.actorOf(Props[LearnerActor], "gPaxosLearner")
//
//    val aPaxosSmr = a.actorOf(Props[StateMachineReplicationActor], "aPaxosSmr")
//    val bPaxosSmr = b.actorOf(Props[StateMachineReplicationActor], "bPaxosSmr")
//    val cPaxosSmr = c.actorOf(Props[StateMachineReplicationActor], "cPaxosSmr")
//    val dPaxosSmr = d.actorOf(Props[StateMachineReplicationActor], "dPaxosSmr")
//    val ePaxosSmr = e.actorOf(Props[StateMachineReplicationActor], "ePaxosSmr")
//    val fPaxosSmr = f.actorOf(Props[StateMachineReplicationActor], "fPaxosSmr")
//    val gPaxosSmr = g.actorOf(Props[StateMachineReplicationActor], "gPaxosSmr")
//
//
//    val aNode = ReplicaNode("aNode", "1", aPaxosSmr, aPaxosAcceptor, aPaxosLearner, aPaxosProposer)
//    val bNode = ReplicaNode("bNode", "2", bPaxosSmr, bPaxosAcceptor, bPaxosLearner, bPaxosProposer)
//    val cNode = ReplicaNode("cNode", "3", cPaxosSmr, cPaxosAcceptor, cPaxosLearner, cPaxosProposer)
//    val dNode = ReplicaNode("dNode", "4", dPaxosSmr, dPaxosAcceptor, dPaxosLearner, dPaxosProposer)
//    val eNode = ReplicaNode("eNode", "5", ePaxosSmr, ePaxosAcceptor, ePaxosLearner, ePaxosProposer)
//    val fNode = ReplicaNode("fNode", "6", fPaxosSmr, fPaxosAcceptor, fPaxosLearner, fPaxosProposer)
//    val gNode = ReplicaNode("gNode", "7", gPaxosSmr, gPaxosAcceptor, gPaxosLearner, gPaxosProposer)
//
//    val membership = Set(aNode, bNode, cNode, dNode, eNode, fNode, gNode)
//
//    //Init
//
//    aPaxosSmr ! replicas.statemachinereplication.Init(membership, aNode)
//    bPaxosSmr ! replicas.statemachinereplication.Init(membership, bNode)
//    cPaxosSmr ! replicas.statemachinereplication.Init(membership, cNode)
//    dPaxosSmr ! replicas.statemachinereplication.Init(membership, dNode)
//    ePaxosSmr ! replicas.statemachinereplication.Init(membership, eNode)
//    fPaxosSmr ! replicas.statemachinereplication.Init(membership, fNode)
//    gPaxosSmr ! replicas.statemachinereplication.Init(membership, gNode)
//    //    hPaxosSmr ! replicas.statemachinereplication.Init(membership, hNode)
//    //     iPaxosSmr ! replicas.statemachinereplication.Init(membership, iNode)
//    //
//
//    aPaxosProposer ! Init(membership, aNode)
//    aPaxosAcceptor ! Init(membership, aNode)
//    aPaxosLearner ! Init(membership, aNode)
//
//    bPaxosProposer ! Init(membership, bNode)
//    bPaxosAcceptor ! Init(membership, bNode)
//    bPaxosLearner ! Init(membership, bNode)
//
//    cPaxosProposer ! Init(membership, cNode)
//    cPaxosAcceptor ! Init(membership, cNode)
//    cPaxosLearner ! Init(membership, cNode)
//
//    dPaxosProposer ! Init(membership, dNode)
//    dPaxosAcceptor ! Init(membership, dNode)
//    dPaxosLearner ! Init(membership, dNode)
//
//    ePaxosProposer ! Init(membership, eNode)
//    ePaxosAcceptor ! Init(membership, eNode)
//    ePaxosLearner ! Init(membership, eNode)
//
//    fPaxosProposer ! Init(membership, fNode)
//    fPaxosAcceptor ! Init(membership, fNode)
//    fPaxosLearner ! Init(membership, fNode)
//
//    gPaxosProposer ! Init(membership, gNode)
//    gPaxosAcceptor ! Init(membership, gNode)
//    gPaxosLearner ! Init(membership, gNode)
//
//    /* hPaxosProposer ! Init(membership, hNode)
//        hPaxosAcceptor ! Init(membership, hNode)
//        hPaxosLearner ! Init(membership, hNode)
//
//         iPaxosProposer ! Init(membership, iNode)
//        iPaxosAcceptor ! Init(membership, iNode)
//        iPaxosLearner ! Init(membership, iNode)
//
//         jPaxosProposer ! Init(membership, jNode)
//        jPaxosAcceptor ! Init(membership, jNode)
//        jPaxosLearner ! Init(membership, jNode)
//        */
//
//    val testSystem = ActorSystem("testSystem")
//
//    val aKeyValueClient = testSystem.actorOf(Props(new ClientActor("localhost", 69)), "aKeyValueClient")
//
//    val testActor = testSystem.actorOf(Props[TestActor], "testActor")
//    aKeyValueClient ! InitClient(membership, -1, testActor) // -1 for random smr
//
//    testActor ! StartTest(membership, aKeyValueClient, 40000)
//    Thread.sleep(50000)
//    testActor ! Validate
//
//    //    a.terminate()
//    //    b.terminate()
//    //    c.terminate()
//    //    d.terminate()
//    //    e.terminate()
//    //    testSystem.terminate()
//  }
//
//}

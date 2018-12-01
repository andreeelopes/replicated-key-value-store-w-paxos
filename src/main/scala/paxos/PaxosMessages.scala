package paxos

import utils.Node

case class Init(membership : Set[Node], myNode: Node)

case class Propose(value: String)

case class Prepare(sn : Int)

case class PrepareOk(sna : Int, va : String)

object PrepareNotOk

case class Accept(sna : Int, va: String)

case class AcceptOk(sna : Int, va: String)

object AcceptNotOk

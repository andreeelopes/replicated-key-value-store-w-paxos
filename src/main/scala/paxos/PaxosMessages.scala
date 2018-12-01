package paxos

case class Propose(value: String)

case class Prepare(sn : Int)

case class PrepareOk(sna : Int, va : String)

object PrepareNotOk

case class Accept(sna : Int, va: String)

object AcceptNotOk

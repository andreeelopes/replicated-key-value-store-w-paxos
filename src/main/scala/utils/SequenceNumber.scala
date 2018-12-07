package utils

class SequenceNumber(var nodeID: String) {
  var seq = 0

  def getSN() : Int = {
    seq +=1
    val strSeq = seq.toString

    (strSeq + nodeID).toInt
  }
}

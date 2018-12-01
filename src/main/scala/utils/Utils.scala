package utils

object Utils {
  def majority[A](nElems: Int, set: Set[A]) =
    nElems > set.size / 2
}

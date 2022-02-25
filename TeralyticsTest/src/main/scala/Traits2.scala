class MultiplierIdentity {
  def identity: Int = 1
}

trait DoubleIdentity extends MultiplierIdentity {
  override def identity: Int = 2 * super.identity
}

trait TripleIdentity extends MultiplierIdentity {
  override def identity: Int = 3 * super.identity
}

class DT1 extends DoubleIdentity with TripleIdentity
class DT2 extends DoubleIdentity with TripleIdentity {
  override def identity: Int = super[DoubleIdentity].identity
}
class DT3 extends DoubleIdentity with TripleIdentity {
  override def identity: Int = super[TripleIdentity].identity
}

class TD1 extends TripleIdentity with DoubleIdentity
class TD2 extends TripleIdentity with DoubleIdentity {
  override def identity: Int = super[DoubleIdentity].identity
}
class TD3 extends TripleIdentity with DoubleIdentity {
  override def identity: Int = super[TripleIdentity].identity
}

object Traits2App extends App {

  val dt1 = new DT1
  val dt2 = new DT2
  val dt3 = new DT3

  println(dt1.identity) // 6
  println(dt2.identity) // 2
  println(dt3.identity) // 6

  val td1 = new TD1
  val td2 = new TD2
  val td3 = new TD3

  println(td1.identity) // 6
  println(td2.identity) // 6
  println(td3.identity) // 3
}

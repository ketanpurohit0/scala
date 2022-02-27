import scala.collection.mutable

trait Database[T] {
  def save(data: T)
}

trait MemoryDatabase[T] extends Database[T] {
  val db: mutable.MutableList[T] = mutable.MutableList.empty

  override def save(data: T): Unit = db.+=:(data); println("memory persist.")
}

trait FileDatabase[T] extends Database[T] {
  override def save(data: T): Unit = println("file persist.")
}

trait History {
  def add() = println("History added.")
}

trait Mystery {
  def add() = println("Mystery added.")
}
trait Persistor[T] {
  self: Database[T] with History with Mystery =>

  def persist(data: T): Unit = {
    save(data)
    add()
  }
}

class FilePersistor[T] extends Persistor[T] with FileDatabase[T] with History with Mystery {
  override def add(): Unit = super[History].add()
}
class MemoryPersistor[T] extends Persistor[T] with MemoryDatabase[T] with History with Mystery {
  override def add(): Unit = super[Mystery].add()
}

object SelfTypeApp extends App {
  val memPersistor = new MemoryPersistor[String]
  val filePersistor = new FilePersistor[Int]

  memPersistor.persist("a")
  filePersistor.persist(1)

}

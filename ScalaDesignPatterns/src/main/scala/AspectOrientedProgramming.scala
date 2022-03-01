import org.json4s._
import org.json4s.jackson.JsonMethods._

import java.io.File

case class Person(firstName: String, lastName: String, age: Int)

trait DataReader {
  def readData(): List[Person]
  def readDataInefficiently(): List[Person]
}

class DataReaderImpl extends DataReader {
  implicit val formats = DefaultFormats
  val jsonStr =
    """ [
      |{"firstName" :"Ivan", "lastName": "Nikolov", "age": 26},
      |{"firstName" :"John", "lastName": "Smith", "age": 55},
      |{"firstName" :"Maria", "lastName": "Cooper", "age": 19}] """.stripMargin
  private def readUntimed(): List[Person] =
    parse(jsonStr)
      .extract[List[Person]]

  override def readData(): List[Person] = readUntimed()

  override def readDataInefficiently(): List[Person] = {
    (1 to 1000).foreach({ case n =>
      readUntimed()
    })

    readUntimed()
  }
}

trait DataReaderTimed extends DataReader {
  abstract override def readData(): List[Person] = {
    val sm = System.currentTimeMillis()
    val result = super.readData()
    val time = System.currentTimeMillis() - sm
    println(s"readData $time ms.")
    result
  }

  abstract override def readDataInefficiently(): List[Person] = {
    val sm = System.currentTimeMillis()
    val result = super.readDataInefficiently()
    val time = System.currentTimeMillis() - sm
    println(s"readDataInefficiently $time ms.")
    result
  }
}

object AOPApp extends App {
  val dataReader = new DataReaderImpl

  println("DataReaderImpl")
  println(dataReader.readData())
  println(dataReader.readDataInefficiently())

  println("DataReaderTimed")
  val dataReaderTimed = new DataReaderImpl with DataReaderTimed
  dataReaderTimed.readData()
  dataReaderTimed.readDataInefficiently()
}

abstract class ApprovalRequest {
  def requestApproval() // : Unit = {
//    println("Approval Request")
//  }
}

trait MarketingApprovalRequest extends ApprovalRequest {
  abstract override def requestApproval(): Unit = {
    println("Marketing Approval Request")
    super.requestApproval()
  }
}

trait FinanceApprovalRequest extends ApprovalRequest {
  abstract override def requestApproval(): Unit = {
    println("Finance Approval Request")
    super.requestApproval()
  }
}

trait ExecutiveApprovalRequest extends ApprovalRequest {
  abstract override def requestApproval(): Unit = {
    println("Executive Approval Request")
    super.requestApproval()
  }
}

object ApprovalApp extends App {

  class ApprovalDelegate extends ApprovalRequest {
    override def requestApproval(): Unit = {
      println("Waiting for approval")
    }
  }
  val seekApproval = new ApprovalDelegate
    with MarketingApprovalRequest
    with FinanceApprovalRequest
    with ExecutiveApprovalRequest

  seekApproval.requestApproval()
}

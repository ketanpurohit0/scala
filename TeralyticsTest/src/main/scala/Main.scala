object Main extends App {

  println(getRunningScalaVersion())
  sys.env.foreach( e => println(e._1, e._2) )
  args.foreach(a => println("arg",a))

  def getRunningScalaVersion(): String = {
    try {
      //OLD WAY
      //val stream = getClass.getResourceAsStream("/library.properties")
      //val iter = scala.io.Source.fromInputStream(stream).getLines
      //val line = (iter find {l => l.startsWith("version.number")}).get
      //val Version = """version\.number=(\d\.\d\.\d).*""".r

      //stainsby's way
      val props = new java.util.Properties
      props.load(getClass.getResourceAsStream("/library.properties"))
      val line = props.getProperty("version.number")
      val Version = """(\d\.\d\.\d).*""".r
      val Version(versionStr) = line
      versionStr
    }
    catch {
      case e => {
        e.printStackTrace()
        "2.8.0" //or some other default version, or re-raise
      }
    }
  }
}


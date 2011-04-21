import sbt._

class SolitariusProject(info: ProjectInfo) extends DefaultProject(info) {
  val specs = "org.scala-tools.testing" % "specs_2.8.1" % "1.6.7" % "test"
}

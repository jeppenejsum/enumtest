name := "enumtest"

version := "1.0"

scalaVersion := "2.10.1"

libraryDependencies  ++=  Seq(
  "org.squeryl" %% "squeryl" % "0.9.6-SNAPSHOT",
  "com.h2database" % "h2" % "1.2.127"
)

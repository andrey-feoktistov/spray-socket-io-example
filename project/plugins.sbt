resolvers ++= Seq(
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Scala sbt" at "http://scalasbt.artifactoryonline.com/scalasbt",
    "Typesafe Snaphot Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
    "sbt-plugin-releases" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.5")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")


name := "JXMPP Playground for Scala"

version := "1.0"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.mavenLocal

libraryDependencies += "org.jxmpp" % "jxmpp-jid" % "latest.integration"
libraryDependencies += "org.jxmpp" % "jxmpp-stringprep-libidn" % "latest.integration"
//libraryDependencies += "org.jxmpp" % "jxmpp-stringprep-icu4j" % "latest.integration"

initialCommands in console += "import org.jxmpp.jid.impl.JidCreate;"

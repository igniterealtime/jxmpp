package org.jxmpp

import org.gradle.api.JavaVersion

class BuildConstants {
	static final JAVA_COMPATIBILITY = JavaVersion.VERSION_1_8
	static final JAVA_MAJOR_COMPATIBILITY = JAVA_COMPATIBILITY.getMajorVersion()
}

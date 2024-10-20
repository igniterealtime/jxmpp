GRADLE ?= ./gradlew

.PHONY: all
all: check codecov eclipse javadocAll jmh

.PHONY: codecov
codecov:
	$(GRADLE) jxmpp-repl:testCodeCoverageReport
	echo "Code coverage report available at file://$(PWD)/jxmpp-repl/build/reports/jacoco/testCodeCoverageReport/html/index.html"

.PHONY: check
check:
	$(GRADLE) $@

.PHONY: eclipse
eclipse:
	$(GRADLE) $@

.PHONY: javadocAll
javadocAll:
	$(GRADLE) $@
	echo "javadoc available at file://$(PWD)/build/javadoc/index.html"

.PHONY: jmh
jmh:
	$(GRADLE) jxmpp-util-cache:jmh

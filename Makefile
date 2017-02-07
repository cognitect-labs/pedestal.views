# Tools/Executables
JAVA ?= $(shell which java)
JAR ?= $(shell which jar)
MVN ?= $(shell which mvn)

CLASSPATH_FILE=bin/cached-classpath

$(CLASSPATH_FILE): pom.xml
	$(MVN) dependency:build-classpath -Dmdep.outputFile=$(CLASSPATH_FILE)
	touch $(CLASSPATH_FILE)
	@echo "Run bin/repl, bin/cider, or bin/srepl now"

.PHONY : classpath
classpath: $(CLASSPATH_FILE)
	@cat $(CLASSPATH_FILE)

.PHONY: jar
jar:
	$(MVN) package

.PHONY : clean-all
clean:
	rm -rf $(CLASSPATH_FILE) target

.PHONY : fetch-middleware
fetch-middleware:
	mkdir -p .repl/lib
	wget -N -P .repl/lib https://repo1.maven.org/maven2/org/clojure/tools.nrepl/0.2.12/tools.nrepl-0.2.12.jar
	wget -N -P .repl/lib https://clojars.org/repo/refactor-nrepl/refactor-nrepl/2.2.0/refactor-nrepl-2.2.0.jar
	wget -N -P .repl/lib https://clojars.org/repo/org/tcrawley/dynapath/0.2.3/dynapath-0.2.3.jar
	wget -N -P .repl/lib https://clojars.org/repo/cider/cider-nrepl/0.14.0/cider-nrepl-0.14.0.jar

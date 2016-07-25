VIOLIN_V := 0.1.0
VIOLIN_R := https://dl.bintray.com/norswap/maven/com/norswap/violin/
VIOLIN_B := $(VIOLIN_R)/$(VIOLIN_V)/violin-$(VIOLIN_V).jar
VIOLIN_D := $(VIOLIN_R)/$(VIOLIN_V)/violin-$(VIOLIN_V)-kdoc.jar
VIOLIN_S := $(VIOLIN_R)/$(VIOLIN_V)/violin-$(VIOLIN_V)-sources.jar

violin:
	mkdir -p lib
	curl -L $(VIOLIN_B) > lib/violin.jar
	curl -L $(VIOLIN_D) > lib/violin-kdoc.jar
	curl -L $(VIOLIN_S) > lib/violin-sources.jar

TINYLOG_V := 1.1
TINYLOG_R := http://central.maven.org/maven2/org/tinylog/tinylog
TINYLOG_B := $(TINYLOG_R)/$(TINYLOG_V)/tinylog-$(TINYLOG_V).jar
TINYLOG_D := $(TINYLOG_R)/$(TINYLOG_V)/tinylog-$(TINYLOG_V)-javadoc.jar
TINYLOG_S := $(TINYLOG_R)/$(TINYLOG_V)/tinylog-$(TINYLOG_V)-sources.jar

tinylog:
	mkdir -p lib
	curl -L $(TINYLOG_B) > lib/tinylog.jar
	curl -L $(TINYLOG_D) > lib/tinylog-javadoc.jar
	curl -L $(TINYLOG_S) > lib/tinylog-sources.jar

# ------------------------------------------------------------------------------

deps: violin, tinylog

# ------------------------------------------------------------------------------

.PHONY:
	violin \
	tinylog \
	deps

.SILENT:

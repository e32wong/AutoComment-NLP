# This is a rudimentary Makefile for rebuilding Stanford CoreNLP.
# We actually use ant (q.v.) or a Java IDE.

JAVAC = javac
JAVAFLAGS = -cp stanford-corenlp-3.6.0.jar:stanford-corenlp-3.6.0-models.jar:xom.jar:joda-time.jar:jollyday.jar:slf4j-api.jar:slf4j-simple.jar:ejml-0.23.jar

# Builds the classes' jar file
all:
	$(JAVAC) $(JAVAFLAGS) CommentParser2.java

clean:
	rm -rf *.class

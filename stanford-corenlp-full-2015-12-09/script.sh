#!/bin/bash

java -cp stanford-corenlp-3.6.0.jar:stanford-corenlp-3.6.0-models.jar:xom.jar:joda-time.jar:jollyday.jar:slf4j-api.jar:slf4j-simple.jar:ejml-0.23.jar:. CommentParser2 1 /home/edmund/research/autocomment/posts/stage3-extracted/java/ /home/edmund/research/autocomment/posts/stage4-nlpProcessed/java2/

java -cp stanford-corenlp-3.6.0.jar:stanford-corenlp-3.6.0-models.jar:xom.jar:joda-time.jar:jollyday.jar:slf4j-api.jar:slf4j-simple.jar:ejml-0.23.jar:. CommentParser2 1 /home/edmund/research/autocomment/posts/stage3-extracted/android/ /home/edmund/research/autocomment/posts/stage4-nlpProcessed/android2/


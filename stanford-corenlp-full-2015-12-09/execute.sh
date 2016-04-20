#!/bin/bash

java -cp stanford-corenlp-3.6.0.jar:stanford-corenlp-3.6.0-models.jar:xom.jar:joda-time.jar:jollyday.jar:slf4j-api.jar:slf4j-simple.jar:ejml-0.23.jar:. CommentParser2 0 input.txt output

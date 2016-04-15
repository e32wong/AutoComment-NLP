#!/usr/bin/python

import subprocess
import re
import os

inputDir = "/home/edmund/research/autocomment/posts/testPreNLP/"

originalDir = os.path.dirname(os.path.realpath(__file__))
nlpDir = "/home/edmund/research/mate-tools/srl/"
nlpCommand = "./scripts/parse_full.sh"
print "Current dir: " + originalDir

# process every single snippet
for f in os.listdir(inputDir):

    with open(inputDir + f, "r") as mapping:
        content = mapping.readlines()

        commentTitle = content[0]
        commentBody = content[1]

        print commentTitle[2:]
        print commentBody[2:]

        os.chdir(nlpDir)
        subprocess.call([nlpCommand, "/home/edmund/research/mate-tools/srl/test.txt"])
        os.chdir(originalDir)














#!/usr/bin/python

import subprocess
import re
import os

# Clean sentence
def cleanSentence(sentences):

    # No need to process empty sentences
    if sentences == "":
        return ""

    # Replace the "i" with "I"
    sentences = re.sub('\\bi\\b', "I", sentences)

    # Replace "..." with "."
    sentences = re.sub('\s*\.{2,}', ".", sentences)

    # Replace ":" with "."
    sentences = re.sub('\s*\:', ".", sentences)

    # Replace "n't" with "not"
    # http://www.learnenglish.de/grammar/shortforms.html
    sentences = re.sub("can't\\b", "cannot", sentences)
    sentences = re.sub("won't\\b", "will not", sentences)
    sentences = re.sub("shan't\\b", "shall not", sentences)
    sentences = re.sub("n't\\b", " not", sentences)
    # Replace "'ll" with " will"
    sentences = re.sub("'ll\\b", " will", sentences)
    # Replace "I'm" with "I am"
    sentences = re.sub("I'm\\b", "I am", sentences)
    # Replace "'ve" with " have
    sentences = re.sub("'ve\\b", " have", sentences)

    # Ensure last word of the paragraph has a valid ending character
    pattern = re.compile('[a-zA-Z;()/]$')
    if pattern.search(sentences):
        sentences = sentences + "."

    # Split sentences and make first word in each sentence capitalize
    # Split sentence using space after the dot
    listOfSentences = re.findall('(.+?(\.|\?|\!)(\s|$)+)', sentences)
    if listOfSentences:
        sentences = ""
        for thisSentence in listOfSentences:
            # Detect for URLs in sentence and remove them
            # Note we substituded ":" with "." previously
            patternURL = re.compile('(http\.|https\.)')
            if patternURL.search(thisSentence[0]):
                continue

            # Make first letter upper case and append to list
            sentences = sentences + thisSentence[0][0:1].upper() + thisSentence[0][1:]

    return sentences

# main #
########

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














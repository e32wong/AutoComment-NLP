import re
import csv
from optparse import OptionParser
from os import listdir
from os.path import isfile, join
from xml.dom import minidom
from HTMLParser import HTMLParser

from xml.sax.saxutils import unescape

#htmlParser = HTMLParser.HTMLParser()

# mode 0 = question; mode 1 = answer;
# mode 2 = question (no threshold)
# mode 3 = answer (no threshold)
# returns a list
def extractProperty (content, tagName, mode, scoreThreshold):
    valueList = []
    if mode == 0:
        # Only have to analyze the title of post
        # which is the first line of the content
        line = content[0]
        xmldoc = minidom.parseString(line)
        itemList = xmldoc.getElementsByTagName('row')

        # Check the score first
        if int(itemList[0].attributes['Score'].value) >= scoreThreshold:
            # Extract property since it satisfied the score requirement
            try:
                valueList.append(itemList[0].attributes[tagName].value)
            except KeyError:
                pass
    elif mode == 1:
        # Analyze the answer of each post
        for line in content[1:]:
            xmldoc = minidom.parseString(line)
            itemList = xmldoc.getElementsByTagName('row')

            # Check the score first
            if int(itemList[0].attributes['Score'].value) >= scoreThreshold:
                # Extract property since it satisfied the score requirement
                try:
                    valueList.append(itemList[0].attributes[tagName].value)
                except KeyError:
                    pass
    elif mode == 2:
        line = content[0]
        xmldoc = minidom.parseString(line)
        itemList = xmldoc.getElementsByTagName('row')
        try:
            valueList.append(itemList[0].attributes[tagName].value)
        except KeyError:
            pass
    elif mode == 3:
        for line in content[1:]:
            xmldoc = minidom.parseString(line)
            itemList = xmldoc.getElementsByTagName('row')
            try:
                valueList.append(itemList[0].attributes[tagName].value)
            except KeyError:
                pass

    return valueList

# Stript out all types of HTML tags
def removeHTMLtags(string):

    string = re.sub('\<code\>' + '|' + '\</code\>', '"',string)

    patternTags = re.compile('\<.+?\>' + '|' + '\</.+?\>')
    string = re.sub(patternTags, '', string)

    return string

# Input is an array of answer's body
# Output is an array of tuples of (codeDescription, codeSegment)
def extractCode(listOfBodies, extractMode):

    mappingList = []

    # Go through each answer
    for body in listOfBodies:
        # Go through each line of the answer

        # Unescape HTML code
        # Perform exception handling
        try:
            body = HTMLParser().unescape(body)
        except ValueError:
            continue

        if extractMode == 0:
            # before only
            patternCodeStart = re.compile('(\<p\>(.+?)\</p\>)'
                + '\n+\<pre\>\<code\>((.|\n)+?)\</code\>\</pre\>')
        else:
            # before and after
            patternCodeStart = re.compile('(\<p\>(.+?)\</p\>)'
                + '\n+\<pre\>\<code\>((.|\n)+?)\</code\>\</pre\>'
                + '(?=(\n+\<p\>(.+?)\</p\>)?(\n+\<pre\>\<code\>((.|\n)+?)\</code\>\</pre\>)?)')

        matchList = re.findall(patternCodeStart,body)
        if matchList:
            # There can be more than one code segment in a post
            for match in matchList:
                # Remove code artifacts

                if checkCode(match[2]) == False:
                    continue

                # Capture the first code-description mapping
                description = removeHTMLtags(match[1])
                codeDescription = description

                if extractMode == 1:
                    if match[6] == "":
                        # No trailing code segment, use both paragraph before and after
                        description = removeHTMLtags(match[5])
                        codeDescription = codeDescription + " " + description

                # Add the description/code pair
                mappingList.append((codeDescription,match[2]))

    return mappingList

def insertScore(list, value):
    inserted = False
    for index,entry in enumerate(list):
        if entry[0] == value:
            list[index] = [value, list[index][1] + 1]
            inserted = True
            break

    if inserted == False:
        list.append([value, 1])

    return list

def checkCode(content):
    # count number of statements
    listNewLines = re.findall(";", content)
    if len(listNewLines) <= 2 or len(listNewLines) > 10:
        return False

    # Check for at least one method calls
    listOfFunctionNames = re.findall("\.[A-Za-z][A-Za-z0-9_]*\(", codeSegment)
    if len(listOfFunctionNames) == 0:
        return False

    return True


### Main function ###
#####################
parser = OptionParser()
parser.add_option("-i", "--inputPath", dest="inputPath", help="specify the path to the posts file", default='./source/')
parser.add_option("-o", "--outputPath", dest="outputPath", help="specify the output folder", default='./output/')
parser.add_option("-s", "--scoreThreshold", dest="scoreThreshold", help="question or answer must have a score equal or higher to this number", default='0')
parser.add_option("-m", "--extractMode", dest="extractMode", \
        help="0 to extract sentence before only, 1 to extract before and after", default='0')
parser.add_option("-t", "--trackScore", dest="trackScore", \
        help="0 to disable, 1 for title of post, 2 for answer of post", default='0')
parser.add_option("-c", "--csvFileName", dest="csvFileName", \
        help="Output file name for the CSV file", default='scoreDistribution.csv')

(options, args) = parser.parse_args()

# Parser user input
inputPath = options.inputPath
outputPath = options.outputPath
scoreThreshold = int(options.scoreThreshold)
extractMode = int(options.extractMode)
trackScore = int(options.trackScore)
csvFileName = options.csvFileName

# Obtain list of files from directory
listOfPosts = [f for f in listdir(inputPath) if isfile(join(inputPath,f))]

# Variable to keep track the score distribution
scoreList = []

# Traverse through file names
numPosts = len(listOfPosts)
modulo = numPosts / 100
if modulo == 0:
    modulo = 1
numProcessedFiles = 0
print "Total number of posts to process: " + str(len(listOfPosts))
for postName in listOfPosts:

    # Print the progress on screen
    if numProcessedFiles % modulo == 0:
        print "Processed " + str(numProcessedFiles)
    numProcessedFiles = numProcessedFiles + 1

    with open(inputPath + postName) as f:
        content = f.readlines()
        f.close

    if trackScore:
        if trackScore == 1:
            # Insert Title Score
            scoreOfTitle = extractProperty(content, "Score", 2, 0)
            scoreList = insertScore(scoreList, int(scoreOfTitle[0]))
        elif trackScore == 2:
            # Insert Answer Score
            scoreOfAnswers = extractProperty(content, "Score", 3, 0)
            for thisScore in scoreOfAnswers:
                scoreList = insertScore(scoreList, int(thisScore))
    else:
        # Check if the post is closed
        closeDate = extractProperty(content, "ClosedDate", 0, scoreThreshold)
        if closeDate != []:
            # Skip processing since it is closed
            continue

        # Obtain title of post
        titleOfQuestion = extractProperty(content, "Title", 0, scoreThreshold)
        if titleOfQuestion != []:
            titleOfQuestion = titleOfQuestion[0]
        else:
            titleOfQuestion = ""

        # Obtain code segments
        bodyOfAnswers = extractProperty(content, "Body", 1, scoreThreshold)
        mappingList = extractCode(bodyOfAnswers, extractMode)

        # Each answer can have multiple code segments
        if mappingList:
            for index,mapping in enumerate(mappingList):

                f = open(outputPath + postName + "-" + str(index), 'w')
                f.write("//" + titleOfQuestion.encode('utf-8') + "\n")
                #print mapping[0]
                f.write("//" + mapping[0].encode('utf-8') + "\n")
                f.write(mapping[1].encode('utf-8'))

# Export the score distribution as a CSV file
if trackScore:
    print scoreList
    resultFile = open(csvFileName,'wb')
    wr = csv.writer(resultFile, delimiter=',')
    for i in range(len(scoreList)):
        wr.writerow(scoreList[i])
    resultFile.close()
    print "Written to scoreDistribution.csv"



## Usage

Stage 1 - Raw files:

You will have to download the Stack Overflow data dump
(https://archive.org/details/stackexchange).
Extract from 7z files to a folder.

Stage 2 - Linking the Stack Overflow Posts:

Use "sort.py" to link the posts together.
The script links each question to its answers.

Yoe have to modify the following variables in the script
to configure the StackOverflow tag, 
input "Posts.xml" file directly from the database dump,
and "targetFolder" where you want to save the output.

tag = "android"
targetFolder = "/home/edmund/research/autocomment/posts/android/"
postFile = "/home/edmund/research/autocomment/posts/stage1-raw/Posts.xml"

Stage 3 - Extract database mappings

Use "parser.py" to extract the mappings between 
the sentence and code segment.
The code segments are identified by the HTML tags.
See the OptionParser for further details on the options.

Sample Usage:
time python parser.py -m 1 -i /home/edmund/research/autocomment/posts/stage2-linked/java/ -o /home/edmund/research/autocomment/posts/stage3-extracted/javaBeforeAfter/

Stage 4 - NLP:

Users are expected to download Stanford CoreNLP 3.6.0
(http://stanfordnlp.github.io/CoreNLP/) and
insert the given files into the extracted folder,
"stanford-corenlp-full-2015-12-09", for compilation.

Compile the program with the given Makefile.
The NLP component requires specific class path to run.
We had included "execute.sh".

Here is the command to for processing a database.
You will have to replace the folder names.

java -cp stanford-corenlp-3.6.0.jar:stanford-corenlp-3.6.0-models.jar:xom.jar:joda-time.jar:jollyday.jar:slf4j-api.jar:slf4j-simple.jar:ejml-0.23.jar:. CommentParser2 1 /home/edmund/research/autocomment/posts/stage3-extracted/java/ /home/edmund/research/autocomment/posts/stage4-nlpProcessed/java/

Stage 5 - Code Clone Detection and Heuristics:

Here you will have to clone CloCom:
https://github.com/e32wong/clocom

We had provided the configuration XMLs that we used
in our experiements under the folder called "configurations".


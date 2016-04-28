import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
//import edu.stanford.nlp.trees.semgraph.*;
import edu.stanford.nlp.trees.tregex.*;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

public class CommentParser2 {

    static String stopWordPattern;

    static void removeTag(Tree tree, String posPattern) {

        TregexPattern p1 = TregexPattern.compile(posPattern);
        TregexMatcher m1 = p1.matcher(tree);

        while (m1.find()) {
            Tree matchedTree = m1.getMatch();
            Tree parentTree = matchedTree.parent(tree);
            int index = parentTree.objectIndexOf(matchedTree);
            parentTree.removeChild(index);
        }
    }

    static String encryptLine(ArrayList<String> listTerms1, ArrayList<String> listTerms2, 
            ArrayList<String> listTerms3, String line) {

        // Replace "." with "@@" to avoid confusion on the sentence splitter
        String pat = "([a-zA-Z])(\\.)([a-zA-Z])";
        line = line.replaceAll(pat, "$1@@$3");

        pat = "(\\b([A-Z_][a-z_0-9]*)([A-Z_][a-z_0-9]+)+\\b)";
        Pattern pattern = Pattern.compile(pat);
        Matcher m = pattern.matcher(line);
        while (m.find()) {
            listTerms1.add(m.group(0));
        }
        line = line.replaceAll(pat, "1item1");

        pat = "\\b([a-z_][a-z_0-9]*)([A-Z_][a-z_0-9]+)+\\b";
        pattern = Pattern.compile(pat);
        m = pattern.matcher(line);
        while (m.find()) {
            listTerms2.add(m.group(0));
        }
        line = line.replaceAll(pat, "2item2");

        pat = "\\b[A-Z0-9_]{3,}\\b";
        pattern = Pattern.compile(pat);
        m = pattern.matcher(line);
        while (m.find()) {
            listTerms3.add(m.group(0));
        }
        line = line.replaceAll(pat, "3item3");

        return line;
    }

    static String decryptLine(ArrayList<String> listTerms1, ArrayList<String> listTerms2,
            ArrayList<String> listTerms3, String line) {

        while (listTerms3.isEmpty() != true) {
            String term = listTerms3.get(0);
            line = line.replace("3item3",term);
            listTerms3.remove(0);
        }

        while (listTerms2.isEmpty() != true) {
            String term = listTerms2.get(0);
            line = line.replace("2item2",term);
            listTerms2.remove(0);
        }

        while (listTerms1.isEmpty() != true) {
            String term = listTerms1.get(0);
            line = line.replace("1item1",term);
            listTerms1.remove(0);
        }

        // Replace "." with "@@" to avoid confusion on the sentence splitter
        String pat = "([a-zA-Z])(@@)([a-zA-Z])";
        line = line.replaceAll(pat, "$1\\.$3");
        return line;
    }

    static String cleanSentence(String text) {
        String finalText = "";

        System.out.println("Before: " + text);

        String[] listLines = text.split("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?)\\s");
        for (String line : listLines) { 
            // remove newline
            line = line.replaceAll("\\n", "");

            // remove front trailing text
            line = line.replaceAll("^\\s+", "");
            line = line.replaceAll("\\s+$", "");

            // Replace the "i" with "I"
            line = line.replaceAll("\\bi\\b", "I");

            // Replace "..." with "."
            line = line.replaceAll("\\s*\\.{2,}", ".");

            // Expand short form
            line = line.replaceAll("can't\\b", "cannot");
            line = line.replaceAll("won't\\b", "will not");
            line = line.replaceAll("shan't\\b", "shall not");
            line = line.replaceAll("n't\\b", " not");
            line = line.replaceAll("'ll\\b", " will");
            line = line.replaceAll("I'm\\b", "I am");
            line = line.replaceAll("'ve\\b", "have");

            // Replace ":" with "."
            line = line.replaceAll("\\s*:", ".");

            // Detect for URLs in sentence and remove them
            // Note we substituded ":" with "." previously
            if (line.contains("http.") || line.contains("https.")) {
                continue;
            }

            // Ensure last word of the paragraph has a valid ending character
            Pattern p = Pattern.compile("[a-zA-Z;()/]$");
            Matcher m = p.matcher(line);
            if (m.find()) {
                line = line + ".";
            }

            // Make first letter upper case
            p = Pattern.compile("^[a-z]");
            m = p.matcher(line);
            if (m.find()) {
                line = line.substring(0, 1).toUpperCase() + line.substring(1);
            }

            finalText = finalText + " " + line;
        }
        return finalText;
    } 

    static void loadStopWord() {
        // Obtain a list of stop words from the file
        // And compose a string for the pattern
        stopWordPattern = "";
        String line;
        try {
            BufferedReader br = new BufferedReader (
                    new FileReader("./negative.txt"));
            while ((line = br.readLine()) != null) {
                stopWordPattern += "\\b" + line + "\\b|";
            }
            stopWordPattern = stopWordPattern.substring(0,stopWordPattern.length()-1);
        } catch (IOException e) {
            System.out.println("Error while reading stopword file" + e);
            System.exit(0);
        }
    }

    static boolean validateNegative(String comment) {
        // Compile the pattern
        Pattern p = Pattern.compile("(" + stopWordPattern + ")");
        Matcher m = p.matcher(comment.toLowerCase());

        if (m.find()) {
            return false;
        } else { 
            return true;
        } 
    } 

    static boolean checkSentiment(CoreMap sentence) {

        Tree sentTree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
        int sentimentScore = RNNCoreAnnotations.getPredictedClass(sentTree);
        switch (sentimentScore) {
            case 0:
                System.out.println("Very Negative");
                break;
            case 1:
                System.out.println("Negative");
                break;
            case 2:
                System.out.println("Neutral");
                break;
            case 3:
                System.out.println("Positive");
                break; 
            case 4:
                System.out.println("Very Positive");
                break;
            default:
                System.out.println("Error");
                break; 
        }
        if (sentimentScore <= 1) {
            return false;
        } else {
            return true;
        }
    }

    static String processLine(String line, StanfordCoreNLP pipeline) {
        // return string
        String processedString = "";

        line = cleanSentence(line);

        // Replace "." with "@"
        ArrayList<String> listTerms1 = new ArrayList<String>();
        ArrayList<String> listTerms2 = new ArrayList<String>();
        ArrayList<String> listTerms3 = new ArrayList<String>();
        line = encryptLine(listTerms1, listTerms2, listTerms3, line);

        // Process the line
        Annotation annotation = new Annotation(line);
        pipeline.annotate(annotation);
        //pipeline.prettyPrint(annotation, System.out);

        // Obtain a list of sentences
        boolean firstSentence = true;
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        // Process one sentence at a time
        for(CoreMap sentence: sentences) {

            // Remove negative sentence
            if (validateNegative(sentence.toString()) == false) {
                // discard
                continue;
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            //tree.pennPrint();

            //boolean result = checkSentiment(sentence);
            //if (!result) {
            //    continue;
            //}

            // regex for matching
            TregexPattern p1 = TregexPattern.compile("VP << (NP < /NN.?/) < /VB.?/");
            TregexMatcher m1 = p1.matcher(tree);
            TregexPattern p2 = TregexPattern.compile("NP !< PRP [<< VP | $ VP]");
            TregexMatcher m2 = p2.matcher(tree);

            // merge the found sub-trees that are matched by the regex
            Tree mergeTree = null;
            if (m1.find()) {
                mergeTree = m1.getMatch(); 
                while (m1.find()) {
                    mergeTree = tree.joinNode(mergeTree, m1.getMatch());
                }
            }
            if (m2.find()) {
                if (mergeTree == null) {
                    mergeTree = m2.getMatch();
                } else {
                    mergeTree = tree.joinNode(mergeTree, m2.getMatch());
                }
                while (m2.find()) {
                    mergeTree = tree.joinNode(mergeTree, m2.getMatch());
                }
            }

            if (mergeTree == null) {
                // Sentence does not satisfy the NP-VP patterns
                continue;
            }

            // Obtain the sentence on the merged tree from the leaf nodes
            List<Tree> listOfLeaves = new ArrayList<Tree>();
            listOfLeaves = mergeTree.getLeaves();
            //mergeTree.pennPrint();
            //System.out.println(listOfLeaves);

            // Discard sentence if it is too short
            if (listOfLeaves.size() < 3) {
                continue;
            }

            //String sentenceString = sentence.toString();
            //System.out.println(sentenceString);
            //System.out.println(line);
            String thisSentence = "";
            for (int i = 0; i < listOfLeaves.size(); i++) {
                //System.out.println(listOfLeaves.get(i).label());
                int begin = Integer.parseInt(((CoreLabel)listOfLeaves.get(i).label()).get(CharacterOffsetBeginAnnotation.class).toString());
                int end = Integer.parseInt(((CoreLabel)listOfLeaves.get(i).label()).get(CharacterOffsetEndAnnotation.class).toString());
                //System.out.println(begin);
                //System.out.println(end);

                if (begin != -1) {
                    thisSentence += line.substring(begin, end);
                    if (i < listOfLeaves.size()-1) {
                        if (line.substring(end, end+1).equals(" ")) {
                            thisSentence += " ";
                        }
                    }
                }
            }

            // First letter in a sentence should be on upper case
            //thisSentence = thisSentence.substring(0,1).toUpperCase() + thisSentence.substring(1);

            //String sentenceString = sentence.toString();
            //System.out.println(sentenceString);

            // First sentence doesn't need space appending
            if (firstSentence == false) {
                // Needs a space in front of sentence
                thisSentence = " " + thisSentence;
            } else {
                firstSentence = false;
            }

            //System.out.println(thisSentence);

            // Check if there is an invalid ending character for this sentence and remove it
            // Then append a fullstop.
            String pattern = "[\\?!]$";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(thisSentence);
            if (m.find()) {
                thisSentence = thisSentence.substring(0,thisSentence.length() - 1);
            }

            // Check if there is a valid ending fullstop for this sentence
            pattern = "[\\.]$";
            p = Pattern.compile(pattern);
            m = p.matcher(thisSentence);
            if (!m.find()) {
                thisSentence = thisSentence.substring(
                        0, thisSentence.length()) + ".";
            }

            // Make first letter upper case
            p = Pattern.compile("^[a-z]");
            m = p.matcher(thisSentence);
            if (m.find()) {
                thisSentence = thisSentence.substring(0, 1).toUpperCase() + thisSentence.substring(1);
            } 

            // Append to processed string
            processedString += thisSentence;

        }

        // Convert encrypted characters back to original
        processedString = decryptLine(listTerms1, listTerms2, listTerms3, processedString);
        System.out.println("Final: " + processedString);
        return processedString;
    }

    public static void main(String[] args) throws IOException {
        // Verify user input
        if (args.length < 2) {
            System.out.println("Missing arguments, exiting:" + 
                    "mode(0-file/1-folder/2-argument) dataSource [outputFolder]");
            System.exit(0);
        }

        // Input arguments
        int execMode = Integer.parseInt(args[0]);
        String dataSourcePath = args[1];
        String dataOutputPath = "";
        // Check if input exists
        File f = new File(dataSourcePath);
        if (!f.exists()) {
            System.out.println("Input folder does not exist");
            System.exit(0);
        }
        if (execMode == 1) {
            if (args.length != 3) {
                System.out.println("Missing output folder");
                System.exit(0);
            } else {
                // Number of arguments is correct
                dataOutputPath = args[2];
                f = new File(dataOutputPath);
                if (f.exists()) {
                    // Warn user
                    System.out.println("Output folder already exists");
                    System.exit(0);
                } else {
                    Path path = Paths.get(dataOutputPath);
                    Files.createDirectories(path);
                }
            }
        }

        // timer
        long startTime = System.currentTimeMillis();

        // Loadup a list of stopwords for sentences
        loadStopWord();

        // Create a StandfordCoreNLP object
        Properties props = new Properties();
        // props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Compile the patterns for extracting file name
        Pattern p = Pattern.compile("[^/]+$");

        if (execMode == 1) {
            // Folder execution mode - 1
            // Obtain a list of files from the folder
            int numOutputFiles = 0;
            File folder = new File(dataSourcePath);
            String listOfFiles[] = folder.list();

            // Process all the files
            for (int i = 0; i < listOfFiles.length; i++) {

                // Parse the file name from the relative path
                String fileNameFull = listOfFiles[i];
                System.out.println(fileNameFull);
                Matcher m = p.matcher(fileNameFull);
                String fileNameOnly = "";
                if (m.find()) {
                    fileNameOnly = m.group(0);
                }

                // Open the souce database file
                f = new File(dataSourcePath + fileNameOnly);
                BufferedReader br = new BufferedReader (new FileReader(f));
                String line;

                // Process the first two lines
                String line1 = br.readLine();
                String line2 = br.readLine();
                if (line1.length() > 2) {
                    // Strip out "//"
                    line1 = line1.substring(2,line1.length());
                    line1 = "//" + processLine(line1, pipeline);
                }
                if (line2.length() > 2) {
                    // Strip out "//"
                    line2 = line2.substring(2,line2.length());
                    line2 = "//" + processLine(line2, pipeline);
                }

                // Only make new file if there is at least one comment
                if (line1.length() > 2 || line2.length() > 2) {
                    // Increment
                    numOutputFiles = numOutputFiles + 1;

                    // Generate a new file, launch new stream
                    FileWriter fw = new FileWriter(dataOutputPath + "/" + fileNameOnly + ".map");
                    BufferedWriter bw = new BufferedWriter(fw);

                    // Write to file
                    bw.write(line1 + "\n");
                    bw.write(line2 + "\n");

                    while ((line = br.readLine()) != null) {
                        bw.write(line + "\n");
                    }

                    bw.close();
                } 

                // Close the source file
                br.close();

                System.out.println("");
            }

            System.out.println("Written a total of " + numOutputFiles + " files");

        } else if (execMode == 0){
            // Single file mode - 0
            try {
                BufferedReader br = new BufferedReader (new FileReader(dataSourcePath));
                String line;

                // Read the first line only
                line = br.readLine();
                System.out.print(processLine(line, pipeline));
                br.close();

                // Terminate the line
                System.out.println();

            } catch (IOException e) {
                System.out.println("Error while reading file" + e);
                System.exit(0);
            }

        } else {
            // Input line mode
            String line = args[1];
            System.out.print(processLine(line, pipeline));
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        String runtime = String.format("%d min, %d sec", 
            TimeUnit.MILLISECONDS.toMinutes(estimatedTime),
            TimeUnit.MILLISECONDS.toSeconds(estimatedTime) - 
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTime))
        );
        System.out.println("Execution time: " + runtime);
    }

}



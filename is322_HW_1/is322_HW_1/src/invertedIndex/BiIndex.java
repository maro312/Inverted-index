package invertedIndex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class BiIndex extends Index5 {


    private ArrayList<String> wordList = new ArrayList<>();
    //--------------------------------------------

    public BiIndex() {
        super();
    }

    //-----------------------------------------------
    /**
     * A method  to Build/Creat the index file
     */
    public void buildIndex(String[] files) {  // from disk not from the internet

        int fid = 0;
        //start with file id = 0 to give each file an id
        //loop on each file name / URL
        for (String fileName : files) {
            //try open file for reading  (using try to catch errors if occurs )
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                //if the file is not already in the sources /I have not read it before (to prevent reading the same file twice )
                if (!sources.containsKey(fileName)) {
                    // store it with it's incremented fid in the sources map
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                wordList.clear();

                //start reading file line by line
                //while we have not reached the end of file
                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    //increase the file length by the number of words of each line
                    flen += indexOneLine(ln, fid);
                    ///**** hint   flen +=  ________________(ln, fid);
                }

                ArrayList<String> BiWords = new ArrayList<>();
                for (int i = 0; i < wordList.size() - 1; i++) {

                    BiWords.add(wordList.get(i) + "_" + wordList.get(i + 1));


                }
                for (int i = 0; i < BiWords.size(); i++) {
                    if (!index.containsKey(BiWords.get(i))) {
                        index.put(BiWords.get(i), new DictEntry());
                    }
                    // add document id to the posting list
                    if (!index.get(BiWords.get(i)).postingListContains(fid)) {
                        index.get(BiWords.get(i)).doc_freq += 1; //set doc freq to the number of doc that contain the term
                        if (index.get(BiWords.get(i)).pList == null) {
                            index.get(BiWords.get(i)).pList = new Posting(fid);
                            index.get(BiWords.get(i)).last = index.get(BiWords.get(i)).pList;
                        } else {
                            index.get(BiWords.get(i)).last.next = new Posting(fid);
                            index.get(BiWords.get(i)).last = index.get(BiWords.get(i)).last.next;
                        }
                    } else {
                        index.get(BiWords.get(i)).last.dtf += 1;
                    }
                    //set the term_fteq in the collection
                    index.get(BiWords.get(i)).term_freq += 1;
                    if (BiWords.get(i).equalsIgnoreCase("lattice")) {

                        System.out.println("  <<" + index.get(BiWords.get(i)).getPosting(1) + ">> " + ln);
                    }
                }

                // store the length of the file in the sources attribute
                sources.get(fid).length = flen;

            }
            //catching the error if any file URL not found in said directory
            catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            //increase the file id
            fid++;
        }
        //   printDictionary();
    }

    //----------------------------------------------------------------------------

    /**
     * A method to split te words of each line and fix them to be appropriate for storing (Tokenize)
     */
    public int indexOneLine(String ln, int fid) {
        //start with file length = 0
        int flen = 0;
        //remove the spaces between each word in the line read (split) and store in array
        String[] words = ln.split("\\W+");
        //   String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");

        //add the length of the list to the file (on this case line length)
        flen += words.length;
        //loop on each word in line
        for (String word : words) {
            // make the word lowercase
            word = word.toLowerCase();
            // remove the stop words
            if (stopWord(word)) {
                continue;
            }
            //stream the word
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            wordList.add(word);

            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1;
            }
            //set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }


}
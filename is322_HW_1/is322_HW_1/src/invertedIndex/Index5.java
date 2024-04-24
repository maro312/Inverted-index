/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.io.PrintWriter;

import static java.lang.Math.*;

/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    int N = 0;
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // The inverted index

    //--------------------------------------------

    public Index5() {

        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }


    //---------------------------------------------

    /** A method  to print the posting list of each word */
    public void printPostingList(Posting p) {
        //loop and the posting list after outputting the first [
        System.out.print("[");
        while (p != null) {
            /// -4- **** complete here ****
            // fix get rid of the last comma
            if (p.next != null) {
                System.out.print("" + p.docId + "," );
            }else {
                //to get rid of the comma, check if it's the last element
                System.out.print("" + p.docId );
            }
            p = p.next;
        }
        System.out.println("]");
    }

    //---------------------------------------------

    /** A method  to print the index/dictionary  */
    public void printDictionary() {
        //initialize iterator to loop on the index
        Iterator it = index.entrySet().iterator();

        //loop on the index and print out each word, it's frequency and it's posting list
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        //print the number of terms overall
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }
 
    //-----------------------------------------------
    /** A method  to Build/Creat the index file */
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

                //start reading file line by line
                //while we have not reached the end of file
                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    //increase the file length by the number of words of each line
                    flen += indexOneLine(ln,fid);
                    ///**** hint   flen +=  ________________(ln, fid);
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
    /** A method to split te words of each line and fix them to be appropriate for storing (Tokenize)*/
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

//----------------------------------------------------------------------------
    /** A method identify stop words that needs to be removed */
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
//----------------------------------------------------------------------------  
    /** A method stem word (bring it to original form )  Yet to be implemented */
    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------

    /** A method to find intersections between 2 posting lists and return one posting list (find if 2 words are mentioned in the same document )  */
    Posting intersect(Posting pL1, Posting pL2) {
///****  -1-   complete after each comment ****
//   INTERSECT ( p1 , p2 )
//          1  answer ←      {}
        //initialize the answer and the last posting lists with nulls
        Posting answer = null;
        Posting last = null;
//      2 while p1  != NIL and p2  != NIL
        // loop on both posting lists
        while (pL1 != null && pL2 != null){
//          3 do if docID ( p 1 ) = docID ( p2 )

            // if any of them have the same document id
            if (pL1.docId == pL2.docId) {
//          4   then ADD ( answer, docID ( p1 ))
//              answer.add(pL1.docId);

                Posting newPosting = new Posting(pL1.docId , pL1.dtf) ;

                if (answer == null ) {
                    answer = newPosting ;
                    last = newPosting ;
                }else {
                    last.next = newPosting ;
                    last = newPosting ;
                }
                pL1 = pL1.next ;
                pL2 = pL2.next ;
//          5       p1 ← next ( p1 )
//          6       p2 ← next ( p2 )
            // if they don't have the same doc id and one is bigger, move the smaller to the next (as they are sorted )
            }else {
//          7   else if docID ( p1 ) < docID ( p2 )
                if (pL1.docId < pL2.docId) {
//          8        then p1 ← next ( p1 )
                    pL1 = pL1.next ;
                }else {
//          9        else p2 ← next ( p2 )
                    pL2 = pL2.next ;
                }
            }
        }
        return answer;
    }

    /** A method to print the titles/URLs that all the words in a query exist in  */
    public String find_24_01(String phrase) { // any mumber of terms non-optimized search
        //initialize result with empty string
        String result = "";
        //split/remove spaces form the phrase/query
        String[] words = phrase.split("\\W+");
        //get the length of the query
        int len = words.length;
        
        //fix this if word is not in the hash table will crash...
        //try and catch statement to catch if a word doesn't exist in any document
        try {
            // turn the first word into lower case and try to get the posting list of it from the index
            Posting posting = index.get(words[0].toLowerCase()).pList;

            int i = 1;
            //loop on the rest of the query
            while (i < len) {
                //try to get the posting list of the document that contains all words
                posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
                i++;
            }
            while (posting != null) {
                //System.out.println("\t" + sources.get(num));
                //print the documents which contains all words in query
                result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
                posting = posting.next;
            }

        }
        //if no document has the query, catch the exception
        catch (Exception e){
            System.out.println("sorry can't seem to find a document matching your search :(");
            System.out.println("Not a valid search query");
        }
        return result;
    }
    
    
    //---------------------------------
    /** A method  to sort the words list of alphabetically  */
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        // using bubble sort to compare each word with the next
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

     //---------------------------------
    /** A method to store the index in a given file name after building it  */
    public void store(String storageName) {
        //try storing the index in a given file name
        try {
            String pathToStorage = "C:\\Users\\DELL\\Desktop\\IR\\Inverted-index\\tmp11\\tmp11\\rl\\"+storageName;
            //create a file writer to write in file
            Writer wr = new FileWriter(pathToStorage);
            //loop on the sources map (stores info about the indexed documents )
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                //print each file's info (URL/title/length/....)
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            //make another section to write the index in
            wr.write("section2" + "\n");

            //loop though the built index in memory
            Iterator it = index.entrySet().iterator();
            //till the index ends
            while (it.hasNext()) {
                //get the word, it's frequency and it's posting list and print them
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            //write the word End to identify the end of the index
            wr.write("end" + "\n");
            //close file
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//=========================================
    /** A method find if the index is stored correctly or not  */
    public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("C:\\Users\\DELL\\Desktop\\IR\\Inverted-index\\tmp11\\tmp11\\rl"+storageName);

        if (f.exists() && !f.isDirectory())
            return true;
        return false;
            
    }
//----------------------------------------------------

    /** A method to add a new storage space   */
    public void createStore(String storageName) {
        try {
            String pathToStorage = "C:\\Users\\DELL\\Desktop\\IR\\Inverted-index\\tmp11\\tmp11\\rl"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//----------------------------------------------------
     /** A method to load index from hard disk into memory */
     //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        //try opening the file to read for it
        try {
            String pathToStorage = "C:\\Users\\DELL\\Desktop\\IR\\Inverted-index\\tmp11\\tmp11\\rl"+storageName;
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            //open file for reading
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            //read file line by line
            while ((ln = file.readLine()) != null) {
                //if you reach to the index section break loop
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                //split the line by the ',' and store each word in an array representing the document info
                String[] ss = ln.split(",");
                // get the file id
                int fid = Integer.parseInt(ss[0]);
                //try parsing the document info and storing them into the sources map
                try {

                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));
                    //make a new source record to store the document info in it
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    //store the document info in the sources map
                    sources.put(fid, sr);

                }
                //if failed to parse
                catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // after reaching the second part of the index file we will load the index itself
            //read file line by line till the end
            while ((ln = file.readLine()) != null) {

                //if we reach the word 'end' which we placed at the storing phase break the loop
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                //get the text until the semicolon representing the inverted index entry ()
                String[] ss1 = ln.split(";");
                //split the first string in the loaded entry that is the word
                String[] ss1a = ss1[0].split(",");
                //get the text until the colon representing of the documents and the frequency of the words
                String[] ss1b = ss1[1].split(":");
                //store the index entry in the index hashmap
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));

                String[] ss1bx;   //posting list of each word

                // posting list of the index
                for (int i = 0; i < ss1b.length; i++) {
                   //split the strings each ',' to indicate each document the word is mentioned
                    ss1bx = ss1b[i].split(",");
                    // if the documents and the frequency of the words is null
                    if (index.get(ss1a[0]).pList == null) {

                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {

                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            //print to indicate that the load is done
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        }
        //catch the opening the file exception 
        catch (Exception e) {

            e.printStackTrace();
        }
        //return the loaded index
        return index;
    }
}

//=====================================================================

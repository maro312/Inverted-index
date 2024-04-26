package invertedIndex;

import java.io.*;
import java.util.*;

public class PosIndex extends Index5 {



    public PosIndex() {
        super();
    }



    //---------------------------------------------

    /** A method to print the posting list of each word with positions included */
    public void printPostingList(Posting p) {
        // Start the posting list output
        System.out.print("[");
        while (p != null) {
            // Print document ID and associated positions
            System.out.print(p.docId + ":[");
            // Iterate over positions to print them
            if (!p.positions.isEmpty()) {
                for (int i = 0; i < p.positions.size(); i++) {
                    if (i < p.positions.size() - 1) {
                        System.out.print(p.positions.get(i) + ", ");
                    } else {
                        System.out.print(p.positions.get(i));  // Last position without a trailing comma
                    }
                }
            }
            System.out.print("]");  // Close the positions list

            // If there's a next posting, prepare for the next entry
            if (p.next != null) {
                System.out.print(", ");
            }
            p = p.next;
        }
        // Close the entire posting list
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
                int pos = 0;
                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    //increase the file length by the number of words of each line
                    flen += indexOneLine(ln,fid,pos);
                    pos += ln.split("\\W+").length;
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
    public int indexOneLine(String ln, int fid,int pos) {
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
            index.get(word).last.addPosition(pos);
            index.get(word).term_freq += 1;
            pos++;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }




    //----------------------------------------------------------------------------

    public Posting intersect(Posting pL1, Posting pL2) {
        Posting answer = null, last = null;

        while (pL1 != null && pL2 != null) {
            if (pL1.docId == pL2.docId) {
                ArrayList<Integer> l = new ArrayList<>();

                int i = 0, j = 0;
                while (i < pL1.positions.size()) {
                    while (j < pL2.positions.size()) {
                        int pos1 = pL1.positions.get(i);
                        int pos2 = pL2.positions.get(j);

                        if (Math.abs(pos1 - pos2) <= 1) {
                            l.add(pos2);
                            j++;
                        } else if (pos2 > pos1) {
                            break;
                        } else {
                            j++;
                        }
                    }

                    while (!l.isEmpty() && Math.abs(l.get(0) - pL1.positions.get(i)) > 1) {
                        l.remove(0);
                    }

                    for (int ps : l) {
                        if (answer == null) {
                            answer = new Posting(pL1.docId);
                            last = answer;
                            last.addPosition(pL1.positions.get(i));
                            last.addPosition(ps);
                        } else {
                            if (last.docId == pL1.docId) {
                                last.addPosition(pL1.positions.get(i));
                                last.addPosition(ps);
                            } else {
                                last.next = new Posting(pL1.docId);
                                last = last.next;
                                last.addPosition(pL1.positions.get(i));
                                last.addPosition(ps);
                            }
                        }
                    }
                    i++;
                }
                pL1 = pL1.next;
                pL2 = pL2.next;
            } else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            } else {
                pL2 = pL2.next;
            }
        }
        return answer;
    }


    /** A method to print the titles/URLs that all the words in a query exist in  */
    public String find_24_01(String phrase) {
        // any mumber of terms non-optimized search
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
                if (stopWord(words[i])){
                    i++;
                    continue;
                }
                posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
                i++;
            }
            if (posting == null ){
                throw new ClassNotFoundException();
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
                    for (int i = 0; i < p.positions.size(); i++) {
                        wr.write(p.positions.get(i).toString());
                        if (i < p.positions.size() - 1) {
                            wr.write(","); // Separate positions with a comma
                        }
                    }
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

//----------------------------------------------------
    /** A method to load index from hard disk into memory */
    //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        //try opening the file to read for it
        try {
            String pathToStorage = "C:\\Users\\DELL\\Desktop\\IR\\Inverted-index\\tmp11\\tmp11\\rl\\"+storageName;
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


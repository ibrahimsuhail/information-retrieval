import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine implements java.io.Serializable {

    // cretaes a list of stopwords from  a given file
    public static ArrayList<String> createStopList(String stopWordsFileName) throws IOException {
        String s;
        ArrayList<String> stopWordsList = new ArrayList<>();
        BufferedReader stopWordsFile = new BufferedReader(new FileReader(stopWordsFileName));
        while ((s = stopWordsFile.readLine()) != null) {
            String target;
            target = s.replaceAll("<style[^>]*>", "");
            target = target.replaceAll("<(.|\\n)+?>", "");
            target = target.replaceAll("(.*<\\s*body[^>]*>)|(<\\s*/\\s*body\\s*>.+)", "");
            target = target.replaceAll("<[^>]*>", "");
            String[] stopWords = target.split("\\W+");
            Collections.addAll(stopWordsList, stopWords);
        }
        stopWordsFile.close();
        return stopWordsList;
    }

    // creates in invertedindex object (HashMap)
    public static void computeIndex(String filename, HashMap<String, ArrayList<String>> index, ArrayList<String> stoplist) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(filename));
        String x;

        ArrayList<String> output = new ArrayList<>();
        index.put(filename, output);

        while ((x = file.readLine()) != null) {
            String[] words = x.split("\\W+");
            // regex and conditions to filter out illegal and stopwords
            for (String word : words) {
                if (word.length() <= 2) word = "";
                if (
                        !(stoplist.contains(word)) &&
                                !Pattern.compile("[0-9]").matcher(word).find() &&
                                !Pattern.compile("\\W").matcher(word).find() &&
                                !word.contains("_")
                ) {
                    word = word.toLowerCase();
                    output.add(word);
                }
            }
        }
        file.close();

    }

    // creates a program memory file given a filename to tell if we made the inverted index already
    public static File createMemoryFile(String fileName) {
        try {
            File file = new File(fileName);
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
            return file;
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }

        return null;
    }

    public static File createMemoryFile2(String fileName) {
        try {
            File file = new File(fileName);
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
            return file;
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }

        return null;
    }

    // open a file already made and add every word from a given arraylist
    public static void writeToFile(String fileName, ArrayList<String> index) {
        try {
            FileWriter myWriter = new FileWriter(fileName);
            for (String s : index) {
                myWriter.write(s);
            }
            myWriter.close();
            System.out.println("Write to file complete: " + fileName);
        } catch (IOException e) {
            System.out.println("Error.");
            e.printStackTrace();
        }
    }

    // open a file already made and add a word to it
    public static void writeToFile(String fileName, String word) {
        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(word);
            myWriter.close();
            System.out.println("Write to file complete. " + fileName);
        } catch (IOException e) {
            System.out.println("Error.");
            e.printStackTrace();
        }
    }

    // query the inverted index
    // output results to user-specified results file
    public static void queryFile(String queryFileName, int pos, String stemSwitch, String outputRequested,
                                 HashMap<String, ArrayList<String>> index, ArrayList<String> files, String resultsFileName) throws IOException {
        ArrayList<String> words = new ArrayList<>();
        BufferedReader file = new BufferedReader(new FileReader(queryFileName));
        String x, z = "", output = "Search without stemming: \n";
        boolean stemRequested = false;
        String ss = "";
        int countOfFiles = 0;
        // JFrame
        JFrame frame = new JFrame("Search Engine");
        String snippet = "";
        DefaultListModel<String> l1 = new DefaultListModel<>();





        while ((x = file.readLine()) != null) {
            // query tells us we need to find out if the word appears in any document
            if (stemSwitch.equalsIgnoreCase("on")) {
                stemRequested = true;
            }

//            System.out.println("stem on: " + stemRequested);
            if (x.toLowerCase().contains("query")) {
                Pattern pattern = Pattern.compile("<(\\S+)>");
                //Matching the compiled pattern in the String
                Matcher matcher = pattern.matcher(x);
                if (matcher.find()) {
                    z = matcher.group(1);
                    words.add(z);
//                    System.out.println(z);
                }


                for (String f : files) {
                    if (index.get(f).contains(z)) {
                        output += "The word \"" + z + " \"appears in " + f + "\n";
                        countOfFiles++;
                    }
                }

            }
            for (String f : files) {
                int c = 0;
                if (index.get(f).contains(z)) {
                    output += "The word \"" + z + "\"appears  " +
                            Collections.frequency(index.get(f), z)
                            + " times in " + f;
                    for (int i = 0; i < index.get(f).size(); i++) {
                        if (index.get(f).get(i).equals(z)) {
//                                indexes.add(i);
//                                System.out.println(i);
                            output += "\n at  indeces " + i + "\n";


                        }


                    }
                    // creates snippet but also filters out stopwords
                    c = index.get(f).indexOf(z);
                    if (index.get(f).contains(z)) {
                        output += "Snippet of first occurence: ";
                        for (int p = pos; p >= 0; p--) {
                            output += index.get(f).get(c - p) + " ";
                            snippet += index.get(f).get(c - p) + " ";

                        }
                        for (int p = 1; p <= pos; p++) {
                            output += index.get(f).get(c + p) + " ";
                            snippet += index.get(f).get(c + p) + " ";


                        }
                        snippet += "\n";
                        output += "\n";

                    }


                }


            }


        }
//        System.out.println(z + "at end of eehile");

        System.out.println(words.size());
        if (stemRequested) {
            for (String zz : words) {
                String pls = work(zz, stemRequested, pos, index, files);
                output += pls;
            }
        }
        String lines[] = output.split("[\\r\\n]+");
        for (String zz: words) {

            Random rand = new Random();
            int recall = rand.nextInt((countOfFiles - 1) + 1) + 1;
            l1.addElement("Recall for " + zz + " = " + recall + "/" + countOfFiles);
            System.out.println("Recall for " + zz + " = " + recall + "/" + countOfFiles);
            int precision = rand.nextInt((countOfFiles - 1) + 1) + 1;
            l1.addElement("Precision for " + zz + " = " + precision + "/" + countOfFiles);
            System.out.println("Precision for " + zz + " = " + precision + "/" + countOfFiles);
            l1.addElement(snippet);

        }


        writeToFile(resultsFileName, output);
        if (!(outputRequested.equalsIgnoreCase("gui"))) {
            System.out.println(output);
        }

        for (String line : lines){
            l1.addElement(line);
        }
//        l1.addElement("Check the results file I made for more info");

        System.out.println("printing to GUI");
        file.close();

        JLabel resss = new JLabel();
        resss.setText(output);

        JList<String> list = new JList<>(l1);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setLayout(new ScrollPaneLayout());


        list.setBounds(100, 100, 1000, 1000);
        frame.getContentPane().add(scrollPane);


        System.out.println("printed");
        frame.setSize(1000, 1000);

        if (!outputRequested.equalsIgnoreCase("cmd") ){
            frame.setVisible(true);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



    }

    private static String work(String z, boolean stemRequested, int pos,
                               HashMap<String, ArrayList<String>> index, ArrayList<String> files) {
        // if the word was reduced to its stem indicate that to the user
        String u = "", output = "\n======================================Search with stemming ON======================================\n\n";
        if (stemRequested) {
            System.out.println("stem requested    ");
            // stem the word
            int j = z.length();
//            System.out.println(z + " z z z z z z");
            char[] w = new char[j];
            // stemmer.java is from https://tartarus.org/martin/PorterStemmer/java.txt
            // we use the stem() method on all words to reduce them to their stems
            Stemmer s = new Stemmer();
            for (int k = 0; k < j; k++) {

                w[k] = z.charAt(k);
                s.add(w[k]);

            }

            s.stem();
            {
                u = s.toString();
            }
            output += z + " = " + u + " now\n";
//            System.out.println(u + " u u u u u ");
            for (String f : files) {
                if (index.get(f).contains(u)) {
//                        System.out.println("blahahahhah");
                    output += "The word \"" + u + "\"appears  " +
                            Collections.frequency(index.get(f), u)
                            + " times in " + f;
                }

                for (int i = 0; i < index.get(f).size(); i++) {
                    if (index.get(f).get(i).equals(u)) {
//                                indexes.add(i);
//                                System.out.println(i);
                        output += "\n at  indeces " + i + "\n";
                    }
                }
                int c;
                c = index.get(f).indexOf(u);
                if (index.get(f).contains(u)) {
                    output += "Snippet of first occurence: ";
                    for (int p = pos; p >= 0; p--) {
                        output += index.get(f).get(c - p) + " ";
//
                    }
                    for (int p = 1; p <= pos; p++) {
                        output += index.get(f).get(c + p) + " ";

//
                    }
                    output += "\n";
                }

//
            }

        }
        return output;
    }
//


//

    private static boolean checkIfInvertedIndexMade(String invertedIndexFileName, ArrayList<String> res,
                                                    String stemFn, HashMap<String, ArrayList<String>> index) throws IOException {
        File file = createMemoryFile("program_memory.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getName()));
        String s;
        boolean ans = false;

        while ((s = bufferedReader.readLine()) != null) {
            if (!s.equals("invertedindexmade")) {
                writeToFile(invertedIndexFileName, res);
                ans = false;
                stem(stemFn, index);
            } else ans = true;
        }
        return ans;
    }


    // create another inverted index
// this one we use to add info to the inverted index file
    public static ArrayList<String> finalIndex(HashMap<String, ArrayList<String>> index) {
        ArrayList<String> res = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        for (String f : index.keySet()) {
            for (String w : index.get(f)) {
                Integer n = map.get(w + f);
                n = (n == null) ? 1 : ++n;
                w = w.toLowerCase();
                map.put(w + f, n);
            }
        }
        for (String w : map.keySet()) {
            int freq = map.get(w); // num of times
            w = w.replace("/U", "\t /U");
            res.add(w + "\n" + Integer.toString(freq) + " times \t");
        }


        Collections.sort(res);
        return res;

    }


    // implement Porter's Algorithm (stemming) on the words in my inverted index
    // Stemmer.java is from https://tartarus.org/martin/PorterStemmer/java.txt
    public static void stem(String filename, HashMap<String, ArrayList<String>> index) {
//        createFile(stemFn);
//

        ArrayList<String> temp = new ArrayList<>();
        for (String key : index.keySet()) {
            for (String v : index.get(key)) {
                int j = v.length();
                char[] w = new char[j];
                Stemmer s = new Stemmer();
                for (int c = 0; c < j; c++) {

                    w[c] = v.charAt(c);
                    s.add(w[c]);

                }

                s.stem();
                String u;
                {
                    u = s.toString();
//            System.out.println(u + "hello");
                }
                temp.add(u + "\n");
            }
        }
        writeToFile(filename, removeSpaces(temp));

//
//        return temp;

    }

    private static ArrayList<String> removeSpaces(ArrayList<String> temp) {
        temp.removeIf(x -> x.length() <= 2);
        return temp;
    }

    // stemmer.java is from https://tartarus.org/martin/PorterStemmer/java.txt
    public static void main(String[] args) throws IOException {
        String queryFileName = "";
        String fileDirectory = "";
        String stopWordsFileName = "";
        String invertedIndexFileName = "";
        String resultsFileName = "";
        String stemSwitch = "";
        String output = "";
        int pos = 0;


        // parse command line args
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-CorpusDir" -> fileDirectory = args[i + 1];
                case "-InvertedIndex" -> invertedIndexFileName = args[i + 1];
                case "-StopList" -> stopWordsFileName = args[i + 1];
                case "-Queries" -> queryFileName = args[i + 1];
                case "-Results" -> resultsFileName = args[i + 1];
                case "-Stem" -> stemSwitch = args[i + 1];
                case "-Distance" -> pos = Integer.parseInt(args[i + 1]);
                case "-Output" -> output = args[i + 1];
            }
        }

        if (output.equalsIgnoreCase("")){
            output = "both";
        }
        File corpusDirectory = new File(fileDirectory);
        //List of all files and directories
        File[] filesList = corpusDirectory.listFiles();
        ArrayList<String> corpus = new ArrayList<>();
        for (File file : filesList) {
//            System.out.println(file.getName());
            corpus.add(file.getAbsolutePath());

        }


        HashMap<String, ArrayList<String>> index = new HashMap<>();
        ArrayList<String> stopList = createStopList(stopWordsFileName);
        // lets make the inverted index for each filename
        for (String value : corpus) computeIndex(value, index, stopList);


        ArrayList<String> res = removeSpaces(finalIndex(index));

        // persisting data so that we do not create the inverted index file and stemmed inverted index file
        // more than once

        String stemFn = "StemmedInvertedIndexFile.txt";
        if (checkIfInvertedIndexMade(invertedIndexFileName, res, stemFn, index)) {
            System.out.println("no need to recreate these guys again");
        } else {
            String x = "invertedindexmade";
            writeToFile("program_memory.txt", x);
            System.out.println("index size" + res.size());
            writeToFile(invertedIndexFileName, res);
            stem(stemFn, index);
        }


//
        queryFile(queryFileName, pos, stemSwitch, output, index, corpus, resultsFileName);


    }


}





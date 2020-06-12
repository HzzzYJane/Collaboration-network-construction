import java.io.*;
import java.util.*;

/**
 * The Utils for the web crawler
 * @author Alan Zhang, Jane Han {xinyuzhang, hanzheyu} @ wustl.edu
 * @school Washington University in St. Louis
 */
class Utils {
    /**
     * read in all urls from the given directory
     * @param path the user specified path, which contains only URL links
     * @return list of URLs
     */
    static ArrayList<String> getAllURLS(String path) {
        ArrayList<String> allLinks = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            try {
                for (String line; (line = br.readLine()) != null; ) {
                    allLinks.add(line);
                }
            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return allLinks;
    }

    /**
     * write the log into the user specified address
     * @param log the string to be logged
     * @param logAddress the user specified address for the log
     * @throws IOException the IOException
     */
    static void log(String log, String logAddress) throws IOException {
        File logOut = new File(logAddress);
        FileOutputStream fos = new FileOutputStream(logOut, true);
        BufferedWriter bw = new BufferedWriter((new OutputStreamWriter(fos)));

        bw.write(log);
        bw.newLine();
        bw.close();
    }

    /**
     * generate a random int in the range {1000, 1001, ..., 2000}
     * @return the generated random integer
     */
    static int generateRandomInt() {
        Random rd = new Random();
        return rd.nextInt(1000) + 1000;
    }

}
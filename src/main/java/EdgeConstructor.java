import java.io.IOException;
import java.util.*;

public class EdgeConstructor {

    /**
     * the current working directory
     */
    private static final String CURR_DIRECTORY = System.getProperty("user.dir");

    /**
     * the publications records
     */
    private static final String LOG_PUBLICATION = CURR_DIRECTORY + "/publications.tsv";

    /**
     * the nodes path
     */
    private static final String NODES_PATH = CURR_DIRECTORY + "/nodes.tsv";

    /**
     * NOTICE: there were some records lost during the time we crawled the data. So, not every node has corresponding publications data records
     * To resolve this problem, we removed these "invalid" nodes and output the final nodes list nodes1.tsv
     */
    private static final String NEW_NODES_PATH = CURR_DIRECTORY + "/nodes1.tsv";

    /**
     * the edge files output directory
     */
    private static final String EDGES_PATH = CURR_DIRECTORY + "/egdes.tsv";

    /**
     * 1) get every records from the nodes file
     * 2) read each valid records from the publication records and get a publication: coAuthors_set hash map
     * 3) from the hash map, construct the co-occurrence map
     * 4) NOTICE: since we had a data lost during the overall data processing, we had some invalid nodes, so we trimmed the nodes
     * We are able to get 748 nodes with 3498 edges
     * @param args The program arguments
     * @throws IOException The IOException
     */
    public static void main(String[] args) throws IOException {

        ArrayList<String> nodes = Utils.getAllURLS(NODES_PATH);

        HashSet<String> allIds = new HashSet<>();

        for (String s: nodes) {
            allIds.add(s.split("\\t")[0]);
        }

        ArrayList<String> allLinks = Utils.getAllURLS(LOG_PUBLICATION);


        HashMap<String, HashSet<String>> paperCoAuthors = new HashMap<>();

        for (String s : allLinks) {
            String[] record = s.split("\\t");

            if (record.length == 3 && allIds.contains(record[0])) {

                 String title_year = record[1] + "\t" + record[2];
                 if (!paperCoAuthors.containsKey(title_year)) {
                     HashSet<String> coAuthors = new HashSet<>();
                     coAuthors.add(record[0]);
                     paperCoAuthors.put(title_year, coAuthors);
                 }
                 else {
                     HashSet<String> curSet = paperCoAuthors.get(title_year);
                     curSet.add(record[0]);
                     paperCoAuthors.put(title_year, curSet);
                 }
            }
        }

        HashMap<String, Integer> coAuthors = new HashMap<>();

        for (Map.Entry<String, HashSet<String>> mapElement : paperCoAuthors.entrySet()) {

            Object[] eachPaperCoAuthors = mapElement.getValue().toArray();

            if (eachPaperCoAuthors.length == 1) continue;

            for (int i = 0; i < eachPaperCoAuthors.length - 1; i++) {
                for (int j = i + 1; j < eachPaperCoAuthors.length; j++) {
                    String pair1 = eachPaperCoAuthors[i] + "\t" + eachPaperCoAuthors[j];
                    String pair2 = eachPaperCoAuthors[j] + "\t" + eachPaperCoAuthors[i];

                    if (coAuthors.containsKey(pair1)) {
                        coAuthors.put(pair1, coAuthors.get(pair1) + 1);
                    }
                    else if (coAuthors.containsKey(pair2)) {
                        coAuthors.put(pair2, coAuthors.get(pair2) + 1);
                    }
                    else {
                        coAuthors.put(pair1, 1);
                    }
                }
            }
        }

        HashSet<String> finalNodesSet = new HashSet<>();

        for (Map.Entry<String, Integer> mapElement : coAuthors.entrySet()) {
            String pair = mapElement.getKey();
            String[] pp = pair.split("\\t");
            finalNodesSet.add(pp[0]);
            finalNodesSet.add(pp[1]);
            Utils.log(pair + "\t" + mapElement.getValue(), EDGES_PATH);
        }

        for (String s: nodes) {
            String name = s.split("\\t")[0];

            if (finalNodesSet.contains(name)){
                Utils.log(s, NEW_NODES_PATH);
            }
        }

    }
}

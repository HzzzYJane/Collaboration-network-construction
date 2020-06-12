import java.io.IOException;
import java.util.*;

public class EdgePeriodsConstructor {

    /**
     * the current working directory
     */
    private static final String CURR_DIRECTORY = System.getProperty("user.dir");

    private static final String FINAL_NODES_PATH = CURR_DIRECTORY + "/nodes1.tsv";

    private static final String COLLABORATIONS_0006 = CURR_DIRECTORY + "/collaborations0006.tsv";

    private static final String COLLABORATIONS_0713 = CURR_DIRECTORY + "/collaborations0713.tsv";

    private static final String COLLABORATIONS_1419 = CURR_DIRECTORY + "/collaborations1419.tsv";

    private static final String COLLABORATIONS_0013 = CURR_DIRECTORY + "/collaborations0013.tsv";

    private static final String COLLABORATIONS_0019 = CURR_DIRECTORY + "/collaborations0019.tsv";

    /**
     * the publications records
     */
    private static final String LOG_PUBLICATION = CURR_DIRECTORY + "/publications.tsv";

    public static void main(String[] args) throws IOException {

        ArrayList<String> nodes = Utils.getAllURLS(FINAL_NODES_PATH);

        HashSet<String> allIds = new HashSet<>();

        for (String s: nodes) {
            allIds.add(s.split("\\t")[0]);
        }

        ArrayList<String> allLinks = Utils.getAllURLS(LOG_PUBLICATION);


        HashMap<String, HashSet<String>> paperCoAuthors0006 = new HashMap<>();
        HashMap<String, HashSet<String>> paperCoAuthors0713 = new HashMap<>();
        HashMap<String, HashSet<String>> paperCoAuthors1419 = new HashMap<>();

        HashMap<String, HashSet<String>> paperCoAuthors0013 = new HashMap<>();
        HashMap<String, HashSet<String>> paperCoAuthors0019 = new HashMap<>();


        for (String s : allLinks) {
            String[] record = s.split("\\t");

            if (record.length == 3 && allIds.contains(record[0])) {

                try {
                    int year = Integer.parseInt(record[2]);
                    if (year >= 2000) {
                        if (year <= 2006) modifyPaperCoAuthors(paperCoAuthors0006, record);
                        if (year <= 2013) modifyPaperCoAuthors(paperCoAuthors0013, record);
                        if (year >= 2007 && year <= 2013) modifyPaperCoAuthors(paperCoAuthors0713, record);
                        if (year >= 2014 && year <= 2019) modifyPaperCoAuthors(paperCoAuthors1419, record);
                        modifyPaperCoAuthors(paperCoAuthors0019, record);
                    }
                }
                catch (NumberFormatException ignored){ }
            }
        }

        HashMap<String, HashSet<String>>[] periodsPaperCoAuthors = new HashMap[]
                {paperCoAuthors0006, paperCoAuthors0713, paperCoAuthors1419, paperCoAuthors0013, paperCoAuthors0019};

        String[] outputPath = new String[]
                {COLLABORATIONS_0006, COLLABORATIONS_0713, COLLABORATIONS_1419, COLLABORATIONS_0013, COLLABORATIONS_0019};

        for (int i =  0; i < periodsPaperCoAuthors.length; i++)
            writeEdges(buildCoOccurrenceCount(periodsPaperCoAuthors[i]), outputPath[i]);

    }

    private static void modifyPaperCoAuthors(HashMap<String, HashSet<String>> paperCoAuthors, String[] record) {

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

    private static HashMap<String, Integer>  buildCoOccurrenceCount(HashMap<String, HashSet<String>> paperCoAuthors) {

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

        return coAuthors;
    }

    private static void writeEdges(HashMap<String, Integer> coAuthors, String edgesPath) throws IOException {

        for (Map.Entry<String, Integer> mapElement : coAuthors.entrySet()) {
            String pair = mapElement.getKey();
            Utils.log(pair + "\t" + mapElement.getValue(), edgesPath);
        }
    }

}

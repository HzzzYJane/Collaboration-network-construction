import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NodeConstructor {

    /**
     * the current working directory
     */
    private static final String currDirectory = System.getProperty("user.dir");

    /**
     * the input file. Must only contain url of researchers (eg. all professors' links of WashU engineering)
     */
    private static final String csInputPath = currDirectory + "/cs_dep.txt";

    /**
     * the input file. Must only contain url of researchers (eg. all professors' links of WashU engineering)
     */
    private static final String bmeInputPath = currDirectory + "/bme_dep.txt";

    /**
     * the input file. Must only contain url of researchers (eg. all professors' links of WashU engineering)
     */
    private static final String eceInputPath = currDirectory + "/ece_dep.txt";

    /**
     * the input file. Must only contain url of researchers (eg. all professors' links of WashU engineering)
     */
    private static final String eseInputPath = currDirectory + "/ese_dep.txt";

    /**
     * the input file. Must only contain url of researchers (eg. all professors' links of WashU engineering)
     */
    private static final String memsInputPath = currDirectory + "/mems_dep.txt";


    private static final String domain = currDirectory + "/domain.tsv";

    /**
     * the researcher hashed name, name, and study domain log output directory
     */
    private static final String logNodes = currDirectory + "/nodes.tsv";


    public static void main(String[] args) throws IOException {

        List<String> deps = new ArrayList<>();
        deps.add(csInputPath + "\t" + "CS");
        deps.add(bmeInputPath + "\t" + "BME");
        deps.add(eceInputPath + "\t" + "ECE");
        deps.add(eseInputPath + "\t" + "ESE");
        deps.add(memsInputPath + "\t" + "MEMS");

        HashMap<String, String> washU = new HashMap<>();

        for (String dep: deps) {
            String[] parse = dep.split("\\t");
            ArrayList<String> allLinks = Utils.getAllURLS(parse[0]);

            for (String s: allLinks) {
                String nodeName = s.split("=")[2];
                washU.put(nodeName, parse[1]);
            }
        }

        ArrayList<String> domains = Utils.getAllURLS(domain);

        for (String s: domains) {

            String[] parse = s.split("\\t");
            String nodeHash = parse[0];
            String nodeName = parse[1];
            String fields = parse[2].substring(parse[2].indexOf("[") + 1, parse[2].indexOf("]"));
            if (washU.containsKey(nodeHash)) {
                String affiliation = washU.get(parse[0]);
                Utils.log(nodeHash + "\t" + nodeName + "\t" + fields + "\t" + affiliation + "\t" + "Y", logNodes);
            }
            else
                Utils.log(nodeHash + "\t" + nodeName + "\t" + fields + "\t" + "N/A" + "\t" + "N", logNodes);
        }

    }
}

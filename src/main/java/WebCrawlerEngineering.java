import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The web crawler for the engineering school at Washington University in St. Louis
 * @author Alan Zhang, Jane Han {xinyuzhang, hanzheyu} @ wustl.edu; Washington University in St. Louis
 */
public class WebCrawlerEngineering {

    /**
     * the current working directory
     */
    private static final String currDirectory = System.getProperty("user.dir");

    /**
     * the input file. Must only contain url of researchers (eg. all professors' links of WashU engineering)
     */
    private static final String inputPath = currDirectory + "/cs_dep_test.txt";

    /**
     * the recovery file. In case the program stopped, the program will recover its status to continue
     */
    private static final String recoveryPath = currDirectory + "/recovery.tsv";

    /**
     * the publication author hash name, publication title, and published year log output directory
     */
    private static final String logPublicationYearPath = currDirectory + "/publications.tsv";

    /**
     * the researcher hashed name, name, and study domain log output directory
     */
    private static final String logFieldsPath = currDirectory + "/domain.tsv";

    /**
     * your local chromedriver directory
     */
    private static final String chromeDriverPath = "/usr/local/bin/chromedriver";

    /**
     * all the links from the {@link #inputPath} file and every link's co-authors (if available) will be crawled.
     */
    private static HashSet<String> washUProfessorsHash = new HashSet<>();

    /**
     * the visited pages to avoid duplicate expansion
     */
    private static HashSet<String> visitedPageHash = new HashSet<>();

    /**
     * the links waiting for getting crawled
     */
    private static Deque<String> LinksToCrawl = new LinkedList<>();

    /**
     * 1) read in links from the inputPath {@link #inputPath}
     * 2) recover the crawling status from the recovery file {@link #recoveryPath}
     * 3) crawl all links that have not been crawled
     * @param args the program argument
     * @throws IOException the IOException
     * @throws InterruptedException the InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        ArrayList<String> allLinks = Utils.getAllURLS(inputPath);

        ConstructStatsFromRecovery();

        for (String s: allLinks) {
            if (!visitedPageHash.contains(s.split("=")[2])) {
                String links = s + "\t" + s.split("=")[2];
                LinksToCrawl.push(links);
                Utils.log(links, recoveryPath);
            }

            washUProfessorsHash.add(s.split("=")[2]); // add in the hash name into the hash set
        }

        // DFS algorithm with avoiding duplicate expansion strategy
        while (!LinksToCrawl.isEmpty()){
            if (visitedPageHash.contains(LinksToCrawl.getFirst().split("\\t")[1])){
                System.out.println("already done: " + LinksToCrawl.getFirst());
                LinksToCrawl.pop();
            }
            else {
                constructPublicationsToAuthorMapFromWeb(LinksToCrawl.poll());
            }
        }
    }

    /**
     * 1) extract the researcher name, study field and log into {@link #logFieldsPath}
     * 2) load all publications (no more than 1000)
     * 3) log all publications't title and year and log into {@link #logPublicationYearPath}
     * 4) if the url is in {@link #washUProfessorsHash}, add in all the co-author links (if available) to crawl
     * @param url the url to crawl. Format: fullLink \t userhashName
     * @throws InterruptedException the InterruptedException
     * @throws IOException the IOException
     */
    private static void constructPublicationsToAuthorMapFromWeb(String url)
            throws InterruptedException, IOException {

        String[] linksToCrawl = url.split("\\t");
        url = linksToCrawl[0];
        String hashName = linksToCrawl[1];

        Thread.sleep(Utils.generateRandomInt()); // prevent Robot detection

        // configure the driver setting to prevent it showing the browser GUI
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get(url);

        System.out.print("Start working on " + url);

        // get the name and research field
        String name = driver.findElement(By.xpath("//*[@id=\"gsc_prf_in\"]")).getText();
        System.out.println(" Name: " + name);

        int fieldsCount = driver.findElements(By.xpath("//*[@id=\"gsc_prf_int\"]/a")).size();
        ArrayList<String> fields = new ArrayList<>();
        for (int i = 1; i <= fieldsCount; i++) {
            String field = driver.findElement(By.xpath("//*[@id=\"gsc_prf_int\"]/a[" + i + "]")).getText();
            fields.add(field);
        }
        if (fields.size() == 0) fields.add("N/A");
        Utils.log(hashName + "\t" + name + "\t" + fields, logFieldsPath);

        // continually load page until there is no more publications to load or the publications are more than 1000 already
        WebElement showMoreButton = driver.findElement(By.id("gsc_bpf_more"));
        int count = 0;
        while (showMoreButton.isEnabled() && count < 1000) {
            showMoreButton.click();
            Thread.sleep(Utils.generateRandomInt()); // wait for new content to load and prevent Robot detection
            count = driver.findElements(By.xpath("//*[@id=\"gsc_a_b\"]/tr")).size();
        }

        // log each publication entry
        for (int i = 1; i <= count; i++) {
            String title = driver.findElement(By.xpath("//*[@id=\"gsc_a_b\"]/tr[" + i + "]/td[1]/a")).getText();
            String year = driver.findElement(By.xpath("//*[@id=\"gsc_a_b\"]/tr[" + i + "]/td[3]/span")).getText();
            if (year.length() == 0) year = "N/A";
            Utils.log(hashName + "\t" + title + "\t" + year, logPublicationYearPath);
        }

        // if the current page is a WashU professor, extract all possible un-crawled co-authors
        if (washUProfessorsHash.contains(hashName)) {

            int coAuthorCount = driver.findElements(By.xpath("//*[@id=\"gsc_rsb_co\"]/ul/li")).size();
            for (int i = 1; i <= coAuthorCount; i++) {
                String coAuthorWebLink = driver
                        .findElement(By.xpath("//*[@id=\"gsc_rsb_co\"]/ul/li[" + i + "]/div/span[2]/a"))
                        .getAttribute("href");

                String[] coAuthorLinks = coAuthorWebLink.split("=");
                if (coAuthorLinks.length == 3) {

                    String coAuthorHashName = coAuthorLinks[1].split("&")[0];
                    if (!visitedPageHash.contains(coAuthorHashName)) {
                        LinksToCrawl.push(coAuthorWebLink + "\t" + coAuthorHashName);
                        Utils.log(coAuthorWebLink + "\t" + coAuthorHashName, recoveryPath);
                        System.out.println("Add " + coAuthorHashName + " to crawl");
                    }

                }
            }
        }

        // add in hash name to prevent duplicate expansion and log the record
        visitedPageHash.add(hashName);
        Utils.log(hashName, recoveryPath);

        driver.close();
    }

    /**
     * from the {@link #recoveryPath} recover the status from the last time before the program stops
     * NOTICE: the url always corrects, the only chance the program stops by itself is the page structure.
     * Simply remove that page link from BOTH the {@link #recoveryPath} and the {@link #inputPath} will resolve the issue.
     * This is not a perfect solution but the issue rarely happens (1 time roughly every 500 pages)
     * @throws IOException the IOException
     */
    private static void ConstructStatsFromRecovery() throws IOException{
        try {
            BufferedReader br = new BufferedReader(new FileReader(recoveryPath));
            try {
                for (String line; (line = br.readLine()) != null; ) {
                    if (line.split("\\t").length == 1)
                        visitedPageHash.add(line);
                    else
                        LinksToCrawl.push(line);
                }
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("No log file yet. Will need to crawl all data from web");
        }
    }
}

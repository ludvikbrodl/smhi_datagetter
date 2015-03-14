package smhi;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Example class for the SMHI metobs API.
 */
public class XMLParse {

    public static final int NUMBER_OF_ROWS_EACH_SHEET = 200; //cannot be more than 253
    // Url for the metobs API
    private String metObsAPI = "http://opendata-download-metobs.smhi.se/api";

    /**
     * Print all available parameters.
     *
     * @return The key for the last parameter.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    private String getParameters() throws IOException,
            ParserConfigurationException, SAXException,
            XPathExpressionException {

        Document parameterDocument = readXmlFromUrl(metObsAPI
                + "/version/latest.xml");
        NodeList parametersNodeList = (NodeList) parseXML(parameterDocument,
                "/version/resource/key");

        String parameterKey = null;

        for (int i = 0; i < parametersNodeList.getLength(); i++) {
            parameterKey = parametersNodeList.item(i).getTextContent();
            System.out.println(parameterKey);
        }

        return parameterKey;
    }

    /**
     * Print all available stations for the given parameter. Return the id for
     * the last station.
     *
     * @param parameterKey The key for the wanted parameter
     * @return The id for the last station
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    private String getStationNames(String parameterKey)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {

        Document stationsDocument = readXmlFromUrl(metObsAPI
                + "/version/latest/parameter/" + parameterKey + ".xml");
        NodeList stationsNodeList = (NodeList) parseXML(stationsDocument,
                "/metObsParameter/station/key");

        String stationId = null;
        for (int i = 0; i < stationsNodeList.getLength(); i++) {
            stationId = stationsNodeList.item(i).getTextContent();
            System.out.println(stationId);
        }

        return stationId;
    }

    /**
     * Print all available periods for the given parameter and station. Return
     * the key for the last period.
     *
     * @param parameterKey The key for the wanted parameter
     * @param stationKey   The key for the wanted station
     * @return The name for the last period
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    private String getPeriodNames(String parameterKey, String stationKey)
            throws IOException, ParserConfigurationException, SAXException,
            XPathExpressionException {

        Document periodsDocument = readXmlFromUrl(metObsAPI
                + "/version/latest/parameter/" + parameterKey + "/station/"
                + stationKey + ".xml");
        NodeList periodsNodeList = (NodeList) parseXML(periodsDocument,
                "/metObsStation/period/key");

        String periodName = null;
        for (int i = 0; i < periodsNodeList.getLength(); i++) {
            periodName = periodsNodeList.item(i).getTextContent();
            System.out.println(periodName);
        }

        return periodName;
    }

    /**
     * Get the data for the given parameter, station and period.
     *
     * @param parameterKey The key for the wanted parameter
     * @param stationKey   The key for the wanted station
     * @param periodName   The name for the wanted period
     * @return The data
     * @throws IOException
     */
    private String getData(String parameterKey, String stationKey,
                           String periodName) throws IOException {
        return readStringFromUrl(metObsAPI + "/version/latest/parameter/"
                + parameterKey + "/station/" + stationKey + "/period/"
                + periodName + "/data.csv");
    }

    private static Document readXmlFromUrl(String url) throws IOException,
            ParserConfigurationException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(url);
    }

    private static NodeList parseXML(Document document, String expression)
            throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        return (NodeList) xPath.compile(expression).evaluate(document,
                XPathConstants.NODESET);
    }

    private static String readStringFromUrl(String url) throws IOException {

        InputStream inputStream = new URL(url).openStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream, Charset.forName("UTF-8")));
            StringBuilder stringBuilder = new StringBuilder();
            int cp;
            while ((cp = reader.read()) != -1) {
                stringBuilder.append((char) cp);
            }
            return stringBuilder.toString();
        } finally {
            inputStream.close();
        }
    }

    public static void main(String... args) {
        // map<Datum<Station, Temperature>>
        HashMap<String, HashMap<String, Double>> map = new HashMap<String, HashMap<String, Double>>();

        String metObsAPI = "http://opendata-download-metobs.smhi.se/api";

        // XMLParse openDataMetobsReader = new XMLParse();
        Scanner scan = new Scanner(System.in);
        System.out.println("Choose document, '19' is min-temp, '20' is max-temp, '2' is average-temp ");
        // Specify which documents to collect.
        // 19 is min-temp
        // 20 is max-temp
        String parameterKey = scan.nextLine();

        try {

            Document stationsDocument = readXmlFromUrl(metObsAPI
                    + "/version/latest/parameter/" + parameterKey + ".xml");
            NodeList stationsNodeList = parseXML(stationsDocument,
                    "/metObsParameter/station/key");

            System.out.println("Enter number of stations to output to file, 'all' is a valid input");
            System.out.println("Note that all is approx 800 stations, this will take at least 2 min to complete");
            String input = scan.nextLine();
            scan.close();
            int numberOfStationsToGet = 0;
            if (input.equalsIgnoreCase("all")) {
               numberOfStationsToGet = stationsNodeList.getLength();
            } else {
                numberOfStationsToGet = Integer.valueOf(input);
            }
            //GO THROUGH ALL STATIONS
            for (int i = 0; i < numberOfStationsToGet; i++) {
                String stationId = stationsNodeList.item(i).getTextContent();
                Document periodsDocument = readXmlFromUrl(metObsAPI
                        + "/version/latest/parameter/" + parameterKey
                        + "/station/" + stationId + ".xml");
                NodeList periodsNodeList = parseXML(periodsDocument,
                        "/metObsStation/period/key");

                //FOR EVERY STATION, GO THROUGH ALL PERIODS
                //IF PERIOD IS A CORRECTED-ARCHIVE -> USE THE DATA, ELSE SKIP.
                for (int j = 0; j < periodsNodeList.getLength(); j++) {
                    String periodName = periodsNodeList.item(j).getTextContent();
                    if (periodName.equals("corrected-archive")) {
                        // NEW DOCUMENT STARTS HERE
                        scan = new Scanner(readStringFromUrl(metObsAPI
                                + "/version/latest/parameter/" + parameterKey
                                + "/station/" + stationId + "/period/"
                                + periodName + "/data.csv"));
                        scan.nextLine(); // SKIP FIRST ROW
                        String stationName = scan.nextLine().split(";")[0];
                        System.out.println("Reading from station #" + (i + 1) + " with ID: {" + stationId + "} name: {" + stationName + "}");
                        String s = scan.nextLine();
                        //skip non data rows
                        while (!s.startsWith("Från Datum Tid")) {
                            s = scan.nextLine();
                        }
                        //Temperature data begins here
                        while (scan.hasNextLine()) {
                            s = scan.nextLine();
                            String[] array = s.split(";");
                            // Fr�n Datum Tid (UTC);Till Datum Tid
                            // (UTC);Representativt dygn;Lufttemperatur;Kvalitet
                            if (array.length < 2) {
                                break;
                            }
                            if (!map.containsKey(array[2])) {
                                map.put(array[2], new HashMap<String, Double>());
                            }
                            // Put value of specific day in the map.
                            HashMap<String, Double> stationMap = map
                                    .get(array[2]);
                            stationMap.put(stationId,
                                    Double.valueOf(array[3]));
                            map.put(array[2], stationMap);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        // ----------------------------------------------------------------------
        // Hashmap filling done.
        Workbook wb = new HSSFWorkbook();
        // map<Datum<Station, Temperature>>
        Set<String> stations = new HashSet<String>();
        for (HashMap<String, Double> date : map.values()) {
            stations.addAll(date.keySet());
        }
        int nbrStations = stations.size();
        int nbrSheets = (nbrStations / (NUMBER_OF_ROWS_EACH_SHEET - 5)); //each sheet can only fit 255 columns.
        //i.e if nbrStations is 280, nbrStations/250 =
        System.out.println(nbrStations);
        System.out.println(nbrSheets);
        Sheet[] sheet = new Sheet[nbrSheets + 1];
        for (int i = 0; i < sheet.length; i++) {
            sheet[i] = wb.createSheet("Min temp #" + (i + 1));
        }
        Set<String> dateSet = map.keySet();
        SortedSet<String> sortedDateKeys = new TreeSet<String>(dateSet);

        Collection<HashMap<String, Double>> stationCollection = map.values();

        SortedSet<String> sortedStationKeys = new TreeSet<String>();
        for (HashMap<String, Double> hm : stationCollection) {
            for (String s : hm.keySet()) {
                if (!sortedStationKeys.contains(s)) {
                    sortedStationKeys.add(s);
                }

            }
        }

        System.out.println("Writing to Excel File...");

        int sheetIndex = 0;
        // skriv stations namnen
        Row row0 = sheet[sheetIndex].createRow(0);
        Iterator<String> itr = sortedStationKeys.iterator();
        for (int i = 0; i < sortedStationKeys.size(); i++) {
            if (i % NUMBER_OF_ROWS_EACH_SHEET == 0) {
                row0 = sheet[(sheetIndex++)].createRow(0);
            }
            row0.createCell((i % 200) + 1).setCellValue(itr.next());
        }
        // Insert Date and temp data into cells
        int dateIndex = 0;
        for (String date : sortedDateKeys) {
            sheetIndex = 0;
            dateIndex++;
            Row row = sheet[sheetIndex].createRow(dateIndex);
            row.createCell(0).setCellValue(date);
            itr = sortedStationKeys.iterator();
            int stationIndex = 0;
            while (itr.hasNext()) {
                if ((stationIndex++) % 200 == 0) {
                    row = sheet[(sheetIndex++)].createRow(dateIndex);
                    row.createCell(0).setCellValue(date);
                    stationIndex = 1;
                }
                String stationKey = itr.next();
                HashMap<String, Double> stationMap = map.get(date);
                if (stationMap.containsKey(stationKey)) {
                    row.createCell(stationIndex).setCellValue(
                            stationMap.get(stationKey));
                }

            }
        }

        // Write the output to a file
        FileOutputStream fileOut = null;

        try {
            fileOut = new FileOutputStream("smhi_workbook.xls");
            wb.write(fileOut);
            fileOut.close();
            System.out.println("Output complete: smhi_workbook.xls");
        } catch (IOException e) {
            System.out.println("Close the excel document or data cannot be written");
            e.printStackTrace();
        }

    }
}
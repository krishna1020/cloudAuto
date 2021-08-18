package com.krogerqa.selenium.utilities.reporting;

import com.krogerqa.seleniumcentral.framework.main.BaseCommands;
import javafx.print.Collation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.testng.ITestResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.krogerqa.selenium.ui.support.DataAttribute.TestCaseId;

public class ReportManager {

    Map<String, String> testData;

    Map<String, String> screenShots;

    ITestResult testResult;

    boolean captureScreens = false;

    BaseCommands baseCommands = new BaseCommands();

    public static String dataFile = "test-data.csv";

    public ReportManager(ITestResult iTestResult, boolean isScreenCapture) {
        this.testResult = iTestResult;
        this.testData = new HashMap<>();
        this.screenShots = new HashMap<>();
        this.testResult.setAttribute("testData", getDataArray(testData));
        this.testResult.setAttribute("screenShots", screenShots);
        setCaptureScreens(isScreenCapture);
    }

    public ReportManager() {
    }

    public void addTestData(String name , String value)
    {
        this.testData.put(name, value);
        this.testResult.setAttribute("testData", getDataArray(testData));
    }

    public void addScreenShot(String description)
    {
        if(captureScreens) {
            String screenShot = baseCommands.takeScreenShot(this.testResult);
            this.screenShots.put(description, screenShot);
            this.testResult.setAttribute("screenShots", screenShots);
        }
    }

    public boolean isCaptureScreens() {
        return captureScreens;
    }

    public void setCaptureScreens(boolean captureScreens) {
        this.captureScreens = captureScreens;
    }

    private String[][] getDataArray(Map<String, String> data)
    {
        if(data.isEmpty())
            return new String[][] {
                    {
                            "No Data Collected"
                    }
            };
        String[][] dataArray = new String[data.size()][2];
        int i =0;
        for (Map.Entry<String,String> entry : data.entrySet())
        {
            dataArray[i][0] = entry.getKey();
            dataArray[i][1] = entry.getValue();
            i++;
        }

        return dataArray;
    }

    public void addTestData(LinkedHashMap<String, String> testData) {
        this.testData.putAll(testData);
        this.testResult.setAttribute("testData", getDataArray(testData));
    }

    public void logData(LinkedHashMap<String, String> testData)
    {
        setTestData(testData);
        logData();
    }

    public void logData()
    {
        boolean isNewFile = !new File(dataFile).exists();
        CSVPrinter csvPrinter;

        try {


            if (isNewFile) {
                BufferedWriter writer = Files.newBufferedWriter(
                        Paths.get(dataFile),
                        StandardOpenOption.CREATE);
                csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader(testData.keySet().toArray(new String[testData.size()])));
            }

            else {
                BufferedWriter writer = Files.newBufferedWriter(
                        Paths.get(dataFile),
                        StandardOpenOption.APPEND);
                csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
            }

            csvPrinter.printRecord(testData.values());
            csvPrinter.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public Map<String, CSVRecord> read() throws IOException
    {
        Reader in = new FileReader(dataFile);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        Map<String,CSVRecord > resultMap = new HashMap<>();
        records.forEach((record)->{resultMap.put(record.get(TestCaseId), record);});
        return resultMap;
    }

    public Map<String, String> getTestData() {
        return testData;
    }

    public void setTestData(Map<String, String> testData) {
        this.testData = testData;
    }
}

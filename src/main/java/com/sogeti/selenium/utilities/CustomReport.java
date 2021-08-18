package com.krogerqa.selenium.utilities.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.IOException;
import java.util.*;

public class CustomReport implements IReporter {
    private ExtentReports extent;

    private static final String OUTPUT_FOLDER = "test-output/";
    private static final String FILE_NAME = "TestReport_"+new Date().getTime()+".html";

    @Override
    public void generateReport(
            List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        init();

        for (ISuite suite : suites) {
            Map<String, ISuiteResult> result = suite.getResults();

            for (ISuiteResult r : result.values()) {
                ITestContext context = r.getTestContext();

                buildTestNodes(context.getFailedTests(), Status.FAIL);
                buildTestNodes(context.getSkippedTests(), Status.SKIP);
                buildTestNodes(context.getPassedTests(), Status.PASS);

            }
        }

        for (String s : Reporter.getOutput()) {
            extent.setTestRunnerOutput(s);
        }

        extent.flush();
    }

    private void init() {
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(OUTPUT_FOLDER + FILE_NAME);
        htmlReporter.config().setDocumentTitle("EHH E2E Test Report");
        htmlReporter.config().setReportName("EHH E2E Test Report");
        htmlReporter.config().setTestViewChartLocation(ChartLocation.BOTTOM);
        htmlReporter.config().setTheme(Theme.DARK);

        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        extent.setReportUsesManualConfiguration(true);
    }

    private void buildTestNodes(IResultMap tests, Status status) {
        ExtentTest test;

        if (tests.size() > 0) {
            for (ITestResult result : tests.getAllResults()) {
                Markup m = MarkupHelper.createTable(result.getAttribute("testData")!=null?(String[][]) result.getAttribute("testData"):new String[0][0]);
                test = extent.createTest(result.getMethod().getMethodName());
                for (String group : result.getMethod().getGroups())
                    test.assignCategory(group);

                test.log(status, m);
                if (result.getThrowable() != null) {
                    extent.setTestRunnerOutput(result.getThrowable().getMessage());
                }

                test.getModel().setStartTime(getTime(result.getStartMillis()));
                test.getModel().setEndTime(getTime(result.getEndMillis()));

                try {
                    Map<String, String>  data = (Map<String, String>)result.getAttribute("screenShots");
                    for (Map.Entry<String,String> entry : data.entrySet())
                    {
                        test.addScreenCaptureFromPath("../target/Screeshots/"+entry.getValue(), entry.getKey());
                    }
                } catch (Exception ignored) {
                }

            }


        }
    }

    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

}

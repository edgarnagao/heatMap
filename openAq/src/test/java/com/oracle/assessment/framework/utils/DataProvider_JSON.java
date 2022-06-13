package com.oracle.assessment.framework.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generic TestNG DataProvider Class for extracting JSON data
 *
 * @author mbergantino
 *
 */
public class DataProvider_JSON {

        // fetchData generic dataProvider variable
        public static String dataFile = "";
        public static String testCaseName = "NA";

        /**
         * fetchData method; generic dataProvider that extracts data by JSON key:value pairs from JSON file
         *
         * @param method
         * @return Object[][]
         * @throws Exception
         */
        @DataProvider(name = "fetchData_JSON")
        public static Object[][] fetchData(Method method) throws Exception {
                Assert.assertNotNull(dataFile, "DataProvider_JSON: Expected dataFile to not be null!");
                Assert.assertNotEquals(dataFile, "", "DataProvider_JSON: Expected dataFile to be assigned!");

                Object rowID, description;
                Object[][] result;
                testCaseName = method.getName();
                List<String> groupNames = Arrays.asList(method.getAnnotation(Test.class).groups());
                groupNames.replaceAll(String::toUpperCase);
                JSONArray testData = (JSONArray) extractJSONData(dataFile).get(testCaseName);
                List<JSONObject> testDataList = new ArrayList<>();

                for ( int i = 0; i < testData.length(); i++ ) {
                        testDataList.add((JSONObject) testData.get(i));
                }

                try {
                        result = new Object[testDataList.size()][testDataList.get(0).length()];

                        for ( int i = 0; i < testDataList.size(); i++ ) {
                                rowID = testDataList.get(i).get("rowID");
                                description = testDataList.get(i).get("description");
                                result[i] = new Object[] { rowID, description, testDataList.get(i) };
                        }
                }
                catch(IndexOutOfBoundsException ie) {
                        result = new Object[0][0];
                }

                return result;
        }


        /**
         * extract JSON data method
         *
         * @param jsonFile
         * @return JSONObject
         * @throws Exception
         */
        public static JSONObject extractJSONData(String jsonFile) throws Exception {
                InputStream is = new FileInputStream(jsonFile);
                String jsonTxt = IOUtils.toString(is, String.valueOf(StandardCharsets.UTF_8));
                return new JSONObject(jsonTxt);
        }


        /**
         * This method returns JSON Array from a JSON File based on rowID.
         * @param jsonFile
         * @param rowId
         * @return
         * @throws Exception
         */
        public static JSONArray fetchJSONRowData(String jsonFile,String rowId) throws Exception {
                return (JSONArray) extractJSONData(jsonFile).get(rowId);
        }

}


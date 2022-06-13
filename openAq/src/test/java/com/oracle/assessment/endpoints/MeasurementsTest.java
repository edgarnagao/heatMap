package com.oracle.assessment.endpoints;

import com.oracle.assessment.framework.utils.DataProvider_JSON;
import com.oracle.assessment.setup.SetUp;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

import io.restassured.module.jsv.JsonSchemaValidator;
import static io.restassured.RestAssured.given;


/**
 * Tests to verify measurement endpoint
 *
 * @author enagao
 */
public class MeasurementsTest extends SetUp {

        // Environment Property File Ingestion Purposes
        private static String properties = "../openAq/src/test/java/com/oracle/assessment/config/config.properties";
        private static String filePath = "../openAq/src/test/java/com/oracle/assessment/resources/schemas/measurments.schema.json";

        @BeforeClass(alwaysRun = true)
        protected void testClassSetup() throws Exception {
                loadProps(Collections.singletonList(properties));
                // Set data file and load the test data
                setDataFile_JSON(configProps.getProperty("data.file"));
        }

        @AfterClass(alwaysRun = true)
        protected void testClassTeardown() {
        }

        @BeforeMethod(alwaysRun = true)
        protected void testMethodSetup(Method method) {
        }

        @AfterMethod(alwaysRun = true)
        protected void testMethodTeardown(ITestResult result) {
        }

        @Test(dataProvider = "fetchData_JSON", dataProviderClass = DataProvider_JSON.class)
        public void tc001_verify_statusCode(String rowID, String description, JSONObject testData) throws Exception {
                String baseurl = configProps.getProperty("openaq.baseurl");
                String request = testData.optString("request");
                System.out.println("--- "+testData.optString("description"));
                JSONObject verifications=testData.optJSONObject("validations");
                String expectedStatus = verifications.optString("statusCode","");
                validateStatusCode(baseurl+request,Integer.parseInt(expectedStatus),testData.optString("error"));
        }

        @Test(dataProvider= "fetchData_JSON", dataProviderClass=DataProvider_JSON.class)
        public void tc002_verify_measurementByCity(String rowID, String description, JSONObject testData)
                throws Exception {
                String baseurl = configProps.getProperty("openaq.baseurl");
                String request = testData.optString("request");
                System.out.println("--- "+testData.optString("description"));
                JSONObject parameters=testData.optJSONObject("params");

                Map<String,String> requestParameters = new HashMap<>();
                requestParameters.put("city",parameters.optString("city",""));
                requestParameters.put("limit",parameters.optString("limit",""));
                requestParameters.put("parameter",parameters.optString("parameter",""));

                validateParamInResponse(getResponse(baseurl+request,requestParameters,filePath),requestParameters,
                        testData.optString("errorParams"),testData.optString("errorCount"));

        }

        private void validateParamInResponse(Response endpointResponse, Map<String, String> parameterss,
                String errorDescription, String errorDescriptionLimit) {
                JsonPath jsResult = new JsonPath(endpointResponse.asString());
                try {
                        ArrayList results = jsResult.get("results");
                        results.forEach( param -> {
                                Assert.assertEquals(((LinkedHashMap) param).get("city"),
                                        parameterss.get("city"), errorDescription);
                                Assert.assertEquals(((LinkedHashMap) param).get("parameter"),
                                        parameterss.get("parameter"),errorDescription);
                        });
                        Assert.assertTrue(Integer.parseInt(parameterss.get("limit")) >= results.size(),errorDescriptionLimit);
                }catch (NullPointerException e){
                        e.printStackTrace();
                }

        }

        private void validateStatusCode(String endpoint, int expectedCode, String errorDescription ){
                int statusCode = given().when().get(endpoint).getStatusCode();
                System.out.println("The response status is "+statusCode);
                Assert.assertEquals(statusCode, Integer.parseInt(String.valueOf(expectedCode)),errorDescription);
        }

        private void correctResponse(Response endpointResponse ) throws Exception {
                int statusCode = endpointResponse.getStatusCode();
                System.out.println("The response status in responseResponse method is "+statusCode);
                JsonPath jsResponse = new JsonPath(endpointResponse.asString());
                ArrayList results = jsResponse.get("results");
                if (results.size()==0) {
                        throw new SkipException("NO RESULTS FOR THE REQUEST - Review Manually the request and params");
                }
                if (statusCode != 200) {
                        //Validate the response have info
                        //JsonPath jsErrorMessage = new JsonPath(endpointResponse.asString());
                        throw new Exception("INVALID REQUEST  ==  validate the error message details "+ jsResponse.get("detail"));
                }
                Assert.assertEquals(statusCode,200,"Verify the structure of the request "
                        + "or the connection to the endpoitn");
        }

        private Response getResponse(String url, Map<String, String> params,String schemaFilePath) throws Exception {
                Response endpointResponse = given().queryParam("city", params.get("city"))
                        .queryParam("parameter", params.get("parameter")).queryParam("limit", params.get("limit"))
                        .when().get(url);
                correctResponse(endpointResponse);
                validateJsonSchema(endpointResponse,schemaFilePath);
                return endpointResponse;
        }

        private void validateJsonSchema(Response endpointResponse, String schemaFilePath){
                //verify JSON Schema
                endpointResponse.then().assertThat()
                        .body(JsonSchemaValidator.matchesJsonSchema
                                (new File(schemaFilePath)));

        }
}

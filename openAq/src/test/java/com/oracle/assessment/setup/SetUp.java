package com.oracle.assessment.setup;

import com.oracle.assessment.framework.utils.DataProvider_JSON;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * PMA UI Application Base Class
 *
 */
public abstract class SetUp {

        protected static final String testDataBaseDirectory="/src/test/java/com/oracle/assessment";
        protected static Properties configProps=new Properties();

        /**
         * setDataFile method to set the path of the JSON data file
         *
         * @param dataFile
         */
        protected void setDataFile_JSON(String dataFile) {
                DataProvider_JSON.dataFile=dataFile;
        }

        public static String getTestBaseDir(String className) {
                return testDataBaseDirectory;
        }

        /**
         * loadProps method to parse list of property files to load
         *
         * @param propFiles - The list of property files to process
         * @throws Exception exc
         */
        public void loadProps(List<String> propFiles) throws Exception {
                // there can be unlimited property files passed in as long as they reside in the same folder...
                for (String getFile : propFiles) {
                        // allow passing absolute path of prop files for Linux or Windows platforms...
                        if (getFile.contains("/")||getFile.contains("\\")) {
                                configProps.load(new FileInputStream(getFile));
                        }
                }

        }
}
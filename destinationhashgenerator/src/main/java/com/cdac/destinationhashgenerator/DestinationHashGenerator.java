package com.cdac.destinationhashgenerator;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <path to JSON file>");
            System.exit(1);
        }

        String prnNumber = args[0].trim().toLowerCase();
        String jsonFilePath = args[1].trim();

        try {
            // Read and parse the JSON file
            FileReader reader = new FileReader(jsonFilePath);
            JSONObject jsonObject = new JSONObject(new JSONTokener(reader));

            // Traverse JSON to find "destination"
            String destinationValue = findDestinationValue(jsonObject);
            if (destinationValue == null) {
                System.err.println("Key 'destination' not found in the JSON file.");
                System.exit(1);
            }

            // Generate random alphanumeric string
            String randomString = generateRandomString(8);

            // Concatenate values and compute MD5 hash
            String concatenatedString = prnNumber + destinationValue + randomString;
            String md5Hash = DigestUtils.md5Hex(concatenatedString);

            // Output the result
            System.out.println(md5Hash + ";" + randomString);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String findDestinationValue(JSONObject jsonObject) {
        // Check for "destination" key in the JSONObject
        if (jsonObject.has("destination")) {
            return jsonObject.getString("destination");
        }

        // Check nested objects and arrays
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                String result = findDestinationValue((JSONObject) value);
                if (result != null) {
                    return result;
                }
            } else if (value instanceof org.json.JSONArray) {
                for (Object item : (org.json.JSONArray) value) {
                    if (item instanceof JSONObject) {
                        String result = findDestinationValue((JSONObject) item);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}

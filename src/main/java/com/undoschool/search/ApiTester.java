package com.undoschool.search;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiTester {
    
    public static void main(String[] args) {
        try {
            System.out.println("Testing API...");
            testEndpoint("http://localhost:8081/api/search");
            
            System.out.println("\nTesting search for Math courses...");
            testEndpoint("http://localhost:8081/api/search?category=Math");
            
            System.out.println("\nTesting fuzzy search...");
            testEndpoint("http://localhost:8081/api/search?q=dinosa");
            
            System.out.println("\nTesting suggestions...");
            testEndpoint("http://localhost:8081/api/search/suggest?q=mat");
        } catch (Exception e) {
            System.err.println("Error testing API: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testEndpoint(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            System.out.println("Response: " + response.toString());
        } catch (Exception e) {
            System.err.println("Error testing endpoint " + urlStr + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
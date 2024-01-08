package com.organica.services.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.organica.services.SunskyApiService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@Service
public class SunskyApiServiceImpl implements SunskyApiService {
    @Value("${my.api.key}")
    private String key;
    @Value("${my.api.secret}")
    private String secret;

    @Autowired
    MD5Digester md5Digester;

    private final Log log = LogFactory.getLog(SunskyApiServiceImpl.class);

    private final String PARAM_ENCODING = "UTF-8";

    public String call(String apiUrl, String secret, Map<String, String> parameters) throws Exception {
        String postStr = joinItems(parameters) + "&signature=" + sign(parameters, secret);
        byte[] postData = postStr.getBytes(PARAM_ENCODING);

        OutputStream output = null;
        BufferedReader reader = null;
        try {
            URL postUrl = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));

            output = conn.getOutputStream();
            output.write(postData);
            output.flush();
            output.close();
            output = null;

            int rc = conn.getResponseCode();
            if (rc == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                StringBuffer rsp = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    rsp.append(line);
                }

                return rsp.toString();
            } else {
                log.error("failed to call open api: \nParameters: " +
                        parameters + "\nResponse Code: " + rc);

                throw new RuntimeException("" + rc);
            }
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    log.error("failed to close output stream to open api", e);
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("failed to close input stream from open api", e);
                }
            }
        }
    }

    public void download(String apiUrl, String secret, Map<String, String> parameters) throws Exception {
        String postStr = joinItems(parameters) + "&signature=" + sign(parameters, secret);
        byte[] postData = postStr.getBytes(PARAM_ENCODING);

        OutputStream output = null;
        InputStream input = null;
        try {
            URL postUrl = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(600000);
            conn.setReadTimeout(600000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));

            output = conn.getOutputStream();
            output.write(postData);
            output.flush();
            output.close();
            output = null;

            int rc = conn.getResponseCode();
            if (rc == 200) {
                input = conn.getInputStream();

                OutputStream fos = new FileOutputStream("D:\\test.zip");
                try {
                    IOUtils.copy(input, fos);
                } finally {
                    fos.close();
                }
            } else {
                log.error("failed to call open api: \nParameters: " +
                        parameters + "\nResponse Code: " + rc);

                throw new RuntimeException("" + rc);
            }
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    log.error("failed to close output stream to open api", e);
                }
            }

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("failed to close input stream from open api", e);
                }
            }
        }
    }

    private String joinItems(Map<String, String> itemMap) {
        if (itemMap == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        for (Entry<String, String> e : itemMap.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();

            if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                if (sb.length() > 0) {
                    sb.append('&');
                }

                sb.append(key);
                sb.append('=');
                sb.append(escape(value));
            }
        }

        return sb.toString();
    }

    private String sign(Map<String, String> itemMap, String securityCode) {
        Map sortedMap = new TreeMap(itemMap);
        StringBuffer sb = new StringBuffer();
        for (Entry e : (Set<Entry>) sortedMap.entrySet()) {
            sb.append(e.getValue());
        }

        return md5Digester.digest(sb + "@" + securityCode);
    }

    private String escape(String str) {
        if (StringUtils.isNotBlank(str)) {
            try {
                return URLEncoder.encode(str, PARAM_ENCODING);
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
        }

        return "";
    }

    public void exampleFetch() throws Exception {



        // Fetch categories
        if (false) {
            String apiUrl = "https://open.sunsky-online.com/openapi/category!getChildren.do";

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("parentId", "100128");

            String result = call(apiUrl, secret, parameters);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResult = objectMapper.readTree(result);
            if (! "success".equals(jsonResult.get("result").asText())) {
                throw new Exception("Error: " + result);
            }

            JsonNode jsonListModel = jsonResult.get("data");
            int pageCount = jsonListModel.get("pageCount").asInt();

            for (int i = 0; i < pageCount; i++) {
                parameters.put("page", Integer.toString(i + 1));
                result = call(apiUrl, secret, parameters);

                jsonResult = objectMapper.readTree(result);
                if (! "success".equals((jsonResult.get("result").asText()))) {
                    throw new Exception("Error: " + result);
                }
                jsonListModel = jsonResult.get("data").get("result");
                for (JsonNode productNode: jsonListModel){
                    ;
                }
            }
        }

        // Fetch products
        if (true) {
            String apiUrl = "https://open.sunsky-online.com/openapi/product!search.do";

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("categoryId", "100833");

            String result = call(apiUrl, secret, parameters);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResult = objectMapper.readTree(result);
            if (! "success".equals(jsonResult.get("result").asText())) {
                throw new Exception("Error: " + result);
            }

            JsonNode jsonListModel = jsonResult.get("data");
            int pageCount = jsonListModel.get("pageCount").asInt();

            for (int i = 0; i < pageCount; i++) {
                parameters.put("page", Integer.toString(i + 1));
                result = call(apiUrl, secret, parameters);

                jsonResult = objectMapper.readTree(result);
                System.out.println(jsonResult);
                if (! "success".equals((jsonResult.get("result").asText()))) {
                    throw new Exception("Error: " + result);
                }
                jsonListModel = jsonResult.get("data").get("result");
                for (JsonNode productNode: jsonListModel){
                    ;
                }
            }


        }
        if (true) {
            String apiUrl = "https://open.sunsky-online.com/openapi/product!search.do";

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("categoryId", "100700");

            String result = call(apiUrl, secret, parameters);
            System.out.println(result);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResult = objectMapper.readTree(result);
            if (! "success".equals(jsonResult.get("result").asText())) {
                throw new Exception("Error: " + result);
            }

            JsonNode jsonListModel = jsonResult.get("data");
            int pageCount = jsonListModel.get("pageCount").asInt();

            for (int i = 0; i < pageCount; i++) {
                parameters.put("page", Integer.toString(i + 1));
                result = call(apiUrl, secret, parameters);

                jsonResult = objectMapper.readTree(result);
                System.out.println(jsonResult);
                if (! "success".equals((jsonResult.get("result").asText()))) {
                    throw new Exception("Error: " + result);
                }
                jsonListModel = jsonResult.get("data").get("result");
                for (JsonNode productNode: jsonListModel){

                }
            }


        }

        // Fetch the details for the product
        if (false) {
            String apiUrl = "https://open.sunsky-online.com/openapi/product!detail.do";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("itemNo", "S-MPH-6016B");
            System.out.println(call(apiUrl, secret, parameters));
        }

        // Download the images
        if (false) {
            String apiUrl = "https://open.sunsky-online.com/openapi/product!getImages.do";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("itemNo", "S-MPH-6016B");
            parameters.put("size", "50");
            parameters.put("watermark", "mysite.com");
            download(apiUrl, secret, parameters);
        }

        // Fetch the countries and states
        if (false) {
            String apiUrl = "https://open.sunsky-online.com/openapi/order!getCountries.do";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            System.out.println(call(apiUrl, secret, parameters));
        }

        // Check the balance
        if (false) {
            String apiUrl = "https://open.sunsky-online.com/openapi/order!getBalance.do";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            System.out.println(call(apiUrl, secret, parameters));

            apiUrl = "https://open.sunsky-online.com/openapi/order!getBillList.do";
            parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("gmtCreatedStart", "1/31/2011");

            String result = call(apiUrl, secret, parameters);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResult = objectMapper.readTree(result);
            if (! "success".equals(jsonResult.get("result").asText())) {
                throw new Exception("Error: " + result);
            }

            JsonNode jsonListModel = jsonResult.get("data");
            int pageCount = jsonListModel.get("pageCount").asInt();
            System.out.println("Page Count: " + pageCount);

            for (int i = 0; i < pageCount; i++) {
                parameters.put("page", Integer.toString(i + 1));
                result = call(apiUrl, secret, parameters);

                jsonResult = objectMapper.readTree(result);
                if (! "success".equals((jsonResult.get("result").asText()))) {
                    throw new Exception("Error: " + result);
                }

                jsonListModel = jsonResult.get("data").get("result");

                for (JsonNode productNode: jsonListModel){
                    System.out.println(productNode.get("productName").asText());
                }
            }
        }

        // Calculate the prices and freights for the items
        if (false) {
            String apiUrl = "https://open.sunsky-online.com/openapi/order!getPricesAndFreights.do";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("countryId", "41");
            parameters.put("items.1.itemNo", "S-IP4G-0363");
            parameters.put("items.1.qty", "20");
            parameters.put("items.2.itemNo", "S-MAC-0230");
            parameters.put("items.2.qty", "5");
            String result = call(apiUrl, secret, parameters);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResult = objectMapper.readTree(result);;
            if (! "success".equals(jsonResult.get("result").asText())) {
                throw new Exception("Error: " + result);
            }

            JsonNode freightList = jsonResult.get("data").get("freightList");
            if (freightList.size() == 0) {
                System.out.println("no shipping way to the country");
                return;
            }

            JsonNode way = freightList.get(0);

            // Create an order
            if (false) {
                apiUrl = "https://open.sunsky-online.com/openapi/order!createOrder.do";
                parameters = new HashMap<String, String>();
                parameters.put("key", key);
                parameters.put("deliveryAddress.countryId", "41");
                parameters.put("deliveryAddress.state", "NY");
                parameters.put("deliveryAddress.city", "New York");
                parameters.put("deliveryAddress.address", "New York");
                parameters.put("deliveryAddress.postcode", "100098");
                parameters.put("deliveryAddress.receiver", "Test");
                parameters.put("deliveryAddress.telephone", "123456");
                parameters.put("deliveryAddress.shippingWayId", way.get("id").asText());
                parameters.put("siteNumber", "MyNumber002");
                parameters.put("items.1.itemNo", "S-IP4G-0363");
                parameters.put("items.1.qty", "20");
                parameters.put("items.2.itemNo", "S-MAC-0230");
                parameters.put("items.2.qty", "5");
                System.out.println(call(apiUrl, secret, parameters));
            }

            String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

            apiUrl = "https://open.sunsky-online.com/openapi/order!getOrderList.do";
            parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("gmtCreatedStart", date);
            parameters.put("gmtCreatedEnd", date);
            result = call(apiUrl, secret, parameters);

            jsonResult = objectMapper.readTree(result);
            if (! "success".equals(jsonResult.get("result").asText())) {
                throw new Exception("Error: " + result);
            }

            JsonNode jsonListModel = jsonResult.get("data");
            int pageCount = jsonListModel.get("pageCount").asInt();
            System.out.println("Page Count: " + pageCount);

            for (int i = 0; i < pageCount; i++) {
                parameters.put("page", Integer.toString(i + 1));
                result = call(apiUrl, secret, parameters);

                jsonResult = objectMapper.readTree(result);
                if (! "success".equals(jsonResult.get("result").asText())) {
                    throw new Exception("Error: " + result);
                }

                jsonListModel = jsonResult.get("data").get(result);
                for (int j = 0, n = jsonListModel.size(); j < n; j++) {
                    JsonNode order = jsonListModel.get(j);

                    // Fetch the details for the order
                    String apiUrl2 = "https://open.sunsky-online.com/openapi/order!getOrderList.do";
                    Map<String, String> parameters2 = new HashMap<String, String>();
                    parameters2.put("key", key);
                    parameters2.put("number", order.get("number").asText());
                    System.out.println(call(apiUrl2, secret, parameters2));
                }
            }
        }

        // Fetch the hot items
        if (false) {
            String apiUrl = "https://open.sunsky-online.com/openapi/stats!getHotItems.do";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("key", key);
            parameters.put("countryId", "41");

            String result = call(apiUrl, secret, parameters);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResult = objectMapper.readTree(result);
            if (! "success".equals(jsonResult.get("result").asText())) {
                throw new Exception("Error: " + result);
            }

            System.out.println(jsonResult.get("data"));
        }
    }

}


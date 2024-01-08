package com.organica.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.organica.entities.Product;
import com.organica.payload.ProductDto;
import com.organica.services.ProductService;
import com.organica.services.SunskyApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/sunsky")
public class SunskyApiController {
    @Value("${my.api.key}")
    private String key;
    @Value("${my.api.secret}")
    private String secret;
    @Autowired
    SunskyApiService sunskyApiService;
    @Autowired
    ProductService productService;

    @GetMapping
    public String addSpecificProduct() throws Exception{

        String apiUrl = "https://open.sunsky-online.com/openapi/product!detail.do";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", key);
        parameters.put("itemNo", "EDA005225701A");

        String product = sunskyApiService.call(apiUrl, secret, parameters);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(product);

        System.out.println(product);

        ProductDto productDto = new ProductDto();
        productDto.setProductName(jsonNode.get("data").get("name").asText());
        productDto.setDescription(jsonNode.get("data").get("description").asText());
        productDto.setPrice(Float.valueOf(jsonNode.get("data").get("price").asText()));
        productDto.setWeight(Float.valueOf(jsonNode.get("data").get("packWeight").asText()));
        productDto.setImg("C:\\Users\\ma43k\\Plocha\\EDA005225701A.jpg");
        productService.CreateProduct(productDto);

        return "Metoda byla spuštěna.";
    }

    @GetMapping("/picture")
    public String getProductPictureFromAPI()  throws Exception{


        String apiUrl = "https://open.sunsky-online.com/openapi/product!getImages.do";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", key);
        parameters.put("itemNo", "EDA005225701A");
        parameters.put("size", "250");
        parameters.put("watermark", "mysite.com");
        sunskyApiService.download(apiUrl, secret, parameters);
        return "Metoda bylas pustena";
    }

    @GetMapping("/allProducts")
    public String addAllProducts() throws Exception{
        String apiUrl = "https://open.sunsky-online.com/openapi/product!search.do";

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", key);
        parameters.put("categoryId", "100833");

        String result = sunskyApiService.call(apiUrl, secret, parameters);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResult = objectMapper.readTree(result);
        if (! "success".equals(jsonResult.get("result").asText())) {
            throw new Exception("Error: " + result);
        }

        JsonNode jsonListModel = jsonResult.get("data");
        int pageCount = jsonListModel.get("pageCount").asInt();

        for (int i = 0; i < 1; i++) {
            parameters.put("page", Integer.toString(i + 1));
            result = sunskyApiService.call(apiUrl, secret, parameters);

            jsonResult = objectMapper.readTree(result);
            System.out.println(jsonResult);
            if (! "success".equals((jsonResult.get("result").asText()))) {
                throw new Exception("Error: " + result);
            }

            jsonListModel = jsonResult.get("data").get("result");
            for (JsonNode productNode: jsonListModel){

                ProductDto productDto = new ProductDto();
                productDto.setProductName(productNode.get("name").asText());
                productDto.setDescription(productNode.get("description").asText());
                productDto.setPrice(Float.valueOf(productNode.get("price").asText()));
                productDto.setWeight(Float.valueOf(productNode.get("packWeight").asText()));
                productDto.setImg("\\images\\product\\"+productNode.get("itemNo").asText());
                productService.CreateProduct(productDto);
            }}

        return "Metoda bylas pustena";
    }

    @GetMapping("/pictures")
    public String getProductPicturesFromAPI()  throws Exception{

        String apiUrl = "https://open.sunsky-online.com/openapi/product!search.do";

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", key);
        parameters.put("categoryId", "100833");

        String result = sunskyApiService.call(apiUrl, secret, parameters);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResult = objectMapper.readTree(result);
        if (! "success".equals(jsonResult.get("result").asText())) {
            throw new Exception("Error: " + result);
        }

        JsonNode jsonListModel = jsonResult.get("data");
        int pageCount = jsonListModel.get("pageCount").asInt();

        for (int i = 0; i < 1; i++) {
            parameters.put("page", Integer.toString(i + 1));
            result = sunskyApiService.call(apiUrl, secret, parameters);

            jsonResult = objectMapper.readTree(result);
            System.out.println(jsonResult);
            if (! "success".equals((jsonResult.get("result").asText()))) {
                throw new Exception("Error: " + result);
            }

            jsonListModel = jsonResult.get("data").get("result");
            for (JsonNode productNode: jsonListModel) {

                    apiUrl = "https://open.sunsky-online.com/openapi/product!getImages.do";
                    Map<String, String> parameters1 = new HashMap<String, String>();
                    parameters1.put("key", key);
                    parameters1.put("itemNo", productNode.get("itemNo").asText());
                    parameters1.put("size", "250");
                    parameters1.put("watermark", "mysite.com");
                    sunskyApiService.download(apiUrl, secret, parameters1);
                }
            }
        return "Metoda bylas pustena";
    }

}

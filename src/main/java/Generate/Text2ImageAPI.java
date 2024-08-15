package Generate;

import com.google.gson.JsonArray;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.*;

public class Text2ImageAPI {
    private String url;
    private String apiKey;
    private String secretKey;
    private CloseableHttpClient client;

    public Text2ImageAPI(String url, String apiKey, String secretKey) {
        this.url = url;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        client = HttpClients.createDefault();
    }

    public String getModel() throws IOException {
        HttpGet request = new HttpGet(url + "key/api/v1/models");
        request.setHeader("X-Key", "Key " + apiKey);
        request.setHeader("X-Secret", "Secret " + secretKey);

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        Gson gson = new GsonBuilder().create();

        JsonArray data = gson.fromJson(responseBody, JsonArray.class);
        return data.get(0).getAsJsonObject().get("id").getAsString();
    }

    public String generate(String prompt, String model, int images, int width, int height, Styles style, String negativePromptUnclip ) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "GENERATE");
        params.put("numImages", images);
        params.put("width", width);
        params.put("height", height);
        params.put("style", style);
        params.put("negativePromptUnclip", negativePromptUnclip);
        Map<String, Object> generateParams = new HashMap<>();
        generateParams.put("query", prompt);
        params.put("generateParams", generateParams);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("model_id", model);
        builder.addTextBody("params", new Gson().toJson(params), ContentType.APPLICATION_JSON);

        HttpPost request = new HttpPost(url + "key/api/v1/text2image/run");
        request.setHeader("X-Key", "Key " + apiKey);
        request.setHeader("X-Secret", "Secret " + secretKey);
        request.setEntity(builder.build());

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        Gson gson = new GsonBuilder().create();
        Map<String, Object> data = gson.fromJson(responseBody, Map.class);
        return (String) data.get("uuid");
    }

    public String[] checkGeneration(String requestId, int attempts, int delay) throws IOException, InterruptedException {
        for (int i = 0; i < attempts; i++) {
            HttpGet request = new HttpGet(url + "key/api/v1/text2image/status/" + requestId);
            request.setHeader("X-Key", "Key " + apiKey);
            request.setHeader("X-Secret", "Secret " + secretKey);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            Gson gson = new GsonBuilder().create();
            Map<String, Object> data = gson.fromJson(responseBody, Map.class);

            if (data.get("status").equals("DONE")) {
                ArrayList<String> images = (ArrayList<String>) data.get("images");
                return images.toArray(new String[0]);
            }
            Thread.sleep(delay * 1000);
        }
        return null;
    }

    public byte[] generatePicture(String promt, Styles style, String negativePrompt ) throws IOException, InterruptedException {
        String modelId = this.getModel();
        String uuid = this.generate(promt, modelId, 1, 512, 512, style, negativePrompt);
        String[] images = this.checkGeneration(uuid, 10, 10);
        String imageBase64 = images[0];
        return Base64.getDecoder().decode(imageBase64);
    }
}
package Generate;


import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataKeys {
    private String botToken;
    private String yourApiKey;
    private String yourSecretKey;
    private static final String FILE_NAME_SRC = "src/main/resources/data.json";

    public DataKeys() {
    }

    public DataKeys(String botToken, String yourApiKey, String yourSecretKey) {
        this.botToken = botToken;
        this.yourApiKey = yourApiKey;
        this.yourSecretKey = yourSecretKey;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getYourApiKey() {
        return yourApiKey;
    }

    public String getYourSecretKey() {
        return yourSecretKey;
    }


    public static List<DataKeys> dataSet() throws ParseException {
        String json = readString(FILE_NAME_SRC);
        List<DataKeys> listKeys = jsonToList(json);
        if (listKeys.isEmpty()) {
            throw new RuntimeException("Файл data.json пуст или не содержит правильных данных");
        }
        return listKeys;
    }


    public static String readString(String fileName) {
        JSONParser parser = new JSONParser();
        try (FileReader fileReader = new FileReader(fileName)) {
            Object result = parser.parse(fileReader);
            JSONArray jsonArray = (JSONArray) result;
            return jsonArray.toJSONString();
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Ошибка чтения: " + fileName, e);
        }
    }

    public static List<DataKeys> jsonToList(String json) throws ParseException {
        Gson gson = new GsonBuilder().create();
        JSONParser parser = new JSONParser();
        List<DataKeys> listKeys = new ArrayList<>();

        JSONArray jsonArray = (JSONArray) parser.parse(json);
        for (Object jsonObject : jsonArray) {
            DataKeys listKey = gson.fromJson(jsonObject.toString(), DataKeys.class);
            listKeys.add(listKey);
        }
        return listKeys;
    }

}

package Generate;

import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    final static String BOT_TOKEN;
    final static String YOUR_API_KEY;
    final static String YOUR_SECRET_KEY;

    static {
        try {
            BOT_TOKEN = DataKeys.dataSet().get(0).getBotToken();
            YOUR_API_KEY = DataKeys.dataSet().get(0).getYourApiKey();
            YOUR_SECRET_KEY = DataKeys.dataSet().get(0).getYourSecretKey();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    final static String URL = "https://api-key.fusionbrain.ai/";

    public static void main(String[] args) throws ParseException {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new GenerateBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("Bot successfully started!");
    }
}
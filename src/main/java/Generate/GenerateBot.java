package Generate;

import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.io.IOException;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class GenerateBot extends TelegramLongPollingBot {

    final static String BOT_TOKEN;
    final static String YOUR_API_KEY;
    final static String YOUR_SECRET_KEY;
    final static String URL = "https://api-key.fusionbrain.ai/";

    static {
        try {
            BOT_TOKEN = DataKeys.dataSet().get(0).getBotToken();
            YOUR_API_KEY = DataKeys.dataSet().get(0).getYourApiKey();
            YOUR_SECRET_KEY = DataKeys.dataSet().get(0).getYourSecretKey();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private final pictSettings mySettings = new pictSettings();
    private static boolean flagSets = false;
    private static boolean flagQuestion = false;

    GenerateBot() {

    }

    public static boolean isFlagSets() {
        return flagSets;
    }

    public static void setFlagSets(boolean flagSets) {
        GenerateBot.flagSets = flagSets;
    }

    public static boolean isFlagQuestion() {
        return flagQuestion;
    }

    public static void setFlagQuestion(boolean flagQuestion) {
        GenerateBot.flagQuestion = flagQuestion;
    }

    @Override
    public String getBotUsername() {
        return "PictGenerateBot";
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                //Извлекаем из объекта сообщение пользователя
                Message inMsg = update.getMessage();
                //Достаем из inMess id чата пользователя
                Long chatId = inMsg.getChatId();
                start(chatId, "ПиктоБот:");
                String response;
                String textMsg = inMsg.getText();
                SendMessage outMsg = new SendMessage();
                outMsg.setChatId(chatId);
                if (textMsg.equalsIgnoreCase("/start") || textMsg.equalsIgnoreCase("start")) {
                    response = """
                            Я бот созданный для генерации изображений! Набирай:
                            /get - сгенерировать изображение
                            Необходимо ввести Get описание_изображения
                            /negative - установить негативный промпт
                            Необходимо ввести Negative негативный_промпт
                            /info - показать текущие параметры, поменять стиль
                            /help - показать список команд""";
                    start(chatId, response);
                } else if (textMsg.equalsIgnoreCase("/help") || textMsg.equalsIgnoreCase("help")) {
                    response = """
                            /help - показ команд
                            /get - генерация изображения
                            /negative - установка негативного промпта
                            /restart - обнуление текущих параметров
                            /info - показывает текущие параметры
                                    вызывает меню смены стиля.""";
                    start(chatId, response);
                } else if (textMsg.startsWith("Get ") || textMsg.startsWith("get ")) {
                    mySettings.setPrompt(textMsg.substring(4)); // вырезаем "Get " или "get "
                    response = "Генерирую изображение...";
                    start(chatId, response);
                    if(mySettings.getNegativePrompt().equals("пусто")){
                        mySettings.setNegativePrompt("");
                        generation(chatId, mySettings.getPrompt(), mySettings.getStyle(), mySettings.getNegativePrompt());
                        GenerateBot.setFlagQuestion(false);
                        mySettings.setNegativePrompt("пусто");
                    }
                    else {
                        generation(chatId, mySettings.getPrompt(), mySettings.getStyle(), mySettings.getNegativePrompt());
                        GenerateBot.setFlagQuestion(false);
                    }
                } else if (textMsg.startsWith("Negative ") || textMsg.startsWith("negative ") ||textMsg.startsWith("NEGATIVE ") ) {
                    mySettings.setNegativePrompt(textMsg.substring(9)); // вырезаем
                    response = "Установлен негативный промпт: " + mySettings.getNegativePrompt();
                    start(chatId, response);
                } else if (textMsg.equalsIgnoreCase("/get") || textMsg.equalsIgnoreCase("get")) {
                    if (mySettings.getPrompt().equals("пусто") || mySettings.getPrompt().isEmpty()) {
                        response = "Введите промпт в виде: Get prompt, где prompt - описание изображения.";
                        start(chatId, response);
                    } else {
                        question(chatId);
                    }
                } else if (textMsg.equalsIgnoreCase("/negative") || textMsg.equalsIgnoreCase("negative")) {
                    response = "Введите Негативный промпт в виде: Negative prompt,\n" +
                            " где prompt - цвет или отдельные вещи, которые исключаются";
                    start(chatId, response);
                }
                else if (textMsg.equalsIgnoreCase("/info") || textMsg.equalsIgnoreCase("info")) {
                    response = "Заданы следующие параметры:\n" +
                            "Prompt: " + mySettings.getPrompt() + "\n" +
                            "Negative: " + mySettings.getNegativePrompt() + "\n" +
                            "Style: " + mySettings.getStyle();
                    setSettings(chatId, response);
                } else if (textMsg.equals("KANDINSKY") && GenerateBot.isFlagSets()) {
                    response = "Устанавливаем стиль KANDINSKY.";
                    mySettings.setStyle(Styles.KANDINSKY);
                    GenerateBot.setFlagSets(false);
                    start(chatId, response);
                } else if (textMsg.equals("UHD") && GenerateBot.isFlagSets()) {
                    response = "Устанавливаем стиль UHD.";
                    mySettings.setStyle(Styles.UHD);
                    GenerateBot.setFlagSets(false);
                    start(chatId, response);
                } else if (textMsg.equals("ANIME") && GenerateBot.isFlagSets()) {
                    response = "Устанавливаем стиль ANIME.";
                    mySettings.setStyle(Styles.ANIME);
                    GenerateBot.setFlagSets(false);
                    start(chatId, response);
                } else if (textMsg.equals("DEFAULT") && GenerateBot.isFlagSets()) {
                    response = "Устанавливаем стиль DEFAULT.";
                    mySettings.setStyle(Styles.DEFAULT);
                    GenerateBot.setFlagSets(false);
                    start(chatId, response);
                } else if (textMsg.equals("YES") && GenerateBot.isFlagQuestion()) {
                    response = "Генерирую изображение c промпта: " + mySettings.getPrompt();
                    start(chatId, response);
                    if(mySettings.getNegativePrompt().equals("пусто")){
                        mySettings.setNegativePrompt("");
                        generation(chatId, mySettings.getPrompt(), mySettings.getStyle(), mySettings.getNegativePrompt());
                        GenerateBot.setFlagQuestion(false);
                        mySettings.setNegativePrompt("пусто");
                    }
                    else {
                        generation(chatId, mySettings.getPrompt(), mySettings.getStyle(), mySettings.getNegativePrompt());
                        GenerateBot.setFlagQuestion(false);
                    }
                } else if (textMsg.equals("NO") && GenerateBot.isFlagQuestion()) {
                    response = "Введите промпт в виде: Get prompt, где prompt - описание изображения.";
                    GenerateBot.setFlagQuestion(false);
                    start(chatId, response);
                } else if (textMsg.equalsIgnoreCase("RESTART") || (textMsg.equalsIgnoreCase("/RESTART"))) {
                    response = "Производится обнуление настроек и рестарт бота!";
                    start(chatId, response);
                    restart(chatId);
                } else if (textMsg.equals("ESC")) {
                    start(chatId, "");
                } else {
                    response = "Команда не распознана";
                    start(chatId, response);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void start(long chatId, String str) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Start"));
        row1.add(new KeyboardButton("Help"));
        keyboardRows.add(row1);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Get"));
        row2.add(new KeyboardButton("Info"));
        keyboardRows.add(row2);
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("RESTART"));
        keyboardRows.add(row3);
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);

         SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(str);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
    private void deleteMessage(long chatId, int messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(messageId);
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void restart(long chatId) {
        mySettings.setStyle(Styles.DEFAULT);
        mySettings.setPrompt("пусто");
        mySettings.setNegativePrompt("пусто");
        start(chatId,"ПиктоБот: ");
    }
    private void question(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("YES"));
        row1.add(new KeyboardButton("NO"));
        keyboardRows.add(row1);

        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);

        GenerateBot.setFlagQuestion(true);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        String str = "Генерировать с текущими настройками?\n"+
                "Заданы следующие параметры:\n" +
                "Prompt: " + mySettings.getPrompt() + "\n" +
                "Negative: " + mySettings.getNegativePrompt() + "\n" +
                "Style: " + mySettings.getStyle();
        message.setText(str);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void setSettings(long chatId, String str) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        GenerateBot.setFlagSets(true);
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("KANDINSKY"));
        row1.add(new KeyboardButton("UHD"));
        keyboardRows.add(row1);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("ANIME"));
        row2.add(new KeyboardButton("DEFAULT"));
        keyboardRows.add(row2);
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("ESC"));
        keyboardRows.add(row3);

        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        StringBuilder response = new StringBuilder();
        response.append("Имеется 4 разных стиля:\n");
        response.append("KANDINSKY - обычный\n");
        response.append("UHD - детальный\n");
        response.append("ANIME - анимэ\n");
        response.append("DEFAULT - по умолчанию.\n");
        response.append("\n");
        response.append(str);
        message.setText(response.toString());
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void generation(long chatId, String prompt, Styles style, String negativePrompt) throws IOException, InterruptedException {
        Text2ImageAPI api = new Text2ImageAPI(URL, YOUR_API_KEY, YOUR_SECRET_KEY);
        byte[] imageData = api.generatePicture(prompt, style, negativePrompt);
        try (FileOutputStream file = new FileOutputStream("image.jpg")) {
            file.write(imageData);
        } catch (IOException e) {
            System.err.println("Ошибка записи файла: " + e.getMessage());
        }
        File file = new File("image.jpg");
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(file));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
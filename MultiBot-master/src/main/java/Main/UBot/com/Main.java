package Main.UBot.com;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;


public class Main {
    public static void main(String[] args){
        Thread botThread = new Thread(botRun);
        botThread.start();
    }
    private static Runnable botRun = () -> {
        ApiContextInitializer.init();
        TelegramBotsApi tgBotsAPI = new TelegramBotsApi();
        MainBotController currentBot = new MainBotController();
        try {
            tgBotsAPI.registerBot(currentBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    };
}

package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.function.BiConsumer;

public class OnlineSession extends Session {
    private boolean choosedStatus;
    transient public static final String[] wrongNameCityMessage = {
            "Unknown city, try again", "Please, verify your city name",
            "That is not correct city name, try again", "There is no city with that name in our data base, try again"
    };
    private transient List<String> buttons = new ArrayList<>(Collections.singletonList("|HOME|"));
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;

    public OnlineSession() {
        super();
    }

    public OnlineSession(boolean sessionOpened, boolean choosedStatus) {
        super(sessionOpened);
        this.choosedStatus = choosedStatus;
    }

    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }

    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if (!choosedStatus) {
            choosedStatus = true;
            return "Выберите свой сетевой статус.";
        }
        switch (inputTxt) {
            case "Online":
                if (user.isOnlineStatus()) {
                    terminateAllProcesses();
                    return "Ваш статус уже 'online'";
                }
                user.setOnlineStatus(true);
                terminateAllProcesses();
                return "Ваш статус переключен в 'online'";
            case "Offline":
                if (!user.isOnlineStatus()) {
                    terminateAllProcesses();
                    return "Ваш статус уже 'offline'";
                }
                user.setOnlineStatus(false);
                terminateAllProcesses();
                return "Ваш статус переключен в 'offline'";
            default:
                return "Непонятная для меня операция.";
        }
    }

    private void setButtons(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rpl = new ReplyKeyboardMarkup();
        KeyboardRow k_r1 = new KeyboardRow();
        k_r1.add("Online");
        KeyboardRow k_r2 = new KeyboardRow();
        k_r2.add("Offline");
        rpl.setKeyboard(List.of(k_r1, k_r2));
        rpl.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(rpl);
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = choosedStatus = false;
    }

    @Override
    public String toString() {
        return "OnlineSession{" +
                "isChoosedCity=<" + choosedStatus +
                ">, sessionOpened=<" + sessionOpened +
                ">}";
    }
}

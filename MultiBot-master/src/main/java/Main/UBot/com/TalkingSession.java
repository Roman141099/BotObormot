package Main.UBot.com;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class TalkingSession extends Session{
    private boolean defMessageSent;
    private transient List<String> buttons = new ArrayList<>(Collections.singletonList("|HOME|"));
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;

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
    public String nextStep(String inputTxt, Message message) {
        inputTxt = inputTxt.toLowerCase();
        if(!defMessageSent){
            defMessageSent = true;
            return "Выберите вариант занятости";
        }
        if(inputTxt.equals("свободен") || inputTxt.equals("занят")){
            terminateAllProcesses();
            return "Ваш статус занятости был переключен на " + inputTxt;
        }
        else return "Я не понимаю";
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = defMessageSent = false;
    }

    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }
}

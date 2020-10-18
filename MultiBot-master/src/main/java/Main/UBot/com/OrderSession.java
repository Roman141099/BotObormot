package Main.UBot.com;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class OrderSession extends Session {
    private boolean orderConfirmed;
    private transient List<String> buttons = List.of("Принять", "Отклонить");
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;

    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if (inputTxt.equals("Отклонить")) {
            user.setBusy(false);
            user.setGotOrder(false);
            terminateAllProcesses();
            return "Вы отказались от заказа";
        }
        if (inputTxt.equals("Принять")) {
            orderConfirmed = true;
            buttons = List.of("Доставлено", "Не доставлено");
            return "Вы приняли заказ, при успешном его выполнении необходимо подтвердить его доставку клавишей 'Доставлено'";
        }
        if (orderConfirmed) {
            if (inputTxt.equals("Не доставлено")) {
                terminateAllProcesses();
                return "Оставьте причину товара вашему куратору";
            } else if (inputTxt.equals("Доставлено")) {
                terminateAllProcesses();
                return "Поздравляем, вы успешно доставили товар!";
            } else {
                return "Неизвестная для меня операция";
            }
        }
        return "Неизвестная для меня операция";
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = orderConfirmed = false;
    }

    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }

    private void setButtons(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rpl = new ReplyKeyboardMarkup();
        rpl.setResizeKeyboard(true);
        KeyboardRow kr = new KeyboardRow();
        kr.addAll(buttons);
        rpl.setKeyboard(List.of(kr));
        sendMessage.setReplyMarkup(rpl);
    }
}

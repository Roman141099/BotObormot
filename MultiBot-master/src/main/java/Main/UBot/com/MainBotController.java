package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class MainBotController extends TelegramLongPollingBot implements Runnable {
    private static final String URL_TO_DB = "MultiBot-master/src/main/resources/Data bases/sessionStates.json";
    private static final Gson jsonSessionDeserializer = new GsonBuilder().setPrettyPrinting().
            registerTypeAdapter(Session.class, new SessionDeserializer()).create();
    private static final Map<Long, User> sessionState;

    static {
        sessionState = uploadInfo(Paths.get(URL_TO_DB));
        Thread orderEx = new Thread(new MainBotController());
        orderEx.start();
    }

    //References for sessions
    private final BiConsumer<Message, User> setStatus = this::OnlineOfflineFunc;
    private final BiConsumer<Message, User> letsTalk = this::BusyFreeFunc;
    private final BiConsumer<Message, User> continueSession = this::continueSession;
    private final BiConsumer<Message, User> goHome = this::goHome;
    //References for keyboards
    private final BiConsumer<SendMessage, User> buttonsWeather = this::buttonsOnlineOffline;
    private final BiConsumer<SendMessage, User> buttonsLetsTalk = this::freeOrBusy;
    private final BiConsumer<SendMessage, User> defaultKeyboard = this::setMainKeyBoard;
    private final Map<String, BiConsumer<Message, User>> middleOpers = Map.of("online/offline", setStatus,
            "free/busy", letsTalk);


    @Override
    public void run() {
        while (true) {
            User user = null;
            while (user == null) {
                user = sessionState.values().stream().filter(o -> !o.isBusy() && o.isOnlineStatus()).findFirst().orElse(null);
            }
            if (!user.hasGeo()) {
                SendMessage reqLoc = new SendMessage();
                reqLoc.setText("Пожалуйста, отправьте вашу геопозицию");
                reqLoc.setChatId(user.getUserChatId());
                user.setBusy(true);

                sessionState.put(user.getUserChatId(), user);
                recordInfo(Paths.get(URL_TO_DB));

                try {
                    execute(reqLoc);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (user.hasGeo() && user.getGeoPoint().length() > 1) {
                user.setHasGeo(false);
                System.out.println("Одобрен заказ");
                SendChatAction sc = new SendChatAction();
                sc.setChatId(user.getUserChatId());
                sc.setAction(ActionType.TYPING);
                try {
                    execute(sc);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SendMessage sendOrder = new SendMessage();
                sendOrder.setChatId(user.getUserChatId());
                user.setBusy(true);
                user.setGotOrder(true);
                user.setCurrentSession(new OrderSession());

                sessionState.put(user.getUserChatId(), user);
                recordInfo(Paths.get(URL_TO_DB));

                sendOrder.setText("Вам одобрен заказ на доставку холодильника\nПринять заказ?");
                ReplyKeyboardMarkup acceptOrd = new ReplyKeyboardMarkup();
                acceptOrd.setResizeKeyboard(true);
                KeyboardRow rowAcc = new KeyboardRow();
                rowAcc.add("Принять");
                rowAcc.add("Отклонить");
                acceptOrd.setKeyboard(List.of(rowAcc));
                sendOrder.setReplyMarkup(acceptOrd);
                try {
                    execute(sendOrder);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        User user = new User(message.getChatId());
        if (message.hasLocation()) {
            System.out.println(message.getLocation());
            if(sessionState.containsKey(user.getUserChatId())){

                user = sessionState.get(user.getUserChatId());

                user.setGeoPoint(message.getLocation().toString());
                user.setBusy(false);
                user.setHasGeo(true);

                sessionState.put(user.getUserChatId(), user);
                recordInfo(Paths.get(URL_TO_DB));
            }
        } else {
            String userMessage = message.getText().toLowerCase().trim();
            System.out.println("Активность от " + sessionState.get(message.getChatId()));
            if (!sessionState.containsKey(user.getUserChatId())) {
                Chat userChat = message.getChat();
                user.setFirstName(userChat.getFirstName() != null ? userChat.getFirstName() : "Unknown");
                user.setLastName(userChat.getLastName() != null ? userChat.getLastName() : "Unknown");

                sessionState.put(message.getChatId(), user);
                //Recording new user to Json
                startBot(message);
                recordInfo(Paths.get(URL_TO_DB));

                System.out.println("Создался новый user, id=" + message.getChatId());
            } else {
                user = sessionState.get(message.getChatId());
                if (userMessage.equals("ok")) {
                    sendMessage(message, "Список моих возможностей", defaultKeyboard);
                }
                System.out.println("User from data base activated, id=" + user.getUserChatId());
            }
            if (user.getCurrentSession() == null || !user.getCurrentSession().isSessionOpened()) {
                if (middleOpers.get(userMessage) != null) middleOpers.get(userMessage).accept(message, user);
                else if (userMessage.equals("моя информация")) sendMyInfo(user);
            } else {
                if (userMessage.equals("⬅на главную")) goHome.accept(message, user);
                else {
                    continueSession.accept(message, user);
                    System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
                }
            }
        }
    }

    private void updateGeo(User user, String location) {
        user.setGeoPoint(location);
        user.setHasGeo(true);
        sessionState.put(user.getUserChatId(), user);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void sendMyInfo(User user) {
        SendMessage sm = new SendMessage();
        sm.setChatId(user.getUserChatId());
        String info = String.format(
                "Сетевой статус - %s\n" +
                        "Статус занятости - %s\n" +
                        "Рейтинг - \n" +
                        "Заработано - \n" +
                        "Заказов доставлено - ", user.isOnlineStatus() ? "Online" : "Offline",
                user.isBusy() ? "Занят" : "Свободен");
        sm.setText(info);
        SendChatAction sc = new SendChatAction();
        sc.setChatId(user.getUserChatId());
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void buttonsOnlineOffline(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        KeyboardRow homeRow = new KeyboardRow();
        KeyboardRow k_r1 = new KeyboardRow();
        k_r1.add("Online");
        KeyboardRow k_r2 = new KeyboardRow();
        k_r2.add("Offline");
        homeRow.add("⬅на главную");
        rkm.setResizeKeyboard(true);
        rkm.setKeyboard(List.of(homeRow, k_r1, k_r2));
        sendMessage.setReplyMarkup(rkm);
    }

    private void freeOrBusy(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        KeyboardRow homeRow = new KeyboardRow();
        homeRow.add("⬅на главную");
        rkm.setResizeKeyboard(true);
        KeyboardRow k_r1 = new KeyboardRow();
        k_r1.add("Free");
        KeyboardRow k_r2 = new KeyboardRow();
        k_r2.add("Busy");
        rkm.setResizeKeyboard(true);
        rkm.setKeyboard(List.of(homeRow, k_r1, k_r2));
        sendMessage.setReplyMarkup(rkm);
    }

    private void OnlineOfflineFunc(Message message, User user) {
        user.setCurrentSession(new OnlineSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsWeather);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void BusyFreeFunc(Message message, User user) {
        user.setCurrentSession(new BusyFreeSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsLetsTalk);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void continueSession(Message message, User user) {
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), user.getCurrentSession().getButtonsMarkUp());
        recordInfo(Paths.get(URL_TO_DB));
        if (!user.getCurrentSession().isSessionOpened())
            sendMessage(message, "Выберите операцию", defaultKeyboard);
    }


    private void startBot(Message msg) {
        ReplyKeyboardMarkup startMarkUp = new ReplyKeyboardMarkup();
        SendMessage startMsg = new SendMessage();
        String startMessage = "Привет, я - твой помощник в работе в курьерской службе MyPoint. Все инструкции будут ниже.";

        startMsg.setText(startMessage);

        startMsg.setChatId(msg.getChatId());
        startMarkUp.setOneTimeKeyboard(true);
        startMarkUp.setResizeKeyboard(true);
        KeyboardRow startRow = new KeyboardRow();
        startRow.add("OK");
        startMarkUp.setKeyboard(List.of(startRow));
        startMsg.setReplyMarkup(startMarkUp);
        try {
            execute(startMsg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void goHome(Message message, User user) {
        user.getCurrentSession().terminateAllProcesses();
        sendMessage(message, "Выберите категорию", defaultKeyboard);
        recordInfo(Paths.get(URL_TO_DB));
        System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
    }

    private void setMainKeyBoard(SendMessage sM, User user) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(false);

        KeyboardRow onlineOfflineRow = new KeyboardRow();
        onlineOfflineRow.add(new KeyboardButton("Online/Offline"));

        KeyboardRow busyFreeRow = new KeyboardRow();
        busyFreeRow.add("Free/Busy");

        KeyboardRow infoRow = new KeyboardRow();
        infoRow.add("Моя информация");

        KeyboardRow locRow = new KeyboardRow();
        KeyboardButton locBut = new KeyboardButton();
        locBut.setText("Отправить геоданные");
        locBut.setRequestLocation(true);
        locRow.add(locBut);

        List<KeyboardRow> keys = List.of(onlineOfflineRow, busyFreeRow, infoRow, locRow);
        markup.setKeyboard(keys);
        sM.setReplyMarkup(markup);
    }

    @Override
    public String getBotUsername() {
        return "MyPointWorkBot";
    }

    @Override
    public String getBotToken() {
        return "1249399381:AAHFR8tvJkcQyz1DTB294mr5B45Y8kMWtHM";
    }

    private void sendMessage(Message msg, String text, BiConsumer<SendMessage, User> buttons) {
        SendMessage sMObj = new SendMessage();
        sMObj.enableMarkdown(true);
        sMObj.setChatId(msg.getChatId());
        sMObj.setText(text);
        
        //Sets keyboard to current session
        buttons.accept(sMObj, sessionState.get(msg.getChatId()));

        try {
            execute(sMObj);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static Map<Long, User> uploadInfo(Path path) {
        Type mapType = new TypeToken<ConcurrentHashMap<Long, User>>() {
        }.getType();
        Map<Long, User> usersDb = Collections.emptyMap();
        try (FileReader reader = new FileReader(path.toString())) {
            usersDb = jsonSessionDeserializer.fromJson(reader, mapType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usersDb;
    }

    private static void recordInfo(Path path) {
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(jsonSessionDeserializer.toJson(sessionState));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

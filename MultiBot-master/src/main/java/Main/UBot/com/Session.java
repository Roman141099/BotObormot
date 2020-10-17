package Main.UBot.com;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

public abstract class Session {

    protected boolean sessionOpened;

    public Session(boolean sessionOpened) {
        this.sessionOpened = sessionOpened;
    }

    public Session() {
        sessionOpened = true;
    }

    public boolean isSessionOpened() {
        return sessionOpened;
    }

    public void setSessionOpened(boolean sessionOpened) {
        this.sessionOpened = sessionOpened;
    }

    //The next step in all sessions
    public abstract String nextStep(String inputTxt, Message message, User user);

    public abstract void terminateAllProcesses();

    public abstract BiConsumer<SendMessage, User> getButtonsMarkUp();

}

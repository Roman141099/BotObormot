package Main.UBot.com;

public enum Phrases {
    SENDED_FROM_BOT("Отправлено от бота"), SENDED_FROM_USER("Отправлено от пользователя");
    private final String message;
    Phrases(String message){
        this.message = message;
    }

}

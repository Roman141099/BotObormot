package Main.UBot.com;

public class NotFoundForecastException extends RuntimeException{
    public NotFoundForecastException(String message) {
        super(message);
    }

    public NotFoundForecastException() {
        super("Weather parser object can not be null");
    }
}

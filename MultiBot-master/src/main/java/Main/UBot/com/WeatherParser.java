package Main.UBot.com;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class WeatherParser {
    private final String country;
    private final String name;
    private final String description;
    private final String degrees;
    private final String icon;
    private final String mainDescription;
    private final double feelsLike;
    private final double humidity;
    private final int windSpeed;
    private final long sunrise;
    private final long sunset;
    private final long dt;

    public WeatherParser(String country, String name, String description, String degrees, String icon,
                         String mainDescription, double feelsLike, double humidity, int windSpeed, long sunrise,
                         long sunset, long dt) {
        this.country = country;
        this.name = name;
        this.description = description;
        this.degrees = degrees;
        this.icon = icon;
        this.mainDescription = mainDescription;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.dt = dt;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        String formattedPars = "Well, now weather states at\n\uD83D\uDDD3 %s\nIn %s (%s) are :\n\uD83C\uDF21Temperature is %s\nfeels like %3.2f \u2103." +
                "\n☁Description - %s, %s\n\uD83D\uDCA7Humidity ≈ %2.2f%s\n\uD83C\uDF2AWind speed equals %s km/h.\n\uD83C\uDF05Sunrise at %s .\n\uD83C\uDF07Sunset at %s .";
        LocalDateTime now = LocalDateTime.ofEpochSecond(dt, 0, ZoneOffset.of("+3"));
        String dateTime = now.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        now = LocalDateTime.ofEpochSecond(sunrise, 0, ZoneOffset.UTC);
        String sunriseTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        now = LocalDateTime.ofEpochSecond(sunset, 0, ZoneOffset.of("+3"));
        String sunsetTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format(formattedPars, dateTime, name, country, degrees + ", ", feelsLike, mainDescription,
                description + ".", humidity, "%.", windSpeed, sunriseTime, sunsetTime);
    }
}

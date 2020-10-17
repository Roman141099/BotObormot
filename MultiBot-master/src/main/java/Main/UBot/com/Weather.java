package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class Weather {
    private final static String WEATHER_API_KEY = "your API key";
    private final static String headURL = "https://api.openweathermap.org/data/2.5/weather?q=";
    protected static Optional<WeatherParser> getWeatherNow(String cityInput){
        String URL = headURL + cityInput + "&units=metric&appid=" + WEATHER_API_KEY;
        HttpURLConnection weatherRequest = null;
        String jsonResponse;
        try {
            weatherRequest = (HttpURLConnection) new URL(URL).openConnection();
            weatherRequest.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert weatherRequest != null;
            try (BufferedReader bR = new BufferedReader(
                    new InputStreamReader(weatherRequest.getInputStream()))){
                jsonResponse = bR.lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject j_Obj = gson.fromJson(jsonResponse, JsonObject.class);
        System.out.println(jsonResponse);
        WeatherParser wP = new WeatherParser(
                getCountry(j_Obj),
                getName(j_Obj),
                getWeatherNow(j_Obj),
                getDegrees(j_Obj),
                getIconField(j_Obj),
                getMainDescription(j_Obj),
                getFeelsLike(j_Obj),
                getHumidity(j_Obj),
                getWindSpeed(j_Obj),
                getSunriseTime(j_Obj),
                getSunsetTime(j_Obj),
                getDt(j_Obj));
        weatherRequest.disconnect();
        return Optional.of(wP);
    }
    private static String getWeatherNow(JsonObject j_Object){
        JsonArray j_Array = j_Object.getAsJsonArray("weather");
        String desc = j_Array.get(0).getAsJsonObject().get("description").getAsString();
        return desc.toUpperCase().charAt(0) + desc.substring(1);
    }
    private static String getName(JsonObject j_Object){
        return j_Object.get("name").getAsString();
    }
    private static String getCountry(JsonObject j_Object){
        JsonObject j_Sys = j_Object.getAsJsonObject("sys");
        return j_Sys.get("country").getAsString();
    }
    private static String getDegrees(JsonObject j_Object){
        JsonObject j_Main = j_Object.getAsJsonObject("main");
        double degrees = j_Main.get("temp").getAsDouble();
        return degrees > 0 ? "plus " + degrees + " \u2103" : "minus " + Math.abs(degrees) + " \u2103";
    }

    private static String getIconField(JsonObject j_Object){
        JsonArray j_Array = j_Object.getAsJsonArray("weather");
        return j_Array.get(0).getAsJsonObject().get("icon").getAsString();
    }

    private static long getDt(JsonObject j_Object){
        return j_Object.get("dt").getAsLong();
    }

    private static double getFeelsLike(JsonObject j_Object){
        return j_Object.getAsJsonObject("main").get("feels_like").getAsDouble();
    }

    private static String getMainDescription(JsonObject j_Object){
        return j_Object.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
    }

    private static double getHumidity(JsonObject j_Object){
        return j_Object.getAsJsonObject("main").get("humidity").getAsDouble();
    }

    private static int getWindSpeed(JsonObject j_Object){
        return j_Object.getAsJsonObject("wind").get("speed").getAsInt();
    }

    private static long getSunriseTime(JsonObject j_Object){
        return j_Object.getAsJsonObject("sys").get("sunrise").getAsLong();
    }

    private static long getSunsetTime(JsonObject j_Object){
        return j_Object.getAsJsonObject("sys").get("sunset").getAsLong();
    }
}

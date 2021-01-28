import com.sun.xml.internal.fastinfoset.util.StringArray;
import org.checkerframework.checker.units.qual.A;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


public final class HttpRequest {
    private final static String SERVER_URL = "http://api.currencylayer.com/live?";
    private final static String SERVER_KEY = "access_key=5d0e13eee66b7418321740af4e183be5";
    // private final static String MAIN_CURRENCY = "&source=RUB";
    private final static String CURRENCY_PAIRS = "&currencies=EUR,RUB,GBP,CHF";



    private static String currencyDate = "";




    public static HashMap<String,Float> getCurrencyData() throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        URL url = createUrl(SERVER_URL + SERVER_KEY /*+ MAIN_CURRENCY*/ + CURRENCY_PAIRS);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String,Float> currencyRates = null;

        if (jsonResponse != null) {
            currencyRates = extractFeatureFromJson(jsonResponse);
        }
         return currencyRates;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }

        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }



    private static HashMap<String,Float> extractFeatureFromJson(String currencyJSON) throws ParseException {
        if (currencyJSON.isEmpty()) {
            return null;
        }

            HashMap<String,Float> pairsAndRates = new HashMap<>();
            JSONArray currencyPairsArray = new JSONArray();
            JSONArray currencyTimeArray = new JSONArray();
            Object obj = new JSONParser().parse(currencyJSON);
            JSONObject baseJsonResponse = (JSONObject) obj;
            currencyPairsArray.add(baseJsonResponse.get("quotes"));
            currencyTimeArray.add(baseJsonResponse.get("timestamp"));
            long date = (long) currencyTimeArray.get(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMMM yyyy 'г.,' HH:mm:ss z");
            currencyDate = dateFormat.format(date*1000);
            JSONObject jsonObject = (JSONObject) currencyPairsArray.get(0);
            Set keys = jsonObject.keySet();

        for (Object key : keys) {
            String s = (String) key;
            pairsAndRates.put(s, Float.parseFloat(jsonObject.get(s).toString()));
        }
        return pairsAndRates;
    }
    public static String getCurrencyDate() {
        return currencyDate;
    }
}
package test.designe.app.controllerapp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import java.util.Base64;


public class TokenManager {

    private static TokenManager tokenManager = null;
    private  static JsonParser jsonParser = new JsonParser();
    private JsonObject payload=null;
    private String token = null;

    private TokenManager(String token) throws JSONException {

        this.token = token;
        if(token!=null) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] chunks = token.split("\\.");
        String payload = new String(decoder.decode(chunks[1]));
        this.payload = (JsonObject) jsonParser.parse(payload);}
        else this.payload =null;
    }



    public String getToken() {
        return this.token;
    }
    public Integer getId() throws JSONException {
        return this.payload.get("id").getAsInt();
    }


    public String getRole() throws JSONException {
        return this.payload.get("roles").getAsJsonArray().get(0).getAsString();
    }


    public static TokenManager getInstance()
    {
        return tokenManager;
    }

    public static void setToken(String token) throws JSONException {
        tokenManager = new TokenManager(token);
    }

    public static boolean loadTokenFromFile()
    {
        throw new UnsupportedOperationException();
    }

    public static String bearer()
    {
        return "Bearer ";
    }

    public boolean isTokenNull()
    {
        return this.token==null;
    }
}

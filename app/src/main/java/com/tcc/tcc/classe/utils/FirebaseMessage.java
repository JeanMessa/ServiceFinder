package com.tcc.tcc.classe.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FirebaseMessage {

    public static void enviar(String title,String body,String token,int posicaoRemetente,String keyConversa,boolean isFotoPadrao,String keyServico ,Context context){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject messageObject = new JSONObject();
            JSONObject dataObject = new JSONObject();
            dataObject.put("title", title);
            dataObject.put("body", body);
            dataObject.put( "posicaoRemetente", String.valueOf(posicaoRemetente));
            dataObject.put("keyConversa",keyConversa);
            dataObject.put("isFotoPadrao",String.valueOf(isFotoPadrao));
            if (keyServico!=null){
                dataObject.put("keyServico",keyServico);
            }
            messageObject.put( "token", token);
            messageObject.put( "data", dataObject);
            mainObj.put("message", messageObject);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method. POST, "https://fcm.googleapis.com/v1/projects/tcc2024-e6adf/messages:send", mainObj, response -> {
                // code run got response
            }, volleyError -> {
                // code run error
            }) {

                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "Bearer "+ getAccesToken());
                    return header;
                }
            };
            requestQueue.add(request);
        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    public static void agendarNotificacao(String token,String keyServico ,Context context){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject messageObject = new JSONObject();
            JSONObject dataObject = new JSONObject();
            dataObject.put("keyServico",keyServico);
            messageObject.put( "token", token);
            messageObject.put( "data", dataObject);
            mainObj.put("message", messageObject);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method. POST, "https://fcm.googleapis.com/v1/projects/tcc2024-e6adf/messages:send", mainObj, response -> {
                // code run got response
            }, volleyError -> {
                // code run error
            }) {

                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "Bearer "+ getAccesToken());
                    return header;
                }
            };
            requestQueue.add(request);
        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    private static String getAccesToken() {
        final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

        String JsonString = "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"tcc2024-e6adf\",\n" +
                "  \"private_key_id\": \"35fabb5235ed9a86e59c5c381322d46777f11a59\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDCdRcZEbxPPB4S\\n+shvfLl7blmgAXOOQdXWEyIOV8+wEXY7MBIYIHdfTSIwiK9PXKJEMIP9rutOGlPP\\ngd543Ef6qK6e9LcXcXSAl5SCwRkmPLNI8SluGyVpGNhN7JUDTU+KGOtcB0ul3HTV\\naaU++3QSWRcRSHzT4+5ExUM0sdeP5J/UJzpclEBMna8dNHhuWzeUGN1bK77p0asY\\ncdyTwuu3ArYFsZ4Kmlp3df16NRB6pv2tdJ+mIqzMvC3ixLc3snQrT2016cKkUor3\\nsKVg89DNwBoj5ItfMO8yEA2Bfd/0tTLMQMmDT60P9YSpi5l4uZkQ/NNu/Aabej+7\\nxJPrmevnAgMBAAECggEANnrJitt+L5C/OEAMkbeW267x6zMc5dkqCLz5iJ0vcbui\\nrThtiVbnOssIbiKNQyXHzGTvRc6Q3CPiITvYUXIdtGs36Q7UkPvXUQOmtu9UlXLi\\nI+h03AU8+PHeAA7tPKyXQTB0GCdvGTvne7cKddfLECznmo79cfvk0F2X05aTtxsv\\nrxNCTw6mgY8d1dSydX042+9jfMdBW7MkxIMYpLYpYGkDetiNuyLd9b8TtpFhZftH\\npgDr9/Hq5Iemo/Y1smdjBv32Z9Ra2i55wAyBgXjoEK32WkIo9sD69UzndtspX+hc\\nYhvPaF51fwM7Hkm6PFBsIMVkjms8leFg10BTmBem8QKBgQDINQGYGljZazOjoHyj\\nmTp/WYyzkyJ8uunFptsLFRs2c8EpfMcoF6d16drP6nA+F5+TB5pKABxB8er/CZkc\\np54QkEDrF9kysyr2LgJf1zri8Yc0xtYnQqBriI6s+luPiuk84yeetD+BXAtuF/mh\\nt1dhuzWbPXvWvJ0MlOyLcM5Q7wKBgQD4peVhiGRJo9XGGHozeObopi5PSN6M6onO\\nG81foNTE8K68JpebFR0foiEP89Vi10z+zEVNc7/pdhMEfwFyNFJ/UXtTHq53c4qL\\n9HBqH6fCIMqjMFlfCaUq1HblQDMHL4/rcnGyDK1nOylQ5bAh3IjfcDExTu+6SoBc\\nJevclSEkiQKBgEZSuBihSIw2J5FWEfG1JOMOpWl+SSLe1LZkgZRG+aQKzNKxzZK7\\nnErCteVKR9rHmXmftgZAO6y+OPkUef+isNbIlMT/P3+bh3+pcZQQmdKVXBShsx9f\\nO9IJyXPhDMBC3uJQN957GcNqfWlSapDvQXZGt/GrixBGeDOvK9de7FlpAoGAfTeu\\naIuzVZJ4WaaCA+HjB06JC8x6pySF+ZHuvzXTcMp0dLRKrQLZA9E2LT5yO7CJSde9\\nHuAnyX543U7If6bx1MRIAvf0jJtXLlwPMKJ4bT7uTXhPyZil4QfWSGUoTXoaVEvy\\nDeRsfwxAJklFhgf1tfBeFaLW/039jmpaeV9wrWECgYEAhhTQVnubt7SMjln/hoZ0\\n0FPd6I5cEKt7yYrlLRFSFFFM7VGNy+VSXZaDM7ajX45pgYw4g2tIO5jFqaYnI//w\\nSD0mvrG/f7lmINucgQKdbKDSXnpkVUkn+crZF8F/rdH8v+gQoszSmCiKQ5PeNXmH\\nPQx0cEXO9oTPY6Irr28G7VQ=\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"firebase-adminsdk-1dmzs@tcc2024-e6adf.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"118291498381003375261\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-1dmzs%40tcc2024-e6adf.iam.gserviceaccount.com\",\n" +
                "  \"universe_domain\": \"googleapis.com\"\n" +
                "}\n";

        InputStream inputStream = new ByteArrayInputStream(JsonString.getBytes(StandardCharsets.UTF_8));

        try {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream).createScoped(firebaseMessagingScope);

            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

}

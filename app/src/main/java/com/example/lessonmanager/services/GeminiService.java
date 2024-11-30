package com.example.lessonmanager.services;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;

public class GeminiService {
    private static final String TAG = "GeminiService";
    private static final String API_KEY = "AIzaSyADjpD-JJMN6-sex9IKP1zhNiLI76vqxkk";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private final OkHttpClient client;
    private final Gson gson;

    public interface RefineCallback {
        void onSuccess(RefinedText refinedText);
        void onError(String error);
    }

    public static class RefinedText {
        private String title;
        private String subject;
        private String description;

        public RefinedText(String title, String subject, String description) {
            this.title = title;
            this.subject = subject;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getSubject() { return subject; }
        public String getDescription() { return description; }
    }

    public GeminiService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public void refineText(String title, String subject, String description, RefineCallback callback) {
        String prompt = String.format(
                "You are a text refinement assistant. Please correct spelling, grammar, and improve the following lesson details." +
                        "Respond ONLY with the three fields below, exactly as shown, with no additional text or explanations:\n\n" +
                        "Input:\n" +
                        "Title: %s\n" +
                        "Subject: %s\n" +
                        "Description: %s\n\n" +
                        "Your response must follow this exact format:\n" +
                        "TITLE: [refined title]\n" +
                        "SUBJECT: [refined subject]\n" +
                        "DESCRIPTION: [refined description]",
                title, subject, description
        );

        RequestContent requestContent = new RequestContent(prompt);
        String jsonBody = gson.toJson(requestContent);

        Request request = new Request.Builder()
                .url(GEMINI_URL + "?key=" + API_KEY)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        RefinedText refinedText = parseGeminiResponse(responseData);

                        // If parsing fails, keep original values
                        if (refinedText.getTitle().isEmpty()) refinedText = new RefinedText(title, subject, description);

                        callback.onSuccess(refinedText);
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing error: " + e.getMessage());
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "API error: " + response.code());
                    callback.onError("API error: " + response.code());
                }
            }
        });
    }

    private RefinedText parseGeminiResponse(String response) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String content = jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            String title = "", subject = "", description = "";

            // Split response into lines and parse each field
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("TITLE:")) {
                    title = line.substring(6).trim();
                } else if (line.startsWith("SUBJECT:")) {
                    subject = line.substring(8).trim();
                } else if (line.startsWith("DESCRIPTION:")) {
                    description = line.substring(12).trim();
                }
            }

            return new RefinedText(title, subject, description);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Gemini response: " + e.getMessage());
            return new RefinedText("", "", "");
        }
    }

    private static class RequestContent {
        private final Content[] contents;

        public RequestContent(String prompt) {
            this.contents = new Content[]{new Content(new Part[]{new Part(prompt)})};
        }

        private static class Content {
            private final Part[] parts;
            public Content(Part[] parts) { this.parts = parts; }
        }

        private static class Part {
            private final String text;
            public Part(String text) { this.text = text; }
        }
    }
}
package com.covvaccine.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FirebaseInitialization {

    @PostConstruct
    public void initialization() throws JSONException {

        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(getClass().getClassLoader().getResourceAsStream("firebase-service-config.json")))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

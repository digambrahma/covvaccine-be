package com.covvaccine.service;

import com.covvaccine.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String COLLECTION_NAME = "users";

    public String saveUser(User user) throws ExecutionException, InterruptedException {

        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(user.getEmail()).set(user);

        return collectionApiFuture.get().getUpdateTime().toString();

    }

    public void savePincode(String email, String pincode) {

        Firestore dbFirestore = FirestoreClient.getFirestore();

        dbFirestore.collection(COLLECTION_NAME).document(email).update("pincode", FieldValue.arrayUnion(pincode));


    }

    public List<Map<String, Object>> getAllUser() throws ExecutionException, InterruptedException {

        Firestore dbFirestore = FirestoreClient.getFirestore();

        List<Map<String, Object>> users = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments().stream().map(f -> f.getData()).collect(Collectors.toList());

        return users;


    }

    public User getUserDetailsByEmail(String email) throws ExecutionException, InterruptedException {

        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(email);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = future.get();

        User user;

        if (document.exists()) {
            user = document.toObject(User.class);
            return user;
        } else {
            return null;
        }
    }

}

package com.pakhi.clicksdigital.Utils;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageInstance {
    private static FirebaseStorageInstance single_instance=null;
    StorageReference rootRef=FirebaseStorage.getInstance().getReference();
    StorageReference groupProfileRef;

    public StorageReference getGroupProfileRef() {
        return rootRef.child(ConstFirebase.groupPhotos).child(ConstFirebase.groupProfile);
    }

    private FirebaseStorageInstance() {
    }

    // static method to create instance of Singleton class
    public static FirebaseStorageInstance getInstance() {
        if (single_instance == null)
            single_instance=new FirebaseStorageInstance();
        return single_instance;
    }

    public StorageReference getRootRef() {
        return rootRef;
    }

}

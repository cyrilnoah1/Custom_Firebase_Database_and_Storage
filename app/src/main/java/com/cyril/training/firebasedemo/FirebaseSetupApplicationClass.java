package com.cyril.training.firebasedemo;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.client.Firebase;

/**
 * Firebase start-up class.
 */
public class FirebaseSetupApplicationClass extends Application
{
    //Setting up Firebase at the Application's start.
    @Override
    public void onCreate()
    {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}

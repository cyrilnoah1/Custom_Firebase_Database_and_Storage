package com.cyril.training.firebasedemo;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{
    /*
    * IMPORTANT:
    *
    * Currently the App. does not use Firebase Authentication. Hence...
    *
    * Remember to change the rules in the root URL:
    * Eg:
    *   In https://console.firebase.google.com/project/fir-demo-24ad3/database/rules:
    *       {
             "rules": {
                          ".read": "auth == null",
                          ".write": "auth == null"
                      }
            }
    *
    *...so that, any user can upload to the database.
    *
    * The same goes for Firebase Storage rules.
    * */

    private static final String LOG_TAG= "firebaseDemoLog";

    Firebase mFirebaseRoot; // To set-up the Firebase root URL.
    Firebase mNewUserReference; // To add children to root URL;
    StorageReference mFirebaseStorageRefPath; // To upload/download for Firebase storage.

    Button mNewUserButton; // To upload new user to the Firebase Database.
    Button mRetrieveUserButton; // To retrieve the uploaded user.
    TextView mRetrievedUser1TextView; // To show the retrieved user.
    ImageView mDownloadedPictureImageView; // To display downloaded image.

    ArrayList<String> mFirebasePushKey; // To store keys after every Firebase push.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNewUserButton = (Button) findViewById(R.id.button_for_new_Firebase_user);
        mRetrieveUserButton = (Button) findViewById(R.id.button_to_retrieve_user_from_Firebase_database);
        mRetrievedUser1TextView = (TextView) findViewById(R.id.textView_to_show_user1_data);
        mDownloadedPictureImageView = (ImageView) findViewById(R.id.imageView_to_show_download_from_FireBase);

        mFirebasePushKey= new ArrayList<>();
        
        // Providing Firebase root URL.
        //TODO- Add your Firebase root URL in the firebase_url.xml file.
        mFirebaseRoot = new Firebase(getString(R.string.firebase_root_url));
        // Creating a child root for the Firebase root URL.
//        mNewUserReference = mFirebaseRoot.child("users");
        mNewUserReference = mFirebaseRoot.child("users");

        // Creating a Firebase storage instance to download an Image.
        FirebaseStorage storage= FirebaseStorage.getInstance();
        // Providing the storage instance to StorageReference to obtain Firebase Storage URL.
        //TODO- Add your Firebase Storage URL in the firebase_url.xml file.
        StorageReference reference= storage.getReferenceFromUrl(getString(R.string.firebase_storage_url));

        // Generating a random number to pick image for download.
        Random random= new Random();
        int indexForImage= random.nextInt(5); // Random number generate will be 0/1/2/3/4

        // Providing the child reference (Image-file name) to StorageReference.
        mFirebaseStorageRefPath= reference.child(randomImageFromFirebaseStorage().get(indexForImage));
    }

    /**
     * Method to provide a String containing the name of
     * the image files in Firebase Storage.
     * @return A String based on the index given.
     */
    private ArrayList<String> randomImageFromFirebaseStorage()
    {
        ArrayList<String> list= new ArrayList<>();

        list.add("flash_icon.png");/*FLASH*/
        list.add("deadpool_icon.png");/*DEADPOOL*/
        list.add("hydra_icon.png");/*HYDRA*/
        list.add("spiderman_icon.png");/*SPIDERMAN*/
        list.add("cap_amer_shield.png");/*CAPTAIN AMERICA*/

        return list;
    }

    /**
     * Method to upload a new user to the provided Firebase root URL, on button click.
     * @param v
     */
    public void createNewUserButton(View v)
    {
        /*
        * IMPORTANT:
        * Pushing new values instead of creating a Child avoids collision,
        * as using push() each time generates child with a unique identity.
        */
        //TODO- Use below code for push();
        Firebase pushUser1Reference= mNewUserReference.push();
//        Firebase pushUser1Reference= mNewUserReference.child("details");

        // Creating a new user to be uploaded.
        Users user1= new Users("Cyril"/*NAME*/,
                                20/*AGE*/,
                                "cnoah@rapidbizapps.com"/*E-MAIL*/,
                                "Hyderabad"/*LOCATION*/,
                                "rapidBizApps"/*WORKING COMPANY*/);

        // Uploading the created user as an object to the provided URL (Gets stored in JSON format).
        // And, checking for success/failure of the upload.
        pushUser1Reference.setValue(user1, new Firebase.CompletionListener()
        {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase)
            {

                // Checking for any errors
                if(firebaseError != null)
                {
                    Log.v(LOG_TAG, "Error: " +firebaseError.getMessage() );
                    Toast.makeText(MainActivity.this, "Error: " +firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // Upload successful.
                else
                {
                    Log.v(LOG_TAG, "Data 1 successfully stored.");
                    Toast.makeText(MainActivity.this, "Data 1 successfully stored.", Toast.LENGTH_SHORT).show();

                    // Displaying mRetrieveUserButton button.
                    mRetrieveUserButton.setVisibility(View.VISIBLE);
                }
            }
        });

        // Storing the unique identity created by push() in an ArrayList
        // to later retrieve data.
        //TODO- Use below code for push();
        mFirebasePushKey.add(pushUser1Reference.getKey());

        // Hiding the mNewUserButton button.
        mNewUserButton.setVisibility(View.GONE);
    }

    /**
     * Method to retrieve the uploaded user, on button click.
     * @param v
     */
    public void retrieveUserButton(View v)
    {
        // Adding valueEventListener to retrieve data asynchronously from the given Firebase URL.
        // Passing the unique ID to child(), to retrieve data.
        mNewUserReference.child( /*"details"*/ mFirebasePushKey.get(0)).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Checking if data has been retrieved.
                if(dataSnapshot != null)
                {
                    try
                    {
                        // Segregating the obtained data in JSON format to String values.
                        JSONObject jsonObject= new JSONObject(String.valueOf(dataSnapshot.getValue()));

                        // Storing name, age, email, location, and company of the user as String values.
                        String name= jsonObject.getString("userName"); // userName
                        String age= jsonObject.getString("userAge"); // userAge
                        String email= jsonObject.getString("userEmail"); // userEmail
                        String location= jsonObject.getString("userLocation"); // userLocation
                        String company= jsonObject.getString("userCompany"); // userCompany

                        // Setting message for the TextView.
                        String userData = "Name: " +name
                                        +"\n\nAge: " +age
                                        +"\n\nEmail: " +email
                                        +"\n\nLocation: " +location
                                        +"\n\nCompany: " +company;

                        // Showing the retrieved value in a TextView.
                        mRetrievedUser1TextView.setText(userData);

                        Log.v(LOG_TAG, "Data retrieval successful.");
                        Toast.makeText(MainActivity.this, "Data retrieval successful.", Toast.LENGTH_SHORT).show();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                // Data not retrieved.
                else
                {
                    Log.v(LOG_TAG, "Unable to retrieve data");
                    Toast.makeText(MainActivity.this, "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError)
            {
                Log.v(LOG_TAG, "Error: " +firebaseError.getMessage());
                Toast.makeText(MainActivity.this, "Error: " +firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Downloading the image from the Firebase Storage and displaying it in ImageView.
        mFirebaseStorageRefPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                Log.v(LOG_TAG, "Image download successful.");
                Toast.makeText(MainActivity.this, "Image download successful", Toast.LENGTH_SHORT).show();

                // Using Universal Image Loader to display the image.
                ImageLoader imageLoader= ImageLoader.getInstance();
                imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));

                imageLoader.displayImage(uri.toString(), mDownloadedPictureImageView);

            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e) 
            {
                Log.v(LOG_TAG, e.getMessage());
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Class for creating a new user to upload to the Firebase Database.
     */
    private class Users
    {
        private int userAge; // User's age.
        private String userName; // User's name.
        private String userEmail; // User's Email Address.
        private String userLocation; // User's location.
        private String userCompany; // Company where user is employed.

        public Users(String userName, int userAge, String userEmail, String userLocation, String userCompany)
        {
            this.userName= userName;
            this.userAge= userAge;
            this.userEmail= userEmail;
            this.userLocation= userLocation;
            this.userCompany= userCompany;
        }

        public String getUserCompany() {
            return userCompany;
        }

        public String getUserLocation() {
            return userLocation;
        }

        public int getUserAge() {
            return userAge;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserEmail() {
            return userEmail;
        }
    }
}

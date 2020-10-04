package com.example.tindereats;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestaurantFilterActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient; // Client for retrieving user location

    // UI Elements
    private EditText locationEdit;
    private static TextView locationErrorTv;
    private EditText categoryEdit;
    private static Button randomBtn;


    // Local variables
    private static String location;
    private static String category;
    private static String price;
    private static Double lat;
    private static Double longit;
    private static boolean useUserLoc; // Boolean for whether user location is in use
    private boolean open;
    private String range;
    private static RetrieveFeedback retrieveFeedback;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Retrieve preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        useUserLoc = false;
        final ArrayList<String> usernameList = getIntent().getStringArrayListExtra("list");

        // Checking if app has location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Confirming if User wants to deny permission
                AlertDialog.Builder locationAlert = new AlertDialog.Builder(this);
                locationAlert.setTitle("Location");
                locationAlert.setMessage("You won't be able to search based on your location if you do not grant permission. Are you sure you want to deny permission?");
                locationAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If denying, make no further actions
                    }
                });
                locationAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //If accepting, re-asking user for permissions
                        getPermission();
                    }
                });
                AlertDialog alertDialog = locationAlert.create();
                alertDialog.show();
            } else {
                // No explanation needed; request the permission
                getPermission();
            }
        }
        // Inflate the layout for this fragment
        setContentView(R.layout.activity_restaurant_filter);

        // Toggle Buttons for price category
        final ToggleButton priceTog1 = findViewById(R.id.priceTog1);
        final ToggleButton priceTog2 = findViewById(R.id.priceTog2);
        final ToggleButton priceTog3 = findViewById(R.id.priceTog3);
        final ToggleButton priceTog4 = findViewById(R.id.priceTog4);

        ImageButton locationBtn = findViewById(R.id.locationBtn); // Button for requesting user location

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // Initialize location client

        // Initialize UI Variables
        locationEdit = findViewById(R.id.locationET);
        locationErrorTv = findViewById(R.id.locationErrorTv);
        categoryEdit = findViewById(R.id.categoryET);
        randomBtn = findViewById(R.id.randomBtn);


        // Restore saved state if one has been stored
        if (savedInstanceState != null) {
            boolean ExistSave = savedInstanceState.getBoolean("ExistingSave");
            if(ExistSave) {
                locationEdit.setText(location);
                categoryEdit.setText(category);
                doneLoading();
            }
        }

        // Listener for the random search button
        // Gets the category entered, checks which price buttons are on, gets the location entered,
        // Then makes a method call for retrieving feedback from a API call
        randomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = categoryEdit.getText().toString();
                List<String> prices = new ArrayList<>();
                if(priceTog1.isChecked()) {
                    prices.add("1");
                }
                if(priceTog2.isChecked()) {
                    prices.add("2");
                }
                if(priceTog3.isChecked()) {
                    prices.add("3");
                }
                if(priceTog4.isChecked()) {
                    prices.add("4");
                }
                String priceArray = prices.toString();
                price = priceArray.substring(1,priceArray.length() - 1);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                UUID uuid = UUID.randomUUID();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("matchDetails/" + uuid.toString());

                if(!useUserLoc) { // If not using user location, then get location inputted
                    location = locationEdit.getText().toString();
                    if(location.isEmpty()) {
                        locationErrorTv.setText("Location is Required");
                    }else {
                        locationErrorTv.setText("");

                        // Make a match object that will be uploaded
                        Match match = new Match(usernameList, location, category, price, uuid.toString(), user.getDisplayName(), user.getUid(), 0.0, 0.0, false);
                        ref.setValue(match);
                        nextActivity(location, category, price);
                    }
                }
                else { // location has been handled elsewhere, make api call
                    Match match = new Match(usernameList, location, category, price, uuid.toString(), user.getDisplayName(), user.getUid(), lat, longit, true);
                    ref.setValue(match);
                    nextActivity(location, category, price);
                }
                locationEdit.onEditorAction(EditorInfo.IME_ACTION_DONE); //closes keyboard after clicking button
                locationEdit.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });

        if(useUserLoc) {
            getLocation();
            locationEdit.setText("");
            locationEdit.setHint("Using current location");
            locationEdit.setEnabled(false);
            locationErrorTv.setVisibility(View.INVISIBLE);
        }
        // Listener for using user location
        // Calls a method to handle the retrieval of the user's location
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askUserLocation();
            }
        });


        // listener that checks if toggle is on or off, changing the background accordingly
        priceTog1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog1.setBackgroundDrawable(getDrawable(R.drawable.left_price_box_on));
                } else {
                    priceTog1.setBackgroundDrawable(getDrawable(R.drawable.left_price_box));
                }
            }
        });

        priceTog2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog2.setBackgroundDrawable(getDrawable(R.drawable.price_box_on));
                } else {
                    priceTog2.setBackgroundDrawable(getDrawable(R.drawable.price_box));
                }
            }
        });

        priceTog3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog3.setBackgroundDrawable(getDrawable(R.drawable.price_box_on));
                } else {
                    priceTog3.setBackgroundDrawable(getDrawable(R.drawable.price_box));
                }
            }
        });

        priceTog4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog4.setBackgroundDrawable(getDrawable(R.drawable.right_price_box_on));
                } else {
                    priceTog4.setBackgroundDrawable(getDrawable(R.drawable.right_price_box));
                }
            }
        });
    }


    // Makes a retrieve feedback object used to make an API call
    public void feedback() {
        retrieveFeedback = new RetrieveFeedback();
        retrieveFeedback.execute(location, category, price, lat + "", longit + "",
                String.valueOf(useUserLoc), String.valueOf(open), range);
    }

    public void nextActivity(String location, String category, String prices) {
        Intent intent = new Intent(getApplicationContext(), MatchActivity.class);
        intent.putExtra("location",location);
        intent.putExtra("category", category);
        intent.putExtra("prices", prices);
        startActivity(intent);
    }

    // Sets the UI elements to a loading state
    public static void loading() {
        randomBtn.setClickable(false);
    }

    // Sets the UI elements to a done loading state
    // Hides progress bar and shows certain UI elements
    public static void doneLoading() {
        randomBtn.setClickable(true);
        retrieveFeedback.cancel(true);
    }

    public static void errorLoading() {

    }

    // Directly requests permission from user to access location
    public void getPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    // Prompts user if they want to use their location
    public void askUserLocation () {
        AlertDialog.Builder locationAlert = new AlertDialog.Builder(this);
        if(useUserLoc) {
            locationAlert.setTitle("Location");
            locationAlert.setMessage("Stop using your current location?");
            locationAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    locationEdit.setEnabled(true);
                    locationEdit.setHint("City, Zip Code, etc.");
                    locationEdit.setText("");
                    useUserLoc = false;
                    locationErrorTv.setVisibility(View.VISIBLE);
                }
            });
            locationAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        else {
            locationAlert.setTitle("Location");
            locationAlert.setMessage("Use your current location instead?");
            locationAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getLocation();
                    locationEdit.setText("");
                    locationEdit.setHint("Using current location");
                    locationEdit.setEnabled(false);
                    locationErrorTv.setVisibility(View.INVISIBLE);
                    useUserLoc = true;
                }
            });
            locationAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        AlertDialog alertDialog = locationAlert.create();
        alertDialog.show();
    }

    // Uses a location client to get a location, along with its coordinates
    public void getLocation() {
        randomBtn.setClickable(false);
        @SuppressLint("MissingPermission") Task<Location> task = fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    lat = location.getLatitude();
                    longit = location.getLongitude();
                    randomBtn.setClickable(true);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AlertDialog.Builder locationError = new AlertDialog.Builder(getApplicationContext());
                locationError.setTitle("Error");
                locationError.setMessage("Could not retrieve your location, please make sure your location services are turned on and try again.");
                locationError.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                AlertDialog alertDialog = locationError.create();
                alertDialog.show();
                lat = 0.0;
                longit = 0.0;
                randomBtn.setClickable(true);
            }
        });
    }




    void makeToast(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    // Saves variables in case instance state is destroyed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("Location", locationEdit.getText().toString());
        savedInstanceState.putString("Category", categoryEdit.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }



    private class RetrieveFeedback extends AsyncTask<String, Integer, String> {
        Response response;

        protected void onPreExecute() {
            loading();
        }

        @Override
        protected String doInBackground(String... strings) {
            //String location, String category, String price, Double lat, Double longit, boolean useUserLoc, boolean open, String range
            String url = "https://api.yelp.com/v3/businesses/search?";
            // Add location to request
            if(strings[5].equals("true")) {
                url += "latitude=" + strings[3];
                url += "&longitude=" + strings[4];
            }
            else {
                url += "location=" + strings[0];
            }
            // Add type of food to request
            if(!strings[1].isEmpty()) {
                url += "&term=" + strings[1];
            }
            // Add price to request
            if(!strings[2].isEmpty()) {
                url += "&price=" + strings[2];
            }
            // Limit results to 50
            url += "&limit=50";
            if(strings[6].equals("true")) {
                url += "&open_now=true";
            }
            if(!strings[7].equals("0")) {
                url += "&radius=" + strings[7];
            }
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            // Don't steal this
            String APIKey = "Bearer jH9lGEx7tYvtD5A5tVfMIeRMbc0VeXsXa4wqWBq1BuP-VbLJF5u0vmUzQYhQa39_t3Qptf5Tw19gzidvuk7jBtrStlhR9MpHCizfsCWHQUga7JO7KTLBz2MgWFd5XnYx";
            okhttp3.Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .addHeader("Authorization", APIKey)
                    .build();
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String Result) {
            while(response == null) {}
            try {
                String resp = response.body().string();
                try {
                    JSONObject obj = new JSONObject(resp);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Gson gson = new Gson();
                    ArrayList<String> blkRestaurantList;
                    if(sharedPref.contains("blkRestaurantList")) {
                        String restaurant = sharedPref.getString("blkRestaurantList", "");
                        blkRestaurantList = gson.fromJson(restaurant, ArrayList.class);
                    }
                    else {
                        blkRestaurantList = new ArrayList<>();
                    }
                    boolean isBlocked = true;
                    JSONArray arr = obj.getJSONArray("businesses");
                    JSONObject business = arr.getJSONObject(0);
                    String name = "";
                    Random rand = new Random();
                    int totalRestaurants = obj.getInt("total");
                    int bound = Math.min(50, totalRestaurants);
                    int count = 0;
                    while(isBlocked) {
                        int randomInt = rand.nextInt(bound);
                        business = arr.getJSONObject(randomInt);
                        name = business.getString("name");
                        if(!blkRestaurantList.contains(name)) {
                            isBlocked = false;
                        }
                        count++;
                        if(count >= bound) {
                            errorLoading();
                            break;
                        }
                    }
                    if(!isBlocked) {
                        String imageURL = business.getString("image_url");
                        float rating = business.getInt("rating");
                        JSONObject location = business.getJSONObject("location");
                        String address = location.getString("address1");
                        String city = location.getString("city");
                        JSONObject coordinates = business.getJSONObject("coordinates");
                        double latitude = coordinates.getInt("latitude");
                        double longitude = coordinates.getInt("longitude");
                        String coordinate = "geo:" + latitude + "," + longitude + "?q=";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            response = null;
            doneLoading();
        }
    }
}


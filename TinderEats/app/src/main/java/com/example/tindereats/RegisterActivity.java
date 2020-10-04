//package com.example.tindereats;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class RegisterActivity extends AppCompatActivity {
//    private FirebaseAuth mAuth;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
//
//        mAuth = FirebaseAuth.getInstance();
//        final EditText userNameREdit = findViewById(R.id.userNameREdit);
//        final EditText passwordREdit = findViewById(R.id.passwordREdit);
//        final EditText numberREdit = findViewById(R.id.numberREdit);
//        Button registerBtn = findViewById(R.id.registerBtn);
//
//        registerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String username = userNameREdit.getText().toString();
//                String password = passwordREdit.getText().toString();
//                String number = numberREdit.getText().toString();
//                try {
//                    if(username.length() > 0 && password.length() > 0) {
//                        authRegister(username, password);
////                        AttemptRegister reg = new AttemptRegister();
////                        reg.execute(username, password, number);
//                    }
//
//                } catch(Exception e)
//                {
//                    Toast.makeText(RegisterActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//    }
//
//    protected void register() {
//        Toast.makeText(RegisterActivity.this,"Account Registered!", Toast.LENGTH_LONG).show();
//        Intent loggedIn = new Intent(getApplicationContext(), HomeActivity.class);
//        loggedIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//        SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean("LoggedIn", true);
//        editor.apply();
//        startActivity(loggedIn);
//    }
//
//    protected void authRegister(String email, String password) {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("tag", "createUserWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            //updateUI(user);
//                            register();
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("tag", "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            //updateUI(null);
//                        }
//
//                        // ...
//                    }
//                });
//    }
//
//
//    private class AttemptRegister extends AsyncTask<String, String, Response> {
//        Response response;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Response doInBackground(String... args) {
//            OkHttpClient client = new OkHttpClient().newBuilder().build();
//            String number = args[2];
//            String password = args[1];
//            String name= args[0];
//            String URL= "http://192.168.1.18/php/index.php";
//            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                        .addFormDataPart("username", name)
//                        .addFormDataPart("password", password)
//                        .addFormDataPart("phone_number", number)
//                        .build();
//            okhttp3.Request request = new Request.Builder()
//                    .url(URL)
//                    .method("POST", formBody)
//                    .build();
//            try {
//                response = client.newCall(request).execute();
//                while(response == null) {}
//                return response;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        protected void onPostExecute(Response result) {
//            if (result != null) {
//                try {
//                    String response = result.body().string();
//                    JSONObject json = new JSONObject(response);
//                    String success = json.getString("success");
//                    String message = json.getString("message");
//                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
//                    if(success.equals("1")) {
//                        register();
//                    }
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Toast.makeText(getApplicationContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//}
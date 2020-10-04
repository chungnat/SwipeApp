package com.example.tindereats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button startMatchBtn = findViewById(R.id.startMatchBtn);
        Button receiveMatchBtn = findViewById(R.id.receiveMatchBtn);
        Button accountBtn = findViewById(R.id.accountBtn);


        accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent account = new Intent(getApplicationContext(), profileActivity.class);
                startActivity(account);
            }
        });

        startMatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMatch = new Intent(getApplicationContext(), SelectFriendsActivity.class);
                startActivity(startMatch);
            }
        });

        receiveMatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recieveMatches = new Intent(getApplicationContext(), ReceiveMatchesActivity.class);
                startActivity(recieveMatches);
            }
        });

    }


}
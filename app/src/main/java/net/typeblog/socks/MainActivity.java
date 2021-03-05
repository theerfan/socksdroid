package net.typeblog.socks;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new ProfileFragment()).commit();
//        this.getFragmen.tManager().beginTransaction().replace(android.R.id.content, new ProfileFragment()).commit();
    }
}

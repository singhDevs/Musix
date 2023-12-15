package com.example.musix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.musix.R;
import com.example.musix.fragments.HomeFragment;
import com.example.musix.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    boolean loadedFragment = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                if(itemId == R.id.navHome){
                    if(!loadedFragment){
                        fragmentTransaction.add(R.id.container, new HomeFragment());
                        loadedFragment = true;
                    }
                    else {
                        fragmentTransaction.replace(R.id.container, new HomeFragment());
                    }
                }
                if(itemId == R.id.navSearch) {
                    if(!loadedFragment){
                        fragmentTransaction.add(R.id.container, new SearchFragment());
                        loadedFragment = true;
                    }
                    else{
                        fragmentTransaction.replace(R.id.container, new SearchFragment());
                    }
                }
                fragmentTransaction.commit();
                return true;
            }
        });



        HomeFragment homeFragment = new HomeFragment();



        fragmentTransaction.replace(R.id.container, homeFragment);
        fragmentTransaction.commit();
    }
}
package com.desert.router;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.desert.router.runtime.Router;

import com.desert.router.annotations.Destination;


@Destination(url = "router://app-main", description = "首页")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv).setOnClickListener(v -> Router.getInstance().go(MainActivity.this, "router://test?name=desert"));

    }

}
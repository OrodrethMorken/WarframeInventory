package com.games.orodreth.warframeinventory;

import android.graphics.Color;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(5000)
                .withBackgroundResource(R.color.colorPrimaryDark)
                .withHeaderText("Warframe Inventory")
                .withBeforeLogoText("Warframe Inventory")
                .withLogo(R.mipmap.ic_warframe_inventory)
                .withAfterLogoText("By Orodreth");

        config.getHeaderTextView().setTextColor(Color.WHITE);
        config.getBeforeLogoTextView().setTextColor(Color.WHITE);
        config.getBeforeLogoTextView().setTextSize(30);
        config.getAfterLogoTextView().setTextColor(Color.WHITE);

        final View easySplashScreen = config.create();
        setContentView(easySplashScreen);
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaPlayer sound = MediaPlayer.create(easySplashScreen.getContext(), R.raw.darvo);
                sound.setLooping(false);
                sound.start();
            }
        }).start();
    }
}

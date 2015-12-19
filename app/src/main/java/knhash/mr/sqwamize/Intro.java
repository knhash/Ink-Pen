package knhash.mr.sqwamize;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class Intro extends AppIntro {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(AppIntroFragment.newInstance("InK-Pen", "Minimalist note-taking minimized", R.drawable.s1, Color.parseColor("#2c001e")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.s2, Color.parseColor("#2c001e")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.s3, Color.parseColor("#2c001e")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.s4, Color.parseColor("#2c001e")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.s5, Color.parseColor("#2c001e")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.s6, Color.parseColor("#2c001e")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.s7, Color.parseColor("#2c001e")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.s8, Color.parseColor("#2c001e")));

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        //addSlide(AppIntroFragment.newInstance(title, description, image, background_colour));

        // OPTIONAL METHODS
        // Override bar/separator color
        //setBarColor(Color.parseColor("#3F51B5"));
        //setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button
        showSkipButton(true);
        showDoneButton(true);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        //setVibrate(true);
        //setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed() {
        // Do something when users tap on Skip button.
        SharedPreferences firstRuncheck = getSharedPreferences("firstRun", 0);
        SharedPreferences.Editor editor = firstRuncheck.edit();
        editor.putBoolean("firstTime", true);
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        SharedPreferences firstRuncheck = getSharedPreferences("firstRun", 0);
        SharedPreferences.Editor editor = firstRuncheck.edit();
        editor.putBoolean("firstTime", false);
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

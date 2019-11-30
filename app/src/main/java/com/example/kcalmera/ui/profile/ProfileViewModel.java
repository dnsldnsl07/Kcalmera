package com.example.kcalmera.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.midi.MidiOutputPort;
import android.widget.Toast;

import com.example.kcalmera.R;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.MODE_WORLD_READABLE;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<String> mText;



    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("정지욱");
    }

    public LiveData<String> getText() {
        return mText;
    }
}


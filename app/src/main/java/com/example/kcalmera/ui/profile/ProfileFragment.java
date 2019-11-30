package com.example.kcalmera.ui.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.kcalmera.R;

import java.io.BufferedReader;
import java.io.FileReader;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textViewName = root.findViewById(R.id.nameText2);
        final TextView textViewSex = root.findViewById(R.id.sexText2);
        final TextView textViewAge = root.findViewById(R.id.ageText2);
        final TextView textViewHeight = root.findViewById(R.id.heightText2);
        final TextView textViewWeight = root.findViewById(R.id.weightText2);
        profileViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                // textViewName.setText(s);

                BufferedReader br = null;
                try {
                    //br = new BufferedReader(new FileReader("/data/data/com.example.kcalmera/files/Documents/"+"profile.txt"));
                    br = new BufferedReader(new FileReader("/mnt/sdcard/Android/data/com.example.kcalmera/files/Documents/"+"profile.txt"));
                }
                catch (Exception e)
                {

                }
                String name = new String();
                String sex = new String();
                String age = new String();
                String ht = new String();
                String wt = new String();

                //
                try {
                    s = br.readLine();
                    textViewName.setText(name.concat(s));
                    s = br.readLine();
                    textViewSex.setText(sex.concat(s));
                    s = br.readLine();
                    textViewAge.setText(age.concat(s));
                    s = br.readLine();
                    textViewHeight.setText(ht.concat(s));
                    s = br.readLine();
                    textViewWeight.setText(wt.concat(s));

                    br.close();
                }
                catch(Exception e)
                {

                }

            }
        });
        return root;
    }

}
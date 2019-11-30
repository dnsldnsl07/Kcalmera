package com.example.kcalmera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        setContentView(R.layout.activity_register);
        Button registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameText = (EditText) findViewById(R.id.nameText);
                final RadioGroup sexSelect = (RadioGroup) findViewById(R.id.sexGroup);
                String sexText = new String();
                EditText ageText = (EditText) findViewById(R.id.ageText);
                EditText heightText = (EditText) findViewById(R.id.heightText);
                EditText weightText = (EditText) findViewById(R.id.weightText);
                int id = sexSelect.getCheckedRadioButtonId();

                if(id == R.id.mButton){
                    sexText="남성";
                }
                else{
                    sexText="여성";
                }

                //Toast.makeText(getApplicationContext(),sexText,Toast.LENGTH_SHORT).show();
                final String name = new String(nameText.getText().toString());
                final String sex = new String(sexText);
                final String age = new String(ageText.getText().toString());
                final String height = new String(heightText.getText().toString());
                final String weight = new String(weightText.getText().toString());

                SharedPreferences pref = getSharedPreferences("register",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();


                //if(모두 빈 스트링이 아니면)

                editor.putInt("First", 1);
                editor.commit();

                File file = new File(getFilesDir(), "profile.txt") ;
                FileWriter fw = null ;
                BufferedWriter bufwr = null ;
                try {
                    fw = new FileWriter(file) ;
                    bufwr = new BufferedWriter(fw) ;
                    bufwr.write(name+"\n");
                    bufwr.write(sex+"\n");
                    bufwr.write(age+"\n");
                    bufwr.write(height+"\n");
                    bufwr.write(weight+"\n");
                    bufwr.flush() ;
                } catch (Exception e) {
                    e.printStackTrace() ;
                }
                try {
                    // close file.
                    if (bufwr != null) {
                        bufwr.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace() ;
                }
                finish();
            }
        });

    }
}
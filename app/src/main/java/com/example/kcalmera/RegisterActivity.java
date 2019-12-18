package com.example.kcalmera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.kcalmera.ui.diet.DietFragment;

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
                final RadioGroup actSelect = (RadioGroup) findViewById(R.id.actGroup);
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

                String exer=new String();
                id = actSelect.getCheckedRadioButtonId();
                switch (id){
                    case R.id.actButton1 : exer="거의 하지 않음"; break;
                    case R.id.actButton2 : exer="주1~2회정도"; break;
                    case R.id.actButton3 : exer="주3~4회정도"; break;
                    case R.id.actButton4 : exer="주5회이상"; break;
                    case R.id.actButton5 : exer="전문 운동선수";
                }

                //Toast.makeText(getApplicationContext(),sexText,Toast.LENGTH_SHORT).show();
                final String name = new String(nameText.getText().toString());
                final String sex = new String(sexText);
                final String age = new String(ageText.getText().toString());
                final String height = new String(heightText.getText().toString());
                final String weight = new String(weightText.getText().toString());
                final String exercise = new String(exer);

                SharedPreferences pref = getSharedPreferences("register",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                //if(모두 빈 스트링이 아니면)

                editor.putInt("First", 1);
                editor.commit();

                //File file = new File(getFilesDir(), "profile.txt") ;
                //File file = new File(getFilesDir(), "profile.txt") ;
                //File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyDocApp");
                File fileDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File file = new File(fileDir.getPath() + File.separator + "profile.txt");
                FileWriter fw = null;
                BufferedWriter bufwr = null ;
                try {
                    fw = new FileWriter(file) ;
                    bufwr = new BufferedWriter(fw) ;
                    bufwr.write(name+"\n");
                    bufwr.write(sex+"\n");
                    bufwr.write(age+"\n");
                    bufwr.write(height+"\n");
                    bufwr.write(weight+"\n");
                    bufwr.write(exercise+"\n");

                    //network

                    MainActivity.userSex = sex;
                    MainActivity.userAge = age;
                    MainActivity.userHeight = height;
                    MainActivity.userWeight = weight;

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
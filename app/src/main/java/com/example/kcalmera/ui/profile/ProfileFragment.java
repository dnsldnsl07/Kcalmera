package com.example.kcalmera.ui.profile;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.kcalmera.MainActivity;
import com.example.kcalmera.R;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    // 미플린 세인트 지어 방정식
    public final static double WEIGHT_FACTOR = 10;
    public final static double HEIGHT_FACTOR = 6.25;
    public final static int AGE_FACTOR = 5;
    public final static int MALE_BIAS = 5;
    public final static int FEMALE_BIAS = -161;
    public final static double[] ACTIVITY_LEVEL = {1.2, 1.375, 1.55, 1.725, 1.9};
    public final static double CARBOHYDRATE_FACTOR = 0.6 / 4.0;
    public final static double PROTEIN_FACTOR = 0.8;
    public final static double FAT_FACTOR = 0.175 / 9.0;
    // 단위: mg
    public final static int NATRIUM_BOUND = 2000;
    public final static int CHOLESTEROL_BOUND = 300;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_profile, container, false);
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

                /*
                 * 권장 영양 성분 및 음식 추천
                 */
                final int AGE = Integer.parseInt(textViewAge.getText().toString());
                final double HEIGHT = Double.parseDouble(textViewHeight.getText().toString());
                final double WEIGHT = Double.parseDouble(textViewWeight.getText().toString());
                final String SEX = textViewSex.getText().toString();
                // TO DO: 활동 레벨 구현
                // 기초 대사율
                final double BMR = WEIGHT_FACTOR * WEIGHT + HEIGHT_FACTOR * HEIGHT - AGE_FACTOR * AGE
                                    + (SEX.equals("남성")? MALE_BIAS : FEMALE_BIAS);
                // TO DO: 활동 레벨 구현 필요
                final double RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[1];
                final double RECOMMENDED_CARBOHYDRATE = RECOMMENDED_KCAL * CARBOHYDRATE_FACTOR;
                final double RECOMMENDED_PROTEIN = WEIGHT * PROTEIN_FACTOR;
                final double RECOMMENDED_FAT = RECOMMENDED_KCAL * FAT_FACTOR;

                // 오늘 식단 탐색
                double kcal = 0, carbohydrate = 0, protein = 0, fat = 0, natrium = 0, cholesterol = 0;

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String curDate = format.format(calendar.getTime());

                Cursor cursor = ((MainActivity) MainActivity.mContext).selectRecord(curDate);
                String dietString = ((MainActivity) MainActivity.mContext).database_test(cursor);
                String diets[] = dietString.split("\n");
                if (!diets[0].equals("")) { // else: 식단 없음
                    for (int i = 0; i < diets.length; ++i) {
                        String diet[] = diets[i].split("/");
                        String foodName = diet[0];
                        double amount = Double.parseDouble(diet[1]);

                        String foodInfo = ((MainActivity) MainActivity.mContext).selectFoodInfo2(foodName);
                        String nutrients[] = foodInfo.split("/");

                        kcal += Double.parseDouble(nutrients[1]) * amount;
                        carbohydrate += Double.parseDouble(nutrients[2]) * amount;
                        protein += Double.parseDouble(nutrients[3]) * amount;
                        fat += Double.parseDouble(nutrients[4]) * amount;
                        natrium += Double.parseDouble(nutrients[6]) * amount;
                        cholesterol += Double.parseDouble(nutrients[7]) * amount;
                    }
                }

                ScrollView scrollView = (ScrollView) root.findViewById(R.id.infoScrollView);
                scrollView.setVerticalScrollBarEnabled(true);

                TextView kcalText = (TextView) root.findViewById(R.id.kcal);
                TextView carbohydrateText = (TextView) root.findViewById(R.id.carbohydrate);
                TextView proteinText = (TextView) root.findViewById(R.id.protein);
                TextView fatText = (TextView) root.findViewById(R.id.fat);
                TextView natriumText = (TextView) root.findViewById(R.id.natrium);
                TextView cholesterolText = (TextView) root.findViewById(R.id.cholesterol);

                String textLine = (int)kcal + " / " + (int)RECOMMENDED_KCAL;
                kcalText.setText(textLine);
                textLine = (int)carbohydrate + " / " + (int)RECOMMENDED_CARBOHYDRATE;
                carbohydrateText.setText(textLine);
                textLine = (int)protein + " / " + (int)RECOMMENDED_PROTEIN;
                proteinText.setText(textLine);
                textLine = (int)fat + " / " + (int)RECOMMENDED_FAT;
                fatText.setText(textLine);
                textLine = (int)natrium + " / " + NATRIUM_BOUND;
                natriumText.setText(textLine);
                textLine = (int)cholesterol + " / " + CHOLESTEROL_BOUND;
                cholesterolText.setText(textLine);


            }
        });
        return root;
    }
}
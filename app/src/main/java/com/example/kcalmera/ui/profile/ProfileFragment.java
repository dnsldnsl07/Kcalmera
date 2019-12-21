package com.example.kcalmera.ui.profile;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
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
import com.example.kcalmera.ui.diet.DietFragment;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

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
    public final static double PROTEIN_PER_WEIGHT = 0.8;
    public final static double CARBOHYDRATE_FACTOR = 11.0 / 4.0;
    public final static double FAT_FACTOR = 5.0 / 9.0;
    // 단위: mg
    public final static int NATRIUM_BOUND = 2000;
    public final static int CHOLESTEROL_BOUND = 300;
    public final static String[] LOW_CARBOHYDRATE_FOODS = {"콩", "견과류", "아보카도"};
    public final static String[] HIGH_CARBOHYDRATE_FOODS = {"잡곡밥", "통밀빵", "해조류"};
    public final static String[] LOW_PROTEIN_FOODS = {"과일", "채소"};
    public final static String[] HIGH_PROTEIN_FOODS = {"닭가슴살", "콩", "계란"};
    public final static String[] LOW_FAT_FOODS = {"채소", "과일", "닭가슴살"};
    public final static String[] HIGH_FAT_FOODS = {"견과류", "생선", "아보카도"};
    public final static String[] LOW_CHOLESTEROL_FOODS = {"과일", "견과류", "콩", "생선"};


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
        final TextView textViewExercise = root.findViewById(R.id.exerciseText2);
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

                //
                try {
                    s = br.readLine();
                    textViewName.setText(s);
                    s = br.readLine();
                    textViewSex.setText(s);
                    s = br.readLine();
                    textViewAge.setText(s);
                    s = br.readLine();
                    textViewHeight.setText(s);
                    s = br.readLine();
                    textViewWeight.setText(s);
                    s = br.readLine();
                    textViewExercise.setText(s);
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
                double RECOMMENDED_KCAL = 0;
                switch(textViewExercise.getText().toString()){
                    case "거의 하지 않음" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[0]; break;
                    case "주1~2회정도" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[1]; break;
                    case "주3~4회정도" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[2]; break;
                    case "주5회이상" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[3]; break;
                    case "전문 운동선수" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[4]; break;
                }

                final double RECOMMENDED_PROTEIN = WEIGHT * PROTEIN_PER_WEIGHT;
                final double RECOMMENDED_CARBOHYDRATE = RECOMMENDED_PROTEIN * CARBOHYDRATE_FACTOR;
                final double RECOMMENDED_FAT = RECOMMENDED_PROTEIN * FAT_FACTOR;

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

                // 영양 분석 메시지 출력
                boolean isKcalExceeded = (kcal > RECOMMENDED_KCAL);
                boolean isCarbohydrateExceeded = (carbohydrate > RECOMMENDED_CARBOHYDRATE);
                boolean isProteinExceeded = (protein > RECOMMENDED_PROTEIN);
                boolean isFatExceeded = (fat > RECOMMENDED_FAT);
                boolean isNatriumExceeded = (natrium > NATRIUM_BOUND);
                boolean isCholesterolExceeded = (cholesterol > CHOLESTEROL_BOUND);
                TextView kcalText = (TextView) root.findViewById(R.id.kcal);
                TextView carbohydrateText = (TextView) root.findViewById(R.id.carbohydrate);
                TextView proteinText = (TextView) root.findViewById(R.id.protein);
                TextView fatText = (TextView) root.findViewById(R.id.fat);
                TextView natriumText = (TextView) root.findViewById(R.id.natrium);
                TextView cholesterolText = (TextView) root.findViewById(R.id.cholesterol);

                // ArrayList<String> recommendationMsgs = new ArrayList<>();
                String textLine = (int)kcal + " / " + (int)RECOMMENDED_KCAL;
                kcalText.setText(textLine);
                Spannable span = (Spannable) kcalText.getText();
                span.setSpan(new RelativeSizeSpan(1.1f), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, textLine.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isKcalExceeded) {
                    span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // recommendationMsgs.add("권장 칼로리 섭취량을 초과했습니다.");
                }

                textLine = (int)carbohydrate + " / " + (int)RECOMMENDED_CARBOHYDRATE;
                carbohydrateText.setText(textLine);
                span = (Spannable) carbohydrateText.getText();
                span.setSpan(new RelativeSizeSpan(1.1f), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, textLine.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isCarbohydrateExceeded) {
                    span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // recommendationMsgs.add("권장 탄수화물 섭취량을 초과했습니다.");
                }

                textLine = (int)protein + " / " + (int)RECOMMENDED_PROTEIN;
                proteinText.setText(textLine);
                span = (Spannable) proteinText.getText();
                span.setSpan(new RelativeSizeSpan(1.1f), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, textLine.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isProteinExceeded) {
                    span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // recommendationMsgs.add("권장 단백질 섭취량을 초과했습니다.");
                }

                textLine = (int)fat + " / " + (int)RECOMMENDED_FAT;
                fatText.setText(textLine);
                span = (Spannable) fatText.getText();
                span.setSpan(new RelativeSizeSpan(1.1f), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, textLine.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isFatExceeded) {
                    span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // recommendationMsgs.add("권장 지방 섭취량을 초과했습니다.");
                }

                textLine = (int)natrium + " / " + NATRIUM_BOUND;
                natriumText.setText(textLine);
                span = (Spannable) natriumText.getText();
                span.setSpan(new RelativeSizeSpan(1.1f), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, textLine.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isNatriumExceeded) {
                    span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // recommendationMsgs.add("권장 나트륨 섭취량을 초과했습니다.");
                }

                textLine = (int)cholesterol + " / " + CHOLESTEROL_BOUND;
                cholesterolText.setText(textLine);
                span = (Spannable) cholesterolText.getText();
                span.setSpan(new RelativeSizeSpan(1.1f), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, textLine.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isCholesterolExceeded) {
                    span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // recommendationMsgs.add("권장 콜레스테롤 섭취량을 초과했습니다.");
                }

                /*
                String recommendationMsg = null;
                if (recommendationMsgs.size() == 0) {
                    recommendationMsg = "특이 사항이 없습니다.";
                } else {
                    recommendationMsg = recommendationMsgs.get(0);
                    for (int i = 1; i < recommendationMsgs.size(); ++i) {
                        recommendationMsg = recommendationMsg.concat("\n" + recommendationMsgs.get(i));
                    }
                }

                TextView recommendationMsgText = (TextView) root.findViewById(R.id.recommendationMsgText);
                recommendationMsgText.setText(recommendationMsg);
                */
                // 음식 추천
                HashSet<String> recommendedFood = new HashSet<>();
                HashSet<String> restrictedFood = new HashSet<>();
                if (isCarbohydrateExceeded) {
                    Collections.addAll(restrictedFood, HIGH_CARBOHYDRATE_FOODS);
                    Collections.addAll(recommendedFood, LOW_CARBOHYDRATE_FOODS);
                } else {
                    Collections.addAll(restrictedFood, LOW_CARBOHYDRATE_FOODS);
                    Collections.addAll(recommendedFood, HIGH_CARBOHYDRATE_FOODS);
                }

                if (isProteinExceeded) {
                    Collections.addAll(restrictedFood, HIGH_PROTEIN_FOODS);
                    Collections.addAll(recommendedFood, LOW_PROTEIN_FOODS);
                } else {
                    Collections.addAll(restrictedFood, LOW_PROTEIN_FOODS);
                    Collections.addAll(recommendedFood, HIGH_PROTEIN_FOODS);
                }

                if (isFatExceeded) {
                    Collections.addAll(restrictedFood, HIGH_FAT_FOODS);
                    Collections.addAll(recommendedFood, LOW_FAT_FOODS);
                } else {
                    Collections.addAll(restrictedFood, LOW_FAT_FOODS);
                    Collections.addAll(recommendedFood, HIGH_FAT_FOODS);
                }

                if (isCholesterolExceeded) {
                    Collections.addAll(recommendedFood, LOW_CHOLESTEROL_FOODS);
                }

                recommendedFood.removeAll(restrictedFood);

                String foodRecommendation = "";
                if (isCarbohydrateExceeded) {
                    HashSet<String> tmp = new HashSet<>();
                    Collections.addAll(tmp, LOW_CARBOHYDRATE_FOODS);
                    tmp.retainAll(recommendedFood);
                    foodRecommendation += "저탄수화물 음식 " + tmp.toString() + '\n';
                } else {
                    HashSet<String> tmp = new HashSet<>();
                    Collections.addAll(tmp, HIGH_CARBOHYDRATE_FOODS);
                    tmp.retainAll(recommendedFood);
                    foodRecommendation += "탄수화물 보충 음식 " + tmp.toString() + '\n';
                }

                if (isProteinExceeded) {
                    HashSet<String> tmp = new HashSet<>();
                    Collections.addAll(tmp, LOW_PROTEIN_FOODS);
                    tmp.retainAll(recommendedFood);
                    foodRecommendation += "저단백질 음식 " + tmp.toString() + '\n';
                } else {
                    HashSet<String> tmp = new HashSet<>();
                    Collections.addAll(tmp, HIGH_PROTEIN_FOODS);
                    tmp.retainAll(recommendedFood);
                    foodRecommendation += "단백질 보충 음식 " + tmp.toString() + '\n';
                }

                if (isFatExceeded) {
                    HashSet<String> tmp = new HashSet<>();
                    Collections.addAll(tmp, LOW_FAT_FOODS);
                    tmp.retainAll(recommendedFood);
                    foodRecommendation += "저지방 음식 " + tmp.toString();
                } else {
                    HashSet<String> tmp = new HashSet<>();
                    Collections.addAll(tmp, HIGH_FAT_FOODS);
                    tmp.retainAll(recommendedFood);
                    foodRecommendation += "지방 보충 음식 " + tmp.toString();
                }


                TextView foodRecommendationText = (TextView) root.findViewById(R.id.foodRecommendationText);
                if (isCarbohydrateExceeded && isProteinExceeded && isFatExceeded && isCholesterolExceeded) {
                    foodRecommendationText.setText("-");
                } else {
                    foodRecommendationText.setText(foodRecommendation);
                }


            }
        });
        return root;
    }
}
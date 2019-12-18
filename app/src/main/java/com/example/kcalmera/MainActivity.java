package com.example.kcalmera;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kcalmera.ui.diet.AddView;
import com.example.kcalmera.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


public class MainActivity extends FragmentActivity {
    private static final int PROFILE_REQUEST_CODE=699, REQUEST_IMAGE_CAPTURE = 700, REQUEST_FOOD_SET = 701;
    private String imageFilePath;
    private Uri photoUri;
    private static final String TAG = "TfLiteCameraDemo";

    public static Context mContext;
    public static String Food;
    public static int check=-1;

    public static String userSex;
    public static String userAge;
    public static String userHeight;
    public static String userWeight;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SharedPreferences pref = getSharedPreferences("register",MODE_PRIVATE);
        setContentView(R.layout.activity_main);

        createDatabase();// CreateActivity에서 생성 하려고한다.


        int firstviewshow = pref.getInt("First",0);
        if(firstviewshow != 1){

            //datbase_initialize
            try {
                db.execSQL("create table diet_diary" + "("
                        + "_id integer PRIMARY KEY autoincrement, "
                        + "time DATETIME DEFAULT (datetime('now','localtime')),"
                        + "foodName text, "
                        + "amount double);");
                //PRIMARY KEY에 NOT NULL 그냥 해봄
                db.execSQL("create table food_info" + "("
                        + "foodName text PRIMARY KEY NOT NULL, "
                        + "gram double, "
                        + "kcal double, "
                        + "carbohydrate double, "
                        + "protein double, "
                        + "fat double, "
                        + "sugars double, "
                        + "natrium double, "
                        + "cholesterol double, "
                        + "fatty_acid double, "
                        + "trans_fat double);");

            }
            catch(Exception e)
            {
                Log.e("ddd",e.getMessage());
            }

            //database_end

            //create foodtable
            createFoodInfoTable();
            //end

            //Toast.makeText(getApplicationContext(), "첫 시작!!",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        }



        if(firstviewshow == 1) {
            try {
                BufferedReader br = null;
                br = new BufferedReader(new FileReader("/mnt/sdcard/Android/data/com.example.kcalmera/files/Documents/" + "profile.txt"));
                String s;
                s = br.readLine();
                s = br.readLine();
                userSex = s;
                s = br.readLine();
                userAge = s;
                s = br.readLine();
                userHeight = s;
                s = br.readLine();
                userWeight = s;

            } catch(Exception e)
            {
            }
        }


        // 권한 체크
        TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("거부하셨습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

        // 네비게이션 뷰
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_camera, R.id.navigation_management, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // 사진촬영
        findViewById(R.id.navigation_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent,REQUEST_FOOD_SET);
            }
                /*
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {

                    }

                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
            */
        });

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(getApplicationContext(), "권한이 허용됨",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한이 거부됨",Toast.LENGTH_SHORT).show();
        }
    };

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Image_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
/*
    public void onClickAccept(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        TextView getFood=(TextView) findViewById(R.id.DetectedFood);
        Food = getFood.getText().toString();
        check=1;
        startActivity(intent);
    }*/

    public void onClickCancel(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {

            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    public void onClickEdit(View v) {
        View editView = getLayoutInflater().inflate(R.layout.profile_edit, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(editView);

        final EditText Name = editView.findViewById(R.id.nameTextE);
        final EditText Age = editView.findViewById(R.id.ageTextE);
        final EditText Ht = editView.findViewById(R.id.heightTextE);
        final EditText Wt = editView.findViewById(R.id.weightTextE);
        final RadioGroup sexSelect = editView.findViewById(R.id.sexGroupE);
        RadioButton mButton = editView.findViewById(R.id.mButtonE);
        RadioButton feButton = editView.findViewById(R.id.feButtonE);
        final RadioGroup actSelect = editView.findViewById(R.id.actGroupE);
        RadioButton actButton1 = editView.findViewById(R.id.actButton1E);
        RadioButton actButton2 = editView.findViewById(R.id.actButton2E);
        RadioButton actButton3 = editView.findViewById(R.id.actButton3E);
        RadioButton actButton4 = editView.findViewById(R.id.actButton4E);
        RadioButton actButton5 = editView.findViewById(R.id.actButton5E);

        final String nameStr = ((EditText) findViewById(R.id.nameText2)).getText().toString();
        final String sexStr = ((EditText) findViewById(R.id.sexText2)).getText().toString();
        final String ageStr = ((EditText) findViewById(R.id.ageText2)).getText().toString();
        final String htStr = ((EditText) findViewById(R.id.heightText2)).getText().toString();
        final String wtStr = ((EditText) findViewById(R.id.weightText2)).getText().toString();
        final String exerStr = ((EditText) findViewById(R.id.exerciseText2)).getText().toString();
        Name.setText(nameStr);
        Age.setText(ageStr);
        Ht.setText(htStr);
        Wt.setText(wtStr);
        switch(sexStr) {
            case "남성":
                mButton.setChecked(true); break;
            case "여성":
                feButton.setChecked(true); break;
        }
        switch (exerStr){
            case "거의 하지 않음": actButton1.setChecked(true); break;
            case "주1~2회정도" : actButton2.setChecked(true); break;
            case "주3~4회정도" : actButton3.setChecked(true); break;
            case "주5회이상" : actButton4.setChecked(true); break;
            case "전문 운동선수" : actButton5.setChecked(true); break;
        }

        // 확인버튼 선택
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String nameStr = Name.getText().toString();
                final String ageStr = Age.getText().toString();
                final String htStr = Ht.getText().toString();
                final String wtStr = Wt.getText().toString();
                String sexStr=new String();
                String exerStr=new String();
                userSex = sexStr;
                userAge = ageStr;
                userHeight = htStr;
                userWeight = wtStr;


                int id = sexSelect.getCheckedRadioButtonId();
                switch (id){
                    case R.id.mButtonE : sexStr="남성"; break;
                    case R.id.feButtonE : sexStr="여성"; break;
                }
                id=actSelect.getCheckedRadioButtonId();
                switch (id){
                    case R.id.actButton1E : exerStr="거의 하지 않음"; break;
                    case R.id.actButton2E : exerStr="주1~2회정도"; break;
                    case R.id.actButton3E : exerStr="주3~4회정도"; break;
                    case R.id.actButton4E : exerStr="주5회이상"; break;
                    case R.id.actButton5E : exerStr="전문 운동선수";
                }

                /*
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader("/mnt/sdcard/Android/data/com.example.kcalmera/files/Documents/"+"profile.txt"));
                }
                catch (Exception e)
                {

                }*/
                //File file = new File(getFilesDir(), "profile.txt") ;
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + File.separator + "profile.txt");
                FileWriter fw = null ;
                BufferedWriter bufwr = null ;
                try {
                    fw = new FileWriter(file) ;
                    bufwr = new BufferedWriter(fw) ;
                    bufwr.write(nameStr+"\n");
                    bufwr.write(sexStr+"\n");
                    bufwr.write(ageStr+"\n");
                    bufwr.write(htStr+"\n");
                    bufwr.write(wtStr+"\n");
                    bufwr.write(exerStr+"\n");
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

                final int AGE = Integer.parseInt(ageStr);
                final double HEIGHT = Double.parseDouble(htStr);
                final double WEIGHT = Double.parseDouble(wtStr);
                final String SEX = sexStr;
                // TO DO: 활동 레벨 구현
                // 기초 대사율
                final double BMR = ProfileFragment.WEIGHT_FACTOR * WEIGHT + ProfileFragment.HEIGHT_FACTOR * HEIGHT - ProfileFragment.AGE_FACTOR * AGE
                        + (SEX.equals("남성")? ProfileFragment.MALE_BIAS : ProfileFragment.FEMALE_BIAS);
                // TO DO: 활동 레벨 구현 필요
                double[] ACTIVITY_LEVEL = {1.2, 1.375, 1.55, 1.725, 1.9};
                double RECOMMENDED_KCAL=0;
                switch(exerStr){
                    case "거의 하지 않음" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[0]; break;
                    case "주1~2회정도" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[1]; break;
                    case "주3~4회정도" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[2]; break;
                    case "주5회이상" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[3]; break;
                    case "전문 운동선수" : RECOMMENDED_KCAL = BMR * ACTIVITY_LEVEL[4]; break;
                }
                final double RECOMMENDED_PROTEIN = WEIGHT * ProfileFragment.PROTEIN_PER_WEIGHT;
                final double RECOMMENDED_CARBOHYDRATE = RECOMMENDED_PROTEIN * ProfileFragment.CARBOHYDRATE_FACTOR;
                final double RECOMMENDED_FAT = RECOMMENDED_PROTEIN * ProfileFragment.FAT_FACTOR;

                double kcal = 0, carbohydrate = 0, protein = 0, fat = 0, natrium = 0, cholesterol = 0;
                TextView kcalText = (TextView) findViewById(R.id.kcal);
                TextView carbohydrateText = (TextView) findViewById(R.id.carbohydrate);
                TextView proteinText = (TextView) findViewById(R.id.protein);
                TextView fatText = (TextView) findViewById(R.id.fat);
                TextView natriumText = (TextView) findViewById(R.id.natrium);
                TextView cholesterolText = (TextView) findViewById(R.id.cholesterol);

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

                // 영양 분석 메시지 출력
                boolean isKcalExceeded = (kcal > RECOMMENDED_KCAL);
                boolean isCarbohydrateExceeded = (carbohydrate > RECOMMENDED_CARBOHYDRATE);
                boolean isProteinExceeded = (protein > RECOMMENDED_PROTEIN);
                boolean isFatExceeded = (fat > RECOMMENDED_FAT);
                boolean isNatriumExceeded = (natrium > ProfileFragment.NATRIUM_BOUND);
                boolean isCholesterolExceeded = (cholesterol > ProfileFragment.CHOLESTEROL_BOUND);

                ArrayList<String> recommendationMsgs = new ArrayList<>();
                if (isKcalExceeded) {
                    recommendationMsgs.add("권장 칼로리 섭취량을 초과했습니다.");
                }

                if (isCarbohydrateExceeded) {
                    recommendationMsgs.add("권장 탄수화물 섭취량을 초과했습니다.");
                }

                if (isProteinExceeded) {
                    recommendationMsgs.add("권장 단백질 섭취량을 초과했습니다.");
                }

                if (isFatExceeded) {
                    recommendationMsgs.add("권장 지방 섭취량을 초과했습니다.");
                }

                if (isNatriumExceeded) {
                    recommendationMsgs.add("권장 나트륨 섭취량을 초과했습니다.");
                }

                if (isCholesterolExceeded) {
                    recommendationMsgs.add("권장 콜레스테롤 섭취량을 초과했습니다.");
                }

                String recommendationMsg = null;
                if (recommendationMsgs.size() == 0) {
                    recommendationMsg = "특이 사항이 없습니다.";
                } else {
                    recommendationMsg = recommendationMsgs.get(0);
                    for (int i = 1; i < recommendationMsgs.size(); ++i) {
                        recommendationMsg = recommendationMsg.concat("\n" + recommendationMsgs.get(i));
                    }
                }

                TextView recommendationMsgText = (TextView) findViewById(R.id.recommendationMsgText);
                recommendationMsgText.setText(recommendationMsg);

                // 음식 추천
                /*
                String foodList = null;
                if (isCarbohydrateExceeded) {
                    foodList = "저탄수화물 음식: " + TextUtils.join(" ", LOW_CARBOHYDRATE_FOODS);
                } else {
                    foodList = "고탄수화물 음식: " + TextUtils.join(" ", HIGH_CARBOHYDRATE_FOODS);
                }

                if (isProteinExceeded) {
                    foodList += "\n저단백질 음식: " + TextUtils.join(" ", LOW_PROTEIN_FOODS);
                } else {
                    foodList += "\n고단백질 음식: " + TextUtils.join(" ", HIGH_PROTEIN_FOODS);
                }

                if (isFatExceeded) {
                    foodList += "\n저지방 음식: " + TextUtils.join(" ", LOW_FAT_FOODS);
                } else {
                    foodList += "\n고지방 음식: " + TextUtils.join(" ", HIGH_FAT_FOODS);
                }

                if (isCholesterolExceeded) {
                    foodList += "\n저콜레스테롤 음식: " + TextUtils.join(" ", LOW_CHOLESTEROL_FOODS);
                }
                */

                HashSet<String> recommendedFood = new HashSet<>();
                HashSet<String> restrictedFood = new HashSet<>();
                if (isCarbohydrateExceeded) {
                    Collections.addAll(restrictedFood, ProfileFragment.HIGH_CARBOHYDRATE_FOODS);
                    Collections.addAll(recommendedFood, ProfileFragment.LOW_CARBOHYDRATE_FOODS);
                } else {
                    Collections.addAll(restrictedFood, ProfileFragment.LOW_CARBOHYDRATE_FOODS);
                    Collections.addAll(recommendedFood, ProfileFragment.HIGH_CARBOHYDRATE_FOODS);
                }

                if (isProteinExceeded) {
                    Collections.addAll(restrictedFood, ProfileFragment.HIGH_PROTEIN_FOODS);
                    Collections.addAll(recommendedFood, ProfileFragment.LOW_PROTEIN_FOODS);
                } else {
                    Collections.addAll(restrictedFood, ProfileFragment.LOW_PROTEIN_FOODS);
                    Collections.addAll(recommendedFood, ProfileFragment.HIGH_PROTEIN_FOODS);
                }

                if (isFatExceeded) {
                    Collections.addAll(restrictedFood, ProfileFragment.HIGH_FAT_FOODS);
                    Collections.addAll(recommendedFood, ProfileFragment.LOW_FAT_FOODS);
                } else {
                    Collections.addAll(restrictedFood, ProfileFragment.LOW_FAT_FOODS);
                    Collections.addAll(recommendedFood, ProfileFragment.HIGH_FAT_FOODS);
                }

                if (isCholesterolExceeded) {
                    Collections.addAll(recommendedFood, ProfileFragment.LOW_CHOLESTEROL_FOODS);
                }

                recommendedFood.removeAll(restrictedFood);

                TextView foodRecommendationText = (TextView) findViewById(R.id.foodRecommendationText);
                if (isCarbohydrateExceeded && isProteinExceeded && isFatExceeded && isCholesterolExceeded) {
                    foodRecommendationText.setText("-");
                } else {
                    foodRecommendationText.setText(recommendedFood.toString());
                }


                String textLine = (int)kcal + " / " + (int)RECOMMENDED_KCAL;
                kcalText.setText(textLine);
                Spannable span = (Spannable) kcalText.getText();
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textLine = (int)carbohydrate + " / " + (int)RECOMMENDED_CARBOHYDRATE;
                carbohydrateText.setText(textLine);
                span = (Spannable) carbohydrateText.getText();
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textLine = (int)protein + " / " + (int)RECOMMENDED_PROTEIN;
                proteinText.setText(textLine);
                span = (Spannable) proteinText.getText();
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textLine = (int)fat + " / " + (int)RECOMMENDED_FAT;
                fatText.setText(textLine);
                span = (Spannable) fatText.getText();
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textLine = (int)natrium + " / " + ProfileFragment.NATRIUM_BOUND;
                natriumText.setText(textLine);
                span = (Spannable) natriumText.getText();
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textLine = (int)cholesterol + " / " + ProfileFragment.CHOLESTEROL_BOUND;
                cholesterolText.setText(textLine);
                span = (Spannable) cholesterolText.getText();
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, textLine.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                final TextView textViewName = findViewById(R.id.nameText2);
                final TextView textViewSex = findViewById(R.id.sexText2);
                final TextView textViewAge = findViewById(R.id.ageText2);
                final TextView textViewHeight = findViewById(R.id.heightText2);
                final TextView textViewWeight = findViewById(R.id.weightText2);
                final TextView textViewExercise = findViewById(R.id.exerciseText2);

                textViewName.setText(nameStr);
                textViewSex.setText(sexStr);
                textViewAge.setText(ageStr);
                textViewHeight.setText(htStr);
                textViewWeight.setText(wtStr);
                textViewExercise.setText(exerStr);
            }
        });

        alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }

    static public Bitmap resizeBitmap(Bitmap original) {
        /*
        int resizeWidth = ImageClassifier.DIM_IMG_SIZE_X;

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        int targetHeight = (int) (resizeWidth * aspectRatio);
        //Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, targetHeight, false);
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, resizeWidth, false);
        if (result != original) {
            original.recycle();
        }
        return result;
        */
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap squaredImage;
        if (width < height) {
            squaredImage = Bitmap.createBitmap(original, 0, (height - width) / 2, width, width);
        }
        else {
            squaredImage = Bitmap.createBitmap(original, (width - height) / 2, 0, height, height);
        }

        int newWidth = ImageClassifier.DIM_IMG_SIZE_X, newHeight = ImageClassifier.DIM_IMG_SIZE_Y;
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);


        float ratioX = newWidth / (float) squaredImage.getWidth();
        float ratioY = newHeight / (float) squaredImage.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(squaredImage, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    //database
    public String insertRecord(String str1, String str2) {
        //str1 음식이름, str2 양
        try {
            db.execSQL("insert into diet_diary(foodName,amount) values (" + String.format("'%s'", str1) + ", " + String.format("'%s'", str2) + ");");
            Cursor c = db.rawQuery("select time, foodName, amount, _id from diet_diary order by _id DESC limit 1", null);
            String str = "";
            c.moveToNext();
            String time = c.getString(0);
            String foodName = c.getString(1);
            double amount = c.getDouble(2);
            int pmk = c.getInt(3);
            str += String.format("%s/%f/%s/%d", foodName, amount, time,pmk);
            c.close();
            return str;
        }
        catch(Exception e)
        {
            Log.e("ddd_insert_func",e.getMessage());
            return null;
        }
    }

    public String insertRecord2(String str1, String str2, String str3) {
        //str1 음식이름, str2 양, str3 시간
        int pmk_tmp=0;
        try {
            try {
                db.execSQL("insert into diet_diary(foodName,amount) values (" + String.format("'%s'", str1) + ", " + String.format("'%s'", str2) + ");");
                Cursor c_tmp = db.rawQuery("select _id from diet_diary order by _id DESC limit 1", null);
                c_tmp.moveToNext();
                pmk_tmp = c_tmp.getInt(0);
                db.execSQL("update diet_diary set time = " + String.format("'%s %s'", str3,"12:00:00") + "where _id = " + String.format("'%d'", pmk_tmp) + ";");
                c_tmp.close();
            }
            catch(Exception e){
                Log.e("eee_query error",e.getMessage());
            }
            //Cursor c = db.rawQuery("select time, foodName, amount, _id from diet_diary order by  DESC limit 1", null);
            Cursor c = db.rawQuery("select time, foodName, amount, _id from diet_diary where _id = " + String.format("'%d'", pmk_tmp) , null);

            String str = "";
            c.moveToNext();
            String time = c.getString(0);
            String foodName = c.getString(1);
            double amount = c.getDouble(2);
            int pmk = c.getInt(3);
            str += String.format("%s/%f/%s/%d", foodName, amount, time,pmk);
            c.close();
            return str;
        }
        catch(Exception e)
        {
            Log.e("ddd_insert_func",e.getMessage());
            return null;
        }
    }

    public void deleteRecord(int pmk){
        //pmk 프라이머리 키
        try {
            db.execSQL("delete from  diet_diary where _id = " + String.format("'%d'", pmk) + ";");
        }
        catch(Exception e)
        {
            Log.e("ddd_delete_func",e.getMessage());
        }
    }

    public String updateRecord(String str1,int pmk){
        //str1 양, pmk 프라이머리 키
        try {
            db.execSQL("update diet_diary set amount = " + String.format("'%s'", str1) +"where _id = " + String.format("'%d'", pmk)+ ";");
            Cursor c = db.rawQuery("select time, foodName, amount from diet_diary where _id = "+String.format("'%d'", pmk) , null);
            String str = "";
            c.moveToNext();
            String time = c.getString(0);
            String foodName = c.getString(1);
            double amount = c.getDouble(2);
            str += String.format("%s/%f/%s", foodName, amount, time);
            c.close();
            return str;
        }
        catch(Exception e)
        {
            Log.e("ddd_update_func",e.getMessage());
            return null;
        }
    }

    public Cursor selectRecord(String str1)
    {   //str1 yyyy-mm-dd
        //str1_ = yyyy-MM-dd HH:mm:ss
        try {
            String str1_start = str1.concat(" 00:00:00");
            String str1_end = str1.concat(" 23:59:59");
            Cursor c1 = db.rawQuery("select time, foodName, amount, _id from diet_diary where time between" + String.format("'%s'", str1_start) + "and" + String.format("'%s'", str1_end), null);
            return c1;
        }
        catch(Exception e)
        {
            Log.e("ddd_select_func",e.getMessage());
            return null;
        }
    }

    public String database_test(Cursor c)
    {
        try {
            int recordCount = c.getCount();
            String str = "";
            System.out.println("cursor count: " + recordCount);

            for (int i = 0; i < recordCount; i++) {
                c.moveToNext();
                String time = c.getString(0);
                String foodName = c.getString(1);
                double amount = c.getDouble(2);
                int pmk = c.getInt(3);
                str += String.format("%s/%f/%s/%d\n",foodName, amount, time,pmk);
            }
            c.close();
            return str;
        }
        catch(Exception e)
        {
            Log.e("ddd_test_func",e.getMessage());
            return null;
        }
    }


    public void createDatabase()
    {
        try {
            // db = mContext.openOrCreateDatabase("MyDb.db", MODE_WORLD_WRITEABLE, null);
            db = mContext.openOrCreateDatabase("MyDb.db", MODE_PRIVATE, null);

            //Toast.makeText(getApplicationContext(), "디비 성공",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "DB 등록에 실패하였습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    public void createFoodInfoTable()
    {
        try{
            String line;
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(this.getAssets().open("food-info.txt")));
            while ((line = reader.readLine()) != null) {
                String[] array = line.split("\t");
                db.execSQL("insert into food_info(foodName,gram,kcal,carbohydrate,protein,fat,sugars,natrium,cholesterol,fatty_acid,trans_fat) values (" + String.format("'%s'", array[0]) + ", " + String.format("'%s'", array[1]) +", " + String.format("'%s'", array[2]) + ", "+ String.format("'%s'", array[3]) + ", "+ String.format("'%s'", array[4]) + ", "+ String.format("'%s'", array[5]) + ", "+ String.format("'%s'", array[6]) + ", "+ String.format("'%s'", array[7]) + ", "+ String.format("'%s'", array[8]) + ", "+ String.format("'%s'", array[9]) + ", "+ String.format("'%s'", array[10]) + ");");
            }
            reader.close();
           // Toast.makeText(getApplicationContext(), "테이블 성공",Toast.LENGTH_SHORT).show();
        }
        catch(Exception e)
        {
            Log.e("table_error",e.getMessage());
            Toast.makeText(getApplicationContext(), "테이블 실패",Toast.LENGTH_SHORT).show();
        }
    }

    //오류 수정 필요할 수 도
    public String selectFoodInfo(String foodName)
    {
        String str = "";
        Cursor c = db.rawQuery("select gram, kcal,carbohydrate,protein,fat from food_info where foodName = " + String.format("'%s'", foodName) , null);
        if((c != null) && (c.getCount() > 0)) {
            c.moveToNext();
            str+= String.format("%.2f/%.2f/%.2f/%.2f/%.2f\n",c.getDouble(0), c.getDouble(1), c.getDouble(2), c.getDouble(3), c.getDouble(4));
        }
        else
            str = null;

        c.close();
        return str;
    }

    public String selectFoodInfo2(String foodName)
    {// 영양정보 팝업 출력
        String str = "";
        Cursor c = db.rawQuery("select gram, kcal,carbohydrate,protein,fat,sugars,natrium,cholesterol,fatty_acid,trans_fat from food_info where foodName = " + String.format("'%s'", foodName) , null);
        if((c != null) && (c.getCount() > 0)) {
            c.moveToNext();
            str+= String.format("%.1f/%.1f/%.1f/%.1f/%.1f/%.1f/%.1f/%.1f/%.1f/%.1f\n",c.getDouble(0), c.getDouble(1), c.getDouble(2), c.getDouble(3), c.getDouble(4),c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getDouble(9));
        }
        else
            str = null;

        c.close();
        return str;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 음식 사진 촬영
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegress(exifOrientation);
            } else {
                exifDegree = 0;
            }
            setContentView(R.layout.fragment_cam);
            // ((ImageView) findViewById(R.id.imageview)).setImageBitmap(rotate(bitmap, exifDegree));

            Bitmap resizeBitmap = resizeBitmap(rotate(bitmap, exifDegree));
            ((ImageView) findViewById(R.id.imageview)).setImageBitmap(resizeBitmap);


            ImageClassifier classifier = null;
            try {
                classifier = new ImageClassifier(this);
            } catch (IOException e) {
                Log.e(TAG, "Failed to initialize an image classifier.");
            }

            String textToShow = classifier.classifyFrame(resizeBitmap);
            Toast.makeText(this, textToShow, Toast.LENGTH_LONG).show();

            TextView Foodview=(TextView) findViewById(R.id.DetectedFood);
            Foodview.setText(Food);

            if (classifier != null) {
                classifier.close();

            }
        }
        else if(requestCode == REQUEST_FOOD_SET && resultCode == RESULT_OK)
        {
            Log.e(TAG,data.getStringExtra("INPUT_TEXT"));
        }
        //프로필 사진 설정
        /*
        else if(requestCode == PROFILE_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                ((ImageView) findViewById(R.id.profile_image)).setImageURI(selectedImageUri);

                File root = Environment.getExternalStorageDirectory();
                File file = new File(root.getAbsolutePath()+"/DCIM/Camera/img.jpg");

            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }

        }
        */

    }
}

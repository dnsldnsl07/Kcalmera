package com.example.kcalmera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


public class MainActivity extends FragmentActivity {
    private static final int PROFILE_REQUEST_CODE=699, REQUEST_IMAGE_CAPTURE = 700;
    private String imageFilePath;
    private Uri photoUri;
    private static final String TAG = "TfLiteCameraDemo";

    public static Context mContext;

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
        });
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

    public void onClickCam1(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }
    public void onClickCam2(View v) {
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
        EditText name = (EditText) findViewById(R.id.nameText2);
        EditText sex = (EditText) findViewById(R.id.sexText2);
        EditText age = (EditText) findViewById(R.id.ageText2);
        EditText ht = (EditText) findViewById(R.id.heightText2);
        EditText wt = (EditText) findViewById(R.id.weightText2);
        Button edit = (Button) findViewById(R.id.edit_button);
        Button confirm = (Button) findViewById(R.id.confirm_button);

        name.setFocusableInTouchMode(true);
        sex.setFocusableInTouchMode(true);
        age.setFocusableInTouchMode(true);
        ht.setFocusableInTouchMode(true);
        wt.setFocusableInTouchMode(true);
        edit.setVisibility(View.INVISIBLE);
        confirm.setVisibility(View.VISIBLE);
    }

    public void onClickConfirm(View v) {
        EditText Name = (EditText) findViewById(R.id.nameText2);
        EditText Sex = (EditText) findViewById(R.id.sexText2);
        EditText Age = (EditText) findViewById(R.id.ageText2);
        EditText Height = (EditText) findViewById(R.id.heightText2);
        EditText Weight = (EditText) findViewById(R.id.weightText2);
        Button Edit = (Button) findViewById(R.id.edit_button);
        Button Confirm = (Button) findViewById(R.id.confirm_button);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/mnt/sdcard/Android/data/com.example.kcalmera/files/Documents/"+"profile.txt"));
        }
        catch (Exception e)
        {

        }
        String s = new String();
        String name = new String(Name.getText().toString());
        String sex = new String(Sex.getText().toString());
        String age = new String(Age.getText().toString());
        String height = new String(Height.getText().toString());
        String weight = new String(Weight.getText().toString());

        //File file = new File(getFilesDir(), "profile.txt") ;
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + File.separator + "profile.txt");
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

        Name.setFocusable(false);
        Name.setFocusableInTouchMode(false);
        Sex.setFocusable(false);
        Sex.setFocusableInTouchMode(false);
        Age.setFocusable(false);
        Age.setFocusableInTouchMode(false);
        Height.setFocusable(false);
        Height.setFocusableInTouchMode(false);
        Weight.setFocusable(false);
        Weight.setFocusableInTouchMode(false);
        Edit.setVisibility(View.VISIBLE);
        Confirm.setVisibility(View.INVISIBLE);
    }

    public void setProfile(View v){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, PROFILE_REQUEST_CODE);
    }

    static public Bitmap resizeBitmap(Bitmap original) {

        int resizeWidth = ImageClassifier.DIM_IMG_SIZE_X;

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        int targetHeight = (int) (resizeWidth * aspectRatio);
        //Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, targetHeight, false);
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, resizeWidth, false);
        if (result != original) {
            original.recycle();
        }
        return result;
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

        return str;
    }

    public String selectFoodInfo2(String foodName)
    {// 영양정보 팝업 출력
        String str = "";
        Cursor c = db.rawQuery("select gram, kcal,carbohydrate,protein,fat,sugars,natrium,cholesterol,fatty_acid,trans_fat from food_info where foodName = " + String.format("'%s'", foodName) , null);
        if((c != null) && (c.getCount() > 0)) {
            c.moveToNext();
            str+= String.format("%.2f/%.2f/%.2f/%.2f/%.2f/%.2f/%.2f/%.2f/%.2f/%.2f\n",c.getDouble(0), c.getDouble(1), c.getDouble(2), c.getDouble(3), c.getDouble(4),c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getDouble(9));
        }
        else
            str = null;

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

            if (classifier != null) {
                classifier.close();

            }
        }

        //프로필 사진 설정
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

    }
}

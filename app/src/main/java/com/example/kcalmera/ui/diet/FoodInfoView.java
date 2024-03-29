package com.example.kcalmera.ui.diet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kcalmera.R;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class FoodInfoView extends ConstraintLayout {

    TextView foodName;
    TextView foodInfo;

    public FoodInfoView(Context context) {
        super(context);
        init(context);
    }

    public FoodInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.foodinfopopup,this,true);
        foodName = (TextView) findViewById(R.id.foodNamepopup);
        foodInfo = (TextView) findViewById(R.id.foodInfopopup);
    }

    public void setText1(String name){
        foodName.setText(name);
    }
    public void setText2(String foodinfo){
        String str[] = foodinfo.split("/");
        foodInfo.setText(String.format("          1회 제공량  %sg\n                 칼로리  %skcal\n             탄수화물  %sg\n                 단백질  %sg\n                     지방  %sg\n                     당류  %sg\n                 나트륨  %smg\n          콜레스테롤  %smg\n          포화지방산  %sg\n",str[0],str[1],str[2],str[3],str[4],str[5],str[6],str[7],str[8]));
    }
}

/*list 의 item view*/

package com.example.kcalmera.ui.diet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.kcalmera.R;

import androidx.annotation.Nullable;

public class TestView extends LinearLayout implements Checkable {

    CheckedTextView ctv;
    TextView tv;

    public TestView(Context context) {
        super(context);
        init(context);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.test_item,this,true);
        ctv = (CheckedTextView)findViewById(R.id.ctv);
        tv = (TextView)findViewById(R.id.testitem1);
    }

    public void setText1(String name,double amount){
        //textView1.setText(name);
        ctv.setText(name + " " + amount);
        //ctv.setChecked(true);
    }
    public void setText2(String foodInfo){
        tv.setText(foodInfo);
    }

    @Override
    public void setChecked(boolean b) {
        ctv.setChecked(b);
    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }
}

/*modify 팝업창 view*/

package com.example.kcalmera.ui.diet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.kcalmera.R;

import androidx.annotation.Nullable;


public class ModifyView extends LinearLayout {

    EditText modifyAmountEditView;

    public ModifyView(Context context) {
        super(context);
        init(context);
    }

    public ModifyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.modify_view,this,true);
        modifyAmountEditView = (EditText) findViewById(R.id.modifyAmountEditView);
    }

}

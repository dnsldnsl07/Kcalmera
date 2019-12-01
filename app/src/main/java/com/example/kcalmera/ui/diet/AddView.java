/*add 팝업창 view*/

package com.example.kcalmera.ui.diet;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.kcalmera.R;

import androidx.annotation.Nullable;


public class AddView extends LinearLayout {
    AutoCompleteTextView addFoodEditView;
    EditText addAmountEditView;

    public AddView(Context context) {
        super(context);
        init(context);
    }

    public AddView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View textEntryView = inflater.inflate(R.layout.add_view,this,true);
        addFoodEditView = (AutoCompleteTextView) textEntryView.findViewById(R.id.addFoodEditView);
        addAmountEditView = (EditText) findViewById(R.id.addAmountEditView);
    }


}

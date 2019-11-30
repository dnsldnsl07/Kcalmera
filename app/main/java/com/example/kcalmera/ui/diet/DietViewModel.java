package com.example.kcalmera.ui.diet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DietViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DietViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("식단관리");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
package com.example.kcalmera.ui.cam;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CamViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CamViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("음식등록");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
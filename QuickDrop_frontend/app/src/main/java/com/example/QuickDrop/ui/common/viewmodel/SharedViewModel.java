package com.example.QuickDrop.ui.common.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<String>> idSetLiveData = new MutableLiveData<>();

    public void setIdSet(List<String> idSet) {
        idSetLiveData.setValue(idSet);
    }

    public LiveData<List<String>> getIdSet() {
        return idSetLiveData;
    }
}

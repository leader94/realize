package com.ps.realize.core.interfaces;

public interface CallbackListener {
    //    void onCallback();
//    void onCallbackWithData(String data);
    void onSuccess();

    default void onSucessWithData() {

    }

    //
    void onFailure();
}

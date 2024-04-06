package com.ps.realize.core.helperClasses;

import com.ps.realize.core.interfaces.IPopBackDataListener;

import java.util.ArrayList;
import java.util.List;

public class PopBackDataHelper {

    private final List<IPopBackDataListener> listeners = new ArrayList<>();


    public void addListener(IPopBackDataListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IPopBackDataListener listener) {
        listeners.remove(listener);
    }

    public void passDataBackToListeners(String data) {
        for (IPopBackDataListener listener : listeners) {
            listener.onPopBackDataRecieved(data);
        }

    }
}

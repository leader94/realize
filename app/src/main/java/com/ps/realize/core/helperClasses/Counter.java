package com.ps.realize.core.helperClasses;


import com.ps.realize.core.interfaces.ICounterListener;

import java.util.ArrayList;
import java.util.List;

public class Counter {

    private final List<ICounterListener> listeners = new ArrayList<>();
    private int count = 0;

    public void increment() {
        count++;
        notifyListeners();
    }

    public void decrement() {
        count--;
        notifyListeners();
    }

    public void reset() {
        count = 0;
        notifyListeners();
    }

    public int getCount() {
        return count;
    }

    public void addListener(ICounterListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ICounterListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ICounterListener listener : listeners) {
            listener.onCounterChanged(count);
        }
    }
}
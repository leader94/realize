package com.ps.realize.core.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.SearchView;

public class CustomSearchView extends SearchView {

    private SearchViewListener _searchViewListener;
    public  interface SearchViewListener {
        void onDispatchKeyEventPreIme(KeyEvent keyEvent, boolean focus);
    }
    public CustomSearchView(Context context) {
        super(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        _searchViewListener.onDispatchKeyEventPreIme(event, hasFocus());
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        _searchViewListener.onDispatchKeyEventPreIme(event, hasFocus());
        return super.dispatchKeyEventPreIme(event);
    }

    public void setSearchViewListener(SearchViewListener listener){
        _searchViewListener = listener;
    }


}

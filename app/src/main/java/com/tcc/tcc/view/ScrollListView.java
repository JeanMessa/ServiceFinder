package com.tcc.tcc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ScrollListView extends ListView {

    private int qtd_max_itens;

    public ScrollListView(Context context) {
        super(context);
        setScrollOnTouch();
    }

    public ScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScrollOnTouch();
    }

    public ScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScrollOnTouch();
    }

    public ScrollListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setScrollOnTouch();
    }



    private void setScrollOnTouch(){
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                performClick();
                v.onTouchEvent(event);
                return true;
            }
        });
    }

    public void atualizarHeight(int tamanhoAdicional){
        LinearLayout.LayoutParams params;
        if(getAdapter().getCount()!=0){
            View listItem = getAdapter().getView(0, null, this);
            listItem.measure(0, 0);


            if (getAdapter().getCount()<4){
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getAdapter().getCount() * listItem.getMeasuredHeight() + tamanhoAdicional);
            }else{
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (3.5 * listItem.getMeasuredHeight() + tamanhoAdicional));
            }
        }else{
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        }

        setLayoutParams(params);
    }
    public int getQtd_max_itens() {
        return qtd_max_itens;
    }

    public void setQtd_max_itens(int qtd_max_itens) {
        this.qtd_max_itens = qtd_max_itens;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}

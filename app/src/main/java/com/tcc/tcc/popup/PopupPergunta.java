package com.tcc.tcc.popup;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tcc.tcc.R;

import java.util.zip.Inflater;

public class PopupPergunta
{
    private PopupWindow popup;

    private ViewGroup parent;

    private Button btnConfirmar,btnRecusar;
    public PopupPergunta(String pergunta, ViewGroup parent){
        this.parent = parent;
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_pergunta, parent, false);
        popup = new PopupWindow(popupView, MATCH_PARENT, WRAP_CONTENT, true);
        TextView textViewPergunta = popupView.findViewById(R.id.textViewPergunta);
        textViewPergunta.setText(pergunta);
        btnConfirmar = popupView.findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        btnRecusar = popupView.findViewById(R.id.btnRecusar);
        btnRecusar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public void setBtnConfirmarListener(View.OnClickListener btnConfirmarListener) {
        btnConfirmar.setOnClickListener(btnConfirmarListener);
    }

    public void setBtnRecusarListener(View.OnClickListener btnRecusarListener) {
        btnRecusar.setOnClickListener(btnRecusarListener);
    }

    public boolean show(@Nullable String keySharedPreferences,Context context){
        SharedPreferences preferences = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        boolean mostrarPopup = preferences.getBoolean(keySharedPreferences,true);
        if (mostrarPopup){
            popup.showAtLocation(parent.getRootView(), Gravity.CENTER,0,0);
            return true;
        }else {
            btnConfirmar.callOnClick();
            return false;
        }

    }
}

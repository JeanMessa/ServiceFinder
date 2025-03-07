package com.tcc.tcc.popup;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Servico;
import com.tcc.tcc.principal.ActivityPrincipal;

public class PopupVerMaisServico {
    private PopupWindow popup;

    private ViewGroup parent;

    public PopupVerMaisServico(String keyServico, ViewGroup parent, String keyPessoaUsuario){
        this.parent = parent;
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_ver_mais_servico, parent, false);
        popup = new PopupWindow(popupView, MATCH_PARENT, WRAP_CONTENT, true);
        Servico.preencherPopupVerMais(keyServico,popupView,keyPessoaUsuario);

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ActivityPrincipal activityPrincipal = (ActivityPrincipal) parent.getContext();
                activityPrincipal.trocarEscurecer(View.GONE);
            }
        });

        ImageButton btnFechar = popupView.findViewById(R.id.btnFechar);
        btnFechar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    public PopupWindow getPopup() {
        return popup;
    }


    public void show(){

        popup.showAtLocation(parent.getRootView(), Gravity.CENTER,0,0);
        ActivityPrincipal activityPrincipal = (ActivityPrincipal) parent.getContext();
        activityPrincipal.trocarEscurecer(View.VISIBLE);
    }
}

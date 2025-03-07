package com.tcc.tcc.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.popup.PopupPergunta;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.util.ArrayList;
import java.util.Arrays;

public class AdapterBloqueados extends ArrayAdapter<Pair<String,String>> {

    private Context context;
    private int resource;
    String keyPessoaUsuario;
    Button btnBloqueados;

    public AdapterBloqueados(Context context, int resource, String keyPessoaUsuario, Button btnBloqueados) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.btnBloqueados = btnBloqueados;
        this.keyPessoaUsuario = keyPessoaUsuario;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, parent, false);
        }

        TextView textViewItem = view.findViewById(R.id.textViewItem);

        Pair<String,String> itemBloqueado = getItem(position);

        if (itemBloqueado!=null){

            String keyBloqueado = itemBloqueado.first;
            String nomeBloqueado = itemBloqueado.second;

            textViewItem.setText(nomeBloqueado);





            PopupPergunta popupPerguntaBloquear = new PopupPergunta("Se você desbloquear " + nomeBloqueado + " ele(a) poderá aparecer novamente nas telas do aplicativo (ainda pode estar ausente em algumas telas até o reinício do aplicativo).\nTem certeza que deseja desbloquear esta pessoa?",parent);
            popupPerguntaBloquear.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    ActivityPrincipal activityPrincipal = (ActivityPrincipal)context;
                    if (activityPrincipal!=null){
                        activityPrincipal.trocarEscurecer(View.GONE);
                    }
                }
            });

            popupPerguntaBloquear.setBtnConfirmarListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(itemBloqueado);
                    Pessoa.removerBloqueados(keyBloqueado,keyPessoaUsuario);
                    if(AdapterBloqueados.this.getCount()==0){
                        btnBloqueados.setVisibility(View.GONE);
                    }
                    popupPerguntaBloquear.getPopup().dismiss();
                }
            });


            if (keyPessoaUsuario!=null){
                ImageButton btnRemover = view.findViewById(R.id.btnRemover);
                btnRemover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(popupPerguntaBloquear.show("confirmacaoBloquear",context)){
                            ActivityPrincipal activityPrincipal = (ActivityPrincipal)context;
                            if (activityPrincipal!=null){
                                activityPrincipal.trocarEscurecer(View.VISIBLE);
                            }
                        }
                    }
                });
            }

        }



        return view;
    }



}
package com.tcc.tcc.principal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tcc.tcc.R;
import com.tcc.tcc.adapter.AdapterConversas;
import com.tcc.tcc.classe.models.Conversa;


public class FragmentPrincipalConversas extends Fragment {

    RecyclerView listConversas;
    AdapterConversas adapterConversas;

    EditText txtNome;
    ImageButton btnLimparNome;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_principal_conversas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listConversas = view.findViewById(R.id.listConversas);
        ActivityPrincipal activityPrincipal = (ActivityPrincipal)getActivity();
        listConversas.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        TextView textViewAviso = view.findViewById(R.id.textViewAviso);
        new Thread(new Runnable() {
            public void run() {
               while (activityPrincipal.getKeyPessoaUsuario()==null);
               adapterConversas = new AdapterConversas(listConversas,activityPrincipal.getKeyPessoaUsuario(),textViewAviso);
               if (!txtNome.getText().toString().isEmpty()){
                   adapterConversas.filtrar(txtNome.getText().toString());
               }
            }
        }).start();

        txtNome = view.findViewById(R.id.txtNome);
        btnLimparNome = view.findViewById(R.id.btnLimparNome);

        txtNome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!txtNome.getText().toString().isEmpty()){
                    btnLimparNome.setVisibility(View.VISIBLE);
                }else {
                    btnLimparNome.setVisibility(View.GONE);
                }
                if (adapterConversas!=null){
                    adapterConversas.filtrar(txtNome.getText().toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnLimparNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtNome.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        Conversa.destruirListener();
        super.onDestroyView();
    }
}
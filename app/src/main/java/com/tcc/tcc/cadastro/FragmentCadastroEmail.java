package com.tcc.tcc.cadastro;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tcc.tcc.R;
import com.tcc.tcc.classe.utils.Email;
import com.tcc.tcc.classe.utils.VerificaCampo;


public class FragmentCadastroEmail extends Fragment {

    private Button btnValidar,btnReenviar;
    private EditText txtCodigo;
    private TextView textViewEmail;
    String email;
    private OnDataPass dataPasser;


    public FragmentCadastroEmail(){

    }

    public static FragmentCadastroEmail newInstance(String email,boolean emailValidado) {
        FragmentCadastroEmail fragment = new FragmentCadastroEmail();
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putBoolean("emailValidado",emailValidado);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cadastro_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        Email e = new Email();

        boolean emailValidado;

        textViewEmail = view.findViewById(R.id.textViewEmail);


        txtCodigo = view.findViewById(R.id.txtCodigo);
        txtCodigo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                    txtCodigo.setHint("");
            }
        });


        btnValidar = view.findViewById(R.id.btnValidar);
        btnValidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VerificaCampo.verificaVazio(view,txtCodigo,"Preencha o campo de código")){
                    if (e.VerificarCodigo(txtCodigo.getText().toString())){
                        Toast.makeText(v.getContext(), "Código correto", Toast.LENGTH_SHORT).show();
                        changeEmailValidado();
                        ((ActivityCadastro)getActivity()).HabilitarTab(4);
                    }else{
                        Toast.makeText(v.getContext(), "Código incorreto, verifique se digitou corretamente", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnReenviar = view.findViewById(R.id.btnReenviar);
        btnReenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e.enviarCodigoValidacao(email,"Código para validar o seu email do ServiceFinder");
            }
        });

        if (getArguments()!=null){
            email = getArguments().getString("email");
            emailValidado = getArguments().getBoolean("emailValidado");
            textViewEmail.setText(email);
            if (!emailValidado){
                e.enviarCodigoValidacao(email,"Código para validar o seu email do ServiceFinder");
            }else {
                changeEmailValidado();
            }
        }

        super.onViewCreated(view, savedInstanceState);
    }

    public interface OnDataPass {
        public void onDataPassEmail();
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public void changeEmailValidado(){
        ImageView imgCheck = requireView().findViewById(R.id.imgCheck);
        imgCheck.setVisibility(View.VISIBLE);

        TextView textViewDigiteCodigo = requireView().findViewById(R.id.textViewDigiteCodigo);
        textViewDigiteCodigo.setText("Seu Email foi validado com sucesso");

        btnValidar.setVisibility(View.GONE);

        btnReenviar.setVisibility(View.GONE);

        Button btnContinuar = requireView().findViewById(R.id.btnContinuar);
        btnContinuar.setVisibility(View.VISIBLE);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityCadastro)getActivity()).trocarTab(4);
            }
        });

        txtCodigo.setHint("Email Validado");
        txtCodigo.setHintTextColor(getResources().getColor(R.color.VerdeCorreto));
        txtCodigo.setEnabled(false);

        dataPasser.onDataPassEmail();
    }


}
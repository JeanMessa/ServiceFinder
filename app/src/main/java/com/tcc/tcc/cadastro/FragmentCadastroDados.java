package com.tcc.tcc.cadastro;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.santalu.maskara.widget.MaskEditText;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Usuario;
import com.tcc.tcc.classe.utils.Senha;
import com.tcc.tcc.classe.utils.VerificaCampo;

import java.util.Objects;


public class FragmentCadastroDados extends Fragment {

    private EditText txtNome, txtSobrenome, txtEmail, txtSenha, txtConfirmarSenha;
    private MaskEditText txtCPF;
    private ImageButton btnVisualizarSenha, btnVisualizarConfirmarSenha;
    private TextView textViewErroNome, textViewErroSobrenome, textViewErroCPF, textViewErroEmail, textViewErroSenha, textViewErroConfirmarSenha;

    private Button btnContinuar;
    private ColorStateList hintColor;

    private OnDataPass dataPasser;

    private String keyPessoaEditar;

    LinearLayout layoutCPF,layoutSenha;


    public FragmentCadastroDados() {}


    public static FragmentCadastroDados newInstance(String nome, String sobrenome, String cpf, String email, String senha) {
        FragmentCadastroDados fragment = new FragmentCadastroDados();
        Bundle args = new Bundle();
        args.putString("nome", nome);
        args.putString("sobrenome",sobrenome);
        args.putString("cpf",cpf);
        args.putString("email",email);
        args.putString("senha",senha);
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentCadastroDados newInstance(String nome, String sobrenome,String email,String keyPessoaEditar) {
        FragmentCadastroDados fragment = new FragmentCadastroDados();
        Bundle args = new Bundle();
        args.putString("nome", nome);
        args.putString("sobrenome",sobrenome);
        args.putString("email",email);
        args.putString("keyPessoaEditar",keyPessoaEditar);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_cadastro_dados, view, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);



        txtNome = view.findViewById(R.id.txtNome);
        txtSobrenome = view.findViewById(R.id.txtSobrenome);
        txtCPF = view.findViewById(R.id.txtCPF);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtSenha = view.findViewById(R.id.txtSenha);
        txtConfirmarSenha = view.findViewById(R.id.txtConfirmarSenha);

        textViewErroNome = view.findViewById(R.id.textViewErroNome);
        textViewErroSobrenome = view.findViewById(R.id.textViewErroSobrenome);
        textViewErroCPF = view.findViewById(R.id.textViewErroCPF);
        textViewErroEmail = view.findViewById(R.id.textViewErroEmail);
        textViewErroSenha = view.findViewById(R.id.textViewErroSenha);
        textViewErroConfirmarSenha = view.findViewById(R.id.textViewErroConfimarSenha);

        layoutCPF = view.findViewById(R.id.layoutCPF);
        layoutSenha = view.findViewById(R.id.layoutSenha);


        if (getArguments() != null) {
            if (getArguments().getString("nome")!=null) {
                txtNome.setText(getArguments().getString("nome"));
                txtSobrenome.setText(getArguments().getString("sobrenome"));
                txtEmail.setText(getArguments().getString("email"));

                if (getArguments().getString("cpf")!=null && !Objects.equals(getArguments().getString("cpf"), "")) {
                    keyPessoaEditar = null;

                    txtCPF.setText(getArguments().getString("cpf"));
                    txtSenha.setText(getArguments().getString("senha"));
                    txtConfirmarSenha.setText(getArguments().getString("senha"));
                }else{
                    keyPessoaEditar = getArguments().getString("keyPessoaEditar");
                    layoutCPF.setVisibility(View.GONE);

                    layoutSenha.setVisibility(View.GONE);
                }


                VerificaCampo.verificaChaveUnicaCPF(txtCPF, textViewErroCPF, "Esse CPF já pertence a outra conta", true,this);
                VerificaCampo.verificaChaveUnicaEmail(txtEmail, textViewErroEmail, "Esse email já pertence a outra conta", true,this,keyPessoaEditar);
            }
        }

        hintColor = txtNome.getHintTextColors();

        btnVisualizarSenha = view.findViewById(R.id.btnVisualizarSenha);
        btnVisualizarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Senha.visualizarSenha(view, txtSenha, btnVisualizarSenha);
            }
        });

        btnVisualizarConfirmarSenha = view.findViewById(R.id.btnVisualizarConfirmarSenha);
        btnVisualizarConfirmarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Senha.visualizarSenha(view, txtConfirmarSenha, btnVisualizarConfirmarSenha);
            }
        });



        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ActivityCadastro)getActivity()).trocarTab(1);
            }
        });

    }

    public interface OnDataPass {
        public void onDataPassDados(Pessoa pessoa,Usuario usuario);
        public void onDataPassDadosEmailTrocado();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public boolean VerificaNome(View view) {
        if (!VerificaCampo.verificaVazio(view, txtNome, textViewErroNome, "O campo de nome deve ser preenchido")) {
            return false;
        } else {
            VerificaCampo.campoCorreto(view, txtNome, textViewErroNome, hintColor);
            return true;
        }
    }

    public boolean VerificaSobrenome(View view) {
        if (!VerificaCampo.verificaVazio(view, txtSobrenome, textViewErroSobrenome, "O campo de sobrenome deve ser preenchido")) {
            return false;
        } else {
            VerificaCampo.campoCorreto(view, txtSobrenome, textViewErroSobrenome, hintColor);
            return true;
        }
    }

    public boolean VerificaCPF(View view) {
        if (layoutCPF.getVisibility()==View.GONE){
            return true;
        }else if (!VerificaCampo.verificaVazio(view, txtCPF, textViewErroCPF, "O campo de CPF deve ser preenchido")) {
            return false;
        } else if (!VerificaCampo.verificaValidadeCPF(view, txtCPF, textViewErroCPF, "O CPF digitado é inválido")) {
            return false;
        } else {
            VerificaCampo.campoCorreto(view, txtCPF, textViewErroCPF, hintColor);
            return true;
        }
    }

    public boolean VerificaEmail(View view) {
        if (!VerificaCampo.verificaVazio(view, txtEmail, textViewErroEmail, "O campo de email deve ser preenchido")) {
            return false;
        } else if (!VerificaCampo.verificaValidadeEmail(view, txtEmail, textViewErroEmail, "O campo de email deve conter um '@'")) {
            return false;
        } else {
            VerificaCampo.campoCorreto(view, txtEmail, textViewErroEmail, hintColor);
            return true;
        }
    }

    public boolean VerificaSenha(View view) {
        if (layoutSenha.getVisibility()==View.GONE){
            return true;
        }else if (!VerificaCampo.verificaVazio(view, txtSenha, textViewErroSenha, "O campo de senha deve ser preenchido")) {
            return false;
        } else {
            VerificaCampo.campoCorreto(view, txtSenha, textViewErroSenha, hintColor);
            return true;
        }
    }

    public boolean VerificaConfirmarSenha(View view) {
        if (layoutSenha.getVisibility()==View.GONE){
            return true;
        }else if (!VerificaCampo.verificaVazio(view, txtConfirmarSenha, textViewErroConfirmarSenha, "O campo de confirmar senha deve ser preenchido")) {
            return false;
        }else if (!VerificaCampo.verificaIgualdade(view, txtConfirmarSenha, txtSenha, textViewErroConfirmarSenha, "O campo de confirmar senha deve conter a mesma senha do campo senha")) {
            return false;
        }else {
            VerificaCampo.campoCorreto(view, txtConfirmarSenha, textViewErroConfirmarSenha, hintColor);
            return true;
        }
    }

    public void continuar(View view, int tabNum) {
        EditText foco = null;
        boolean focar = true;
        if (!VerificaConfirmarSenha(view)) {
            foco = txtConfirmarSenha;
        }
        if (!VerificaSenha(view)) {
            foco = txtSenha;
        }
        if (!VerificaEmail(view)) {
            foco = txtEmail;
        }
        if (!VerificaCPF(view)) {
            foco = txtCPF;
        }
        if (!VerificaSobrenome(view)) {
            foco = txtSobrenome;
        }
        if (!VerificaNome(view)) {
            foco = txtNome;
        }
        if (foco == null) {
            ActivityCadastro.permissaoTrocarFragment = true;
            Pessoa pessoa = new Pessoa();
            pessoa.setNome(txtNome.getText().toString());
            pessoa.setSobrenome(txtSobrenome.getText().toString());
            pessoa.setCpf(txtCPF.getMasked());
            if (getArguments()!=null){
                if (!txtEmail.getText().toString().equals(requireArguments().getString("email"))){
                    dataPasser.onDataPassDadosEmailTrocado();
                }
            }

            Usuario usuario = new Usuario();
            usuario.setEmail(txtEmail.getText().toString());
            usuario.setSenha(txtSenha.getText().toString());
            dataPasser.onDataPassDados(pessoa,usuario);
            VerificaCampo.continuarCadastroDados(view,tabNum,pessoa.getCpf(),usuario.getEmail(),keyPessoaEditar);
        } else {
            ActivityCadastro activityCadastro = (ActivityCadastro)getActivity();
            ActivityCadastro.permissaoTrocarFragment = false;
            activityCadastro.trocarTab(0);
            activityCadastro.rolarScrollFragment(View.FOCUS_UP);
            foco.requestFocus();
            focar = !(foco == txtNome || foco == txtSobrenome || foco == txtCPF);
        }
        VerificaCampo.verificaChaveUnicaEmail(txtEmail, textViewErroEmail, "Esse email já pertence a outra conta", focar,this,keyPessoaEditar);
        VerificaCampo.verificaChaveUnicaCPF(txtCPF, textViewErroCPF, "Esse CPF já pertence a outra conta", focar,this);
    }

}
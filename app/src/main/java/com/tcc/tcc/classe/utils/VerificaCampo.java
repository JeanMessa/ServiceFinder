package com.tcc.tcc.classe.utils;

import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.santalu.maskara.widget.MaskEditText;
import com.tcc.tcc.R;
import com.tcc.tcc.cadastro.ActivityCadastro;
import com.tcc.tcc.cadastro.FragmentCadastroDados;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Usuario;

import java.util.ArrayList;

public class VerificaCampo {
    public static boolean verificaVazio(View view,EditText txt, TextView textView, String mensagem){
        if(!txt.getText().toString().equals("")){
            return true;
        }else{
            ColorStateList vermelhoErro = view.getContext().getColorStateList(R.color.VermelhoErro);
            textView.setText(mensagem);
            textView.setVisibility(View.VISIBLE);
            txt.setHintTextColor(vermelhoErro);
            txt.setBackgroundTintList(vermelhoErro);
            return false;
        }

    }

    public static boolean verificaVazio(View view,EditText txt, String mensagem){
        if(!txt.getText().toString().equals("")){
            return true;
        }else{
            Toast.makeText(view.getContext(), mensagem, Toast.LENGTH_SHORT).show();
            return false;
        }

    }


    public static boolean verificaValidadeCPF(View view, MaskEditText txt, TextView textView, String mensagem){
        String cpf = txt.getUnMasked();
        boolean valido = true;

        // Elimina CPFs invalidos conhecidos
        if (cpf.length() != 11 ||
        cpf.equals("00000000000") ||
        cpf.equals("11111111111") ||
        cpf.equals("22222222222")  ||
        cpf.equals("33333333333") ||
        cpf.equals("44444444444")  ||
        cpf.equals("55555555555")  ||
        cpf.equals("66666666666")  ||
        cpf.equals("77777777777")  ||
        cpf.equals("88888888888") ||
        cpf.equals("99999999999") ){
            valido = false;
        }else {
            // Valida primeiro digito
            int add = 0;
            for (int i = 0; i < 9; i++) {
                add += Integer.parseInt(String.valueOf(cpf.charAt(i))) * (10 - i);
            }
            int rev = 11 - (add % 11);
            if (rev == 10 || rev == 11) {
                rev = 0;
            }
            if (rev != Integer.parseInt(String.valueOf(cpf.charAt(9)))) {
                valido = false;
            } else {
                // Valida 2o digito
                add = 0;
                for (int i = 0; i < 10; i++) {
                    add += Integer.parseInt(String.valueOf(cpf.charAt(i))) * (11 - i);
                }
                rev = 11 - (add % 11);
                if (rev == 10 || rev == 11)
                    rev = 0;
                if (rev != Integer.parseInt(String.valueOf(cpf.charAt(10)))) {
                    valido = false;
                }
            }
        }
        if(!valido){
            ColorStateList vermelhoErro = view.getContext().getColorStateList(R.color.VermelhoErro);
            textView.setText(mensagem);
            textView.setVisibility(View.VISIBLE);
            txt.setHintTextColor(vermelhoErro);
            txt.setBackgroundTintList(vermelhoErro);
        }
        return valido;

    }
    public static boolean verificaValidadeEmail(View view,EditText txt, TextView textView, String mensagem){

        if(txt.getText().toString().contains("@")){
            return true;
        }else{
            ColorStateList vermelhoErro = view.getContext().getColorStateList(R.color.VermelhoErro);
            textView.setText(mensagem);
            textView.setVisibility(View.VISIBLE);
            txt.setHintTextColor(vermelhoErro);
            txt.setBackgroundTintList(vermelhoErro);
            return false;
        }

    }

    public static boolean verificaIgualdade(View view,EditText txt, EditText txt2, TextView textView, String mensagem){

        if(txt.getText().toString().equals(txt2.getText().toString())){
            return true;
        }else{
            ColorStateList vermelhoErro = view.getContext().getColorStateList(R.color.VermelhoErro);
            textView.setText(mensagem);
            textView.setVisibility(View.VISIBLE);
            txt.setHintTextColor(vermelhoErro);
            txt.setBackgroundTintList(vermelhoErro);
            return false;
        }

    }


    public static void verificaChaveUnicaCPF(EditText txt, TextView textView, String mensagem, boolean focar, FragmentCadastroDados fragmentCadastroDados){
        View view = fragmentCadastroDados.getView();
        Query query = Pessoa.obterReferencia().orderByChild("cpf").equalTo(txt.getText().toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean pessoaAtiva = false;
                for(DataSnapshot child : snapshot.getChildren()){
                    if (!pessoaAtiva) {
                        Pessoa pessoa = child.getValue(Pessoa.class);
                        if (pessoa!=null) {
                            pessoaAtiva = pessoa.getStatus() == Pessoa.ATIVO;
                        }
                    }
                }


                if (pessoaAtiva) {
                    ColorStateList vermelhoErro = view.getContext().getColorStateList(R.color.VermelhoErro);
                    textView.setText(mensagem);
                    textView.setVisibility(View.VISIBLE);
                    txt.setHintTextColor(vermelhoErro);
                    txt.setBackgroundTintList(vermelhoErro);
                    if (focar) {
                        ((ActivityCadastro) view.getContext()).rolarScrollFragment(View.FOCUS_UP);
                        txt.requestFocus();
                    }
                }else{
                    fragmentCadastroDados.VerificaCPF(view);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na verificação de chave unica de CPF: " + error);
            }
        });
    }

    public static void verificaChaveUnicaEmail(EditText txt, TextView textView, String mensagem, boolean focar, FragmentCadastroDados fragmentCadastroDados,@Nullable String keyExcecao){
        View view = fragmentCadastroDados.getView();
        Query queryUsuario = Usuario.obterReferencia().orderByChild("email").equalTo(txt.getText().toString());
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotUsuario) {

                if (snapshotUsuario.getChildrenCount() == 0){
                    fragmentCadastroDados.VerificaEmail(view);
                }else {

                    ArrayList<String> arrayListKeyPessoa = new ArrayList<>();
                    for (DataSnapshot child : snapshotUsuario.getChildren()) {

                        Usuario usuario = child.getValue(Usuario.class);

                        if (usuario != null) {
                            arrayListKeyPessoa.add(usuario.getKeyPessoa());
                        }
                    }

                    Runnable runnableSucesso = new Runnable() {
                        @Override
                        public void run() {
                            fragmentCadastroDados.VerificaEmail(view);
                        }
                    };

                    Runnable runnableErro = new Runnable() {
                        @Override
                        public void run() {
                            ColorStateList vermelhoErro = view.getContext().getColorStateList(R.color.VermelhoErro);
                            textView.setText(mensagem);
                            textView.setVisibility(View.VISIBLE);
                            txt.setHintTextColor(vermelhoErro);
                            txt.setBackgroundTintList(vermelhoErro);
                            if (focar) {
                                ((ActivityCadastro) view.getContext()).rolarScrollFragment(View.FOCUS_UP);
                                txt.requestFocus();
                            }
                        }
                    };

                    VerificacaoUsuarioAtivo vua = new VerificacaoUsuarioAtivo(arrayListKeyPessoa,keyExcecao,runnableSucesso,runnableErro);
                    vua.verificarPessoaAtiva();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na verificação de chave unica de email: " + error);
            }
        });
    }

    public static void campoCorreto(View view, EditText txt, TextView textView,ColorStateList hintColor){
        ColorStateList verdeCorreto = view.getContext().getColorStateList(R.color.VerdeCorreto);
        textView.setVisibility(View.GONE);
        txt.setHintTextColor(hintColor);
        txt.setBackgroundTintList(verdeCorreto);
    }

    public static void continuarCadastroDados(View view, int tabNum, String cpf, String email,String keyPessoaEditar){
        ActivityCadastro activityCadastro = (ActivityCadastro) view.getContext();
        Query queryCPF = Pessoa.obterReferencia().orderByChild("cpf").equalTo(cpf);
        queryCPF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean pessoaAtiva = false;
                for(DataSnapshot child : snapshot.getChildren()){
                    if (!pessoaAtiva) {
                        Pessoa pessoaChild = child.getValue(Pessoa.class);
                        if (pessoaChild!=null) {
                            pessoaAtiva = pessoaChild.getStatus() == Pessoa.ATIVO;
                        }
                    }
                }

                if (snapshot.getChildrenCount() == 0 || !pessoaAtiva) {

                    Query queryEmail = Usuario.obterReferencia().orderByChild("email").equalTo(email);
                    queryEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotUsuario) {

                            if (snapshotUsuario.getChildrenCount() == 0){
                                activityCadastro.trocarFragment(tabNum);
                                activityCadastro.trocarTab(tabNum);
                                ActivityCadastro.permissaoTrocarFragment = true;
                            }else{
                                ArrayList<String> arrayListKeyPessoa = new ArrayList<>();
                                for(DataSnapshot child : snapshotUsuario.getChildren()){
                                    Usuario usuario = child.getValue(Usuario.class);
                                    if (usuario!=null) {
                                        arrayListKeyPessoa.add(usuario.getKeyPessoa());
                                    }
                                }

                                Runnable runnableSucesso = new Runnable() {
                                    @Override
                                    public void run() {
                                        activityCadastro.trocarFragment(tabNum);
                                        activityCadastro.trocarTab(tabNum);
                                        ActivityCadastro.permissaoTrocarFragment = true;
                                    }
                                };

                                Runnable runnableErro = new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityCadastro.permissaoTrocarFragment = false;
                                        activityCadastro.trocarTab(0);
                                        Toast.makeText(view.getContext(), "O email inserido já pertence a outra conta", Toast.LENGTH_SHORT).show();
                                    }
                                };

                                VerificacaoUsuarioAtivo vua = new VerificacaoUsuarioAtivo(arrayListKeyPessoa,keyPessoaEditar,runnableSucesso,runnableErro);
                                vua.verificarPessoaAtiva();


                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            ActivityCadastro.permissaoTrocarFragment = false;
                            activityCadastro.trocarTab(0);
                            Log.i("ERRO_FIREBASE", "Erro na tentativa de verificação de email para cadastro: " + error);
                        }
                    });

                }else{
                    ActivityCadastro.permissaoTrocarFragment = false;
                    activityCadastro.trocarTab(0);
                    Toast.makeText(view.getContext(), "O cpf inserido já pertence a outra conta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                ActivityCadastro.permissaoTrocarFragment = false;
                activityCadastro.trocarTab(0);
                Log.i("ERRO_FIREBASE", "Erro na tentativa de verificação de cpf para cadastro: " + error);
            }
        });

    }
}

package com.tcc.tcc.classe.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tcc.tcc.ActivityAlterarSenha;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Usuario;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.util.ArrayList;
import java.util.Objects;

public class VerificacaoUsuarioAtivo {
    private ArrayList<String> arrayListKeyPessoa,arrayListKeyUsuario;

    private String keyExcecao;

    private Runnable runnableTodosDesativados, runnableAtivo;

    int i;

    public VerificacaoUsuarioAtivo(ArrayList<String> arrayListKeyPessoa, String keyExcecao, Runnable runnableTodosDesativados, Runnable runnableAtivo) {
        this.arrayListKeyPessoa = arrayListKeyPessoa;
        this.keyExcecao = keyExcecao;
        this.runnableTodosDesativados = runnableTodosDesativados;
        this.runnableAtivo = runnableAtivo;
        i=0;
    }

    public VerificacaoUsuarioAtivo(ArrayList<String> arrayListKeyPessoa,ArrayList<String> arrayListKeyUsuario, String keyExcecao, Runnable runnableTodosDesativados, Runnable runnableAtivo) {
        this.arrayListKeyPessoa = arrayListKeyPessoa;
        this.arrayListKeyUsuario = arrayListKeyUsuario;
        this.keyExcecao = keyExcecao;
        this.runnableTodosDesativados = runnableTodosDesativados;
        this.runnableAtivo = runnableAtivo;
        i=0;
    }

    public void verificarPessoaAtiva(){
        String keyPessoa = arrayListKeyPessoa.get(i);
        i++;
        if (keyPessoa!=null) {
            Log.i("TESTE", "verificarPessoaAtiva: " + i + keyPessoa + arrayListKeyPessoa.size());

            Query query = Pessoa.obterReferencia().child(keyPessoa);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Pessoa pessoa = snapshot.getValue(Pessoa.class);
                    Log.i("TESTE", keyPessoa + "onDataChange: " + keyExcecao);
                    if (pessoa != null && pessoa.getStatus() == Pessoa.ATIVO && !Objects.equals(keyPessoa, keyExcecao)) {
                        Log.i("TESTE", "ERRO");
                        runnableAtivo.run();
                    } else if (i == arrayListKeyPessoa.size()) {
                        Log.i("TESTE", "Sucesso");
                        runnableTodosDesativados.run();
                    } else {
                        Log.i("TESTE", "RECURSIVA: " + i);
                        verificarPessoaAtiva();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void verificarPessoaAtivaLogin(Activity activity){
        String keyPessoa = arrayListKeyPessoa.get(i);
        i++;
        if (keyPessoa!=null) {
            Log.i("TESTE", "verificarPessoaAtiva: " + i + keyPessoa + arrayListKeyPessoa.size());

            Query query = Pessoa.obterReferencia().child(keyPessoa);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Pessoa pessoa = snapshot.getValue(Pessoa.class);
                    if (pessoa != null && pessoa.getStatus() == Pessoa.ATIVO) {
                        Log.i("TESTE", "Logou"+i);
                        Usuario.salvarUsuarioAtivo(activity, arrayListKeyUsuario.get(i-1));
                        Intent intent = new Intent(activity, ActivityPrincipal.class);
                        activity.startActivity(intent);
                        activity.finishAffinity();
                    } else if (i == arrayListKeyPessoa.size()) {
                        Log.i("TESTE", "ERRO");
                        runnableTodosDesativados.run();
                    } else {
                        Log.i("TESTE", "RECURSIVA: " + i);
                        verificarPessoaAtivaLogin(activity);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(activity, "Erro na busca por usuário ativo para logar", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void verificarPessoaAtivaActivityAlterarSenha(ActivityAlterarSenha activityAlterarSenha){
        String keyPessoa = arrayListKeyPessoa.get(i);
        i++;
        if (keyPessoa!=null) {
            Log.i("TESTE", "verificarPessoaAtiva: " + i + keyPessoa + arrayListKeyPessoa.size());

            Query query = Pessoa.obterReferencia().child(keyPessoa);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Pessoa pessoa = snapshot.getValue(Pessoa.class);
                    if (pessoa != null && pessoa.getStatus() == Pessoa.ATIVO) {
                        activityAlterarSenha.alterarVisibilidadeEtapaEmail(View.GONE);
                        activityAlterarSenha.alterarVisibilidadeEtapaCodigo(View.VISIBLE);
                        activityAlterarSenha.setKeyUsuario(arrayListKeyUsuario.get(i-1));
                        activityAlterarSenha.incrementarEtapa();
                    } else if (i == arrayListKeyPessoa.size()) {
                        runnableTodosDesativados.run();
                    } else {
                        Log.i("TESTE", "RECURSIVA: " + i);
                        verificarPessoaAtivaActivityAlterarSenha(activityAlterarSenha);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(activityAlterarSenha, "Erro na busca por usuário ativo para alterar senha", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}

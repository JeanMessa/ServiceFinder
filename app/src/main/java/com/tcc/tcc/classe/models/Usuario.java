package com.tcc.tcc.classe.models;

import static android.content.Context.MODE_PRIVATE;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;
import com.tcc.tcc.ActivityAlterarSenha;
import com.tcc.tcc.ActivityLogin;
import com.tcc.tcc.R;
import com.tcc.tcc.cadastro.ActivityCadastro;
import com.tcc.tcc.classe.utils.Imagem;
import com.tcc.tcc.classe.utils.Senha;
import com.tcc.tcc.classe.utils.VerificacaoUsuarioAtivo;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Usuario implements Serializable {
    private String email;
    private String senha;
    private String keyPessoa;


    protected static final DatabaseReference referencia = FirebaseDatabase.getInstance().getReference().child("usuario");

    public void login(Activity activity) {
        Query queryUsuario = referencia.orderByChild("email").equalTo(email);
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotUsuario) {
                if (snapshotUsuario.getChildrenCount() > 0) {

                    ArrayList<String> arrayListKeyUsuario = new ArrayList<>();
                    ArrayList<String> arrayListKeyPessoa = new ArrayList<>();


                    for (DataSnapshot item : snapshotUsuario.getChildren()) {

                        Usuario usuario = item.getValue(Usuario.class);
                        if (usuario!=null) {
                            if (Senha.compararSenha(senha,usuario.senha)) {
                                arrayListKeyUsuario.add(item.getKey());
                                arrayListKeyPessoa.add(usuario.keyPessoa);
                            }
                        }

                    }

                    if (arrayListKeyPessoa.size()>0){


                        Runnable runnableErro = new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, "Usuário desativado, impossivel entrar", Toast.LENGTH_SHORT).show();
                            }
                        };

                        VerificacaoUsuarioAtivo vua = new VerificacaoUsuarioAtivo(arrayListKeyPessoa,arrayListKeyUsuario,null,runnableErro,null);
                        vua.verificarPessoaAtivaLogin(activity);

                    }else{
                        Toast.makeText(activity, "Email ou senha incorreto", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(activity, "Email ou senha incorreto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de login: " + error);
            }
        });
    }

    public static void loginAutomatico(String keyUsuario,Activity activity){
        referencia.child(keyUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                if (usuario!=null){
                    Pessoa.obterReferencia().child(usuario.keyPessoa).child("tokenFCM").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String tokenFCMUsuario = snapshot.getValue(String.class);
                            if (tokenFCMUsuario!=null){
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.w(TAG, "Falha ao Registrar Token", task.getException());
                                                    return;
                                                }
                                                String tokenFCMDispositivo = task.getResult();
                                                if (Objects.equals(tokenFCMDispositivo,tokenFCMUsuario)){
                                                    Intent intent = new Intent(activity, ActivityPrincipal.class);
                                                    activity.startActivity(intent);
                                                    activity.finish();
                                                }else {
                                                    Toast.makeText(activity, "Outro dipositivo acessou sua conta. Entre novamente.", Toast.LENGTH_SHORT).show();
                                                    SharedPreferences.Editor editor = activity.getSharedPreferences("usuarioAtivo", MODE_PRIVATE).edit();
                                                    editor.clear().apply();
                                                    activity.recreate();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar tokenFCM para login automático: " + error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar usuario para login automático: " + error);
            }
        });

    }

    public static void logout(Activity activity){
        Usuario.limparTokenFCM(activity);
        SharedPreferences.Editor editor = activity.getSharedPreferences("usuarioAtivo", MODE_PRIVATE).edit();
        editor.clear().apply();
        Intent intent = new Intent(activity, ActivityLogin.class);
        activity.startActivity(intent);
        activity.finishAffinity();
    }

    public static void salvarUsuarioAtivo(Activity activity,String key){
        SharedPreferences.Editor editor = activity.getSharedPreferences("usuarioAtivo", MODE_PRIVATE).edit();
        editor.putString("key",key);
        editor.apply();
    }



    public static void limparTokenFCM(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences("usuarioAtivo", MODE_PRIVATE);
        String keyUsuario = preferences.getString("key", null);
        if (keyUsuario != null && !keyUsuario.isEmpty()) {
            Query query = referencia.child(keyUsuario).child("keyPessoa");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue(String.class) != null) {
                        String keyPessoa = snapshot.getValue(String.class);
                        Pessoa.obterReferencia().child(keyPessoa).child("tokenFCM").setValue("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getKeyPessoa() {
        return keyPessoa;
    }

    public void setKeyPessoa(String keyPessoa) {
        this.keyPessoa = keyPessoa;
    }

    public void setPessoaFromDB(String keyUsuario, ActivityPrincipal activity, ImageView imageView, TextView textView) {
        Query queryUsuario = referencia.child(keyUsuario);
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    keyPessoa = snapshot.child("keyPessoa").getValue().toString();
                    activity.runRunnablesUsuario();
                    Query queryPessoa = Pessoa.obterReferencia().child(keyPessoa);
                    queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                           if(!(boolean)snapshot.child("fotoPadrao").getValue()){
                               Imagem.download(imageView,keyPessoa+".jpg",Pessoa.obterReferenciaStorage());;
                           }
                           textView.setText(snapshot.child("nome").getValue().toString().concat(" ").concat(snapshot.child("sobrenome").getValue().toString()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else{
                    Toast.makeText(activity, "Ocorreu um problema com a sessão ativa, conecte novamente", Toast.LENGTH_SHORT).show();
                    logout(activity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar pessoa: " + error);
            }
        });
    }

    public static void preencherDadosMeuPerfil(String keyUsuario, ImageView imgFotoPerfil, TextView textViewNome, TextView textViewCPF, TextView textViewEmail, TextView textViewTipo, LinearLayout layoutPrestador,Activity activity){
        Query queryUsuario = referencia.child(keyUsuario);
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    if (usuario!=null){
                        textViewEmail.setText(usuario.email);
                        Query queryPessoa = Pessoa.obterReferencia().child(usuario.keyPessoa);
                        queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getChildrenCount()>0){
                                    Pessoa pessoa = snapshot.getValue(Pessoa.class);
                                    if (pessoa!=null){
                                        if(!pessoa.fotoPadrao){
                                            Imagem.download(imgFotoPerfil,usuario.keyPessoa+".jpg",Pessoa.obterReferenciaStorage());
                                        }
                                        textViewNome.setText(String.format("%s %s", pessoa.nome, pessoa.sobrenome));
                                        textViewCPF.setText(pessoa.cpf);
                                        if (pessoa.prestador){
                                            Prestador prestador = snapshot.getValue(Prestador.class);
                                            textViewTipo.setText(R.string.prestador);
                                            if (prestador!=null){
                                                prestador.preencherDadosPrestadorPerfil(layoutPrestador);
                                            }
                                        }else {
                                            textViewTipo.setText(R.string.cliente);
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(activity, "Ocorreu um problema com a sessão ativa, conecte novamente", Toast.LENGTH_SHORT).show();
                                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar pessoa: " + error);
                                logout(activity);
                            }
                        });
                    }

                }else{
                    Toast.makeText(activity, "Ocorreu um problema com a sessão ativa, conecte novamente", Toast.LENGTH_SHORT).show();
                    logout(activity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar pessoa: " + error);
            }
        });

    }
    public static void chamarActivityCadastroEditar(Context context, String keyUsuario){
        Query queryUsuario = referencia.child(keyUsuario);
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);

                if (usuario!=null){
                    Bundle bundle = new Bundle();
                    bundle.putString("keyUsuario",snapshot.getKey());
                    bundle.putSerializable("usuario",usuario);

                    Query queryPessoa = Pessoa.referencia.child(usuario.getKeyPessoa());

                    queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Pessoa pessoa = snapshot.getValue(Pessoa.class);
                            if (pessoa!=null){
                                bundle.putString("keyPessoa",snapshot.getKey());

                                if (!pessoa.isPrestador()){
                                    bundle.putSerializable("pessoa",pessoa);
                                }else{
                                    Prestador prestador = snapshot.getValue(Prestador.class);
                                    if (prestador!=null){
                                        bundle.putSerializable("prestador",prestador);
                                    }
                                }

                                Intent intent = new Intent(context, ActivityCadastro.class);

                                intent.putExtra("bundleEditar",bundle);
                                context.startActivity(intent);



                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar a pessoa pela chave: " + error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar o usuário pela chave: " + error);
            }
        });
    }

    public static void validarEditar(String keyUsuario, String keyPessoa, Usuario usuario, Pessoa pessoa, Prestador prestador, ByteArrayOutputStream bytesFotoPerfil, String senha,Boolean excluirFotoBD, Activity activity){
        Query query = referencia.child(keyUsuario).child("senha");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (Senha.compararSenha(senha,snapshot.getValue(String.class))){
                    Query queryVerificarEmail = referencia.orderByChild("email").equalTo(usuario.email);
                    queryVerificarEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotEmail) {
                            
                            if (snapshotEmail.getChildrenCount()==0) {
                                editar(keyUsuario,keyPessoa,usuario,pessoa,prestador,bytesFotoPerfil,excluirFotoBD,activity);
                            }else{
                                ArrayList<String> arrayListKeyPessoa = new ArrayList<>();
                                for (DataSnapshot child : snapshotEmail.getChildren()) {

                                    Usuario usuario = child.getValue(Usuario.class);

                                    if (usuario != null) {
                                        arrayListKeyPessoa.add(usuario.getKeyPessoa());
                                    }
                                }

                                Runnable runnableSucesso = new Runnable() {
                                    @Override
                                    public void run() {
                                        editar(keyUsuario,keyPessoa,usuario,pessoa,prestador,bytesFotoPerfil,excluirFotoBD,activity);
                                    }
                                };

                                Runnable runnableErro = new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity,"Outro usuário já está usando o novo email que está tentando utilizar.",Toast.LENGTH_SHORT).show();
                                        activity.finish();
                                    }
                                };

                                VerificacaoUsuarioAtivo vua = new VerificacaoUsuarioAtivo(arrayListKeyPessoa,keyPessoa,runnableSucesso,runnableErro);
                                vua.verificarPessoaAtiva();
                                
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ERRO_FIREBASE", "Erro na tentativa de veficar email repetido: " + error);
                        }
                    });

                }else{
                    Toast.makeText(activity, "Senha Incorreta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de validar senha: " + error);
            }
        });
    }

    public static void editar(String keyUsuario, String keyPessoa, Usuario usuario, Pessoa pessoa, Prestador prestador, ByteArrayOutputStream bytesFotoPerfil,boolean excluirFotoBD, Activity activity){
        CardView cvEscurecer = activity.findViewById(R.id.cvEscurecer);
        cvEscurecer.setVisibility(View.VISIBLE);
        LinearLayout layoutCarregando = activity.findViewById(R.id.layoutCarregando);
        layoutCarregando.setVisibility(View.VISIBLE);
        TextView textViewEscurecer = layoutCarregando.findViewById(R.id.textViewEscurecer);
        textViewEscurecer.setText(R.string.aguarde_edicao);


        usuario.atualizarEmail(keyUsuario);
        pessoa.atualizar(keyPessoa);
        if (pessoa.isPrestador()) {
            prestador.atualizarCidadeServico(keyPessoa);
        }
        Intent intent = new Intent(activity, ActivityPrincipal.class);
        if (bytesFotoPerfil!=null) {
            Imagem.upload(keyPessoa+".jpg",bytesFotoPerfil,Pessoa.obterReferenciaStorage()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    activity.startActivity(intent);
                    activity.finishAffinity();
                }
            });
        } else {
            if (excluirFotoBD){
                Pessoa.obterReferenciaStorage().child(keyPessoa+".jpg").delete();
            }
            activity.startActivity(intent);
            activity.finishAffinity();
        }
    }

    public static void passarUsuarioIntent(String keyUsuario,Intent intent,Bundle bundle,String nomeBundle,Context context){
        Query query = referencia.child(keyUsuario);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                bundle.putSerializable("usuario",usuario);
                intent.putExtra(nomeBundle,bundle);
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar usuário para adicionar na intent: " + error);
            }
        });

    }

    public void atualizarEmail(String keyUsuario){
        referencia.child(keyUsuario).child("email").setValue(email.toLowerCase());
    }

    public static void atualizarSenha(String keyUsuario,String senha){
        referencia.child(keyUsuario).child("senha").setValue(Senha.criptografarSenha(senha));
    }

    public static void encontrarUsuarioAtivoAlterarSenha(String email, ActivityAlterarSenha activityAlterarSenha){
        Query queryUsuario = referencia.orderByChild("email").equalTo(email);
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotUsuario) {
                if (snapshotUsuario.getChildrenCount() > 0) {

                    ArrayList<String> arrayListKeyUsuario = new ArrayList<>();
                    ArrayList<String> arrayListKeyPessoa = new ArrayList<>();


                    for (DataSnapshot item : snapshotUsuario.getChildren()) {

                        Usuario usuario = item.getValue(Usuario.class);
                        if (usuario!=null) {
                            arrayListKeyUsuario.add(item.getKey());
                            arrayListKeyPessoa.add(usuario.keyPessoa);
                        }

                    }

                    Runnable runnableErro = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activityAlterarSenha, "Usuário desativado, impossivel alterar senha", Toast.LENGTH_SHORT).show();
                        }
                    };

                    VerificacaoUsuarioAtivo vua = new VerificacaoUsuarioAtivo(arrayListKeyPessoa,arrayListKeyUsuario,null,runnableErro,null);
                    vua.verificarPessoaAtivaActivityAlterarSenha(activityAlterarSenha);

                } else {
                    Toast.makeText(activityAlterarSenha, "Não há nenhum usuário cadastrado com esse email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de login: " + error);
            }
        });
    }



    public static DatabaseReference obterReferencia(){
        return referencia;
    }
}

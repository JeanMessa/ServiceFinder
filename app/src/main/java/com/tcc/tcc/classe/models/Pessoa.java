package com.tcc.tcc.classe.models;


import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tcc.tcc.R;
import com.tcc.tcc.adapter.AdapterBloqueados;
import com.tcc.tcc.adapter.AdapterCadastroPrestador;
import com.tcc.tcc.adapter.AdapterChat;
import com.tcc.tcc.adapter.AdapterHomePrestador;
import com.tcc.tcc.cadastro.ActivityCadastro;
import com.tcc.tcc.classe.utils.FirebaseMessage;
import com.tcc.tcc.classe.utils.Imagem;
import com.tcc.tcc.classe.utils.Senha;
import com.tcc.tcc.classe.utils.VerificacaoUsuarioAtivo;
import com.tcc.tcc.principal.ActivityPrincipal;
import com.tcc.tcc.principal.FragmentPrincipalHome;
import com.tcc.tcc.view.ScrollListView;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class Pessoa implements Serializable {
    protected static final DatabaseReference referencia = FirebaseDatabase.getInstance().getReference().child("pessoa");;
    protected static final StorageReference referenciaStorage = FirebaseStorage.getInstance().getReference().child("foto_perfil");
    protected String nome;
    protected String sobrenome;
    protected String cpf;
    protected boolean fotoPadrao;
    protected boolean prestador;
    protected String tokenFCM;
    protected ArrayList<String> bloqueados;
    protected int status;

    public static final int DESATIVADO = 0, ATIVO = 1;

    public Pessoa() {
        fotoPadrao = true;
        prestador = false;
        tokenFCM = "";
        status = ATIVO;
        bloqueados = null;
    }




    public void Validainsert(View view, ByteArrayOutputStream bytesFotoPerfil, Usuario usuario){
        Pessoa pessoa = this;
        Query queryCPF = referencia.orderByChild("cpf").equalTo(cpf);
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
                    Query queryEmail = Usuario.obterReferencia().orderByChild("email").equalTo(usuario.getEmail());
                    queryEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotUsuario) {


                            if (snapshotUsuario.getChildrenCount() == 0) {
                                insert(view,bytesFotoPerfil,usuario,pessoa);
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
                                        insert(view,bytesFotoPerfil,usuario,pessoa);
                                    }
                                };

                                Runnable runnableErro = new Runnable() {
                                    @Override
                                    public void run() {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((ActivityCadastro)view.getContext()).trocarTab(0);
                                            }
                                        }, 1000);
                                        Toast.makeText(view.getContext(), "O email inserido já pertence a outra conta", Toast.LENGTH_SHORT).show();
                                    }
                                };

                                VerificacaoUsuarioAtivo vua = new VerificacaoUsuarioAtivo(arrayListKeyPessoa,null,runnableSucesso,runnableErro);
                                vua.verificarPessoaAtiva();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ERRO_FIREBASE", "Erro na tentativa de verificação de email para cadastro: " + error);
                        }
                    });
                }else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((ActivityCadastro)view.getContext()).trocarTab(0);
                        }
                    }, 1000);
                    Toast.makeText(view.getContext(), "O CPF inserido já pertence a outra conta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de verificação de CPF para cadastro: " + error);
            }
        });

    }

    public void insert(View view, ByteArrayOutputStream bytesFotoPerfil, Usuario usuario,Pessoa pessoa) {
        String keyPessoa = referencia.push().getKey();
        referencia.child(keyPessoa).setValue(pessoa);

        usuario.setKeyPessoa(keyPessoa);
        usuario.setSenha(Senha.criptografarSenha(usuario.getSenha()));
        String keyUsuario = Usuario.obterReferencia().push().getKey();
        Usuario.obterReferencia().child(keyUsuario).setValue(usuario);
        ActivityCadastro activityCadastro = (ActivityCadastro) view.getContext();
        usuario.salvarUsuarioAtivo(activityCadastro,keyUsuario);



        if (!isFotoPadrao()){
            activityCadastro.trocarEscurecer("Finalizando Cadastro. Por favor aguarde...",View.VISIBLE);

            Imagem.upload(keyPessoa+".jpg",bytesFotoPerfil,referenciaStorage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Intent intent = new Intent(view.getContext(), ActivityPrincipal.class);
                    view.getContext().startActivity(intent);
                    activityCadastro.finishAffinity();
                }
            });
        }else {
            Intent intent = new Intent(view.getContext(), ActivityPrincipal.class);
            view.getContext().startActivity(intent);
            activityCadastro.finishAffinity();
        }
    }

//    public void setFotoPerfil(ImageView imageView, String key){
//        StorageReference arquivo = referenciaStorage.child(key + ".jpg");
//        arquivo.getBytes(1048576).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//                imageView.setImageBitmap(bitmap);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.i("ERRO_FIREBASE_STORAGE", "Falha ao carregar imagem: "+ e);
//            }
//        });
//
//    }



    public static void preencherDadosPessoa(String keyPessoa, ImageView imgFotoPerfil, TextView textViewNome, Activity activity){

        Query queryPessoa = Pessoa.obterReferencia().child(keyPessoa);
        queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    Pessoa pessoa = snapshot.getValue(Pessoa.class);
                    if (pessoa!=null){
                        if (pessoa.status == Pessoa.ATIVO) {
                            if (!pessoa.fotoPadrao) {
                                Imagem.download(imgFotoPerfil, keyPessoa + ".jpg", referenciaStorage);
                            }
                            textViewNome.setText(String.format("%s %s", pessoa.nome, pessoa.sobrenome));
                        }else{
                            ((ActivityPrincipal)activity).trocarTab(1);
                            Toast.makeText(activity, "A pessoa que você estava conversando desabilitou a conta", Toast.LENGTH_SHORT).show();
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity, "Problema ao encontrar o perfil do prestador prestador", Toast.LENGTH_SHORT).show();
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar pessoa: " + error);
                activity.finish();
            }
        });
    }

    public static void verificaBloqueadoChat(String keyPessoaChat,String keyPessoaUsuario,LinearLayout layoutExtras,LinearLayout layoutMensagem, LinearLayout layoutBloqueado){
        referencia.child(keyPessoaChat).child("bloqueados").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> bloqueados;
                if (snapshot.getValue()!=null){
                    bloqueados = (ArrayList<String>)snapshot.getValue();
                }else {
                    bloqueados = new ArrayList<>();
                }
                if (bloqueados.contains(keyPessoaUsuario)){
                    layoutExtras.setVisibility(View.GONE);
                    layoutMensagem.setVisibility(View.GONE);
                    layoutBloqueado.setVisibility(View.VISIBLE);
                }else {
                    layoutExtras.setVisibility(View.VISIBLE);
                    layoutMensagem.setVisibility(View.VISIBLE);
                    layoutBloqueado.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar bloqueados para chat: " + error);
            }
        });
    }

    public static void verificaBloqueadosUsuarioChat(String keyPessoaUsuario,String keyPessoaChat, Activity activity){
        referencia.child(keyPessoaUsuario).child("bloqueados").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> bloqueados;
                if (snapshot.getValue()!=null){
                    bloqueados = (ArrayList<String>)snapshot.getValue();
                }else {
                    bloqueados = new ArrayList<>();
                }
                if (bloqueados.contains(keyPessoaChat)){
                    ((ActivityPrincipal)activity).trocarTab(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar bloqueados para chat: " + error);
            }
        });
    }

    public static void AdicionarNomeEmTextView(String keyPessoa, TextView textView,@Nullable String textoRestante){

        Query queryPessoa = Pessoa.obterReferencia().child(keyPessoa);
        queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Pessoa pessoa = snapshot.getValue(Pessoa.class);
                if (pessoa!=null) {
                    if (textoRestante==null){
                        textView.setText(String.format(Locale.ROOT, "%s %s", pessoa.nome, pessoa.sobrenome));
                    }else {
                        textView.setText(String.format(Locale.ROOT, "%s %s %s", pessoa.nome, pessoa.sobrenome, textoRestante));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar o nome da pessoa: " + error);
            }
        });
    }

    public static void trocarVisibilidadePrestador(View view,String keyPessoa){
        Query queryPessoa = Pessoa.obterReferencia().child(keyPessoa).child("prestador");
        queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isPrestador = Boolean.TRUE.equals(snapshot.getValue(boolean.class));
                if (isPrestador){
                    view.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar se a pessoa é prestador: " + error);
            }
        });
    }

    public static void trocarVisibilidadePrestador(TabLayout view, String keyPessoa, Thread criarAdapterServico){
        Query queryPessoa = Pessoa.obterReferencia().child(keyPessoa).child("prestador");

        queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isPrestador = Boolean.TRUE.equals(snapshot.getValue(boolean.class));
                if (isPrestador){
                    view.setVisibility(View.VISIBLE);

                }
                criarAdapterServico.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar se a pessoa é prestador: " + error);
            }
        });
    }

    public static void enviarFirebaseMessage(String keyConversa,int posicaoRemetente, String body, Context context){
        String keyPessoaRemetente,keyPessoaDestinatario;
        String[] KeyConversaSeparada = keyConversa.split(" ");
        if (posicaoRemetente==0){
            keyPessoaRemetente = KeyConversaSeparada[0];
            keyPessoaDestinatario = KeyConversaSeparada[1];
        }else{
            keyPessoaDestinatario = KeyConversaSeparada[0];
            keyPessoaRemetente = KeyConversaSeparada[1];
        }



        Query queryDestinatario = referencia.child(keyPessoaDestinatario).child("tokenFCM");
        queryDestinatario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(String.class)!=null){
                    String tokenFCMDestinatario = snapshot.getValue(String.class);
                    Query queryRemetente = referencia.child(keyPessoaRemetente);
                    queryRemetente.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue(Pessoa.class)!=null){
                                Pessoa remetente = snapshot.getValue(Pessoa.class);
                                if (remetente!=null && !remetente.getTokenFCM().equals("")){
                                    FirebaseMessage.enviar(remetente.nome+" "+remetente.sobrenome,body,tokenFCMDestinatario,posicaoRemetente,keyConversa, remetente.isFotoPadrao(), null,context);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar remetente para enviar FirebaseMessage: " + error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar tokenFCM do destinatario para enviar FirebaseMessage: " + error);
            }
        });

    }

    public void atualizar(String keyPessoa){
        referencia.child(keyPessoa).child("nome").setValue(nome);
        referencia.child(keyPessoa).child("sobrenome").setValue(sobrenome);
        referencia.child(keyPessoa).child("fotoPadrao").setValue(fotoPadrao);
        referencia.child(keyPessoa).child("prestador").setValue(prestador);
    }

    public static void desativar(String keyPessoa,Context context){
        referencia.child(keyPessoa).child("status").setValue(DESATIVADO);
        Servico.obterReferencia().orderByChild("keyCliente").equalTo(keyPessoa).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item:snapshot.getChildren()){
                    Servico servico = item.getValue(Servico.class);
                    if (servico!=null && (servico.getStatus()==Servico.PENDENTE || servico.getStatus()==Servico.ACEITO)) {
                        Servico.atualizarStatus(item.getKey(), Servico.CANCELADO);
                        String keyConversa = Conversa.gerarKeyConversa(servico.getKeyCliente(),servico.getKeyPrestador());
                        int posicaoRemententeInformativa = Conversa.gerarPosicaoPessoa(keyPessoa,servico.getKeyPrestador());
                        Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.getTipoServico().toLowerCase() + "foi cancelado, pois o prestador desativou a conta.",posicaoRemententeInformativa);
                        mensagemInformativa.enviar(keyConversa,item.getKey(),context);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de cancelar servicos contratados pelo usuario: " + error);
            }
        });

        Servico.obterReferencia().orderByChild("keyPrestador").equalTo(keyPessoa).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item:snapshot.getChildren()){
                    Servico servico = item.getValue(Servico.class);
                    if (servico!=null && (servico.getStatus()==Servico.PENDENTE || servico.getStatus()==Servico.ACEITO)) {
                        Servico.atualizarStatus(item.getKey(), Servico.CANCELADO);
                        String keyConversa = Conversa.gerarKeyConversa(servico.getKeyCliente(),servico.getKeyPrestador());
                        int posicaoRemententeInformativa = Conversa.gerarPosicaoPessoa(keyPessoa,servico.getKeyCliente());
                        Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.getTipoServico().toLowerCase() + " foi cancelado, pois o cliente desativou a conta.",posicaoRemententeInformativa);
                        mensagemInformativa.enviar(keyConversa,item.getKey(),context);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de cancelar os seus servicos que foram contratados por outros usuarios: " + error);
            }
        });
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }


    public boolean isFotoPadrao() {
        return fotoPadrao;
    }

    public void setFotoPadrao(boolean fotoPadrao) {
        this.fotoPadrao = fotoPadrao;
    }

    public boolean isPrestador() {
        return prestador;
    }

    public void setPrestador(boolean prestador) {
        this.prestador = prestador;
    }

    public ArrayList<String> getBloqueados() {
        return bloqueados;
    }

    public void setBloqueados(ArrayList<String> bloqueados) {
        this.bloqueados = bloqueados;
    }

    public static void addBloqueados(String keyPessoaBloqueada, String keyPessoaUsuario) {
        referencia.child(keyPessoaUsuario).child("bloqueados").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> bloqueados = (ArrayList<String>)snapshot.getValue();
                if (bloqueados==null){
                    bloqueados = new ArrayList<String>();
                }
                if (!bloqueados.contains(keyPessoaBloqueada)){
                    bloqueados.add(keyPessoaBloqueada);
                }
                referencia.child(keyPessoaUsuario).child("bloqueados").setValue(bloqueados);
                FragmentPrincipalHome.bloqueadosAtualizar.add(keyPessoaBloqueada);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar bloqueados: " + error);
            }
        });
    }

    public static void removerBloqueados(String keyPessoaDesbloqueada, String keyPessoaUsuario) {
        referencia.child(keyPessoaUsuario).child("bloqueados").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> bloqueados = (ArrayList<String>)snapshot.getValue();
                if (bloqueados==null){
                    bloqueados = new ArrayList<String>();
                }
                bloqueados.remove(keyPessoaDesbloqueada);
                referencia.child(keyPessoaUsuario).child("bloqueados").setValue(bloqueados);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar bloqueados: " + error);
            }
        });
    }

    public static void setListBloqueados(ScrollListView listBloqueados, Button btnBloqueados, String keyPessoa){
        referencia.child(keyPessoa).child("bloqueados").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> bloqueados = (ArrayList<String>)snapshot.getValue();
                if (bloqueados!=null){
                    AdapterBloqueados adapterBloqueados = new AdapterBloqueados(listBloqueados.getContext(), R.layout.item_configuracoes_bloqueados,keyPessoa,btnBloqueados){
                        @Override
                        public void remove(@Nullable Pair<String,String> object) {
                            super.remove(object);
                            listBloqueados.atualizarHeight(10);
                        }
                    };
                    for (String item: bloqueados) {
                        if (item!=null){
                            referencia.child(item).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Pessoa pessoa = snapshot.getValue(Pessoa.class);
                                    if (pessoa!=null && pessoa.status!=DESATIVADO){
                                        adapterBloqueados.add(new Pair<>(item,pessoa.nome + " " + pessoa.sobrenome));
                                        if (btnBloqueados.getVisibility()!=View.VISIBLE){
                                            btnBloqueados.setVisibility(View.VISIBLE);
                                        }
                                        listBloqueados.atualizarHeight(10);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar nome do bloqueado: " + error);
                                }
                            });
                        }
                    }

                    listBloqueados.setAdapter(adapterBloqueados);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar bloqueados: " + error);
            }
        });
    }

    public String getTokenFCM() {
        return tokenFCM;
    }

    public void setTokenFCM(String tokenFCM) {
        this.tokenFCM = tokenFCM;
    }

    public static void salvarTokenFCM(String keyPessoa){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Falha ao Registrar Token", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        Pessoa.obterReferencia().child(keyPessoa).child("tokenFCM").setValue(token);
                    }
                });
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDados(Pessoa pessoa){
        nome = pessoa.nome;
        sobrenome = pessoa.sobrenome;
        cpf = pessoa.cpf;
    }
    public static DatabaseReference obterReferencia(){
        return referencia;
    }

    public static StorageReference obterReferenciaStorage(){
        return referenciaStorage;
    }


}

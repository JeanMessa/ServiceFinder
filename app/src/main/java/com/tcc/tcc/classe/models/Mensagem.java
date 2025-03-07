package com.tcc.tcc.classe.models;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tcc.tcc.adapter.AdapterChat;
import com.tcc.tcc.classe.utils.Horario;
import com.tcc.tcc.classe.utils.Imagem;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;


public class Mensagem{
    public static final int TEXTO = 0,IMAGEM = 1,ARQUIVO = 2,AUDIO = 3,AGENDAMENTO = 4,INFORMATIVO_SERVICO=5;
    private String texto;
    private int posicaoRemetente;
    private long horario;
    private int tipoMensagem;

    private String nomeArquivo;

    private long duracaoAudio;

    private String keyServico;

    private boolean lido;
    private boolean ativo;
    private ArrayList<Boolean> visivel;

    private String DataprimeiraMensagem;
    private static ValueEventListener listenerMensagem;

    private static Query queryMensagem;
    private static final DatabaseReference referencia = FirebaseDatabase.getInstance().getReference().child("conversa");
    private static final StorageReference referenciaStorage = FirebaseStorage.getInstance().getReference().child("conversa");

    public Mensagem() {}

    public Mensagem(String texto, int posicaoRemetente){
        this.texto = texto;
        this.posicaoRemetente = posicaoRemetente;

        horario = Horario.getHorarioAtual();

        ativo = true;
        visivel = new ArrayList<>();
        lido = false;
        visivel.addAll(Arrays.asList(true, true));
    }

    public Mensagem(long duracaoAudio, int posicaoRemetente) {
        this.duracaoAudio = duracaoAudio;
        this.posicaoRemetente = posicaoRemetente;

        horario = Horario.getHorarioAtual();

        ativo = true;
        visivel = new ArrayList<>();
        visivel.addAll(Arrays.asList(true, true));
    }

    public void enviar(String keyConversa,LinearLayout layoutCarregamento){
        tipoMensagem = Mensagem.TEXTO;
        layoutCarregamento.setVisibility(View.VISIBLE);
        referencia.child(keyConversa).push().setValue(this).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                layoutCarregamento.setVisibility(View.GONE);
            }
        });
        Pessoa.enviarFirebaseMessage(keyConversa,posicaoRemetente,texto,layoutCarregamento.getContext());
    }

    public void enviar(String keyConversa,Context context){
        tipoMensagem = Mensagem.TEXTO;

        referencia.child(keyConversa).push().setValue(this);
        Pessoa.enviarFirebaseMessage(keyConversa,posicaoRemetente,texto,context);
    }

    public void enviar(String keyConversa, ByteArrayOutputStream bytesImagem,LinearLayout layoutCarregamento){
        tipoMensagem = Mensagem.IMAGEM;
        String keyMensagem = referencia.child(keyConversa).push().getKey();
        StorageReference referenciaConversa = referenciaStorage.child(keyConversa);
        layoutCarregamento.setVisibility(View.VISIBLE);
        Imagem.upload(keyMensagem+".jpg",bytesImagem,referenciaConversa.child("imagem")).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                referencia.child(keyConversa).child(keyMensagem).setValue(Mensagem.this).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        layoutCarregamento.setVisibility(View.GONE);
                        Pessoa.enviarFirebaseMessage(keyConversa,posicaoRemetente,"🖼️ Imagem " + texto,layoutCarregamento.getContext());
                    }
                });
            }
        });
    }

    public void enviar(String keyConversa, LinearLayout layoutCarregamento, Uri uriArquivo,String nomeArquivo){
        tipoMensagem = Mensagem.ARQUIVO;
        this.nomeArquivo = nomeArquivo;
        String keyMensagem = referencia.child(keyConversa).push().getKey();
        StorageReference referenciaConversa = referenciaStorage.child(keyConversa);
        layoutCarregamento.setVisibility(View.VISIBLE);
        referenciaConversa.child("arquivo").child(keyMensagem).putFile(uriArquivo).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                referencia.child(keyConversa).child(keyMensagem).setValue(Mensagem.this).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        layoutCarregamento.setVisibility(View.GONE);
                        Pessoa.enviarFirebaseMessage(keyConversa,posicaoRemetente,"📄 " + nomeArquivo + " " + texto,layoutCarregamento.getContext());
                    }
                });
            }
        });
    }

    public void enviar(String keyConversa, LinearLayout layoutCarregamento, String keyServico){
        this.tipoMensagem = AGENDAMENTO;
        this.keyServico = keyServico;
        layoutCarregamento.setVisibility(View.VISIBLE);
        referencia.child(keyConversa).push().setValue(this).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                layoutCarregamento.setVisibility(View.GONE);
                Servico.enviarFirebaseMessage(keyConversa,posicaoRemetente,keyServico, layoutCarregamento.getContext());
            }
        });
    }

    public void enviar(String keyConversa, String keyServico, Context context) {
        this.tipoMensagem = INFORMATIVO_SERVICO;
        this.keyServico = keyServico;
        referencia.child(keyConversa).push().setValue(this);
        Pessoa.enviarFirebaseMessage(keyConversa,posicaoRemetente,"ℹ️ " + texto, context);
    }

    public void enviarAudio(String keyConversa,Uri uri,LinearLayout layoutCarregamento){
        tipoMensagem = Mensagem.AUDIO;
        String keyMensagem = referencia.child(keyConversa).push().getKey();
        StorageReference referenciaConversa = referenciaStorage.child(keyConversa);
        layoutCarregamento.setVisibility(View.VISIBLE);
        referenciaConversa.child("audio").child(keyMensagem+".mp3").putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                referencia.child(keyConversa).child(keyMensagem).setValue(Mensagem.this).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        layoutCarregamento.setVisibility(View.GONE);
                        Pessoa.enviarFirebaseMessage(keyConversa,posicaoRemetente,"🎤 " + "Áudio " + new SimpleDateFormat("mm:ss", Locale.ROOT).format(duracaoAudio),layoutCarregamento.getContext());
                    }
                });
            }
        });

    }

    public void atualizarLido(String keyConversa,String keyMensagem,boolean lido){
        referencia.child(keyConversa).child(keyMensagem).child("lido").setValue(lido);
    }

    public void esconder(String keyConversa,String keyMensagem,int posicaoRemetente){
        visivel.set(posicaoRemetente,false);
        referencia.child(keyConversa).child(keyMensagem).setValue(this);
    }
    public void excluir(String keyConversa,String keyMensagem){
        ativo = false;
        lido = true;
        referencia.child(keyConversa).child(keyMensagem).setValue(this);
    }

    public static void preencherAdapter(AdapterChat adapterChat, RecyclerView listChat,String keyConversa,int posicaoPessoa){
        queryMensagem = referencia.child(keyConversa).orderByChild("horario");
        listenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinkedList<String> listKey = new LinkedList<>();
                LinkedList<Mensagem> listMensagem = new LinkedList<>();
                LinkedHashMap<String,Mensagem> mapMensagem = new LinkedHashMap<>();

                if (snapshot.getChildrenCount() > 0) {
                    String dataMensagemAnterior = null;
                    for(DataSnapshot item : snapshot.getChildren()){
                        Mensagem mensagem = item.getValue(Mensagem.class);
                        if (mensagem !=null && mensagem.ativo && mensagem.getVisivelAt(posicaoPessoa)){
                            String dataMensagemAtual = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(mensagem.horario);
                            if (dataMensagemAnterior==null || !dataMensagemAnterior.equals(dataMensagemAtual)){
                                dataMensagemAnterior = dataMensagemAtual;
                                mensagem.DataprimeiraMensagem = dataMensagemAtual;
                            }
                            listKey.addFirst(item.getKey());
                            listMensagem.addFirst(mensagem);
                        }
                    }
                }
                for (int i=0;i<listKey.size();i++){
                    mapMensagem.put(listKey.get(i),listMensagem.get(i));
                }
                if (adapterChat.getItemCount()==0){
                    adapterChat.setMapMensagem(mapMensagem);
                    listChat.setAdapter(adapterChat);
                }else {
                    adapterChat.setMapMensagem(mapMensagem);
                    adapterChat.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro ao preencher adapter de mensagem: " + error);
            }
        };
        queryMensagem.addValueEventListener(listenerMensagem);
    }




    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getPosicaoRemetente() {
        return posicaoRemetente;
    }

    public void setPosicaoRemetente(int posicaoRemetente) {
        this.posicaoRemetente = posicaoRemetente;
    }

    public long getHorario() {
        return horario;
    }

    public void setHorario(long horario) {
        this.horario = horario;
    }

    public int getTipoMensagem() {
        return tipoMensagem;
    }

    public void setTipoMensagem(int tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public long getDuracaoAudio() {
        return duracaoAudio;
    }

    public void setDuracaoAudio(long duracaoAudio) {
        this.duracaoAudio = duracaoAudio;
    }

    public String getKeyServico() {
        return keyServico;
    }

    public void setKeyServico(String keyServico) {
        this.keyServico = keyServico;
    }

    public boolean isLido() {
        return lido;
    }

    public void setLido(boolean lido) {
        this.lido = lido;
    }
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public ArrayList<Boolean> getVisivel() {
        return visivel;
    }

    public void setVisivel(ArrayList<Boolean> visivel) {
        this.visivel = visivel;
    }

    public boolean getVisivelAt(int posicao) {
        return visivel.get(posicao);
    }

    public void setVisivelAt(boolean visivel,int posicao) {
        this.visivel.set(posicao,visivel);
    }

    public String obterDataprimeiraMensagem() {
        return DataprimeiraMensagem;
    }

    public void definirDataprimeiraMensagem(String dataPrimeiraMensagem) {
        this.DataprimeiraMensagem = dataPrimeiraMensagem;
    }

    public static void destruirListener(){
        if (queryMensagem!=null){
            queryMensagem.removeEventListener(listenerMensagem);
        }
    }

    public static DatabaseReference obterReferencia(){
        return referencia;
    }
    public static StorageReference obterReferenciaStorage(){
        return referenciaStorage;
    }
}

package com.tcc.tcc.classe.models;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.tcc.tcc.R;
import com.tcc.tcc.ReceiverLembreteServico;
import com.tcc.tcc.RecieverResponderAgendamento;
import com.tcc.tcc.RecieverResponderMensagem;
import com.tcc.tcc.adapter.AdapterConversas;
import com.tcc.tcc.classe.utils.Imagem;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class Conversa implements Comparable<Conversa>{
    private int numNaoLidas;
    private int posicaoPessoaChat;
    private String keyPessoaChat;
    private Pessoa pessoaChat;
    private long horarioUltimaMensagem;

    private static ValueEventListener listenerConversa,listenerUsuario;

    private static final Query queryConversa = Mensagem.obterReferencia();
    private static Query queryUsuario;

    public Conversa(){
        horarioUltimaMensagem = 0;
    }
    public int getNumNaoLidas() {
        return numNaoLidas;
    }

    public void setNumNaoLidas(int numNaoLidas) {
        this.numNaoLidas = numNaoLidas;
    }

    public int getPosicaoPessoaChat() {
        return posicaoPessoaChat;
    }

    public void setPosicaoPessoaChat(int posicaoPessoaChat) {
        this.posicaoPessoaChat = posicaoPessoaChat;
    }

    public String getKeyPessoaChat() {
        return keyPessoaChat;
    }

    public void setKeyPessoaChat(String keyPessoaChat) {
        this.keyPessoaChat = keyPessoaChat;
    }

    public Pessoa getPessoaChat() {
        return pessoaChat;
    }

    public void setPessoaChat(Pessoa pessoaChat) {
        this.pessoaChat = pessoaChat;
    }

    public long getHorarioUltimaMensagem() {
        return horarioUltimaMensagem;
    }

    public void setHorarioUltimaMensagem(long horarioUltimaMensagem) {
        this.horarioUltimaMensagem = horarioUltimaMensagem;
    }

    public static void preencherAdapter(AdapterConversas adapterConversas, RecyclerView listConversas, TextView textViewAviso){
        listenerConversa = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                queryUsuario = Pessoa.referencia.child(adapterConversas.getKeyPessoaUsuario()).child("bloqueados");
                listenerUsuario = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotUsuario) {
                        ArrayList<String> bloqueados;
                        if (snapshotUsuario.getValue()!=null){
                            bloqueados = (ArrayList<String>) snapshotUsuario.getValue();
                        }else {
                            bloqueados = new ArrayList<String>();
                        }
                        ArrayList<Conversa> arrayListConversa = new ArrayList<>();
                        if (snapshot.getChildrenCount() > 0) {
                            for(DataSnapshot item : snapshot.getChildren()){
                                String keyConversa = item.getKey();
                                Conversa conversa = new Conversa();
                                if (keyConversa!=null && keyConversa.contains(adapterConversas.getKeyPessoaUsuario())){
                                    String[] keyPessoas = keyConversa.split(" ");
                                    if (Objects.equals(adapterConversas.getKeyPessoaUsuario(), keyPessoas[0])){
                                        conversa.setPosicaoPessoaChat(1);
                                    }else{
                                        conversa.setPosicaoPessoaChat(0);
                                    }
                                    String keyPessoaChat = keyPessoas[conversa.getPosicaoPessoaChat()];

                                    if (!bloqueados.contains(keyPessoaChat)){
                                        conversa.setKeyPessoaChat(keyPessoaChat);
                                        int contadorNaoLidas = 0;

                                        for (DataSnapshot itemMensagem : item.getChildren()){
                                            Mensagem mensagem = itemMensagem.getValue(Mensagem.class);
                                            if (mensagem.getHorario()>conversa.getHorarioUltimaMensagem()){
                                                conversa.setHorarioUltimaMensagem(mensagem.getHorario());
                                            }
                                            if(mensagem.getPosicaoRemetente()==conversa.getPosicaoPessoaChat() && !mensagem.isLido()){
                                                contadorNaoLidas++;
                                            }
                                        }
                                        conversa.setNumNaoLidas(contadorNaoLidas);
                                        arrayListConversa.add(conversa);
                                    }
                                }
                            }
                            if (arrayListConversa.size()>0) {
                                listConversas.setVisibility(View.VISIBLE);
                                for (int i = 0; i < arrayListConversa.size(); i++) {
                                    Query queryPessoa = Pessoa.obterReferencia().child(arrayListConversa.get(i).keyPessoaChat);
                                    final int copiai = i;
                                    queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Pessoa pessoa = snapshot.getValue(Pessoa.class);
                                            arrayListConversa.get(copiai).setPessoaChat(pessoa);

                                            if (copiai == arrayListConversa.size() - 1) {
                                                Collections.sort(arrayListConversa);
                                                arrayListConversa.removeIf(new Predicate<Conversa>() {
                                                    @Override
                                                    public boolean test(Conversa conversa) {
                                                        if (conversa.getPessoaChat()!=null) {
                                                            return conversa.getPessoaChat().status == Pessoa.DESATIVADO;
                                                        }else{
                                                            return false;
                                                        }
                                                    }
                                                });
                                                if (arrayListConversa.size()==0){
                                                    listConversas.setVisibility(View.GONE);
                                                    textViewAviso.setVisibility(View.VISIBLE);
                                                }
                                                if (adapterConversas.getItemCount() == 0) {
                                                    adapterConversas.setArrayListConversa(arrayListConversa);
                                                    adapterConversas.filtrar(null);
                                                    listConversas.setAdapter(adapterConversas);
                                                } else {
                                                    adapterConversas.setArrayListConversa(arrayListConversa);
                                                    adapterConversas.filtrar(null);
                                                }
                                            }


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.i("ERRO_FIREBASE", "Erro ao preencher adapter de conversas: " + error);
                                        }
                                    });
                                }
                            }else{
                                listConversas.setVisibility(View.GONE);
                                textViewAviso.setVisibility(View.VISIBLE);
                            }
                        }else{
                            listConversas.setVisibility(View.GONE);
                            textViewAviso.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar bloqueados para preencher adapter de conversas: " + error);
                    }
                };
                queryUsuario.addValueEventListener(listenerUsuario);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro ao preencher adapter de conversas: " + error);
            }
        };
        queryConversa.addValueEventListener(listenerConversa);
    }

    public static void enviarNotificacao(Map<String,String> dados, Context context){

        queryConversa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=0;
                for(DataSnapshot item : snapshot.getChildren()){
                    if (dados!= null && Objects.requireNonNull(dados.get("keyConversa")).equals(item.getKey())){
                        if (Objects.equals(dados.get("isFotoPadrao"), "true")){
                            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.person_background);

                            if (drawable !=null) {
                                int width = drawable.getIntrinsicWidth();
                                int height = drawable.getIntrinsicHeight();

                                Bitmap largeIcon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(largeIcon);
                                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                drawable.draw(canvas);
                                if (dados.get("keyServico")==null){
                                    enviarNotificacao(dados,context,i,largeIcon);
                                }else {
                                    enviarNotificacaoAgendamento(dados,context,i,largeIcon);
                                }
                            }

                        }else {
                            StorageReference arquivo = Pessoa.obterReferenciaStorage().child(Objects.requireNonNull(dados.get("keyConversa")).split(" ")[Integer.parseInt(Objects.requireNonNull(dados.get("posicaoRemetente")))]+".jpg");
                            int finalI = i;
                            arquivo.getBytes(1048576).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap largeIcon = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    if (dados.get("keyServico")==null){
                                        enviarNotificacao(dados,context,finalI,largeIcon);
                                    }else {
                                        enviarNotificacaoAgendamento(dados,context,finalI,largeIcon);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("ERRO_FIREBASE_STORAGE", "Falha ao carregar imagem: "+ e);
                                }
                            });
                        }
                    }
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE_STORAGE", "Falha ao contatos para notificacao: "+ error);
            }
        });
    }

    private static void enviarNotificacao(Map<String,String> dados, Context context, int i, Bitmap largeIcon) {

        SharedPreferences preferences = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        boolean mostrarNotificacao = preferences.getBoolean("notificacaoChat",true);

        if (mostrarNotificacao){
            Intent intentAbrir = new Intent(context, ActivityPrincipal.class);
            intentAbrir.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);


            int posicaoDestinatario;
            if (Objects.equals(dados.get("posicaoRemetente"), "0")) {
                posicaoDestinatario = 1;
            }else {
                posicaoDestinatario = 0;
            }

            Bundle bundle = new Bundle();
            bundle.putInt("idNotificacao",i);
            bundle.putString("keyPessoaChat", Objects.requireNonNull(dados.get("keyConversa")).split(" ")[Integer.parseInt(Objects.requireNonNull(dados.get("posicaoRemetente")))]);
            intentAbrir.putExtra("bundleConversa",bundle);

            PendingIntent pendingIntentAbrir = PendingIntent.getActivity(
                    context, i, intentAbrir,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );


            RemoteInput remoteInput = new RemoteInput.Builder(Objects.requireNonNull(dados.get("keyConversa")))
                    .setLabel("Resposta")
                    .build();

            Intent intentEnviarMensagem = new Intent(context, RecieverResponderMensagem.class);
            intentEnviarMensagem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle resposta = new Bundle();
            resposta.putInt("idNotificacao",i);
            resposta.putString("keyConversa",dados.get("keyConversa"));
            resposta.putInt("posicaoRemetente",posicaoDestinatario);
            intentEnviarMensagem.putExtra("resposta",resposta);

            PendingIntent pendingIntentResposta =
                    PendingIntent.getBroadcast(context,
                            i,
                            intentEnviarMensagem,
                            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE
                    );

            NotificationCompat.Action actionResponder =
                    new NotificationCompat.Action.Builder(R.drawable.send_24,
                            "Responder", pendingIntentResposta)
                            .addRemoteInput(remoteInput)
                            .build();

            largeIcon = Imagem.resizeAndCropCenter(largeIcon,1000,true);

            largeIcon = Imagem.getCircleBitmap(largeIcon,context);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("Mensagens", "Mensagens", importance);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notificationRecuperada = null;
            StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();
            for(StatusBarNotification item: barNotifications) {
                if (item.getId() == i) {
                    notificationRecuperada = item.getNotification();
                }
            }

            ArrayList<String> listMensagens;

            if (notificationRecuperada!=null){
                listMensagens = notificationRecuperada.extras.getStringArrayList("mensagens");
                if (listMensagens ==null){
                    listMensagens = new ArrayList<>();
                }
            }else {
                listMensagens = new ArrayList<>();
            }

            listMensagens.add(dados.get("body"));

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Mensagens")
                    .setSmallIcon(R.drawable.ic_sf_notification)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .setSummaryText(dados.get("title")))
                    .setContentTitle(dados.get("title"))
                    .setContentText(dados.get("body"))
                    .setColor(ContextCompat.getColor(context,R.color.AzulSecundario))
                    .setLargeIcon(largeIcon)
                    .addAction(actionResponder)
                    .setContentIntent(pendingIntentAbrir)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            for (String mensagem: listMensagens) {
                inboxStyle.addLine(mensagem);
            }
            builder.setStyle(inboxStyle);

            Notification notification = builder.build();

            notification.extras.putStringArrayList("mensagens",listMensagens);

            notificationManager.notify(i, notification);
        }

    }

    public static void enviarNotificacaoAgendamento(Map<String,String> dados, Context context, int i, Bitmap largeIcon){

        SharedPreferences preferences = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        boolean mostrarNotificacao = preferences.getBoolean("notificacaoChat",true);

        if (mostrarNotificacao){
            int posicaoDestinatario;
            if (Objects.equals(dados.get("posicaoRemetente"), "0")) {
                posicaoDestinatario = 1;
            } else {
                posicaoDestinatario = 0;
            }

            largeIcon = Imagem.resizeAndCropCenter(largeIcon, 1000, true);

            largeIcon = Imagem.getCircleBitmap(largeIcon, context);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("Mensagens", "Mensagens", importance);
                notificationManager.createNotificationChannel(channel);
            }

            i+=10000;
            StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();
            for (int j =0;j<barNotifications.length;j++) {
                if (barNotifications[j].getId()==i){
                    j=0;
                    i+=10000;
                }
            }

            Intent intentAbrir = new Intent(context, ActivityPrincipal.class);
            intentAbrir.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);


            Bundle bundle = new Bundle();
            bundle.putInt("idNotificacao", i);
            bundle.putString("keyServico", dados.get("keyServico"));
            intentAbrir.putExtra("bundleAgendamento", bundle);

            PendingIntent pendingIntentAbrir = PendingIntent.getActivity(
                    context, i, intentAbrir,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Intent intentAceitarAgendamento = new Intent(context, RecieverResponderAgendamento.class);
            intentAceitarAgendamento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent intentRecusarAgendamento = new Intent(context, RecieverResponderAgendamento.class);
            intentRecusarAgendamento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Bundle respostaAceita = new Bundle();
            respostaAceita.putInt("idNotificacao", i);
            respostaAceita.putString("keyConversa",dados.get("keyConversa"));
            respostaAceita.putInt("posicaoRemetente",posicaoDestinatario);
            respostaAceita.putString("keyServico", dados.get("keyServico"));
            Bundle respostaRecusa = new Bundle(respostaAceita);
            respostaAceita.putInt("status",Servico.ACEITO);
            intentAceitarAgendamento.putExtra("resposta",respostaAceita);
            respostaRecusa.putInt("status",Servico.CANCELADO);
            intentRecusarAgendamento.putExtra("resposta",respostaRecusa);

            PendingIntent pendingIntentAceitar =
                    PendingIntent.getBroadcast(context,
                            i,
                            intentAceitarAgendamento,
                            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE
                    );


            PendingIntent pendingIntentRecusar =
                    PendingIntent.getBroadcast(context,
                            Integer.MAX_VALUE-i,
                            intentRecusarAgendamento,
                            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE
                    );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Mensagens")
                    .setSmallIcon(R.drawable.ic_sf_notification)
                    .setContentTitle(dados.get("title"))
                    .setContentText(dados.get("body"))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(dados.get("body")))
                    .setColor(ContextCompat.getColor(context,R.color.AzulSecundario))
                    .setLargeIcon(largeIcon)
                    .addAction(0,"Sim",pendingIntentAceitar)
                    .addAction(1,"Não",pendingIntentRecusar)
                    .setContentIntent(pendingIntentAbrir)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            notificationManager.notify(i, builder.build());
        }
    }

    public static void destruirListener(){
        if (queryConversa!=null){
            queryConversa.removeEventListener(listenerConversa);
        }
        if (queryUsuario!=null){
            queryUsuario.removeEventListener(listenerUsuario);
        }
    }

    @Override
    public int compareTo(Conversa o) {
        return Long.compare(o.getHorarioUltimaMensagem(), this.getHorarioUltimaMensagem());
    }

    public static String gerarKeyConversa(String keyPessoa1, String keyPessoa2){
        if (keyPessoa1.compareTo(keyPessoa2) < 0) {
            return keyPessoa1 + " " + keyPessoa2;
        } else {
            return keyPessoa2 + " " + keyPessoa1;
        }
    }

    public static int gerarPosicaoPessoa(String keyPessoa, String keyPessoaOutro){
        if (keyPessoa.compareTo(keyPessoaOutro) < 0) {
            return 0;
        } else {
            return 1;
        }
    }
}

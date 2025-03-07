package com.tcc.tcc;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tcc.tcc.classe.models.Conversa;
import com.tcc.tcc.classe.models.Servico;
import com.tcc.tcc.principal.FragmentPrincipalChat;

import java.util.Map;
import java.util.Objects;

public class FCMReciever extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dados = remoteMessage.getData();
        Context context = this.getApplicationContext();
        if (dados.get("keyConversa")!=null){
            if (!Objects.equals(dados.get("keyConversa"), FragmentPrincipalChat.obterKeyConversa())) {
                Conversa.enviarNotificacao(dados, context);
            }
        }else {
            Servico.obterReferencia().child(Objects.requireNonNull(dados.get("keyServico"))).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Servico servico = snapshot.getValue(Servico.class);
                    if (servico!=null){
                        servico.agendarNotificacao(context,dados.get("keyServico"),false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar servico para agendar notificação: " + error);
                }
            });
        }


        super.onMessageReceived(remoteMessage);
    }



    @Override
    public void onNewToken(@NonNull String token){

    }
}

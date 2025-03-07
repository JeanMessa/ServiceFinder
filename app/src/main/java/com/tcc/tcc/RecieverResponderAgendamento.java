package com.tcc.tcc;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tcc.tcc.classe.models.Mensagem;
import com.tcc.tcc.classe.models.Servico;

import java.util.Objects;

public class RecieverResponderAgendamento extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras()!=null){
            Bundle resposta = intent.getExtras().getBundle("resposta");
            if (resposta != null){
                int status = resposta.getInt("status");
                Query query = Servico.obterReferencia().child(Objects.requireNonNull(resposta.getString("keyServico")));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Servico servico = snapshot.getValue(Servico.class);
                        if (servico!=null && servico.getStatus()==Servico.PENDENTE){
                            Servico.atualizarStatus(resposta.getString("keyServico"), status);
                            Mensagem mensagemInformativa;
                            if (status==Servico.ACEITO){
                                mensagemInformativa = new Mensagem("O serviço de " + servico.getTipoServico().toLowerCase() + " foi aceito.", resposta.getInt("posicaoRemetente"));
                                servico.agendarNotificacao(context, resposta.getString("keyServico"),true);
                            }else {
                                mensagemInformativa = new Mensagem("O serviço de " + servico.getTipoServico().toLowerCase() + " foi recusado.", resposta.getInt("posicaoRemetente"));
                            }
                            mensagemInformativa.enviar(resposta.getString("keyConversa"),resposta.getString("keyServico"),context);
                            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                            notificationManager.cancel(resposta.getInt("idNotificacao"));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("ERRO_FIREBASE", "Erro ao recuperar servico para responder mensagem por notificação: " + error);
                    }
                });

            }
        }


    }
}


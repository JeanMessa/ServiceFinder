package com.tcc.tcc;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.tcc.tcc.classe.models.Mensagem;

public class RecieverResponderMensagem extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (intent.getExtras()!=null && remoteInput!=null){
            Bundle resposta = intent.getExtras().getBundle("resposta");
            if (resposta!=null){
                Mensagem mensagem = new Mensagem(remoteInput.getString(resposta.getString("keyConversa")),resposta.getInt("posicaoRemetente"));
                mensagem.enviar(resposta.getString("keyConversa"),context);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.cancel(resposta.getInt("idNotificacao"));
            }
        }


    }
}

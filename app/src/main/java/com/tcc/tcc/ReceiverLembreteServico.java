package com.tcc.tcc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tcc.tcc.classe.models.Servico;

import java.util.Objects;

public class ReceiverLembreteServico extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras()!=null){

            Log.i("TESTE", "onReceive: "+intent.getExtras().getString("notificacaoText") + intent.getExtras().getString("keyServico"));

            Query query = Servico.obterReferencia().child(Objects.requireNonNull(intent.getExtras().getString("keyServico"))).child("status");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() !=null){
                            int status = snapshot.getValue(Integer.class);
                            if (status==Servico.ACEITO){
                                Data.Builder data = new Data.Builder();
                                data.putString("keyServico",intent.getExtras().getString("keyServico"));
                                data.putString("notificacaoText",intent.getExtras().getString("notificacaoText"));
                                OneTimeWorkRequest workerLembreteServico =
                                        new OneTimeWorkRequest.Builder(WorkerLembreteServico.class)
                                                .setInputData(data.build())
                                                .build();


                                WorkManager.getInstance(context).enqueue(workerLembreteServico);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("ERRO_FIREBASE", "Erro ao consultar status para notificacao: " + error);
                    }
                });
            }
    }
}

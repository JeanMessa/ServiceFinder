package com.tcc.tcc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tcc.tcc.principal.ActivityPrincipal;

public class WorkerLembreteServico extends Worker {

    public static int contadorNotificacao = -1;

    private Context context;

    public WorkerLembreteServico(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        SharedPreferences preferences = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        boolean mostrarNotificacao = preferences.getBoolean("notificacaoAgenda",true);


        if (mostrarNotificacao){
            if (getInputData().getString("notificacaoText")!=null){


                Context context = this.getApplicationContext();

                Intent intent = new Intent(getApplicationContext(), ActivityPrincipal.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                Bundle bundle = new Bundle();
                bundle.putInt("idNotificacao",contadorNotificacao);
                bundle.putString("keyServico",getInputData().getString("keyServico"));

                intent.putExtra("bundleAgendamento",bundle);


                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), contadorNotificacao, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Agenda")
                        .setSmallIcon(R.drawable.ic_sf_notification)
                        .setColor(ContextCompat.getColor(context,R.color.AzulSecundario))
                        .setContentTitle("Serviço em Breve")
                        .setContentText(getInputData().getString("notificacaoText"))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getInputData().getString("notificacaoText")))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("Agenda", "Agenda", NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(contadorNotificacao, builder.build());
                contadorNotificacao--;
                if (contadorNotificacao==-10000){
                    contadorNotificacao=-1;
                }

                return Result.success();
            }else {
                return Result.failure();
            }
        }else {
            return  Result.success();
        }





    }
}

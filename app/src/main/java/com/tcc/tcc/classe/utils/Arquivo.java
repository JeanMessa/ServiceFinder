package com.tcc.tcc.classe.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.tcc.tcc.R;

import java.io.File;
import java.net.URLConnection;
import java.util.Locale;

public class Arquivo {
    File file;
    StorageReference referencia;
    private static int contadorIdNotificao = 0;
    int idNotificacao;

    public Arquivo(File file, StorageReference referencia) {
        this.file = file;
        this.referencia = referencia;

    }

    public static String transformarEmMedidasUsuais(float tamanho){
        if (tamanho>1024){
            tamanho /= 1024;
            if (tamanho>1024){
                tamanho /= 1024;
                return String.format(Locale.ROOT,"%.2f MB", tamanho);
            }else{
                return String.format(Locale.ROOT,"%.2f KB", tamanho);
            }
        }else{
            return String.format(Locale.ROOT,"%.2f B", tamanho);
        }
    }

    public void download(Context context) {
        idNotificacao = contadorIdNotificao;
        contadorIdNotificao++;
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Download")
                .setSmallIcon(R.drawable.download_24)
                .setContentTitle(file.getName())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("Download", "Download", importance);
            notificationManager.createNotificationChannel(channel);
        }
        referencia.getFile(file).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                builder.setProgress((int)snapshot.getTotalByteCount(),(int) snapshot.getBytesTransferred(), false)
                        .setStyle(bigText.bigText(transformarEmMedidasUsuais(snapshot.getBytesTransferred())+ " / " + transformarEmMedidasUsuais(snapshot.getTotalByteCount())));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(idNotificacao, builder.build());
                }

            }
        }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri uri = FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName()+".provider",file);
                intent.setDataAndType(uri, URLConnection.guessContentTypeFromName(file.getName()));
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                notificationManager.cancel(idNotificacao);
                builder.setProgress(0,0,false)
                        .setStyle(bigText.bigText("Download Completo"))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
                notificationManager.notify(idNotificacao, builder.build());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("FIREBASE", "Erro ao baixar arquivo: " + e);
            }
        });
    }
}

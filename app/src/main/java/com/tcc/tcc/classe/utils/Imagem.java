package com.tcc.tcc.classe.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tcc.tcc.R;

import java.io.ByteArrayOutputStream;

public class Imagem {
    public static UploadTask upload(String nomeArquivo, ByteArrayOutputStream bytesFotoPerfil, StorageReference referencia){
        return referencia.child(nomeArquivo).putBytes(bytesFotoPerfil.toByteArray());
    }

    public static void download(ImageView imageView,String nomeArquivo,StorageReference referencia){
        StorageReference arquivo = referencia.child(nomeArquivo);
        arquivo.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(!((Activity)(imageView.getContext())).isDestroyed()) {
                    Glide.with(imageView.getContext())
                            .load(uri)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .thumbnail(Glide.with(imageView.getContext()).load(R.drawable.replay_24))
                            .error(R.drawable.close_24)
                            .into(imageView);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("ERRO_FIREBASE_STORAGE", "Falha ao carregar imagem: " + e);

            }
        });

    }

    public static Task<Uri> download(String nomeArquivo,StorageReference referencia) {
        StorageReference arquivo = referencia.child(nomeArquivo);
        return arquivo.getDownloadUrl();
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap, Context context) {
        final Bitmap retorno = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(retorno);
        final int color = ContextCompat.getColor(context,R.color.AzulSecundario);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return retorno;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) return bitmap;
        float scale = (float) size / Math.min(w,  h);
        Bitmap retorno = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(retorno);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return retorno;
    }




}

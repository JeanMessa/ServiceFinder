package com.tcc.tcc.classe.utils;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.tcc.tcc.R;

import java.io.File;
import java.io.IOException;

public class Audio {
    private String keyMensagem;
    private long tempo;
    private ProgressBar progressBar;
    private ImageButton btnPlayPause;

    private MediaPlayer mediaPlayer;

    private TextView textViewDuracao;

    private Chronometer chronometerDuracao;

    private boolean pausado;

    private boolean completo;

    private boolean mensagemPropria;

    private Drawable play,pause;

    private int branco,pretoOuBranco;


    public Audio(String keyMensagem,ProgressBar progressBar, ImageButton btnPlayPause, TextView textViewDuracao, Chronometer chronometerDuracao,boolean mensagemPropria) {
        tempo = 0;
        mediaPlayer = new MediaPlayer();
        this.keyMensagem = keyMensagem;
        this.progressBar = progressBar;
        this.btnPlayPause = btnPlayPause;
        this.textViewDuracao = textViewDuracao;
        this.chronometerDuracao = chronometerDuracao;
        this.mensagemPropria = mensagemPropria;
        pausado = true;

        play = ContextCompat.getDrawable(btnPlayPause.getContext(),R.drawable.play_arrow_24);
        pause = ContextCompat.getDrawable(btnPlayPause.getContext(),R.drawable.pause_24);

        branco = btnPlayPause.getContext().getColor(R.color.Branco);
        pretoOuBranco = btnPlayPause.getContext().getColor(R.color.PretoOuBranco);


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                chronometerDuracao.setBase(SystemClock.elapsedRealtime());
                chronometerDuracao.start();
                textViewDuracao.setText(String.format("/ %s", textViewDuracao.getText()));

            }
        });

        chronometerDuracao.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                if (SystemClock.elapsedRealtime()-chronometerDuracao.getBase()>=mediaPlayer.getDuration()){
                    chronometerDuracao.setText(textViewDuracao.getText().subSequence(2,textViewDuracao.getText().length()));
                }
                progressBar.setProgress(mediaPlayer.getCurrentPosition());
                if (completo){
                    reset();
                    completo = false;
                }
            }
        });



        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                progressBar.setProgress(progressBar.getMax());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(0);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        completo = true;
                    }
                }).start();
            }
        });

    }

    public void setNovaView(ProgressBar progressBar, ImageButton btnPlayPause, TextView textViewDuracao, Chronometer chronometerDuracao) {
        this.progressBar = progressBar;
        this.btnPlayPause = btnPlayPause;
        this.textViewDuracao = textViewDuracao;
        this.chronometerDuracao = chronometerDuracao;

        progressBar.setMax(mediaPlayer.getDuration());

        chronometerDuracao.setVisibility(View.VISIBLE);
        chronometerDuracao.setBase(SystemClock.elapsedRealtime()-mediaPlayer.getCurrentPosition());

        if (!pausado){
            progressBar.setProgress(mediaPlayer.getCurrentPosition());
            btnPlayPause.setImageDrawable(pause);
            chronometerDuracao.start();
        }else{
            progressBar.setProgress((int) tempo);
            btnPlayPause.setImageDrawable(play);
            chronometerDuracao.stop();
        }

        if (mensagemPropria){
            btnPlayPause.getDrawable().setTint(branco);
        }else{
            btnPlayPause.getDrawable().setTint(pretoOuBranco);
        }



        chronometerDuracao.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                progressBar.setProgress(mediaPlayer.getCurrentPosition());
                if (completo){
                    reset();
                    completo = false;
                }
            }
        });

        textViewDuracao.setText(String.format("/ %s", textViewDuracao.getText()));

    }

    public void play(File file){
        btnPlayPause.setImageDrawable(pause);
        if (mensagemPropria){
            btnPlayPause.getDrawable().setTint(branco);
        }else{
            btnPlayPause.getDrawable().setTint(pretoOuBranco);
        }
        pausado = false;

        chronometerDuracao.setVisibility(View.VISIBLE);

        try {
            mediaPlayer.setDataSource(progressBar.getContext(), Uri.fromFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void reset(){
        tempo = 0;
        keyMensagem = null;
        progressBar.setProgress(0);
        btnPlayPause.setImageDrawable(play);
        if (mensagemPropria){
            btnPlayPause.getDrawable().setTint(branco);
        }else{
            btnPlayPause.getDrawable().setTint(pretoOuBranco);
        }
        pausado = false;

        if (chronometerDuracao.getVisibility()==View.VISIBLE){
            chronometerDuracao.setVisibility(View.GONE);
            textViewDuracao.setText(textViewDuracao.getText().subSequence(2,textViewDuracao.getText().length()));
            mediaPlayer.pause();
        }
        chronometerDuracao.stop();
    }

    public void pause(){
        btnPlayPause.setImageDrawable(play);
        if (mensagemPropria){
            btnPlayPause.getDrawable().setTint(branco);
        }else{
            btnPlayPause.getDrawable().setTint(pretoOuBranco);
        }
        pausado = true;

        chronometerDuracao.stop();
        tempo = SystemClock.elapsedRealtime() - chronometerDuracao.getBase();
        mediaPlayer.pause();
    }

    public void continuar(){
        btnPlayPause.setImageDrawable(pause);
        if (mensagemPropria){
            btnPlayPause.getDrawable().setTint(branco);
        }else{
            btnPlayPause.getDrawable().setTint(pretoOuBranco);
        }
        pausado = false;

        chronometerDuracao.setBase(SystemClock.elapsedRealtime() - tempo);
        chronometerDuracao.start();
        mediaPlayer.start();
    }



    public String getKeyMensagem() {
        return keyMensagem;
    }

    public void setKeyMensagem(String keyMensagem) {
        this.keyMensagem = keyMensagem;
    }


    public long getTempo() {
        return tempo;
    }

    public void setTempo(long tempo) {
        this.tempo = tempo;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public ImageButton getBtnPlayPause() {
        return btnPlayPause;
    }

    public void setBtnPlayPause(ImageButton btnPlayPause) {
        this.btnPlayPause = btnPlayPause;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public TextView getTextViewDuracao() {
        return textViewDuracao;
    }

    public void setTextViewDuracao(TextView textViewDuracao) {
        this.textViewDuracao = textViewDuracao;
    }

    public Chronometer getChronometerDuracao() {
        return chronometerDuracao;
    }

    public void setChronometerDuracao(Chronometer chronometerDuracao) {
        this.chronometerDuracao = chronometerDuracao;
    }

    public boolean isPausado() {
        return pausado;
    }

    public void setPausado(boolean pausado) {
        this.pausado = pausado;
    }
}

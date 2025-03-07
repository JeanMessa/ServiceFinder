package com.tcc.tcc.adapter;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Mensagem;
import com.tcc.tcc.classe.models.Servico;
import com.tcc.tcc.classe.utils.Arquivo;
import com.tcc.tcc.classe.utils.Audio;
import com.tcc.tcc.classe.utils.Horario;
import com.tcc.tcc.classe.utils.Imagem;
import com.tcc.tcc.popup.PopupPergunta;
import com.tcc.tcc.popup.PopupVerMaisServico;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.ViewHolder> {

    private LinkedHashMap<String, Mensagem> mapMensagem;
    private int posicaoPessoa;
    private String keyConversa;
    private PopupWindow popupImagem;
    private ImageView imgVisualizar;
    private StorageReference referenciaConversa;

    HashMap<String,File> hashMapAudio;
    private Audio audioAtual;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public View getView() {
            return view;
        }


    }

    public AdapterChat(RecyclerView listChat,String keyConversa,int posicaoPessoa) {


        this.keyConversa = keyConversa;
        referenciaConversa = Mensagem.obterReferenciaStorage().child(keyConversa);
        this.posicaoPessoa = posicaoPessoa;
        Mensagem.preencherAdapter(this,listChat,keyConversa,posicaoPessoa);

        LayoutInflater inflater = (LayoutInflater) listChat.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupImagemView = inflater.inflate(R.layout.popup_chat_visualizar_imagem,(ViewGroup) listChat.getRootView(),false);

        hashMapAudio = new HashMap<>();

        popupImagem = new PopupWindow(popupImagemView, MATCH_PARENT, WRAP_CONTENT, true);
        popupImagem.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ActivityPrincipal activity = (ActivityPrincipal)listChat.getContext();
                activity.trocarEscurecer(View.GONE);
            }
        });

        imgVisualizar = popupImagemView.findViewById(R.id.imgVisualizar);

        ImageButton btnVoltar = popupImagemView.findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupImagem.dismiss();
            }
        });

    }

    public void setMapMensagem(LinkedHashMap<String, Mensagem> mapMensagem) {
        this.mapMensagem = mapMensagem;
    }

    public void putMapMensagem(String key,Mensagem mensagem) {
        this.mapMensagem.put(key,mensagem);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_chat, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        LinearLayout view = (LinearLayout) viewHolder.getView();
        String key = getKeyatPosition(position);
        Mensagem mensagem = mapMensagem.get(key);

        if (mensagem!=null){
            TextView textViewDia = view.findViewById(R.id.textViewDia);
            if (mensagem.obterDataprimeiraMensagem()!=null){
                textViewDia.setVisibility(View.VISIBLE);

                long ontem = Horario.adicionarTempo(Horario.getHorarioAtual(),Calendar.DAY_OF_WEEK,-1);
                if (mensagem.obterDataprimeiraMensagem().equals(new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Horario.getHorarioAtual()))){
                    textViewDia.setText(R.string.hoje);
                }else if (mensagem.obterDataprimeiraMensagem().equals(new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(ontem))){
                    textViewDia.setText(R.string.ontem);
                }else{
                    textViewDia.setText(mensagem.obterDataprimeiraMensagem());
                }
            }else{
                textViewDia.setVisibility(View.GONE);
            }

            TextView textViewMensagem = view.findViewById(R.id.textViewMensagem);
            textViewMensagem.setText(mensagem.getTexto());

            TextView textViewHorario = view.findViewById(R.id.textViewHorario);
            String horario = new SimpleDateFormat("HH:mm", Locale.ROOT).format(mensagem.getHorario());
            textViewHorario.setText(horario);

            LinearLayout layoutExcluir = view.findViewById(R.id.layoutExcluir);
            layoutExcluir.setVisibility(View.GONE);
            CardView cardViewMensagem = view.findViewById(R.id.cardViewMensagem);
            cardViewMensagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (layoutExcluir.getVisibility()==View.GONE){
                        layoutExcluir.setVisibility(View.VISIBLE);
                    }else {
                        layoutExcluir.setVisibility(View.GONE);
                    }
                }
            });

            ImageView imgMensagem = view.findViewById(R.id.imgMensagem);



            int branco = ContextCompat.getColor(view.getContext(),R.color.Branco);
            int pretoOuBranco = ContextCompat.getColor(view.getContext(),R.color.PretoOuBranco);

            if (mensagem.getTipoMensagem() == Mensagem.AUDIO || mensagem.getTexto().equals("")){
                textViewMensagem.setVisibility(View.GONE);
            }else {
                textViewMensagem.setVisibility(View.VISIBLE);
            }

            if (mensagem.getTipoMensagem() == Mensagem.IMAGEM){
                imgMensagem.setVisibility(View.VISIBLE);

                Imagem.download(imgMensagem,key+".jpg",referenciaConversa.child("imagem"));
                imgMensagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imgVisualizar.setImageDrawable(imgMensagem.getDrawable());
                        popupImagem.showAtLocation((ViewGroup) view.getRootView(),Gravity.CENTER,0,0);
                        ActivityPrincipal activity = (ActivityPrincipal)view.getContext();
                        activity.trocarEscurecer(View.VISIBLE,0.9F);
                    }
                });
            }else{
                imgMensagem.setVisibility(View.GONE);
            }

            LinearLayout layoutArquivo = view.findViewById(R.id.layoutArquivo);

            if (mensagem.getTipoMensagem() == Mensagem.ARQUIVO){
                layoutArquivo.setVisibility(View.VISIBLE);

                TextView textViewArquivo = layoutArquivo.findViewById(R.id.textViewArquivo);
                textViewArquivo.setText(mensagem.getNomeArquivo());

                ImageButton btnBaixarArquivo = layoutArquivo.findViewById(R.id.btnBaixarArquivo);
                ImageView imgIconeArquivo = layoutArquivo.findViewById(R.id.imgIconeArquivo);
                if (mensagem.getPosicaoRemetente()==posicaoPessoa) {
                    btnBaixarArquivo.setVisibility(View.GONE);
                    textViewArquivo.setTextColor(branco);
                    imgIconeArquivo.getDrawable().setTint(branco);
                }else{
                    btnBaixarArquivo.setVisibility(View.VISIBLE);
                    textViewArquivo.setTextColor(pretoOuBranco);
                    imgIconeArquivo.getDrawable().setTint(pretoOuBranco);


                    btnBaixarArquivo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityPrincipal activity = (ActivityPrincipal) view.getContext();
                            StorageReference referenciaDownload = referenciaConversa.child("arquivo").child(key);
                            if(Build.VERSION.SDK_INT >= 30) {
                                File raiz = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"ServiceFinder");
                                File localFile = new File(raiz, mensagem.getNomeArquivo());
                                if (!raiz.exists()){
                                    raiz.mkdirs();
                                }
                                int i = 0;
                                while (localFile.exists()){
                                    i++;
                                    String novoNomeArquivo = mensagem.getNomeArquivo().replaceFirst("\\.","("+i+").");
                                    localFile = new File(raiz,novoNomeArquivo);
                                }
                                Arquivo arquivo = new Arquivo(localFile,referenciaDownload);
                                arquivo.download(view.getContext());
                            }else {
                                File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mensagem.getNomeArquivo());
                                Arquivo arquivo = new Arquivo(localFile,referenciaDownload);
                                if (!activity.isDestroyed()) {
                                    activity.realizarDownload(arquivo);
                                }
                            }
                        }
                    });
                }
            }else{
                layoutArquivo.setVisibility(View.GONE);
            }

            LinearLayout layoutAudio = view.findViewById(R.id.layoutAudio);

            if (mensagem.getTipoMensagem()==Mensagem.AUDIO){
                layoutAudio.setVisibility(View.VISIBLE);

                ProgressBar progressBarAudio = layoutAudio.findViewById(R.id.progressBarAudio);

                Chronometer chronometerAudio = layoutAudio.findViewById(R.id.chronometerDuracao);

                ImageButton btnAudio = layoutAudio.findViewById(R.id.btnAudio);
                Drawable play = ContextCompat.getDrawable(view.getContext(),R.drawable.play_arrow_24);

                TextView textViewDuracao = layoutAudio.findViewById(R.id.textViewDuracao);
                textViewDuracao.setText(new SimpleDateFormat("mm:ss", Locale.ROOT).format(mensagem.getDuracaoAudio()));

                if (audioAtual==null||!Objects.equals(key, audioAtual.getKeyMensagem())){
                    progressBarAudio.setProgress(0);
                    chronometerAudio.setVisibility(View.GONE);
                    btnAudio.setImageDrawable(play);

                }else {
                    audioAtual.setNovaView(progressBarAudio,btnAudio,textViewDuracao,chronometerAudio);
                }


                try {
                    if (!hashMapAudio.containsKey(key)){
                        File temp = File.createTempFile("tempAtual",".mp3");
                        referenciaConversa.child("audio").child(key+".mp3").getFile(temp).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                hashMapAudio.put(key,temp);
                            }
                        });
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                btnAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hashMapAudio.containsKey(key)){
                            if (audioAtual==null || !Objects.equals(audioAtual.getKeyMensagem(), key) || audioAtual.isPausado()){
                                if (audioAtual==null || !Objects.equals(audioAtual.getKeyMensagem(), key)){
                                    if (audioAtual!=null){
                                        audioAtual.reset();
                                    }
                                    audioAtual = new Audio(key,progressBarAudio,btnAudio,textViewDuracao,chronometerAudio,mensagem.getPosicaoRemetente()==posicaoPessoa);
                                    audioAtual.play(hashMapAudio.get(key));
                                }else{
                                    audioAtual.continuar();
                                }
                            }else{
                                audioAtual.pause();
                            }
                        }else{
                            Toast.makeText(view.getContext(), "Áudio sendo baixado, tente novamente em breve", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                if (mensagem.getPosicaoRemetente()==posicaoPessoa){
                    btnAudio.getDrawable().setTint(branco);
                    textViewDuracao.setTextColor(branco);
                    chronometerAudio.setTextColor(branco);
                    progressBarAudio.getProgressDrawable().setTint(branco);
                }else{
                    btnAudio.getDrawable().setTint(pretoOuBranco);
                    textViewDuracao.setTextColor(pretoOuBranco);
                    chronometerAudio.setTextColor(pretoOuBranco);
                    progressBarAudio.getProgressDrawable().setTint(pretoOuBranco);
                }

            }else{
                layoutAudio.setVisibility(View.GONE);
            }

            LinearLayout layoutAgendamento = view.findViewById(R.id.layoutAgendamento);
            LinearLayout layoutSimNao= layoutAgendamento.findViewById(R.id.layoutSimNao);

            Button btnVerServico= layoutAgendamento.findViewById(R.id.btnVerServico);
            btnVerServico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupVerMaisServico popupVerMaisServico = new PopupVerMaisServico(mensagem.getKeyServico(), (ViewGroup) view,keyConversa.split(" ")[posicaoPessoa]);
                    popupVerMaisServico.show();
                }
            });


            if (mensagem.getTipoMensagem()==Mensagem.AGENDAMENTO){
                layoutAgendamento.setVisibility(View.VISIBLE);
                if (mensagem.getPosicaoRemetente()!=posicaoPessoa){
                    Servico.completarMensagemAgendamento(mensagem.getKeyServico(),layoutAgendamento,textViewMensagem,mensagem,keyConversa,key);
                }else{
                    textViewMensagem.setText(String.format("Você %s", textViewMensagem.getText()));
                    layoutSimNao.setVisibility(View.GONE);
                }
            }else{
                layoutAgendamento.setVisibility(View.GONE);
            }

            if (mensagem.getTipoMensagem()==Mensagem.INFORMATIVO_SERVICO){
                layoutAgendamento.setVisibility(View.VISIBLE);
                layoutSimNao.setVisibility(View.GONE);
            }

            PopupPergunta popupPerguntaEsconder = new PopupPergunta("Se você ocultar essa mensagem nunca mais poderá vê-la porém o outro participante dessa conversa ainda verá.\nTem certeza que deseja ocultar esta mensagem?",(ViewGroup) view);
            popupPerguntaEsconder.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    ActivityPrincipal activityPrincipal = (ActivityPrincipal) view.getContext();
                    activityPrincipal.trocarEscurecer(View.GONE);
                }
            });
            popupPerguntaEsconder.setBtnConfirmarListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mensagem.esconder(keyConversa,key,posicaoPessoa);
                    popupPerguntaEsconder.getPopup().dismiss();
                }
            });

            ImageButton btnEsconder = view.findViewById(R.id.btnEsconder);
            btnEsconder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(popupPerguntaEsconder.show("confirmacaoOcultar",btnEsconder.getContext())){
                        ActivityPrincipal activityPrincipal = (ActivityPrincipal) view.getContext();
                        activityPrincipal.trocarEscurecer(View.VISIBLE);
                    }
                }
            });

            ImageButton btnExcluir = view.findViewById(R.id.btnExcluir);


            LinearLayout layoutMensagem = view.findViewById(R.id.layoutMensagem);

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) cardViewMensagem.getLayoutParams();
            final float density = view.getResources().getDisplayMetrics().density;




            if (mensagem.getPosicaoRemetente()==posicaoPessoa){
                layoutMensagem.setGravity(Gravity.END);
                cardViewMensagem.setCardBackgroundColor(ContextCompat.getColor(view.getContext(),R.color.AzulSecundario));

                layoutParams.setMargins((int)(25*density),0,(int)(10*density),0);
                cardViewMensagem.setLayoutParams(layoutParams);


                textViewMensagem.setTextColor(branco);
                textViewHorario.setTextColor(branco);

                if (mensagem.getTipoMensagem()==Mensagem.AGENDAMENTO || mensagem.getTipoMensagem()==Mensagem.INFORMATIVO_SERVICO){
                    btnVerServico.setTextColor(branco);
                }

                PopupPergunta popupPerguntaExcluir = new PopupPergunta("Se você excluir essa mensagem ela nunca mais poderá ser vista por nenhum participante dessa conversa.\nTem certeza que deseja excluir esta mensagem?",(ViewGroup) view);
                popupPerguntaExcluir.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        ActivityPrincipal activityPrincipal = (ActivityPrincipal) view.getContext();
                        activityPrincipal.trocarEscurecer(View.GONE);
                    }
                });
                popupPerguntaExcluir.setBtnConfirmarListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mensagem.excluir(keyConversa,key);
                        popupPerguntaExcluir.getPopup().dismiss();
                    }
                });


                btnEsconder.getDrawable().setTint(branco);
                if (mensagem.getTipoMensagem()!=Mensagem.INFORMATIVO_SERVICO && mensagem.getTipoMensagem()!=Mensagem.AGENDAMENTO){
                    btnExcluir.getDrawable().setTint(branco);
                    btnExcluir.setVisibility(View.VISIBLE);
                }else {
                    btnExcluir.setVisibility(View.GONE);
                }

                btnExcluir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupPerguntaExcluir.show("confirmacaoExcluir", btnExcluir.getContext())){
                            ActivityPrincipal activityPrincipal = (ActivityPrincipal) view.getContext();
                            activityPrincipal.trocarEscurecer(View.VISIBLE);
                        }
                    }
                });

            }else{
                if (!mensagem.isLido()){
                    mensagem.atualizarLido(keyConversa,key,true);
                }

                layoutMensagem.setGravity(Gravity.START);
                cardViewMensagem.setCardBackgroundColor(ContextCompat.getColor(view.getContext(),R.color.FundoMensagem));

                layoutParams.setMargins((int)(10*density),0,(int)(25*density),0);
                cardViewMensagem.setLayoutParams(layoutParams);


                textViewMensagem.setTextColor(pretoOuBranco);
                textViewHorario.setTextColor(pretoOuBranco);

                if (mensagem.getTipoMensagem()==Mensagem.AGENDAMENTO || mensagem.getTipoMensagem()==Mensagem.INFORMATIVO_SERVICO){
                    btnVerServico.setTextColor(ContextCompat.getColor(view.getContext(),R.color.AzulSecundario));
                }


                btnExcluir.setVisibility(View.GONE);
            }

        }
    }


    public String getKeyatPosition(int position){
        return mapMensagem.keySet().toArray()[position].toString();
    }

    @Override
    public int getItemCount() {
        if (mapMensagem!=null){
            return mapMensagem.size();
        }else {
            return 0;
        }
    }

}
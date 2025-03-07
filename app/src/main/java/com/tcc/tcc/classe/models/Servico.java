package com.tcc.tcc.classe.models;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tcc.tcc.R;
import com.tcc.tcc.ReceiverLembreteServico;
import com.tcc.tcc.WorkerLembreteServico;
import com.tcc.tcc.adapter.AdapterAgenda;

import com.tcc.tcc.classe.utils.FirebaseMessage;
import com.tcc.tcc.classe.utils.Horario;
import com.tcc.tcc.view.ScrollViewMaxHeight;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Servico {
    private String keyPrestador;
    private String keyCliente;
    private long horarioPrevisto;
    private String descricao;
    private String tipoServico;
    private long horarioFinalizacao;
    private double avaliacao;
    private int status;

    private static ValueEventListener listenerAgenda,listenerVerMais;
    private static Query queryAgenda,queryVerMais;

    public static final int PENDENTE = 0 ,ACEITO = 1, CANCELADO = 2, CONCLUIDO = 3;


    private static final DatabaseReference referencia = FirebaseDatabase.getInstance().getReference().child("servico");

    public Servico(){}

    public Servico(String keyPrestador, String keyCliente, long horarioPrevisto, String descricao, String tipoServico) {
        this.keyPrestador = keyPrestador;
        this.keyCliente = keyCliente;
        this.horarioPrevisto = horarioPrevisto;
        this.descricao = descricao;
        this.tipoServico = tipoServico;
        avaliacao = -1;
        horarioFinalizacao = -1;
        status = PENDENTE;
    }

    public String cadastrar(){
        String key = referencia.push().getKey();
        referencia.child(key).setValue(this);
        return key;
    }

    public static void atualizarStatus(String keyServico,int status){
        referencia.child(keyServico).child("status").setValue(status);
        if (status==CONCLUIDO||status==CANCELADO){
            referencia.child(keyServico).child("horarioFinalizacao").setValue(Horario.getHorarioAtual());
        }
    }

    public static void completarMensagemAgendamento(String keyServico, LinearLayout layoutAgendamento, TextView textViewMensagem,Mensagem mensagem,String keyConversa,String keyMensagem){
        String textoAgendamento = textViewMensagem.getText().toString();
        textViewMensagem.setText(String.format("O prestador %s", textoAgendamento));
        if (keyServico!=null){
            Query query = referencia.child(keyServico);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Servico servico = snapshot.getValue(Servico.class);
                    LinearLayout layoutSimNao= layoutAgendamento.findViewById(R.id.layoutSimNao);

                    if (servico!=null) {
                        String textoAdicional = "";
                        servico.vefificarVencimento(keyServico,keyConversa, layoutAgendamento.getContext());
                        if (servico.status == PENDENTE) {
                            textoAdicional = ", deseja aceitar?";
                            layoutAgendamento.setVisibility(View.VISIBLE);
                            layoutSimNao.setVisibility(View.VISIBLE);

                            int posicaoRemententeInformativa;
                            if (mensagem.getPosicaoRemetente()==0){
                                posicaoRemententeInformativa = 1;
                            }else{
                                posicaoRemententeInformativa = 0;
                            }

                            Button btnAceitar = layoutAgendamento.findViewById(R.id.btnAceitarServico);
                            btnAceitar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Servico.atualizarStatus(mensagem.getKeyServico(),Servico.ACEITO);
                                    mensagem.atualizarLido(keyConversa,keyMensagem,false);
                                    Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.tipoServico.toLowerCase() + " foi aceito.",posicaoRemententeInformativa);
                                    mensagemInformativa.enviar(keyConversa,keyServico, btnAceitar.getContext());

                                    servico.agendarNotificacao(layoutAgendamento.getContext(),keyServico,true);
                                }
                            });

                            Button btnRecusar = layoutAgendamento.findViewById(R.id.btnRecusarServico);
                            btnRecusar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Servico.atualizarStatus(mensagem.getKeyServico(),Servico.CANCELADO);
                                    mensagem.atualizarLido(keyConversa,keyMensagem,false);
                                    Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.tipoServico.toLowerCase() + " foi recusado.",posicaoRemententeInformativa);
                                    mensagemInformativa.enviar(keyConversa,keyServico, btnRecusar.getContext());

                                }
                            });
                        }else{
                            layoutSimNao.setVisibility(View.GONE);
                        }
                        Pessoa.AdicionarNomeEmTextView(servico.keyPrestador, textViewMensagem, textoAgendamento + textoAdicional);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar o serviço: " + error);
                }
            });
        }

    }

    public static void preencherAdapter(AdapterAgenda adapterAgenda, RecyclerView listAgenda, String relacaoServico){
        queryAgenda = referencia.orderByChild("key"+relacaoServico).equalTo(adapterAgenda.getKeyPessoaUsuario());
        listenerAgenda = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinkedHashMap<String,Servico> mapServico = new LinkedHashMap<>();
                if (snapshot.getChildrenCount() > 0) {
                    for(DataSnapshot snapshotServico : snapshot.getChildren()){
                        String keyServico = snapshotServico.getKey();
                        Servico servico = snapshotServico.getValue(Servico.class);
                        if (servico!=null){
                            servico.vefificarVencimento(keyServico,Conversa.gerarKeyConversa(servico.keyCliente,servico.keyPrestador), listAgenda.getContext());
                            mapServico.put(keyServico,servico);
                        }

                    }
                }

                Comparator<LinkedHashMap.Entry<String, Servico>> valueComparator = new Comparator<LinkedHashMap.Entry<String, Servico>>() {
                    @Override
                    public int compare(Map.Entry<String, Servico> s1, Map.Entry<String, Servico> s2) {
                        return Long.compare(s1.getValue().horarioPrevisto,s2.getValue().horarioPrevisto);
                    }
                };

                mapServico= mapServico.entrySet().stream().sorted(valueComparator).
                        collect(Collectors.toMap(LinkedHashMap.Entry::getKey, LinkedHashMap.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));



                if (adapterAgenda.getItemCount()==0){
                    adapterAgenda.setMapServico(mapServico);
                    adapterAgenda.filtrar();
                    listAgenda.setAdapter(adapterAgenda);
                }else {
                    adapterAgenda.setMapServico(mapServico);
                    adapterAgenda.filtrar();
                    adapterAgenda.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro ao preencher adapter da agenda: " + error);
            }
        };
        queryAgenda.addValueEventListener(listenerAgenda);
    }

    public void vefificarVencimento(String keyServico,String keyConversa, Context context){
        if (status == PENDENTE && horarioPrevisto<Horario.getHorarioAtual()){
            int posicaoRemententeInformativa = Conversa.gerarPosicaoPessoa(keyPrestador,keyCliente);
            Mensagem mensagemInformativa = new Mensagem("O serviço de " + tipoServico.toLowerCase() + " foi cancelado, pois expirou.",posicaoRemententeInformativa);
            mensagemInformativa.enviar(keyConversa,keyServico,context);
            status = CANCELADO;
            referencia.child(keyServico).child("status").setValue(Servico.CANCELADO);
        }
    }


    public static void preencherPopupVerMais(String keyServico, View popupView,String keyPessoaUsuario){
        queryVerMais = referencia.child(keyServico);
        listenerVerMais = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Servico servico = snapshot.getValue(Servico.class);

                if (servico!=null){
                    TextView textViewTipo = popupView.findViewById(R.id.textViewTipo);
                    textViewTipo.setText(servico.tipoServico);

                    TextView textViewPrestador = popupView.findViewById(R.id.textViewPrestador);
                    Pessoa.AdicionarNomeEmTextView(servico.getKeyPrestador(),textViewPrestador,null);

                    TextView textViewCliente = popupView.findViewById(R.id.textViewCliente);
                    Pessoa.AdicionarNomeEmTextView(servico.getKeyCliente(),textViewCliente,null);

                    TextView textViewHorario = popupView.findViewById(R.id.textViewHorario);
                    textViewHorario.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(servico.horarioPrevisto));

                    if (servico.horarioFinalizacao!=-1){
                        LinearLayout layoutFinalizado = popupView.findViewById(R.id.layoutFinalizado);
                        layoutFinalizado.setVisibility(View.VISIBLE);
                        TextView textViewViewHorarioFinalizado = popupView.findViewById(R.id.textViewHorarioFinalizado);
                        textViewViewHorarioFinalizado.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(servico.horarioFinalizacao));
                    }


                    TextView textViewStatus = popupView.findViewById(R.id.textViewStatus);
                    ImageView imgStatus = popupView.findViewById(R.id.imgStatus);

                    Drawable iconeStatus;
                    if (servico.getStatus()==Servico.PENDENTE){
                        textViewStatus.setText(R.string.pendente);
                        iconeStatus = ContextCompat.getDrawable(popupView.getContext(),R.drawable.pending_24);
                        iconeStatus.setTint(ContextCompat.getColor(popupView.getContext(),R.color.AmareloAlerta));
                    }else if (servico.getStatus()==Servico.CANCELADO){
                        textViewStatus.setText(R.string.cancelado);
                        iconeStatus = ContextCompat.getDrawable(popupView.getContext(),R.drawable.cancel_24);
                        iconeStatus.setTint(ContextCompat.getColor(popupView.getContext(),R.color.VermelhoErro));
                    }else if (servico.getStatus() == Servico.CONCLUIDO){
                        textViewStatus.setText(R.string.concluido);
                        iconeStatus = ContextCompat.getDrawable(popupView.getContext(),R.drawable.check_circle_24);
                        iconeStatus.setTint(ContextCompat.getColor(popupView.getContext(),R.color.VerdeCorreto));
                    }else {
                        textViewStatus.setText(R.string.aceito);
                        iconeStatus = ContextCompat.getDrawable(popupView.getContext(),R.drawable.calendar_clock_24px);
                        iconeStatus.setTint(ContextCompat.getColor(popupView.getContext(),R.color.AzulSecundario));
                    }
                    imgStatus.setImageDrawable(iconeStatus);

                    if (!Objects.equals(servico.descricao, "")){
                        LinearLayout layoutDescricao = popupView.findViewById(R.id.layoutDescricao);
                        layoutDescricao.setVisibility(View.VISIBLE);

                        TextView textViewDescricao = layoutDescricao.findViewById(R.id.textViewDescricao);
                        textViewDescricao.setText(servico.descricao);


                        ScrollViewMaxHeight scrollViewDescricao = popupView.findViewById(R.id.scrollViewDescricao);
                        scrollViewDescricao.setMaxHeight(300);

                    }

                    Button btnConcluir = popupView.findViewById(R.id.btnConcluir);
                    Button btnCancelar = popupView.findViewById(R.id.btnCancelar);

                    String keyConversa = Conversa.gerarKeyConversa(servico.keyCliente,servico.keyPrestador);
                    int posicaoUsuario;
                    if (Objects.equals(keyPessoaUsuario, servico.keyCliente)){
                        posicaoUsuario = Conversa.gerarPosicaoPessoa(keyPessoaUsuario,servico.keyPrestador);

                        LinearLayout layoutPendente = popupView.findViewById(R.id.layoutPendente);
                        LinearLayout layoutAvaliacao = popupView.findViewById(R.id.layoutAvaliacao);




                        if (servico.status == PENDENTE){
                            layoutPendente.setVisibility(View.VISIBLE);
                            Button btnAceitar = layoutPendente.findViewById(R.id.btnAceitar);
                            btnAceitar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    atualizarStatus(keyServico, ACEITO);
                                    layoutPendente.setVisibility(View.GONE);
                                    Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.tipoServico.toLowerCase() + " foi aceito.",posicaoUsuario);
                                    mensagemInformativa.enviar(keyConversa,keyServico, btnAceitar.getContext());

                                    servico.agendarNotificacao(popupView.getContext(),keyServico,true);
                                }
                            });

                            Button btnRecusar = layoutPendente.findViewById(R.id.btnRecusar);
                            btnRecusar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    atualizarStatus(keyServico, CANCELADO);
                                    layoutPendente.setVisibility(View.GONE);
                                    Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.tipoServico.toLowerCase() + " foi recusado.",posicaoUsuario);
                                    mensagemInformativa.enviar(keyConversa,keyServico, btnRecusar.getContext());
                                }
                            });



                        }else if (servico.status == CONCLUIDO || (servico.status == CANCELADO && servico.horarioFinalizacao>servico.horarioPrevisto)){
                            layoutAvaliacao.setVisibility(View.VISIBLE);

                            RatingBar ratingBar = popupView.findViewById(R.id.ratingAvaliacao);
                            TextView textViewAvaliacao = popupView.findViewById(R.id.textViewAvaliacao);

                            if (servico.avaliacao!=-1){
                                ratingBar.setRating((float)servico.avaliacao);
                                ratingBar.setIsIndicator(true);

                                textViewAvaliacao.setText(R.string.avaliacao_do_servico);
                            }else{
                                Button btnAvaliar = popupView.findViewById(R.id.btnAvaliar);
                                btnAvaliar.setVisibility(View.VISIBLE);
                                btnAvaliar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Prestador.avaliar(servico.keyPrestador,keyServico,ratingBar.getRating());

                                        btnAvaliar.setVisibility(View.GONE);

                                        ratingBar.setRating((float)servico.avaliacao);
                                        ratingBar.setIsIndicator(true);

                                        textViewAvaliacao.setText(R.string.avaliacao_do_servico);
                                    }
                                });
                            }

                        }
                    }else {
                        posicaoUsuario = Conversa.gerarPosicaoPessoa(keyPessoaUsuario,servico.keyCliente);

                        if (servico.status==ACEITO && servico.getHorarioPrevisto()<Horario.getHorarioAtual()){
                            btnConcluir.setVisibility(View.VISIBLE);
                            btnConcluir.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    atualizarStatus(keyServico,CONCLUIDO);
                                    btnConcluir.setVisibility(View.GONE);
                                    btnCancelar.setVisibility(View.GONE);
                                    Prestador.incrementarNumServicosPrestados(keyPessoaUsuario);
                                    Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.tipoServico.toLowerCase() + " foi concluido. Toque em \"Ver Mais\" para avaliá-lo.",posicaoUsuario);
                                    mensagemInformativa.enviar(keyConversa,keyServico, btnConcluir.getContext());
                                }
                            });
                        }

                    }


                    if (servico.status==ACEITO || (servico.status==PENDENTE && keyPessoaUsuario.equals(servico.keyPrestador))){
                        btnCancelar.setVisibility(View.VISIBLE);
                        btnCancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                atualizarStatus(keyServico,CANCELADO);
                                btnConcluir.setVisibility(View.GONE);
                                btnCancelar.setVisibility(View.GONE);
                                Mensagem mensagemInformativa = new Mensagem("O serviço de " + servico.tipoServico.toLowerCase() + " foi cancelado.",posicaoUsuario);
                                mensagemInformativa.enviar(keyConversa,keyServico, btnConcluir.getContext());

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro ao preencher PopupVerMais: " + error);
            }
        };
        queryVerMais.addValueEventListener(listenerVerMais);
    }

    public void agendarNotificacao(Context context, String keyServico,boolean agendamentoCliente){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, ReceiverLembreteServico.class);

        if (agendamentoCliente){
            alarmIntent.putExtra("notificacaoText","Você tem um serviço de " + tipoServico.toLowerCase() + " contratado para as " + new SimpleDateFormat("HH:mm", Locale.ROOT).format(horarioPrevisto));
        }else {
            alarmIntent.putExtra("notificacaoText","Você tem um serviço de " + tipoServico.toLowerCase() + " para ser realizado as " + new SimpleDateFormat("HH:mm", Locale.ROOT).format(horarioPrevisto));
        }
        alarmIntent.putExtra("keyServico",keyServico);

        int requestCodeNotificacao = WorkerLembreteServico.contadorNotificacao;
        WorkerLembreteServico.contadorNotificacao--;
        if (WorkerLembreteServico.contadorNotificacao==-10000){
            WorkerLembreteServico.contadorNotificacao=-1;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCodeNotificacao, alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, Horario.adicionarTempo(horarioPrevisto, Calendar.HOUR,-1), pendingIntent);

        if (agendamentoCliente){
            Pessoa.obterReferencia().child(keyPrestador).child("tokenFCM").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String tokenFCM = snapshot.getValue(String.class);
                    if (tokenFCM!=null){
                        FirebaseMessage.agendarNotificacao(tokenFCM,keyServico,context);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar tokenFCM para agendar notificação: " + error);
                }
            });
        }
    }

    public static void enviarFirebaseMessage(String keyConversa,int posicaoRemetente, String keyServico, Context context){
        String keyPessoaRemetente,keyPessoaDestinatario;
        String[] KeyConversaSeparada = keyConversa.split(" ");
        if (posicaoRemetente==0){
            keyPessoaRemetente = KeyConversaSeparada[0];
            keyPessoaDestinatario = KeyConversaSeparada[1];
        }else{
            keyPessoaDestinatario = KeyConversaSeparada[0];
            keyPessoaRemetente = KeyConversaSeparada[1];
        }



        Query queryDestinatario = Pessoa.obterReferencia().child(keyPessoaDestinatario).child("tokenFCM");
        queryDestinatario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(String.class)!=null){
                    String tokenFCMDestinatario = snapshot.getValue(String.class);
                    Query queryRemetente = Pessoa.obterReferencia().child(keyPessoaRemetente);
                    queryRemetente.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue(Pessoa.class)!=null){
                                Pessoa remetente = snapshot.getValue(Pessoa.class);
                                if (remetente!=null && !remetente.getTokenFCM().equals("")){
                                    Query queryServico = referencia.child(keyServico);
                                    queryServico.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue(Servico.class)!=null){
                                                Servico servico = snapshot.getValue(Servico.class);
                                                if (servico!=null){
                                                    String body = "solicitou um agendamento de um serviço de " + servico.getTipoServico().toLowerCase()  + " para dia " + new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(servico.getHorarioPrevisto()) + " às " + new SimpleDateFormat("HH:mm", Locale.ROOT).format(servico.getHorarioPrevisto()) + ", deseja aceitar?";
                                                    FirebaseMessage.enviar(remetente.nome+" "+remetente.sobrenome,body,tokenFCMDestinatario,posicaoRemetente,keyConversa, remetente.isFotoPadrao(), keyServico,context);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar servico para enviar FirebaseMessage: " + error);
                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar remetente para enviar FirebaseMessage: " + error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar tokenFCM do destinatario para enviar FirebaseMessage: " + error);
            }
        });

    }

    public static int obterIcone(Context context,String tipoServico){
        switch (tipoServico){
            case "Pedreiro":
                return R.drawable.servico_pedreiro;
            case "Encanador":
                return R.drawable.servico_encanador;
            case "Eletricista":
                return R.drawable.servico_eletricista;
            case "Manutenção de Computadores":
                return R.drawable.servico_manutencao_computadores;
            case "Costureiro":
                return R.drawable.servico_costureiro;
            case "Soldador":
                return R.drawable.servico_soldador;
            case "Mecânico":
                return R.drawable.servico_mecanico;
            case "Funileiro":
                return R.drawable.servico_funileiro;
            case "Borracheiro":
                return R.drawable.servico_borracheiro;
            case "Vidraceiro":
                return R.drawable.servico_vidraceiro;
            case "Manutenção de Eletrodomésticos":
                return R.drawable.servico_manutencao_eletrodomesticos;
            case "Manutenção de Ar-Condicionado":
                return R.drawable.servico_manutencao_ar_condicionado;
            case "Fotógrafo":
                return R.drawable.servico_fotografo;
            case "Editor de Mídias":
                return R.drawable.servico_editor_midias;
            case "Designer Digital":
                return R.drawable.servico_designer_digital;
            case "Designer de Interiores":
                return R.drawable.servico_designer_interiores;
            case "Guia Turístico":
                return R.drawable.servico_guia_turistico;
            case "Segurança":
                return R.drawable.servico_seguranca;
            case "Faxineiro":
                return R.drawable.servico_faxineiro;
            case "Jardineiro":
                return R.drawable.servico_jardineiro;
            case "Podador":
                return R.drawable.servico_podador;
            case "Professor de Linguagens":
                return R.drawable.servico_professor_linguagens;
            case "Professor de Exatas":
                return R.drawable.servico_professor_exatas;
            case "Professor de Humanas":
                return R.drawable.servico_professor_humanas;
            case "Professor de Tecnologia":
                return R.drawable.servico_professor_tecnologia;
            case "Professor Outros":
                return R.drawable.servico_professor_outros;
            case "Animador de Festa":
                return R.drawable.servico_animador_festa;
            case "Garçom":
                return R.drawable.servico_garcom;
            case "Manutenção de Telefonia":
                return R.drawable.servico_manutencao_telefonia;
            case "Manutenção de Bicicletas":
                return R.drawable.servico_manutencao_bicicletas;
            case "Montador de Móveis":
                return R.drawable.servico_montador_moveis;
            case "Cabeleireiro":
                return R.drawable.servico_cabeleireiro;
            case "Maquiador":
                return R.drawable.servico_maquiador;
            case "Manicure":
                return R.drawable.servico_manicure;
            case "Pedicure":
                return R.drawable.servico_pedicure;
            case "Massagista":
                return R.drawable.servico_massagista;
            case "Cozinheiro":
                return R.drawable.servico_cozinheiro;
            case "Babá":
                return R.drawable.servico_baba;
            case "Carpinteiro":
                return R.drawable.servico_carpinteiro;
            case "Arquiteto":
                return R.drawable.servico_arquiteto;
            case "Pintor":
                return R.drawable.servico_pintor;
            case "Manutenção de Celulares":
                return R.drawable.servico_manutencao_celulares;
            default:
                return R.drawable.settings_24;
        }
    }

    public static void destruirListener(){
        if (queryAgenda!=null){
            queryAgenda.removeEventListener(listenerAgenda);
        }
        if (queryVerMais!=null){
            queryVerMais.removeEventListener(listenerVerMais);
        }
    }


    public String getKeyPrestador() {
        return keyPrestador;
    }

    public void setKeyPrestador(String keyPrestador) {
        this.keyPrestador = keyPrestador;
    }

    public String getKeyCliente() {
        return keyCliente;
    }

    public void setKeyCliente(String keyCliente) {
        this.keyCliente = keyCliente;
    }

    public long getHorarioPrevisto() {
        return horarioPrevisto;
    }

    public void setHorarioPrevisto(long horarioPrevisto) {
        this.horarioPrevisto = horarioPrevisto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoServico() {
        return tipoServico;
    }

    public long getHorarioFinalizacao() {
        return horarioFinalizacao;
    }

    public void setHorarioFinalizacao(long horarioFinalizacao) {
        this.horarioFinalizacao = horarioFinalizacao;
    }

    public double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public void setTipoServico(String tipoServico) {
        this.tipoServico = tipoServico;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static DatabaseReference obterReferencia(){
        return referencia;
    }
}

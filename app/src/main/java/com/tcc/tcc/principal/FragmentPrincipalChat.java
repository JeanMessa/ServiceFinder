package com.tcc.tcc.principal;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tcc.tcc.ActivityPerfil;
import com.tcc.tcc.R;
import com.tcc.tcc.adapter.AdapterChat;
import com.tcc.tcc.classe.models.Conversa;
import com.tcc.tcc.classe.models.Mensagem;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Prestador;
import com.tcc.tcc.classe.models.Servico;
import com.tcc.tcc.classe.utils.Arquivo;
import com.tcc.tcc.classe.utils.Horario;
import com.tcc.tcc.popup.PopupPergunta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FragmentPrincipalChat extends Fragment {

    private String keyPessoaUsuario, keyPessoaChat;

    private TextView textViewNomeChat;
    private ImageView imgFotoPerfilChat, imgPopupImagem;
    private RecyclerView listChat;
    private AdapterChat adapterChat;
    private TextView textViewNomeArquivo, textViewTamanhoArquivo;
    private LinearLayout layoutExtras,layoutAudio,layoutGravandoAudio,layoutArquivo;
    private CardView cardViewMensagem;
    private EditText txtMensagem, txtMensagemPopup;

    private ImageButton btnEnviar, btnAudio, btnGaleria, btnCamera, btnAgendarServico, btnArquivo, btnCancelarArquivo, btnEsconderExtras, btnMostrarExtras;

    private ImageButton btnVoltar,btnFecharPopup, btnEnviarPopup;

    File audioTemp;
    Chronometer chronometerAudio;
    private Button btnCancelarAudio;

    MediaRecorder mediaRecorder;
    private static final int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;

    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

    PopupWindow popupImagem,popupChatServico;
    private ByteArrayOutputStream bytesImagem;

    String nomeArquivo;

    Uri uriArquivo;
    private ViewGroup parent;

    private static String keyConversa;

    public static FragmentPrincipalChat newInstance(String keyPessoaUsuario, String keyPessoaChat) {
        FragmentPrincipalChat fragment = new FragmentPrincipalChat();
        Bundle args = new Bundle();
        args.putString("keyPessoaUsuario", keyPessoaUsuario);
        args.putString("keyPessoaChat", keyPessoaChat);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parent = container;
        return inflater.inflate(R.layout.fragment_principal_chat, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            keyPessoaUsuario = getArguments().getString("keyPessoaUsuario");
            keyPessoaChat = getArguments().getString("keyPessoaChat");
            btnVoltar = view.findViewById(R.id.btnVoltar);
            btnVoltar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   ActivityPrincipal activity = (ActivityPrincipal)getActivity();
                   activity.trocarTab(1);
                }
            });


            textViewNomeChat = view.findViewById(R.id.textViewNomeChat);
            imgFotoPerfilChat = view.findViewById(R.id.imgFotoPerfilChat);

            Pessoa.preencherDadosPessoa(keyPessoaChat, imgFotoPerfilChat, textViewNomeChat, getActivity());

            layoutExtras = view.findViewById(R.id.layoutExtras);
            LinearLayout layoutMensagem = view.findViewById(R.id.layoutMensagem);
            LinearLayout layoutBloqueado = view.findViewById(R.id.layoutBloqueado);

            Pessoa.verificaBloqueadoChat(keyPessoaChat,keyPessoaUsuario,layoutExtras,layoutMensagem,layoutBloqueado);

            Context context = getContext();

            if (context!=null && getActivity()!=null){

                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View popupViewOpcoes = inflater.inflate(R.layout.popup_opcoes_chat,(ViewGroup) getActivity().getWindow().getDecorView().getRootView(),false);
                PopupWindow popupOpcoes = new PopupWindow(popupViewOpcoes, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                ImageButton btnOpcoes = view.findViewById(R.id.btnMaisOpcoes);
                btnOpcoes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity()!=null && getActivity().getClass()== ActivityPrincipal.class) {
                            popupOpcoes.showAsDropDown(btnOpcoes,0,15);
                        }
                    }
                });


                Button btnPerfil = popupViewOpcoes.findViewById(R.id.btnPerfil);
                Prestador.trocarVisibilidadePrestador(btnPerfil,keyPessoaUsuario);
                btnPerfil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ActivityPerfil.class);
                        intent.putExtra("keyPessoa",keyPessoaChat);
                        intent.putExtra("keyPessoaUsuario",keyPessoaUsuario);
                        popupOpcoes.dismiss();
                        v.getContext().startActivity(intent);
                    }
                });


                PopupPergunta popupPerguntaBloquear = new PopupPergunta("Se você bloquear essa pessoa não poderá mais vê-la em nenhuma tela do aplicativo, ao menos que desbloquei ela nas configurações.\nTem certeza que deseja bloquear esta pessoa?",parent);
                popupPerguntaBloquear.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        ActivityPrincipal activityPrincipal = (ActivityPrincipal)getActivity();
                        if (activityPrincipal!=null){
                            activityPrincipal.trocarEscurecer(View.GONE);
                        }
                    }
                });


                popupPerguntaBloquear.setBtnConfirmarListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Pessoa.addBloqueados(keyPessoaChat,keyPessoaUsuario);
                        popupPerguntaBloquear.getPopup().dismiss();
                        popupOpcoes.dismiss();
                        btnVoltar.callOnClick();
                    }
                });

                Button btnBloquear = popupViewOpcoes.findViewById(R.id.btnBloquear);
                btnBloquear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(popupPerguntaBloquear.show("confirmacaoBloquear",context)){
                            ActivityPrincipal activityPrincipal = (ActivityPrincipal)getActivity();
                            if (activityPrincipal!=null){
                                activityPrincipal.trocarEscurecer(View.VISIBLE);
                            }
                        }
                    }
                });


            }



            keyConversa = Conversa.gerarKeyConversa(keyPessoaUsuario,keyPessoaChat);
            int posicaoUsuario = Conversa.gerarPosicaoPessoa(keyPessoaUsuario,keyPessoaChat);


            listChat = view.findViewById(R.id.listChat);
            listChat.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true));

            adapterChat = new AdapterChat(listChat, keyConversa, posicaoUsuario);


            btnEsconderExtras = view.findViewById(R.id.btnEsconderExtras);
            btnMostrarExtras = view.findViewById(R.id.btnMostrarExtras);


            layoutAudio = view.findViewById(R.id.layoutAudio);
            layoutGravandoAudio = view.findViewById(R.id.layoutGravandoAudio);


            cardViewMensagem = view.findViewById(R.id.cardViewMensagem);
            txtMensagem = view.findViewById(R.id.txtMensagem);

            LinearLayout layoutCarregamento = view.findViewById(R.id.layoutCarregamento);

            btnEnviar = view.findViewById(R.id.btnEnviar);
            btnEnviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (layoutCarregamento.getVisibility()==View.GONE){
                        if (layoutGravandoAudio.getVisibility() == View.GONE) {
                            String texto = txtMensagem.getText().toString();
                            Mensagem mensagem = new Mensagem(texto, posicaoUsuario);
                            if (uriArquivo == null) {

                                if (!texto.equals("")) {
                                    mensagem.enviar(keyConversa, layoutCarregamento);
                                    txtMensagem.setText("");
                                }


                            } else {
                                mensagem.enviar(keyConversa, layoutCarregamento, uriArquivo, nomeArquivo);
                                uriArquivo = null;
                                TransitionManager.beginDelayedTransition(layoutArquivo, new Slide());
                                layoutArquivo.setVisibility(View.GONE);
                                layoutExtras.setVisibility(View.VISIBLE);
                                txtMensagem.setText("");
                            }

                        } else {
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            chronometerAudio.stop();
                            long duracaoAudio = SystemClock.elapsedRealtime() - chronometerAudio.getBase();
                            Mensagem mensagem = new Mensagem(duracaoAudio,posicaoUsuario);
                            mensagem.enviarAudio(keyConversa, Uri.fromFile(audioTemp), layoutCarregamento);

                            cardViewMensagem.setVisibility(View.VISIBLE);
                            layoutGravandoAudio.setVisibility(View.GONE);
                            layoutExtras.setVisibility(View.VISIBLE);
                            layoutAudio.setVisibility(View.GONE);

                        }
                    }else{
                        Toast.makeText(getContext(), "Aguarde o envio da mensagem anterior", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            btnEsconderExtras.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransitionManager.beginDelayedTransition(layoutExtras, new Slide());
                    layoutExtras.setVisibility(View.GONE);

                    TransitionManager.beginDelayedTransition((ViewGroup) btnMostrarExtras.getParent(), new Slide(Gravity.TOP).setDuration(100));
                    btnMostrarExtras.setVisibility(View.VISIBLE);
                }
            });

            btnMostrarExtras.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransitionManager.beginDelayedTransition(layoutExtras, new Slide());
                    layoutExtras.setVisibility(View.VISIBLE);

                    TransitionManager.beginDelayedTransition((ViewGroup) btnMostrarExtras.getParent(), new Slide(Gravity.TOP).setDuration(100));
                    btnMostrarExtras.setVisibility(View.GONE);
                }
            });

            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupImagemView = inflater.inflate(R.layout.popup_chat_imagem, parent, false);

            popupImagem = new PopupWindow(popupImagemView, WRAP_CONTENT, WRAP_CONTENT, true);
            popupImagem.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


            btnFecharPopup = popupImagemView.findViewById(R.id.btnFechar);
            btnFecharPopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupImagem.dismiss();
                }
            });

            imgPopupImagem = popupImagemView.findViewById(R.id.imgMensagem);
            txtMensagemPopup = popupImagemView.findViewById(R.id.txtMensagem);


            btnEnviarPopup = popupImagemView.findViewById(R.id.btnEnviar);
            btnEnviarPopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (layoutCarregamento.getVisibility()==View.GONE){
                        String texto = txtMensagemPopup.getText().toString();
                        Mensagem mensagem = new Mensagem(texto, posicaoUsuario);
                        popupImagem.dismiss();
                        mensagem.enviar(keyConversa, bytesImagem, layoutCarregamento);
                        txtMensagem.setText("");
                        txtMensagemPopup.setText("");
                    }else{
                        Toast.makeText(getContext(), "Aguarde o envio da mensagem anterior", Toast.LENGTH_SHORT).show();
                    }

                }
            });


            chronometerAudio = view.findViewById(R.id.chronometerAudio);

            btnAudio = view.findViewById(R.id.btnAudio);
            btnAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityResultMicrofone.launch(new String[] {
                            Manifest.permission.RECORD_AUDIO});
                }
            });

            btnCancelarAudio = view.findViewById(R.id.btnCancelarAudio);
            btnCancelarAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaRecorder.stop();
                    chronometerAudio.stop();
                    cardViewMensagem.setVisibility(View.VISIBLE);
                    layoutGravandoAudio.setVisibility(View.GONE);
                    layoutExtras.setVisibility(View.VISIBLE);
                    layoutAudio.setVisibility(View.GONE);
                }
            });


            btnGaleria = view.findViewById(R.id.btnGaleria);
            btnGaleria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    activityResultGaleria.launch(intent);
                }
            });

            btnCamera = view.findViewById(R.id.btnCamera);
            btnCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activityResultCamera.launch(intent);
                }
            });

            layoutArquivo = view.findViewById(R.id.layoutArquivo);
            textViewNomeArquivo = view.findViewById(R.id.textViewNomeArquivo);
            textViewTamanhoArquivo = view.findViewById(R.id.textViewTamanhoArquivo);

            btnArquivo = view.findViewById(R.id.btnArquivo);
            btnArquivo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    activityResultArquivo.launch(intent);
                }
            });

            btnCancelarArquivo = view.findViewById(R.id.btnCancelarArquivo);
            btnCancelarArquivo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uriArquivo = null;
                    TransitionManager.beginDelayedTransition(layoutArquivo,new Slide());
                    layoutArquivo.setVisibility(View.GONE);
                    layoutExtras.setVisibility(View.VISIBLE);
                }
            });

            View popupChatServicoView = inflater.inflate(R.layout.popup_chat_servico, parent, false);

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            popupChatServico = new PopupWindow(popupChatServicoView, MATCH_PARENT, WRAP_CONTENT, true);
            popupChatServico.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popupChatServico.setWidth(width-100);

            popupChatServico.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    ((ActivityPrincipal)getActivity()).trocarEscurecer(View.GONE);
                }
            });

            MaterialSpinner spinnerServico = popupChatServicoView.findViewById(R.id.spinnerServico);
            Prestador.preencherSpinnerServico(keyPessoaUsuario,spinnerServico);

            EditText txtData = popupChatServicoView.findViewById(R.id.txtData);

            EditText txtHora = popupChatServicoView.findViewById(R.id.txtHora);


            EditText txtDescricao = popupChatServicoView.findViewById(R.id.txtDescricao);

            Button btnSolicitarAgendamento = popupChatServicoView.findViewById(R.id.btnSolicitarAgendamento);
            btnSolicitarAgendamento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long horarioPrevisto = 0;
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm",Locale.ROOT);
                    Date date;
                    try {
                        date = (Date)formatter.parse(txtData.getText().toString()+ " " + txtHora.getText().toString());
                        if (date!=null){
                            horarioPrevisto = date.getTime();
                        }
                    }catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    if (!(horarioPrevisto<Horario.getHorarioAtual())){
                        Servico servico = new Servico(keyPessoaUsuario,keyPessoaChat,horarioPrevisto,txtDescricao.getText().toString(),spinnerServico.getText().toString());
                        String keyServico= servico.cadastrar();

                        popupChatServico.dismiss();

                        String textoMensagem = "solicitou um agendamento de um serviço de " + servico.getTipoServico().toLowerCase()  + " para dia " + txtData.getText() + " às " + txtHora.getText();

                        Mensagem mensagem = new Mensagem(textoMensagem,posicaoUsuario);
                        mensagem.enviar(keyConversa,layoutCarregamento,keyServico);
                    }else {
                        Toast.makeText(getContext(), "O horário do serviço deve ser menor que o tempo atual", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            btnAgendarServico = view.findViewById(R.id.btnAgendarServico);
            Pessoa.trocarVisibilidadePrestador(btnAgendarServico,keyPessoaUsuario);

            btnAgendarServico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),R.style.Calendario);
                    txtData.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!datePickerDialog.isShowing()){
                                datePickerDialog.getDatePicker().setMinDate(Horario.getHorarioAtual());
                                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        txtData.setText(String.format(Locale.ROOT,"%02d/%02d/%d", dayOfMonth, month+1, year));
                                    }
                                });
                                datePickerDialog.show();
                            }
                        }
                    });

                    MaterialTimePicker materialTimePicker =
                            new MaterialTimePicker.Builder().
                                    setTimeFormat(TimeFormat.CLOCK_24H)
                                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                                    .build();
                    txtHora.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (!materialTimePicker.isVisible()){
                                materialTimePicker.show(getParentFragmentManager(),null);
                                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        txtHora.setText(String.format(Locale.ROOT,"%02d:%02d", materialTimePicker.getHour(), materialTimePicker.getMinute()));
                                    }
                                });
                            }
                        }
                    });

                    ((ActivityPrincipal)getActivity()).trocarEscurecer(View.VISIBLE);
                    popupChatServico.showAtLocation(getView(), Gravity.CENTER, 0, 0);
                    txtData.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Horario.getHorarioAtual()));
                    txtHora.setText("00:00");

                    txtDescricao.setText("");

                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Mensagem.destruirListener();
        keyConversa = null;
    }

    public static String obterKeyConversa(){
        return keyConversa;
    }

    ActivityResultLauncher<Intent> activityResultGaleria = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getData()!=null){
                        Uri uri = result.getData().getData();
                        imgPopupImagem.setImageURI(uri);
                        BitmapDrawable drawable =(BitmapDrawable) imgPopupImagem.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();
                        bytesImagem = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytesImagem);
                        ((ActivityPrincipal)getActivity()).trocarEscurecer(View.VISIBLE);
                        popupImagem.showAtLocation(getView(), Gravity.CENTER, 0, 0);

                        popupImagem.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                ((ActivityPrincipal)getActivity()).trocarEscurecer(View.GONE);
                            }
                        });
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> activityResultCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()== Activity.RESULT_OK && result.getData()!=null){
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        imgPopupImagem.setImageBitmap(bitmap);
                        bytesImagem = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytesImagem);
                        ((ActivityPrincipal)getActivity()).trocarEscurecer(View.VISIBLE);
                        popupImagem.showAtLocation(getView(), Gravity.CENTER, 0, 0);

                        popupImagem.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                ((ActivityPrincipal)getActivity()).trocarEscurecer(View.GONE);
                            }
                        });
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> activityResultArquivo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getData()!=null){
                        uriArquivo = result.getData().getData();

                        Cursor returnCursor = getActivity().getContentResolver().query(uriArquivo, null, null, null, null);
                        int indiceNome = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int indiceTamanho = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();




                        nomeArquivo = returnCursor.getString(indiceNome);
                        float tamanhoArquivo = returnCursor.getLong(indiceTamanho);
                        if (tamanhoArquivo<=10485760){ //10MB

                            layoutExtras.setVisibility(View.GONE);
                            layoutArquivo.setVisibility(View.VISIBLE);

                            textViewNomeArquivo.setText(nomeArquivo);

                            textViewTamanhoArquivo.setText(Arquivo.transformarEmMedidasUsuais(tamanhoArquivo));

                        }else{
                            uriArquivo = null;
                            Toast.makeText(getContext(), "O arquivo deve conter até 10 MB", Toast.LENGTH_SHORT).show();
                        }


                    }
                }
            }
    );

    ActivityResultLauncher<String[]> activityResultMicrofone =
            registerForActivityResult(new ActivityResultContracts
                    .RequestMultiplePermissions(), result -> {
                Boolean leituraPermitida = result.getOrDefault(
                    Manifest.permission.RECORD_AUDIO, false);
                if (Boolean.TRUE.equals(leituraPermitida)){
                    cardViewMensagem.setVisibility(View.GONE);
                    layoutGravandoAudio.setVisibility(View.VISIBLE);

                    layoutExtras.setVisibility(View.GONE);
                    layoutAudio.setVisibility(View.VISIBLE);

                    mediaRecorder = new MediaRecorder();

                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

                    try {
                        audioTemp = File.createTempFile("temp",".mp3");
                        audioTemp.deleteOnExit();
                        mediaRecorder.setOutputFile(audioTemp.getPath());
                        mediaRecorder.prepare();
                        mediaRecorder.start();

                        chronometerAudio.setBase(SystemClock.elapsedRealtime());
                        chronometerAudio.start();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Toast.makeText(getContext(), "Sem permissão para gravar áudio", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onResume() {
        Pessoa.verificaBloqueadosUsuarioChat(keyPessoaUsuario,keyPessoaChat,getActivity());
        super.onResume();
    }
}
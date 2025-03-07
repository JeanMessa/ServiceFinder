package com.tcc.tcc.principal;

import static android.content.Intent.getIntent;

import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.tcc.tcc.R;
import com.tcc.tcc.adapter.AdapterAgenda;
import com.tcc.tcc.adapter.AdapterChat;
import com.tcc.tcc.classe.models.Mensagem;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Servico;
import com.tcc.tcc.classe.utils.Horario;
import com.tcc.tcc.popup.PopupVerMaisServico;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class FragmentPrincipalAgenda extends Fragment {

    private Button btnData;
    private ImageButton btnLimparData;
    private MaterialButton btnStatusServico;
    private TabLayout tabResponsavelServico;
    private LinearLayout layoutStatus;
    private CheckBox checkBoxAceito,checkBoxPendente,checkBoxConcluido,checkBoxCancelado;
    private RecyclerView listAgenda;
    private AdapterAgenda adapterAgenda;

    private boolean[] status;

    private String diaFiltrado;


    public static FragmentPrincipalAgenda newInstance(String keyPessoaUsuario) {
        FragmentPrincipalAgenda fragment = new FragmentPrincipalAgenda();
        Bundle args = new Bundle();
        args.putString("keyPessoa", keyPessoaUsuario);
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentPrincipalAgenda newInstance(String keyPessoaUsuario,String keyServico,int idNotificacao) {
        FragmentPrincipalAgenda fragment = new FragmentPrincipalAgenda();
        Bundle args = new Bundle();
        args.putString("keyPessoa", keyPessoaUsuario);
        args.putString("keyServico", keyServico);
        args.putInt("idNotificacao", idNotificacao);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_principal_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        btnData = view.findViewById(R.id.btnData);
        btnLimparData = view.findViewById(R.id.btnLimparData);
        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),R.style.Calendario);
                datePickerDialog.show();
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        diaFiltrado = String.format(Locale.ROOT,"%02d/%02d/%d", dayOfMonth, month+1, year);
                        btnData.setText(diaFiltrado);
                        btnLimparData.setVisibility(View.VISIBLE);
                        filtrarAdapterAgenda();
                    }
                });
            }
        });

        if (getActivity()!=null){
            SharedPreferences preferencesConfig = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
            if (preferencesConfig.getBoolean("diaAtualAgenda",false)){
                diaFiltrado = String.format(Locale.ROOT, new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Horario.getHorarioAtual()));
                btnData.setText(diaFiltrado);
                btnLimparData.setVisibility(View.VISIBLE);
                filtrarAdapterAgenda();
            }
        }

        btnLimparData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnData.setText(R.string.todos_os_dias);
                btnLimparData.setVisibility(View.GONE);
                diaFiltrado = null;
                filtrarAdapterAgenda();
            }
        });


        layoutStatus = view.findViewById(R.id.layoutStatus);
        btnStatusServico = view.findViewById(R.id.btnStatusServico);
        btnStatusServico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (layoutStatus.getVisibility()==View.GONE){
                    TransitionManager.beginDelayedTransition((ViewGroup) layoutStatus.getParent(), new Slide(Gravity.TOP));
                    layoutStatus.setVisibility(View.VISIBLE);
                    btnStatusServico.setIconResource(R.drawable.keyboard_arrow_up_24);
                }else {
                    TransitionManager.beginDelayedTransition((ViewGroup) layoutStatus.getParent(), new Slide(Gravity.TOP));
                    layoutStatus.setVisibility(View.GONE);
                    btnStatusServico.setIconResource(R.drawable.keyboard_arrow_down_24);
                }
            }
        });

        CompoundButton.OnCheckedChangeListener listenerCheckBox = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        };

        status = new boolean[]{true, true, false, false};

        checkBoxAceito = view.findViewById(R.id.checkBoxAceito);
        checkBoxAceito.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                status[Servico.ACEITO] = isChecked;
                atualizarCheckBox(isChecked,checkBoxAceito);
            }
        });

        checkBoxPendente = view.findViewById(R.id.checkBoxPendente);
        checkBoxPendente.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                status[Servico.PENDENTE] = isChecked;
                atualizarCheckBox(isChecked,checkBoxPendente);
            }
        });

        checkBoxConcluido = view.findViewById(R.id.checkBoxConcluido);
        checkBoxConcluido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                status[Servico.CONCLUIDO] = isChecked;
                atualizarCheckBox(isChecked,checkBoxConcluido);
            }
        });

        checkBoxCancelado = view.findViewById(R.id.checkBoxCancelado);
        checkBoxCancelado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                status[Servico.CANCELADO] = isChecked;
                atualizarCheckBox(isChecked,checkBoxCancelado);
            }
        });

        listAgenda = view.findViewById(R.id.listAgenda);
        listAgenda.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        String keyPessoa;

        if (getArguments()!=null){
            keyPessoa = getArguments().getString("keyPessoa");

            tabResponsavelServico = view.findViewById(R.id.tabResponsavelServico);

            if (keyPessoa != null) {


                TextView textViewAviso = view.findViewById(R.id.textViewAviso);

                Thread criarAdapterServico = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String relacaoServico;
                        if (tabResponsavelServico.getVisibility()==View.VISIBLE){
                            relacaoServico = "Prestador";
                        }else{
                            relacaoServico = "Cliente";
                        }
                        adapterAgenda = new AdapterAgenda(listAgenda, keyPessoa,relacaoServico,diaFiltrado,status,textViewAviso);




                    }
                });

                Pessoa.trocarVisibilidadePrestador(tabResponsavelServico,keyPessoa,criarAdapterServico);

                if (getArguments().getString("keyServico") != null) {
                    PopupVerMaisServico popupVerMaisServico = new PopupVerMaisServico(getArguments().getString("keyServico"),(ViewGroup) view,keyPessoa);
                    popupVerMaisServico.show();
                    NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
                    notificationManager.cancel(getArguments().getInt("idNotificacao"));
                }



                tabResponsavelServico.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        String relacaoServico;
                        if (tab.getPosition()==0){
                            relacaoServico = "Prestador";
                        }else{
                            relacaoServico = "Cliente";
                        }

                        adapterAgenda = new AdapterAgenda(listAgenda, keyPessoa,relacaoServico,diaFiltrado,status,textViewAviso);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
            }
        }

    }

    private void atualizarCheckBox(boolean isChecked,CheckBox checkBox){
        if (isChecked){
            checkBox.setButtonTintList(ContextCompat.getColorStateList(requireContext(),R.color.AzulPrincipal));
        }else {
            checkBox.setButtonTintList(ContextCompat.getColorStateList(requireContext(),R.color.Branco));
        }
        filtrarAdapterAgenda();
    }

    private void filtrarAdapterAgenda(){
        if (adapterAgenda!=null){

            adapterAgenda.setFiltros(diaFiltrado,status);
            adapterAgenda.filtrar();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Servico.destruirListener();
    }
}
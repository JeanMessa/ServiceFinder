package com.tcc.tcc.principal;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Cidade;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.view.ScrollListView;

import java.util.ArrayList;
import java.util.Arrays;

public class FragmentPrincipalConfiguracoes extends Fragment {


    private int ultimaCidadeSelecionada;
    private String cidadePadrao;

    public FragmentPrincipalConfiguracoes() {}

    public static FragmentPrincipalConfiguracoes newInstance(String keyPessoa) {
        FragmentPrincipalConfiguracoes fragment = new FragmentPrincipalConfiguracoes();
        Bundle args = new Bundle();
        args.putString("keyPessoa", keyPessoa);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_principal_configuracoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,null);
        final Activity activity = getActivity();
        if(activity!=null) {
            SharedPreferences.Editor editor = activity.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
            SharedPreferences preferences = activity.getSharedPreferences("config",Context.MODE_PRIVATE);

            MaterialSpinner spinnerCidade = view.findViewById(R.id.spinnerCidade);


            ArrayList<String> opcoesCidadePadrao = new ArrayList<>();
            opcoesCidadePadrao.add("Localização");
            opcoesCidadePadrao.add("Nenhuma");
            opcoesCidadePadrao.add("Escolher...");
            spinnerCidade.setItems(opcoesCidadePadrao);

            cidadePadrao = preferences.getString("cidadePadrao","Localização");
            setCidadePadraoSpinner(spinnerCidade,cidadePadrao,opcoesCidadePadrao);

            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);




            final int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;

            View popupViewCidade = inflater.inflate(R.layout.popup_cidade, (ViewGroup) view.getParent(), false);
            PopupWindow popupCidade = new PopupWindow(popupViewCidade, wrapContent, wrapContent, true);
            ListView listCidade = popupViewCidade.findViewById(R.id.listCidade);
            ArrayAdapter<String> adapterCidade = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_selectable_list_item);
            adapterCidade.notifyDataSetChanged();
            listCidade.setAdapter(adapterCidade);
            ultimaCidadeSelecionada = -1;

            ArrayList<String> listEstados = new ArrayList<>();
            listEstados.add("UF");
            listEstados.addAll(Arrays.asList(getResources().getStringArray(R.array.estados)));


            MaterialSpinner spinnerEstados = popupViewCidade.findViewById(R.id.spinnerEstado);
            spinnerEstados.setItems(listEstados);
            spinnerEstados.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    adapterCidade.clear();
                    if (spinnerEstados.getItems().get(0).equals("UF")) {
                        spinnerEstados.setItems(getResources().getStringArray(R.array.estados));
                        spinnerEstados.setSelectedIndex(position - 1);
                    }
                    TextView textViewInformacao = popupViewCidade.findViewById(R.id.textViewInformacao);
                    textViewInformacao.setVisibility(View.GONE);
                    listCidade.clearChoices();
                    listCidade.getSelector().setAlpha(0);
                    listCidade.smoothScrollToPositionFromTop(0, 0, 0);

                    int idUF = getResources().getIdentifier(item.toString(), "array", requireActivity().getPackageName());
                    String[] arrayCidades = view.getResources().getStringArray(idUF);
                    Arrays.sort(arrayCidades);
                    adapterCidade.addAll(arrayCidades);
                    EditText txtBuscar = popupViewCidade.findViewById(R.id.txtBuscar);
                    adapterCidade.getFilter().filter(txtBuscar.getText());
                    adapterCidade.notifyDataSetChanged();
                }
            });

            spinnerCidade.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                    if (!item.toString().equals("Escolher...")) {
                        if (spinnerCidade.getItems().size()>3){
                            spinnerCidade.setItems(opcoesCidadePadrao);
                            spinnerCidade.setSelectedIndex(position-1);
                        }
                        cidadePadrao = item.toString();
                        editor.putString("cidadePadrao", cidadePadrao);
                        editor.apply();
                    } else {
                        ((ActivityPrincipal) requireActivity()).trocarEscurecer(View.VISIBLE);

                        popupCidade.showAtLocation(view, Gravity.CENTER, 0, 0);
                        popupCidade.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                setCidadePadraoSpinner(spinnerCidade,cidadePadrao,opcoesCidadePadrao);
                                ((ActivityPrincipal) requireActivity()).trocarEscurecer(View.GONE);
                            }
                        });


                        ImageButton btnFechar = popupViewCidade.findViewById(R.id.btnFechar);
                        btnFechar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupCidade.dismiss();
                            }
                        });

                        EditText txtBuscar = popupViewCidade.findViewById(R.id.txtBuscar);
                        txtBuscar.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                adapterCidade.getFilter().filter(s);
                                listCidade.clearChoices();
                                ultimaCidadeSelecionada = -1;
                                listCidade.getSelector().setAlpha(0);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });


                        listCidade.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position == ultimaCidadeSelecionada) {
                                    cidadePadrao = new Cidade(adapterCidade.getItem(ultimaCidadeSelecionada),spinnerEstados.getText().toString()).toString();
                                    editor.putString("cidadePadrao", cidadePadrao);
                                    editor.apply();
                                    spinnerCidade.setSelectedIndex(0);
                                    popupCidade.dismiss();
                                } else {
                                    listCidade.getSelector().setAlpha(255);
                                    ultimaCidadeSelecionada = position;
                                }
                            }
                        });

                        MaterialButton btnSelecionar = popupViewCidade.findViewById(R.id.btnAdd);
                        btnSelecionar.setText(R.string.selecionar);
                        btnSelecionar.setIcon(null);
                        btnSelecionar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ultimaCidadeSelecionada != -1) {
                                    cidadePadrao = new Cidade(adapterCidade.getItem(ultimaCidadeSelecionada),spinnerEstados.getText().toString()).toString();
                                    editor.putString("cidadePadrao", cidadePadrao);
                                    editor.apply();
                                    spinnerCidade.setSelectedIndex(0);
                                    popupCidade.dismiss();
                                } else {
                                    Toast.makeText(view.getContext(), "Nenhuma cidade selecionada", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }
                }
            });

            MaterialSpinner spinnerTema =view.findViewById(R.id.spinnerTema);
            spinnerTema.setItems("Tema do Dispositivo","Claro","Escuro");
            String tema = preferences.getString("tema","Tema do Dispositivo");
            switch (tema){
                case "Tema do Dispositivo":
                    spinnerTema.setSelectedIndex(0);
                    break;
                case "Claro":
                    spinnerTema.setSelectedIndex(1);
                    break;
                case "Escuro":
                    spinnerTema.setSelectedIndex(2);
                    break;
            }


            spinnerTema.setText(tema);
            spinnerTema.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    editor.putString("tema",item.toString());
                    editor.apply();
                    int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    boolean temaTrocou = false;
                    switch (item.toString()){
                        case "Tema do Dispositivo":
                            activity.finish();
                            temaTrocou = true;
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                        case "Claro":
                            if (currentNightMode != Configuration.UI_MODE_NIGHT_NO){
                                activity.finish();
                                temaTrocou = true;
                            }
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case "Escuro":
                            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES){
                                activity.finish();
                                temaTrocou = true;
                            }
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                    }
                    if (temaTrocou) {
                        Intent intent = new Intent(activity, ActivityPrincipal.class);
                        intent.putExtra("config", true);
                        activity.startActivity(intent);
                    }
                }
            });

            SwitchCompat switchDiaAtualAgenda = getView().findViewById(R.id.switchDiaAtualAgenda);
            boolean teste = preferences.getBoolean("diaAtualAgenda",true);

            switchDiaAtualAgenda.setChecked(teste);
            switchDiaAtualAgenda.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("diaAtualAgenda",isChecked);
                    editor.apply();
                }
            });

            SwitchCompat switchNotificacaoAgenda = view.findViewById(R.id.switchNotificacaoAgenda);
            switchNotificacaoAgenda.setChecked(preferences.getBoolean("notificacaoAgenda",true));
            switchNotificacaoAgenda.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("notificacaoAgenda",isChecked);
                    editor.apply();
                }
            });

            SwitchCompat switchNotificacaoChat = view.findViewById(R.id.switchNotificacaoChat);
            switchNotificacaoChat.setChecked(preferences.getBoolean("notificacaoChat",true));
            switchNotificacaoChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("notificacaoChat",isChecked);
                    editor.apply();
                }
            });

            SwitchCompat switchConfirmacaoOcultar = view.findViewById(R.id.switchConfirmacaoOcultar);
            switchConfirmacaoOcultar.setChecked(preferences.getBoolean("confirmacaoOcultar",true));
            switchConfirmacaoOcultar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("confirmacaoOcultar",isChecked);
                    editor.apply();
                }
            });

            SwitchCompat switchConfirmacaoExcluir = view.findViewById(R.id.switchConfirmacaoExcluir);
            switchConfirmacaoExcluir.setChecked(preferences.getBoolean("confirmacaoExcluir",true));
            switchConfirmacaoExcluir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("confirmacaoExcluir",isChecked);
                    editor.apply();
                }
            });

            SwitchCompat switchConfirmacaoBloquear = view.findViewById(R.id.switchConfirmacaoBloquear);
            switchConfirmacaoBloquear.setChecked(preferences.getBoolean("confirmacaoBloquear",true));
            switchConfirmacaoBloquear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("confirmacaoBloquear",isChecked);
                    editor.apply();
                }
            });

            ScrollView scrollViewConfiguracoes = view.findViewById(R.id.scrollViewConfiguracoes);

            ScrollListView listBloqueados = view.findViewById(R.id.listBloqueados);

            MaterialButton btnBloqueados = view.findViewById(R.id.btnBloqueados);
            btnBloqueados.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listBloqueados.getVisibility()==View.GONE){
                        listBloqueados.setVisibility(View.VISIBLE);
                        btnBloqueados.setIconResource(R.drawable.keyboard_arrow_up_24);
                        scrollViewConfiguracoes.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollViewConfiguracoes.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });

                    }else {
                        listBloqueados.setVisibility(View.GONE);
                        btnBloqueados.setIconResource(R.drawable.keyboard_arrow_down_24);
                    }
                }
            });

            if (getArguments()!=null){
                String keyPessoa = getArguments().getString("keyPessoa");
                Pessoa.setListBloqueados(listBloqueados,btnBloqueados,keyPessoa);
            }


        }
    }

    private void setCidadePadraoSpinner(MaterialSpinner spinnerCidade,String cidadePadrao,ArrayList<String> opcoesCidadePadrao){
        switch (cidadePadrao){
            case "Localização":
                spinnerCidade.setSelectedIndex(0);
                break;
            case "Nenhuma":
                spinnerCidade.setSelectedIndex(1);
                break;
            default:
                ArrayList<String> opcoesCidadadePadraoEscolher = new ArrayList<>();
                opcoesCidadadePadraoEscolher.add(cidadePadrao);
                opcoesCidadadePadraoEscolher.addAll(opcoesCidadePadrao);
                spinnerCidade.setItems(opcoesCidadadePadraoEscolher);
        }
    }

}
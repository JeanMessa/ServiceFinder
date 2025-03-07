package com.tcc.tcc.principal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tcc.tcc.R;
import com.tcc.tcc.adapter.AdapterHomePrestador;
import com.tcc.tcc.classe.models.Cidade;
import com.tcc.tcc.classe.models.Prestador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FragmentPrincipalHome extends Fragment {

    private TabLayout tabFiltros, tabOrdenar;
    private LinearLayout layoutFiltro, layoutPesquisa, layoutOrdenar;
    private Button btnLimpar, btnServico, btnCidade;
    private ImageButton btnLimparServico, btnLimparCidade,btnLimparNome;

    private String filtroServico,filtroNome;
    private Cidade filtroCidade;
    boolean listadoSemCidadeAtual;
    private int ultimoServicoSelecionado, ultimaCidadeSelecionado;
    private LayoutInflater inflater;
    private EditText txtNome;
    private Drawable iconeOrdenarDesc, iconeOrdenarAsc, iconeFiltro;

    boolean ordemAsc;

    private ViewGroup parent;
    AdapterHomePrestador adapterHomePrestador;
    private RecyclerView listPrestador;

    private TextView textViewAviso;

    public static ArrayList<String> bloqueadosAtualizar = new ArrayList<>();

    private boolean salvarEstado;

    public static FragmentPrincipalHome newInstance(String keyPessoaUsuario) {
        FragmentPrincipalHome fragment = new FragmentPrincipalHome();
        Bundle args = new Bundle();
        args.putString("keyPessoaUsuario", keyPessoaUsuario);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parent = container;
        return inflater.inflate(R.layout.fragment_principal_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        if (getArguments()!=null) {
            salvarEstado = true;

            iconeOrdenarDesc = AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_drop_down_24);
            iconeOrdenarAsc = AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_drop_up_24);
            iconeFiltro = AppCompatResources.getDrawable(requireContext(), R.drawable.filter_alt_24);


            layoutFiltro = view.findViewById(R.id.layoutFiltro);
            layoutPesquisa = view.findViewById(R.id.layoutPesquisa);
            layoutOrdenar = view.findViewById(R.id.layoutOrdenar);

            btnServico = view.findViewById(R.id.btnServico);
            btnLimparServico = view.findViewById(R.id.btnLimparServico);

            btnCidade = view.findViewById(R.id.btnCidade);
            btnLimparCidade = view.findViewById(R.id.btnLimparCidade);

            tabOrdenar = view.findViewById(R.id.tabOrdenar);

            btnLimpar = view.findViewById(R.id.btnLimpar);

            textViewAviso = view.findViewById(R.id.textViewAviso);

            String keyPessoaUsuario = getArguments().getString("keyPessoaUsuario");

            listPrestador = view.findViewById(R.id.recyclerPrestador);

            listPrestador.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            adapterHomePrestador = new AdapterHomePrestador(listPrestador,keyPessoaUsuario);
            listPrestador.setAdapter(adapterHomePrestador);

            if (savedInstanceState != null) {
                filtroServico = savedInstanceState.getString("filtroServico");
                filtroCidade = (Cidade) savedInstanceState.getSerializable("filtroCidade");
                if (filtroServico != null) {
                    btnServico.setText(filtroServico);
                    btnLimparServico.setVisibility(View.VISIBLE);
                }
                if (filtroCidade != null && filtroCidade.getCidade() != null) {
                    btnCidade.setText(filtroCidade.toString());
                    btnLimparCidade.setVisibility(View.VISIBLE);
                }
                int tabStatePosition = savedInstanceState.getInt("tabOrdenarPosition");
                tabOrdenar.selectTab(tabOrdenar.getTabAt(tabStatePosition));
                ordemAsc = savedInstanceState.getBoolean("ordemAsc");
                if (tabStatePosition != 0) {
                    tabOrdenar.getTabAt(0).setIcon(null);
                    if (ordemAsc) {
                        tabOrdenar.getTabAt(tabStatePosition).setIcon(iconeOrdenarAsc);
                    } else {
                        tabOrdenar.getTabAt(tabStatePosition).setIcon(iconeOrdenarDesc);
                    }
                } else if (ordemAsc) {
                    tabOrdenar.getTabAt(0).setIcon(iconeOrdenarAsc);
                }
            } else {
                filtroServico = null;
                filtroCidade = new Cidade();
                filtroNome = null;
                ordemAsc = false;
                if (getActivity()!=null){
                    SharedPreferences preferences = getActivity().getSharedPreferences("config",Context.MODE_PRIVATE);
                    String cidadePadrao = preferences.getString("cidadePadrao","Localização");
                    if (cidadePadrao.equals("Localização")){
                        setCidadeAtual();
                    }else if (!cidadePadrao.equals("Nenhuma")){
                        filtroCidade.setCidadeComEstado(cidadePadrao);
                        btnCidade.setText(filtroCidade.toString());
                        btnLimparCidade.setVisibility(View.VISIBLE);
                        if (listadoSemCidadeAtual){
                            listar();
                        }
                    }
                }



            }

            tabFiltros = view.findViewById(R.id.tabFiltros);
            tabFiltros.selectTab(null);
            tabFiltros.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    tab.view.setBackgroundColor(requireContext().getColor(R.color.AzulTerciario));
                    switch (tab.getPosition()) {
                        case 0:
                            layoutFiltro.setVisibility(View.VISIBLE);
                            break;
                        case 1:
                            layoutPesquisa.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            layoutOrdenar.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    tab.view.setBackgroundColor(requireContext().getColor(R.color.AzulSecundario));
                    switch (tab.getPosition()) {
                        case 0:
                            layoutFiltro.setVisibility(View.GONE);
                            break;
                        case 1:
                            layoutPesquisa.setVisibility(View.GONE);
                            break;
                        case 2:
                            layoutOrdenar.setVisibility(View.GONE);
                            break;
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    tabFiltros.selectTab(null);
                }
            });

            btnLimpar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filtroServico = null;
                    btnServico.setText(R.string.servico);
                    btnLimparServico.setVisibility(View.GONE);
                    filtroCidade.setCidade(null);
                    filtroCidade.setUF(null);
                    btnCidade.setText(R.string.cidade);
                    btnLimparCidade.setVisibility(View.GONE);
                    txtNome.setText("");
                }
            });

            inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;

            View popupViewServico = inflater.inflate(R.layout.popup_servico, parent, false);
            PopupWindow popupServico = new PopupWindow(popupViewServico, wrapContent, wrapContent, true);
            ListView listServico = popupViewServico.findViewById(R.id.listServico);
            String[] arrayServicos = view.getResources().getStringArray(R.array.servicos);
            Arrays.sort(arrayServicos);
            ArrayAdapter<String> adapterServico = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_selectable_list_item, arrayServicos);
            adapterServico.notifyDataSetChanged();
            listServico.setAdapter(adapterServico);
            ultimoServicoSelecionado = -1;


            btnServico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ActivityPrincipal) requireActivity()).trocarEscurecer(View.VISIBLE);

                    popupServico.showAtLocation(view, Gravity.CENTER, 0, 0);
                    popupServico.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            ((ActivityPrincipal) requireActivity()).trocarEscurecer(View.GONE);
                        }
                    });


                    ImageButton btnFechar = popupViewServico.findViewById(R.id.btnFechar);
                    btnFechar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupServico.dismiss();
                        }
                    });

                    EditText txtBuscar = popupViewServico.findViewById(R.id.txtBuscar);
                    txtBuscar.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            adapterServico.getFilter().filter(s);
                            listServico.clearChoices();
                            ultimoServicoSelecionado = -1;
                            listServico.getSelector().setAlpha(0);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });


                    listServico.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == ultimoServicoSelecionado) {
                                popupServico.dismiss();
                                filtroServico = adapterServico.getItem(ultimoServicoSelecionado);
                                btnServico.setText(filtroServico);
                                listar();
                                btnLimparServico.setVisibility(View.VISIBLE);
                            } else {
                                listServico.getSelector().setAlpha(255);
                                ultimoServicoSelecionado = position;
                            }
                        }
                    });

                    MaterialButton btnAdd = popupViewServico.findViewById(R.id.btnAdd);
                    btnAdd.setText(R.string.filtrar);
                    btnAdd.setIcon(iconeFiltro);
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ultimoServicoSelecionado != -1) {
                                popupServico.dismiss();
                                filtroServico = adapterServico.getItem(ultimoServicoSelecionado);
                                btnServico.setText(filtroServico);
                                listar();
                                btnLimparServico.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(view.getContext(), "Nenhum serviço selecionado", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
            });

            btnLimparServico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnLimparServico.setVisibility(View.GONE);
                    filtroServico = null;
                    btnServico.setText(R.string.servico);
                    listar();
                }
            });

            View popupViewCidade = inflater.inflate(R.layout.popup_cidade, parent, false);
            PopupWindow popupCidade = new PopupWindow(popupViewCidade, wrapContent, wrapContent, true);
            ListView listCidade = popupViewCidade.findViewById(R.id.listCidade);
            ArrayAdapter<String> adapterCidade = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_selectable_list_item);
            adapterCidade.notifyDataSetChanged();
            listCidade.setAdapter(adapterCidade);
            ultimaCidadeSelecionado = -1;

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


            btnCidade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ActivityPrincipal) requireActivity()).trocarEscurecer(View.VISIBLE);

                    popupCidade.showAtLocation(view, Gravity.CENTER, 0, 0);
                    popupCidade.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
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
                            ultimaCidadeSelecionado = -1;
                            listCidade.getSelector().setAlpha(0);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });


                    listCidade.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == ultimaCidadeSelecionado) {
                                popupCidade.dismiss();
                                filtroCidade.setCidade(adapterCidade.getItem(position));
                                filtroCidade.setUF(spinnerEstados.getText().toString());
                                btnCidade.setText(filtroCidade.toString());
                                listar();
                                btnLimparCidade.setVisibility(View.VISIBLE);
                            } else {
                                listCidade.getSelector().setAlpha(255);
                                ultimaCidadeSelecionado = position;
                            }
                        }
                    });

                    MaterialButton btnAdd = popupViewCidade.findViewById(R.id.btnAdd);
                    btnAdd.setText(R.string.filtrar);
                    btnAdd.setIcon(iconeFiltro);
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ultimaCidadeSelecionado != -1) {
                                popupCidade.dismiss();
                                filtroCidade.setCidade(adapterCidade.getItem(ultimaCidadeSelecionado));
                                filtroCidade.setUF(spinnerEstados.getText().toString());
                                btnCidade.setText(filtroCidade.toString());

                                listar();
                                btnLimparCidade.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(view.getContext(), "Nenhuma cidade selecionada", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
            });


            btnLimparCidade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnLimparCidade.setVisibility(View.GONE);
                    filtroCidade.setCidade(null);
                    filtroCidade.setUF(null);
                    btnCidade.setText(R.string.cidade);
                    listar();
                }
            });

            btnLimparNome = view.findViewById(R.id.btnLimparNome);
            btnLimparNome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtNome.setText("");
                }
            });

            txtNome = view.findViewById(R.id.txtNome);
            txtNome.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtroNome = txtNome.getText().toString();
                    if (!filtroNome.isEmpty()) {
                        btnLimparNome.setVisibility(View.VISIBLE);
                    } else {
                        filtroNome = null;
                        btnLimparNome.setVisibility(View.GONE);
                    }
                    listar();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            tabOrdenar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() != 2) {
                        tab.setIcon(iconeOrdenarDesc);
                        ordemAsc = false;
                    } else {
                        tab.setIcon(iconeOrdenarAsc);
                        ordemAsc = true;
                    }
                    ordenar();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    tab.setIcon(null);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (tab.getIcon() == iconeOrdenarAsc) {
                        tab.setIcon(iconeOrdenarDesc);
                        ordemAsc = false;
                    } else {
                        tab.setIcon(iconeOrdenarAsc);
                        ordemAsc = true;
                    }
                    ordenar();
                }
            });


            listadoSemCidadeAtual = false;
            new Thread(new Runnable() {

                public void run() {
                    boolean esperandoDados;

                    do {
                        esperandoDados = adapterHomePrestador.isMapPrestadorOriginalNull();
                    } while (esperandoDados);
                    if (adapterHomePrestador.getItemCount() == 0) {
                        textViewAviso.setVisibility(View.VISIBLE);
                    }
                    if (filtroServico != null || filtroCidade.getCidade() != null || filtroNome != null || tabOrdenar.getSelectedTabPosition() != 0 || ordemAsc) {
                        listar();
                    } else {
                        adapterHomePrestador.ordenarAvaliacao(false);
                        listadoSemCidadeAtual = true;
                    }
                }
            }).start();
        }
    }


    public void setCidadeAtual(){
        ActivityResultLauncher<String[]> pedirPermissaoLocalizacao =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean localizacaoExataPermitida = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean localizacaoAproximadaPermitida = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if ((localizacaoExataPermitida != null && localizacaoExataPermitida) || (localizacaoAproximadaPermitida != null && localizacaoAproximadaPermitida)) {
                                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                    FusedLocationProviderClient fusedLocationClient;
                                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
                                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            if (location != null) {
                                                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                                                List<Address> enderecos;
                                                try {
                                                    enderecos = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                if (enderecos.get(0).getCountryName().equals("Brasil") ){
                                                    String cidade = enderecos.get(0).getSubAdminArea();
                                                    String uf = enderecos.get(0).getAdminArea();
                                                    uf = Cidade.obterSigla(uf);
                                                    if (uf!=null){
                                                        int idUF = getResources().getIdentifier(uf,"array", requireActivity().getPackageName());
                                                        String[] arrayCidades = getResources().getStringArray(idUF);
                                                        if(Arrays.binarySearch(arrayCidades,cidade)>=0){
                                                            filtroCidade.setCidade(cidade);
                                                            filtroCidade.setUF(uf);
                                                            btnCidade.setText(filtroCidade.toString());
                                                            btnLimparCidade.setVisibility(View.VISIBLE);
                                                            if (listadoSemCidadeAtual){
                                                                listar();
                                                            }
                                                        };
                                                    }
                                                }



                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Location", "onFailure: " + e);
                                        }
                                    });
                                }
                            }
                        }

                );
        pedirPermissaoLocalizacao.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        



    }

    public void listar(){
        if(adapterHomePrestador!=null && !adapterHomePrestador.isMapPrestadorOriginalNull()){
            adapterHomePrestador.filtrar(filtroNome,filtroServico,filtroCidade);
            ordenar();
            if(adapterHomePrestador.getItemCount()==0){
                textViewAviso.setVisibility(View.VISIBLE);
            }else {
                textViewAviso.setVisibility(View.GONE);
            }
            if(filtroNome == null && filtroServico == null && filtroCidade.getCidade() == null ){
                btnLimpar.setVisibility(View.GONE);
            }else {
                btnLimpar.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void ordenar(){
        switch (tabOrdenar.getSelectedTabPosition()){
            case 0:
                adapterHomePrestador.ordenarAvaliacao(ordemAsc);
                break;
            case 1:
                adapterHomePrestador.ordenarNumServicos(ordemAsc);
                break;
            case 2:
                adapterHomePrestador.ordenarNome(ordemAsc);
                break;
        }
        adapterHomePrestador.notifyDataSetChanged();

    }
    public void setSalvarEstado(boolean salvarEstado) {
        this.salvarEstado = salvarEstado;
    }

    public void removeFromAdapter(String keyPessoaBloqueada){
        if (adapterHomePrestador!=null){
            adapterHomePrestador.removeMapPrestador(keyPessoaBloqueada);
        }
    }

    @Override
    public void onResume() {
        verificarAtualizacoesBloqueados();
        super.onResume();
    }

    public void verificarAtualizacoesBloqueados(){
        if (bloqueadosAtualizar.size()>0){
            for (String bloqueado: bloqueadosAtualizar) {
                removeFromAdapter(bloqueado);
            }
            adapterHomePrestador.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (salvarEstado){
            outState.putString("filtroServico",filtroServico);
            outState.putSerializable("filtroCidade",filtroCidade);
            outState.putInt("tabOrdenarPosition",tabOrdenar.getSelectedTabPosition());
            outState.putBoolean("ordemAsc",ordemAsc);
        }
        super.onSaveInstanceState(outState);
    }
}
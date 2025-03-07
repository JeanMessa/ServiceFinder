package com.tcc.tcc.cadastro;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tcc.tcc.R;
import com.tcc.tcc.view.ScrollListView;
import com.tcc.tcc.adapter.AdapterCadastroPrestador;
import com.tcc.tcc.classe.models.Cidade;
import com.tcc.tcc.classe.models.Prestador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class FragmentCadastroPrestador extends Fragment {
    private RadioGroup radioGroupTipoUsuario;
    private RadioButton radioPrestador;
    private LinearLayout formularioPrestador;
    private Button btnAddServico,btnAddCidade,btnContinuar;

    private PopupWindow popupServico,popupCidade;

    private ScrollListView listServico,listCidade;
    private ListView listPopupServico,listPopupCidade;

    int ultimoServicoSelecionado,ultimaCidadeSelecionada;

    private AdapterCadastroPrestador adapterCidade,adapterServico;
    private ArrayAdapter<String> adapterPopupServico,adapterPopupCidade;

    private LayoutInflater inflater;

    private ViewGroup parent;

    private ArrayList<String> arrayListCidade;

    private MaterialSpinner spinnerEstados;

    private Map<String,ArrayList<String>> mapEstados;

    private String ultimoUF;

    private OnDataPass dataPasser;

    public FragmentCadastroPrestador() {
    }

    public static FragmentCadastroPrestador newInstance(Prestador prestador) {
        FragmentCadastroPrestador fragment = new  FragmentCadastroPrestador();
        Bundle args = new Bundle();
        args.putStringArrayList("servicos",prestador.getServicos());
        args.putSerializable("cidades",prestador.getCidades());
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
        parent = container;
        return inflater.inflate(R.layout.fragment_cadastro_prestador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);

        radioGroupTipoUsuario = view.findViewById(R.id.radioGroupTipoUsuario);
        radioPrestador = radioGroupTipoUsuario.findViewById(R.id.radioPrestador);
        formularioPrestador = view.findViewById(R.id.formularioPrestador);

        radioGroupTipoUsuario.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioPrestador){
                    formularioPrestador.setVisibility(View.VISIBLE);
                }else{
                    formularioPrestador.setVisibility(View.GONE);
                }
            }
        });

        int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;

        inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        listServico = view.findViewById(R.id.listServico);
        adapterServico = new AdapterCadastroPrestador(view.getContext(), R.layout.item_cadastro_prestador){
            @Override
            public void remove(@Nullable String object) {
                super.remove(object);
                listServico.atualizarHeight(10);
            }
        };
        adapterServico.notifyDataSetChanged();
        listServico.setAdapter(adapterServico);

        View popupViewServico = inflater.inflate(R.layout.popup_servico,parent,false);


        popupServico = new PopupWindow(popupViewServico, wrapContent, wrapContent, true);

        listPopupServico = popupViewServico.findViewById(R.id.listServico);
        String[] arrayServicos = view.getResources().getStringArray(R.array.servicos);
        Arrays.sort(arrayServicos);
        ArrayList<String> arrayListServico = new ArrayList<>(Arrays.asList(arrayServicos));
        adapterPopupServico = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_selectable_list_item,arrayListServico);

        btnAddServico = view.findViewById(R.id.btnAddServico);

        configList(listServico,adapterServico,listPopupServico,btnAddServico,popupServico,popupViewServico,adapterPopupServico);

        listCidade = view.findViewById(R.id.listCidade);
        adapterCidade = new AdapterCadastroPrestador(view.getContext(), R.layout.item_cadastro_prestador){
            @Override
            public void remove(@Nullable String object) {
                super.remove(object);
                listCidade.atualizarHeight(10);
            }
        };
        adapterCidade.notifyDataSetChanged();
        listCidade.setAdapter(adapterCidade);

        View popupViewCidade = inflater.inflate(R.layout.popup_cidade,parent,false);

        popupCidade = new PopupWindow(popupViewCidade, wrapContent, wrapContent, true);

        listPopupCidade = popupViewCidade.findViewById(R.id.listCidade);


        ultimoUF = "UF";

        btnAddCidade = view.findViewById(R.id.btnAddCidade);
        mapEstados = new HashMap<>();
        spinnerEstados = popupViewCidade.findViewById(R.id.spinnerEstado);
        ArrayList<String> listEstados= new ArrayList<>();
        listEstados.add("UF");
        listEstados.addAll(Arrays.asList(getResources().getStringArray(R.array.estados)));
        spinnerEstados.setItems(listEstados);
        adapterPopupCidade = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_selectable_list_item);
        spinnerEstados.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                boolean existe = false;

                if(!mapEstados.isEmpty()){
                    for (String uf: mapEstados.keySet()) {
                        if (uf.equals(item.toString())){
                            existe = true;

                        }
                    }
                }
                adapterPopupCidade.clear();
                if (!ultimoUF.equals("UF")){
                    mapEstados.put(ultimoUF,arrayListCidade);

                }else {
                    spinnerEstados.setItems(getResources().getStringArray(R.array.estados));
                    spinnerEstados.setSelectedIndex(position-1);
                    TextView textViewInformacao = popupViewCidade.findViewById(R.id.textViewInformacao);
                    textViewInformacao.setVisibility(View.GONE);
                }
                if (!existe){
                    int idUF = getResources().getIdentifier(item.toString(),"array", requireActivity().getPackageName());
                    String[] arrayCidades = view.getResources().getStringArray(idUF);
                    Arrays.sort(arrayCidades);
                    arrayListCidade = new ArrayList<>(Arrays.asList(arrayCidades));
                    mapEstados.put(item.toString(),arrayListCidade);

                }else{
                    arrayListCidade = mapEstados.get(item.toString());
                }
                ultimaCidadeSelecionada = -1;
                listPopupCidade.clearChoices();
                listPopupCidade.getSelector().setAlpha(0);
                listPopupCidade.smoothScrollToPositionFromTop(0,0,0);
                adapterPopupCidade.addAll(arrayListCidade);
                EditText txtBuscar = popupViewCidade.findViewById(R.id.txtBuscar);
                adapterPopupCidade.getFilter().filter(txtBuscar.getText());
                adapterPopupCidade.notifyDataSetChanged();


                ultimoUF = item.toString();
            }

        });

        configList(listCidade,adapterCidade,listPopupCidade,btnAddCidade,popupCidade,popupViewCidade,adapterPopupCidade);

        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissaoContinuar(view)){
                    ((ActivityCadastro)(getActivity())).trocarTab(3);
                }
            }
        });

        if (getArguments() != null) {
            if(getArguments().getStringArrayList("servicos")!=null){
                radioPrestador.setChecked(true);
                for (String item : Objects.requireNonNull(requireArguments().getStringArrayList("servicos"))){
                    adapterServico.add(item);
                    adapterPopupServico.remove(item);
                }
                adapterServico.notifyDataSetChanged();
                adapterPopupServico.notifyDataSetChanged();
                ArrayList<Cidade> arrayCidade = (ArrayList<Cidade>) requireArguments().getSerializable("cidades");
                if (arrayCidade != null) {
                    for (Cidade item : arrayCidade){
                        adapterCidade.add(item.toString());
                        ArrayList<String>arrayListCidadeTemp = new ArrayList<>();
                        if(mapEstados.get(item.getUF()) ==null){
                            int idUF = getResources().getIdentifier(item.getUF(),"array", requireActivity().getPackageName());
                            String[] stringArrayCidades = view.getResources().getStringArray(idUF);
                            Arrays.sort(stringArrayCidades);
                            arrayListCidadeTemp.addAll(Arrays.asList(stringArrayCidades));

                        }else{
                            arrayListCidadeTemp = mapEstados.get(item.getUF());
                        }
                        arrayListCidadeTemp.remove(item.getCidade());
                        mapEstados.put(item.getUF(),arrayListCidadeTemp);
                    }
                }
                adapterCidade.notifyDataSetChanged();

                listServico.atualizarHeight(10);
                listCidade.atualizarHeight(10);
            }

        }

    }

    public interface OnDataPass {
        public void onDataPassPrestador();
        public void onDataPassPrestador(ArrayList<String> servicos, ArrayList<Cidade> cidades);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public int getUltimoSelecionado(int listID){
        if(listID == listPopupServico.getId()){
            return ultimoServicoSelecionado;
        }else if(listID == listPopupCidade.getId()){
            return ultimaCidadeSelecionada;
        }
        return -1;
    }

    public void setUltimoSelecionado(int listID, int valor){
        if(listID == listPopupServico.getId()){
            ultimoServicoSelecionado = valor;
        }else if(listID == listPopupCidade.getId()){
            ultimaCidadeSelecionada = valor;
        }
    }

    public void configList(ScrollListView list, AdapterCadastroPrestador adapter, ListView listPopup, Button btnAdd, PopupWindow popupWindow, View popupView, ArrayAdapter<String> adapterPopup){
        listPopup.setAdapter(adapterPopup);

        listPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopup.getSelector().setAlpha(255);
                if (position == getUltimoSelecionado(listPopup.getId())){
                    addList(list,adapter,listPopup,adapterPopup,popupWindow);
                }else{
                    setUltimoSelecionado(listPopup.getId(),position);
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean listAlterada = false;
                ((ActivityCadastro)getActivity()).trocarEscurecer(View.VISIBLE);
                while (adapter.getBufferRemovidos().size()>0) {
                    String item = adapter.getPrimeiroBufferRemovidos();
                    if (listPopup.getId()==listPopupCidade.getId()){
                        Cidade cidade = new Cidade();
                        cidade.setCidadeComEstado(item);

                        if (cidade.getUF().equals(spinnerEstados.getText().toString())){
                            adapterPopup.add(cidade.getCidade());
                        }else{
                            ArrayList<String> arrayListCidadeTemp = mapEstados.get(cidade.getUF());
                            arrayListCidadeTemp.add(cidade.getCidade());
                            Collections.sort(arrayListCidadeTemp);
                            mapEstados.replace(cidade.getUF(),arrayListCidadeTemp);
                        }
                    }else{
                        adapterPopup.add(item);
                    }

                    adapter.removeBufferRemovidos(item);
                    listAlterada = true;
                }

                if (listAlterada){
                    adapterPopup.sort(new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o1.compareTo(o2);
                        }
                    });
                    adapterPopup.notifyDataSetChanged();
                }

                setUltimoSelecionado(listPopup.getId(),-1);
                adapterPopup.getFilter().filter("");
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });

        ImageButton btnFechar = popupView.findViewById(R.id.btnFechar);
        btnFechar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        EditText txtBuscar = popupView.findViewById(R.id.txtBuscar);
        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterPopup.getFilter().filter(s);
                listPopup.clearChoices();
                setUltimoSelecionado(listPopup.getId(),-1);
                listPopup.getSelector().setAlpha(0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button btnPopupAdd = popupView.findViewById(R.id.btnAdd);
        btnPopupAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addList(list,adapter,listPopup,adapterPopup,popupWindow);
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                txtBuscar.setText("");
                ((ActivityCadastro)getActivity()).trocarEscurecer(View.GONE);
            }
        });
    }

    public void addList(ScrollListView list, AdapterCadastroPrestador adapter, ListView listPopup, ArrayAdapter<String> adapterPopup, PopupWindow popupWindow){
        if (listPopup.getCheckedItemPosition()!=-1){
            int position = listPopup.getCheckedItemPosition();
            String item = listPopup.getAdapter().getItem(position).toString();
            adapterPopup.remove(item);
            if (listPopup.getId()==listPopupCidade.getId()){
                arrayListCidade.remove(item);
                Cidade cidade = new Cidade(item,spinnerEstados.getText().toString());
                item = cidade.toString();
            }
            adapter.add(item);
            listPopup.getSelector().setAlpha(0);
            listPopup.clearChoices();
            adapter.notifyDataSetChanged();
            list.atualizarHeight(10);
            popupWindow.dismiss();

        }else {
            Toast.makeText(getView().getContext(), "Primeiro selecione o item que deseja adicionar", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean permissaoContinuar(View view){
        if (radioGroupTipoUsuario.getCheckedRadioButtonId() == R.id.radioCliente){
            dataPasser.onDataPassPrestador();
            return true;
        } else if (listServico.getCount()>0 && listCidade.getCount()>0) {
            ArrayList<Cidade> arrayCidade = new ArrayList<>();
            for (int i = 0;i<adapterCidade.getCount();i++){
                Cidade cidade = new Cidade();
                cidade.setCidadeComEstado(adapterCidade.getItem(i));
                arrayCidade.add(cidade);
            }
            dataPasser.onDataPassPrestador(adapterServico.getAll(),arrayCidade);
            return true;
        } else {
            if(listServico.getCount()==0 && listCidade.getCount()==0){
                Toast.makeText(view.getContext(), "Para se tornar um prestador insira pelo menos um serviço e uma cidade", Toast.LENGTH_LONG).show();
            }else if(listServico.getCount()==0){
                Toast.makeText(view.getContext(), "Para se tornar um prestador insira pelo menos um serviço prestado", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(view.getContext(), "Para se tornar um prestador insira pelo menos uma cidade de antedimento", Toast.LENGTH_LONG).show();
            }
            return false;
        }

    }

}
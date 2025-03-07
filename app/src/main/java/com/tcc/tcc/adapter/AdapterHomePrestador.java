package com.tcc.tcc.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tcc.tcc.ActivityPerfil;
import com.tcc.tcc.ActivityPerfilMeuPerfil;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Cidade;
import com.tcc.tcc.classe.models.Prestador;
import com.tcc.tcc.classe.utils.Imagem;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdapterHomePrestador extends RecyclerView.Adapter<AdapterHomePrestador.ViewHolder>
implements Serializable {

    LinkedHashMap<String,Prestador> mapPrestador,mapPrestadorOriginal;

    String keyPessoaUsuario;


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


    public AdapterHomePrestador(RecyclerView listPrestador,String keyPessoaUsuario) {
        Prestador.preencherAdapter(this,listPrestador,keyPessoaUsuario);
        this.keyPessoaUsuario = keyPessoaUsuario;
    }

    public void setMapPrestador(LinkedHashMap<String, Prestador> mapPrestador) {
        this.mapPrestador = mapPrestador;
        mapPrestadorOriginal = new LinkedHashMap<>(mapPrestador);
    }

    public  boolean isMapPrestadorOriginalNull() {
        if (mapPrestadorOriginal==null){
            return true;
        }else {
            return false;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_home_prestador, viewGroup, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        View view = viewHolder.getView();
        String key = getKeyatPosition(position);
        Prestador prestador = mapPrestador.get(key);

        if(prestador!=null){

            ImageView imageView = view.findViewById(R.id.imgFotoPerfil);
            if(!mapPrestador.get(key).isFotoPadrao()){
                Imagem.download(imageView,key+".jpg",Prestador.obterReferenciaStorage());
            }else{
                imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(),R.drawable.person_24));
            }

            TextView textViewNome = view.findViewById(R.id.textViewNome);
            textViewNome.setText(String.format("%s %s", prestador.getNome(), prestador.getSobrenome()));

            ImageView[] imgAvaliacao = new ImageView[5];
            imgAvaliacao[0] = view.findViewById(R.id.imgEstrela1);
            imgAvaliacao[1] = view.findViewById(R.id.imgEstrela2);
            imgAvaliacao[2] = view.findViewById(R.id.imgEstrela3);
            imgAvaliacao[3] = view.findViewById(R.id.imgEstrela4);
            imgAvaliacao[4] = view.findViewById(R.id.imgEstrela5);

            TextView textViewAvaliacao = view.findViewById(R.id.textViewAvaliacao);


            if(prestador.getNumAvaliacoes()>0){

                Drawable estrelaCheia = ContextCompat.getDrawable(view.getContext(),R.drawable.star_rate_24);
                Drawable estrelaMetade = ContextCompat.getDrawable(view.getContext(),R.drawable.star_half_24);
                Drawable estrelaVazia = ContextCompat.getDrawable(view.getContext(),R.drawable.star_border_24);

                BigDecimal avaliacaoArredondada = BigDecimal.valueOf(prestador.getAvaliacao()).setScale(1, RoundingMode.HALF_EVEN);

                for (int i=0;i<5;i++){
                    imgAvaliacao[i].setVisibility(View.VISIBLE);
                    if(avaliacaoArredondada.doubleValue()>=i+1){
                        imgAvaliacao[i].setImageDrawable(estrelaCheia);
                    }else if(avaliacaoArredondada.doubleValue() >= i + 0.5){
                        imgAvaliacao[i].setImageDrawable(estrelaMetade);
                    }else{
                        imgAvaliacao[i].setImageDrawable(estrelaVazia);
                    }
                }

                textViewAvaliacao.setText(String.format(Locale.getDefault(),"%.1f",avaliacaoArredondada.doubleValue()));

            }else {
                for (ImageView estrela : imgAvaliacao) {
                    estrela.setVisibility(View.GONE);
                }
                textViewAvaliacao.setText(R.string.nao_avaliado);
            }

            TextView textViewNumServicos = view.findViewById(R.id.textViewNumServicos);
            textViewNumServicos.setText(String.valueOf(prestador.getNumServicos()));
            TextView textViewServicosPrestados = view.findViewById(R.id.textViewServicosPrestados);
            if(prestador.getNumServicos()==1){
                textViewServicosPrestados.setText(view.getContext().getString(R.string.servico_prestado));
            }else {
                textViewServicosPrestados.setText(view.getContext().getString(R.string.servicos_prestados));
            }

            ImageButton btnChat = view.findViewById(R.id.btnChat);

            if (!Objects.equals(keyPessoaUsuario, key)){
                btnChat.setVisibility(View.VISIBLE);
                btnChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityPrincipal activityPrincipal = (ActivityPrincipal) v.getContext();
                        activityPrincipal.setKeyPessoaChat(key);
                    }
                });
            }else {
                btnChat.setVisibility(View.GONE);
            }



            ImageButton btnPerfil = view.findViewById(R.id.btnPerfil);


            btnPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Objects.equals(keyPessoaUsuario, key)) {
                        Intent intent = new Intent(v.getContext(), ActivityPerfil.class);
                        intent.putExtra("keyPessoa", key);
                        intent.putExtra("keyPessoaUsuario", keyPessoaUsuario);
                        v.getContext().startActivity(intent);
                    }else {
                        Intent intent = new Intent(v.getContext(), ActivityPerfilMeuPerfil.class);
                        v.getContext().startActivity(intent);
                    }
                }
            });

            LinearLayout layoutServicos = view.findViewById(R.id.layoutServicos);
            layoutServicos.removeAllViews();


            for (String servico : prestador.getServicos()) {
                View viewServico = LayoutInflater.from(view.getContext())
                        .inflate(R.layout.item_home_prestador_servico,layoutServicos,false);
                TextView textViewServico = viewServico.findViewById(R.id.textViewServico);
                textViewServico.setText(servico);
                layoutServicos.addView(viewServico);
            }
        }
    }

    public String getKeyatPosition(int position){
        return mapPrestador.keySet().toArray()[position].toString();
    }


    public void filtrar(String nome, String servico, Cidade cidade){
        if (mapPrestador!=null){
            mapPrestador.clear();
            mapPrestador.putAll(mapPrestadorOriginal);
            for (int i = 0;i<mapPrestador.size();i++) {
                String key = getKeyatPosition(i);
                Prestador prestador = mapPrestador.get(key);
                if((nome!=null && !prestador.getNome().concat(" " + prestador.getSobrenome()).toLowerCase().contains(nome.toLowerCase()))
                        || (servico!=null && !prestador.getServicos().contains(servico))){
                    mapPrestador.remove(key);
                    i--;
                }else if (cidade.getCidade()!=null){
                    boolean removerPrestador = true;
                    for (Cidade itemCidade:prestador.getCidades()){

                        if (itemCidade.getCidade().equals(cidade.getCidade()) && itemCidade.getUF().equals(cidade.getUF())){
                            removerPrestador = false;
                        }
                    }
                    if (removerPrestador){
                        mapPrestador.remove(key);
                        i--;
                    }
                }
            }
        }



    }

    public void ordenarAvaliacao(boolean ordemAsc){
        if(mapPrestador !=null){
            Comparator<LinkedHashMap.Entry<String, Prestador>> valueComparator = new Comparator<LinkedHashMap.Entry<String, Prestador>>() {
                @Override
                public int compare(Map.Entry<String, Prestador> o1, Map.Entry<String, Prestador> o2) {
                    if (ordemAsc){
                        return comparePeso(o1,o2,5,-3,1);
                    }else{
                        return comparePeso(o1,o2,-5,-3,1);
                    }
                }


            };

            mapPrestador= mapPrestador.entrySet().stream().sorted(valueComparator).
                    collect(Collectors.toMap(LinkedHashMap.Entry::getKey, LinkedHashMap.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));
        }

    }
    

    public void ordenarNumServicos(boolean ordemAsc){
        Comparator<LinkedHashMap.Entry<String, Prestador>> valueComparator = new Comparator<LinkedHashMap.Entry<String, Prestador>>() {
            @Override
            public int compare(Map.Entry<String, Prestador> o1, Map.Entry<String, Prestador> o2) {
                if (ordemAsc){
                    return comparePeso(o1,o2,-3,5,1);
                }else{
                    return comparePeso(o1,o2,-3,-5,1);
                }
            }
        };

        mapPrestador= mapPrestador.entrySet().stream().sorted(valueComparator).
                collect(Collectors.toMap(LinkedHashMap.Entry::getKey, LinkedHashMap.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    public void ordenarNome(boolean ordemAsc){
        Comparator<LinkedHashMap.Entry<String, Prestador>> valueComparator = new Comparator<LinkedHashMap.Entry<String, Prestador>>() {
            @Override
            public int compare(Map.Entry<String, Prestador> o1, Map.Entry<String, Prestador> o2) {
                if (ordemAsc){
                    return comparePeso(o1,o2,-3,-1,5);
                }else{
                    return comparePeso(o1,o2,-3,-1,-5);
                }
            }


        };

        mapPrestador= mapPrestador.entrySet().stream().sorted(valueComparator).
                collect(Collectors.toMap(LinkedHashMap.Entry::getKey, LinkedHashMap.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    public int comparePeso(Map.Entry<String, Prestador> o1, Map.Entry<String, Prestador> o2,int pesoAvaliacao,int pesoNumServicos,int pesoNome) {
        return pesoAvaliacao * Double.compare(o1.getValue().getAvaliacao(),o2.getValue().getAvaliacao())
                + pesoNumServicos * Integer.compare(o1.getValue().getNumServicos(),o2.getValue().getNumServicos())
                + pesoNome * Integer.compare(o1.getValue().getNome().concat(" " + o1.getValue().getSobrenome()).toLowerCase().compareTo(o2.getValue().getNome().concat(" " + o2.getValue().getSobrenome()).toLowerCase()),0);
    }


    @Override
    public int getItemCount() {
        if (mapPrestador!=null){
            return mapPrestador.size();
        }else {
            return 0;
        }
    }

    public void removeMapPrestador(String keyPessoaBloqueada){
        if (mapPrestador!=null) {
            mapPrestador.remove(keyPessoaBloqueada);
            mapPrestadorOriginal.remove(keyPessoaBloqueada);
        }
    }
}
package com.tcc.tcc.classe.models;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tcc.tcc.R;
import com.tcc.tcc.view.ScrollListView;
import com.tcc.tcc.adapter.AdapterHomePrestador;
import com.tcc.tcc.classe.utils.Imagem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class Prestador extends Pessoa{
    private double avaliacao;
    private int numAvaliacoes,numServicos;
    private ArrayList<String> servicos;
    private ArrayList<Cidade> cidades;

    public Prestador(){
        super();
        prestador = true;
        avaliacao = 4;
        numAvaliacoes = 0;
        numServicos = 0;
    }
    public void setPessoa(Pessoa pessoa){
        nome = pessoa.nome;
        sobrenome = pessoa.sobrenome;
        cpf = pessoa.cpf;
        fotoPadrao = pessoa.isFotoPadrao();
    }
    public double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public int getNumAvaliacoes() {
        return numAvaliacoes;
    }

    public void setNumAvaliacoes(int numAvaliacoes) {
        this.numAvaliacoes = numAvaliacoes;
    }

    public int getNumServicos() {
        return numServicos;
    }

    public void setNumServicos(int numServicos) {
        this.numServicos = numServicos;
    }

    public ArrayList<String> getServicos() {
        return servicos;
    }

    public void setServicos(ArrayList<String> servicos) {
        this.servicos = servicos;
    }

    public ArrayList<Cidade> getCidades() {
        return cidades;
    }

    public void setCidades(ArrayList<Cidade> cidades) {
        this.cidades = cidades;
    }

    public ArrayList<String> cidadesToString() {
        ArrayList<String> arrayCidades = new ArrayList<>();
        for (Cidade cidade : cidades){
            arrayCidades.add(cidade.toString());
        }
        return arrayCidades;
    }

    public static void preencherAdapter(AdapterHomePrestador adapterHomePrestador, RecyclerView listViewPrestador,String keyPessoaUsuario){
        Query queryUsuario = referencia.child(keyPessoaUsuario).child("bloqueados");
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> bloqueados;
                if (snapshot.getValue()!=null){
                    bloqueados = (ArrayList<String>) snapshot.getValue();
                }else {
                    bloqueados = new ArrayList<String>();
                }
                Query query = referencia.orderByChild("prestador").equalTo(true);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        LinkedHashMap<String,Prestador> mapPrestador = new LinkedHashMap<>();
                        if (snapshot.getChildrenCount() > 0) {
                            for(DataSnapshot prestador : snapshot.getChildren()){

                                if (prestador.child("status").getValue(Integer.class).equals(1)
                                && !bloqueados.contains(prestador.getKey())){
                                    mapPrestador.put(prestador.getKey(),prestador.getValue(Prestador.class));
                                }
                            }
                        }
                        adapterHomePrestador.setMapPrestador(mapPrestador);
                        listViewPrestador.setAdapter(adapterHomePrestador);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("ERRO_FIREBASE", "Erro ao preencher adapter de prestador: " + error);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar bloqueados para preencher adapter de prestador: " + error);
            }
        });

    }

    public static void preencherDadosPessoa(String keyPessoa, ImageView imgFotoPerfil, TextView textViewNome, LinearLayout layoutPrestador, Activity activity){

        Query queryPessoa = Pessoa.obterReferencia().child(keyPessoa);
        queryPessoa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    Prestador prestador = snapshot.getValue(Prestador.class);
                    if (prestador!=null){
                        if(!prestador.fotoPadrao){
                            Imagem.download(imgFotoPerfil,keyPessoa+".jpg",referenciaStorage);
                        }
                        textViewNome.setText(String.format("%s %s", prestador.nome, prestador.sobrenome));

                        prestador.preencherDadosPrestadorPerfil(layoutPrestador);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity, "Problema ao encontrar o perfil do prestador prestador", Toast.LENGTH_SHORT).show();
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar pessoa: " + error);
                activity.finish();
            }
        });
    }
    public void preencherDadosPrestadorPerfil(LinearLayout layoutPrestador){
        layoutPrestador.setVisibility(View.VISIBLE);

        TextView textViewNumServicos = layoutPrestador.findViewById(R.id.textViewNumServicos);
        textViewNumServicos.setText(String.valueOf(numServicos));

        TextView textViewNumAvaliacoes = layoutPrestador.findViewById(R.id.textViewNumAvaliacoes);
        textViewNumAvaliacoes.setText(String.valueOf(numAvaliacoes));

        ImageView[] imgAvaliacao = new ImageView[5];
        imgAvaliacao[0] = layoutPrestador.findViewById(R.id.imgEstrela1);
        imgAvaliacao[1] = layoutPrestador.findViewById(R.id.imgEstrela2);
        imgAvaliacao[2] = layoutPrestador.findViewById(R.id.imgEstrela3);
        imgAvaliacao[3] = layoutPrestador.findViewById(R.id.imgEstrela4);
        imgAvaliacao[4] = layoutPrestador.findViewById(R.id.imgEstrela5);

        TextView textViewAvaliacao = layoutPrestador.findViewById(R.id.textViewAvaliacao);

        if(numAvaliacoes>0){

            Drawable estrelaCheia = ContextCompat.getDrawable(layoutPrestador.getContext(),R.drawable.star_rate_24);
            Drawable estrelaMetade = ContextCompat.getDrawable(layoutPrestador.getContext(),R.drawable.star_half_24);

            BigDecimal avaliacaoArredondada = BigDecimal.valueOf(avaliacao).setScale(1, RoundingMode.HALF_EVEN);

            for (int i=0;i<5;i++){
                imgAvaliacao[i].setVisibility(View.VISIBLE);
                if(avaliacaoArredondada.doubleValue()>=i+1){
                    imgAvaliacao[i].setImageDrawable(estrelaCheia);
                }else if(avaliacaoArredondada.doubleValue() >= i + 0.5){
                    imgAvaliacao[i].setImageDrawable(estrelaMetade);
                }
            }

            textViewAvaliacao.setText(String.format(Locale.getDefault(),"%.1f",avaliacaoArredondada.doubleValue()));

        }else {
            for (ImageView estrela : imgAvaliacao) {
                estrela.setVisibility(View.GONE);
            }
            textViewAvaliacao.setText(R.string.nao_avaliado);
        }

        ScrollListView listServico;
        listServico = layoutPrestador.findViewById(R.id.listServico);
        ArrayAdapter<String> adapterServico = new ArrayAdapter<>(layoutPrestador.getContext(), R.layout.item_simples,R.id.textViewSimples,servicos);
        listServico.setAdapter(adapterServico);
        listServico.atualizarHeight(0);

        ScrollListView listCidade;
        listCidade = layoutPrestador.findViewById(R.id.listCidade);
        ArrayAdapter<String> adapterCidade = new ArrayAdapter<>(layoutPrestador.getContext(), R.layout.item_simples,R.id.textViewSimples, cidadesToString());
        listCidade.setAdapter(adapterCidade);
        listCidade.atualizarHeight(0);
    }

    public static void preencherSpinnerServico(String keyPessoa, MaterialSpinner spinner){

        Query query = Pessoa.obterReferencia().child(keyPessoa).child("servicos");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    ArrayList<String> servicos = new ArrayList<>();
                    for(DataSnapshot item: snapshot.getChildren()){
                        servicos.add(item.getValue().toString());
                    }
                    spinner.setItems(servicos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar serviços do prestador " + keyPessoa + ": " + error);
            }
        });
    }

    public static void incrementarNumServicosPrestados(String keyPessoa){
        Query query = Pessoa.obterReferencia().child(keyPessoa).child("numServicos");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot!=null)
                {
                    int numServico;
                    if (snapshot.getValue()!=null){
                        numServico = snapshot.getValue(Integer.class);
                    }else {
                        numServico = 0;
                    }
                        numServico++;
                        Pessoa.obterReferencia().child(keyPessoa).child("numServicos").setValue(numServico);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar serviços do prestador " + keyPessoa + ": " + error);
            }
        });
    }

    public static void avaliar(String keyPessoa, String keyServico, double nota){
        Query query = Pessoa.obterReferencia().child(keyPessoa);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Prestador prestador = snapshot.getValue(Prestador.class);
                prestador.setNumAvaliacoes(prestador.numAvaliacoes+1);
                double avaliacaoTotal = (prestador.avaliacao*(prestador.numAvaliacoes))+nota;
                prestador.avaliacao = avaliacaoTotal/(prestador.numAvaliacoes+1);
                Pessoa.obterReferencia().child(keyPessoa).setValue(prestador);
                Servico.obterReferencia().child(keyServico).child("avaliacao").setValue(nota);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("ERRO_FIREBASE", "Erro na tentativa de recuperar serviços do prestador " + keyPessoa + ": " + error);
            }
        });
    }

    public void atualizarCidadeServico(String keyPessoa){
        referencia.child(keyPessoa).child("servicos").setValue(servicos);
        referencia.child(keyPessoa).child("cidades").setValue(cidades);
    }


}

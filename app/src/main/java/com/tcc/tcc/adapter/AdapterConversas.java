package com.tcc.tcc.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tcc.tcc.ActivityPerfil;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Conversa;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.utils.Imagem;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.util.ArrayList;

public class AdapterConversas extends RecyclerView.Adapter<AdapterConversas.ViewHolder> {

    private ArrayList<Conversa> arrayListConversa, arrayListConversaOriginal;
    private String keyPessoaUsuario;

    private String filtroNome;

    RecyclerView listConversas;
    TextView textViewAviso;

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

    public AdapterConversas(RecyclerView listConversas,String keyPessoaUsuario,TextView textViewAviso){
        this.keyPessoaUsuario = keyPessoaUsuario;
        Conversa.preencherAdapter(this,listConversas,textViewAviso);
        arrayListConversaOriginal = new ArrayList<>();
        this.listConversas = listConversas;
        this.textViewAviso = textViewAviso;
    }

    public void setArrayListConversa(ArrayList<Conversa> arrayListConversa) {
        this.arrayListConversa = arrayListConversa;
        arrayListConversaOriginal.clear();
        arrayListConversaOriginal.addAll(arrayListConversa);
    }

    public String getKeyPessoaUsuario() {
        return keyPessoaUsuario;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_conversas, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        View view = viewHolder.getView();
        Conversa conversa = arrayListConversa.get(position);

        if (conversa!=null){

            ImageView imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
            if (conversa.getPessoaChat()!=null){
                if (!conversa.getPessoaChat().isFotoPadrao()){
                    Imagem.download(imgFotoPerfil,conversa.getKeyPessoaChat()+".jpg", Pessoa.obterReferenciaStorage());
                }else{
                    imgFotoPerfil.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.person_24));
                }
                TextView textViewNome = view.findViewById(R.id.textViewNome);
                textViewNome.setText(String.format("%s %s", conversa.getPessoaChat().getNome(), conversa.getPessoaChat().getSobrenome()));
            }


            CardView cardViewNaoLidas = view.findViewById(R.id.cardViewNaoLidas);
            if (conversa.getNumNaoLidas()>0){
                cardViewNaoLidas.setVisibility(View.VISIBLE);
                TextView textViewNaoLidas = view.findViewById(R.id.textViewNaoLidas);
                textViewNaoLidas.setText(String.valueOf(conversa.getNumNaoLidas()));
            }else{
                cardViewNaoLidas.setVisibility(View.GONE);
            }


            ImageButton btnChat = view.findViewById(R.id.btnChat);
            btnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityPrincipal activity = (ActivityPrincipal)view.getContext();
                    activity.setKeyPessoaChat(conversa.getKeyPessoaChat());
                    activity.trocarTab(1);
                }
            });

            ImageButton btnPerfil = view.findViewById(R.id.btnPerfil);
            if (conversa.getPessoaChat()!=null && conversa.getPessoaChat().isPrestador()) {
                btnPerfil.setVisibility(View.VISIBLE);
                btnPerfil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ActivityPerfil.class);
                        intent.putExtra("keyPessoa", conversa.getKeyPessoaChat());
                        intent.putExtra("keyPessoaUsuario",keyPessoaUsuario);
                        v.getContext().startActivity(intent);

                    }
                });
            }else{
                btnPerfil.setVisibility(View.GONE);
            }
        }
    }

    public void filtrar(@Nullable String nome){
        if (nome==null){
            nome = filtroNome;
        }else {
            filtroNome = nome;
        }
        if (arrayListConversa!=null){

            arrayListConversa.clear();
            arrayListConversa.addAll(arrayListConversaOriginal);
            notifyDataSetChanged();
            if (arrayListConversa.size()>0) {
                for (int i = 0; i < arrayListConversa.size(); i++) {
                    Pessoa pessoa = arrayListConversa.get(i).getPessoaChat();
                    if (pessoa != null && (nome != null && !pessoa.getNome().concat(" " + pessoa.getSobrenome()).toLowerCase().contains(nome.toLowerCase()))) {
                        arrayListConversa.remove(i);
                        notifyItemRemoved(i);
                        i--;
                    }
                }
            }
            if (nome!=null && !nome.equals("") && arrayListConversa.size()==0){
                listConversas.setVisibility(View.GONE);
                textViewAviso.setText(R.string.nenhum_contato_disponivel_nesta_pesquisa);
                textViewAviso.setVisibility(View.VISIBLE);
            }else if (arrayListConversa.size()>0){
                listConversas.setVisibility(View.VISIBLE);
            }else{
                listConversas.setVisibility(View.GONE);
                textViewAviso.setText(R.string.voce_nao_possui_nenhum_contato_inicie_conversas_primeiro);
                textViewAviso.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public int getItemCount() {
        if (arrayListConversa!=null){
            return arrayListConversa.size();
        }else {
            return 0;
        }
    }
}

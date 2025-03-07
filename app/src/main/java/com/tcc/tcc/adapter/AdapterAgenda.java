package com.tcc.tcc.adapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Servico;
import com.tcc.tcc.popup.PopupVerMaisServico;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;

public class AdapterAgenda extends RecyclerView.Adapter<AdapterAgenda.ViewHolder>{
    private LinkedHashMap<String, Servico> mapServico,mapServicoOriginal;
    private String keyPessoaUsuario;

    private String dia;
    private boolean[] status;

    private String relacaoServico;

    private TextView textViewAviso;
    private RecyclerView listAgenda;


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

    public AdapterAgenda(RecyclerView listAgenda,String keyPessoaUsuario, String relacaoServico,@Nullable String dia,boolean[] status,TextView textViewAviso){
        this.keyPessoaUsuario = keyPessoaUsuario;
        this.dia = dia;
        this.status = status;
        this.relacaoServico = relacaoServico;
        Servico.preencherAdapter(this,listAgenda,relacaoServico);
        this.listAgenda = listAgenda;
        this.textViewAviso = textViewAviso;

    }

    public void setMapServico(LinkedHashMap<String, Servico> mapServico) {
        this.mapServico = mapServico;
        mapServicoOriginal = new LinkedHashMap<>(mapServico);
    }

    public String getKeyPessoaUsuario() {
        return keyPessoaUsuario;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_agenda, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        View view = viewHolder.getView();

        String key = getKeyatPosition(position);
        Servico servico = mapServicoOriginal.get(key);


        if (servico!=null){

            ImageView imgIconeServico = view.findViewById(R.id.imgIconeServico);
            imgIconeServico.setImageResource(Servico.obterIcone(imgIconeServico.getContext(),servico.getTipoServico()));



            TextView textViewServico = view.findViewById(R.id.textViewServico);
            textViewServico.setText(servico.getTipoServico());

            TextView textViewNome = view.findViewById(R.id.textViewNome);
            textViewNome.setText("");
            if (relacaoServico.equals("Prestador")){
                Pessoa.AdicionarNomeEmTextView(servico.getKeyCliente(),textViewNome,null);
            }else{
                Pessoa.AdicionarNomeEmTextView(servico.getKeyPrestador(),textViewNome,null);
            }

            TextView textViewHorario = view.findViewById(R.id.textViewHorario);
            textViewHorario.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(servico.getHorarioPrevisto()));

            ImageView imgStatus = view.findViewById(R.id.imgStatus);
            Drawable iconeStatus;
            if (servico.getStatus()==Servico.PENDENTE){
                 iconeStatus = ContextCompat.getDrawable(view.getContext(),R.drawable.pending_24);
                 iconeStatus.setTint(ContextCompat.getColor(view.getContext(),R.color.AmareloAlerta));
            }else if (servico.getStatus()==Servico.CANCELADO){
                iconeStatus = ContextCompat.getDrawable(view.getContext(),R.drawable.cancel_24);
                iconeStatus.setTint(ContextCompat.getColor(view.getContext(),R.color.VermelhoErro));
            }else if (servico.getStatus() == Servico.CONCLUIDO){
                iconeStatus = ContextCompat.getDrawable(view.getContext(),R.drawable.check_circle_24);
                iconeStatus.setTint(ContextCompat.getColor(view.getContext(),R.color.VerdeCorreto));
            }else {
                iconeStatus = ContextCompat.getDrawable(view.getContext(),R.drawable.calendar_clock_24px);
                iconeStatus.setTint(ContextCompat.getColor(view.getContext(),R.color.AzulSecundario));
            }
            imgStatus.setImageDrawable(iconeStatus);

            Button btnVerMais = view.findViewById(R.id.btnVerServico);
            btnVerMais.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupVerMaisServico popupVerMaisServico = new PopupVerMaisServico(key,(ViewGroup) view,keyPessoaUsuario);
                    popupVerMaisServico.show();
                }
            });


        }



    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrar(){
        if (mapServico!=null){

            mapServico.clear();
            mapServico.putAll(mapServicoOriginal);

            for (int i = 0;i<mapServico.size();i++) {
                String key = getKeyatPosition(i);
                Servico servico = mapServico.get(key);

                if (servico!=null){
                    if ((dia != null && !new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(servico.getHorarioPrevisto()).equals(dia))
                            || !status[servico.getStatus()]){

                        mapServico.remove(key);
                        i--;
                    }
                }
            }
            if (mapServico.size()>0){
                listAgenda.setVisibility(View.VISIBLE);
                textViewAviso.setVisibility(View.GONE);
            }else{
                listAgenda.setVisibility(View.GONE);
                textViewAviso.setVisibility(View.VISIBLE);
            }

        }else{
            listAgenda.setVisibility(View.GONE);
            textViewAviso.setVisibility(View.VISIBLE);
        }

        notifyDataSetChanged();
    }

    public void setFiltros(@Nullable String dia,boolean[] status){
        this.dia = dia;
        this.status = status;
    }

    @Override
    public int getItemCount() {
        if (mapServico !=null){
            return mapServico.size();
        }else {
            return 0;
        }
    }

    public String getKeyatPosition(int position){
        return mapServico.keySet().toArray()[position].toString();
    }
}

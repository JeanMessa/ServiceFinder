package com.tcc.tcc.adapter;

import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tcc.tcc.R;

import java.util.ArrayList;
import java.util.Arrays;

public class AdapterCadastroPrestador extends ArrayAdapter<String> {

    private Context context;
    private int resource;

    private ArrayList<String> bufferRemovidos;

    public AdapterCadastroPrestador(Context context, int resource) {
        super(context,resource);
        this.context = context;
        this.resource = resource;
        bufferRemovidos = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position,  @Nullable View view, @NonNull ViewGroup parent) {

        if (view == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource,parent,false);
        }

        TextView textViewItem = view.findViewById(R.id.textViewItem);
        String textoItem = getItem(position);

        textViewItem.setText(textoItem);

        ImageButton btnRemoverServico = view.findViewById(R.id.btnRemover);
        btnRemoverServico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(textoItem);
                bufferRemovidos.add(textoItem);
            }
        });



        return view;
    }

    public ArrayList<String> getAll(){
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 0;i<getCount();i++){
            arrayList.add(getItem(i));
        }
        return arrayList;
    }

    public void removeBufferRemovidos(String item){
        bufferRemovidos.remove(item);
    }

    public ArrayList<String> getBufferRemovidos(){
        return bufferRemovidos;
    }

    public String getPrimeiroBufferRemovidos(){
        return bufferRemovidos.get(0);
    }
}

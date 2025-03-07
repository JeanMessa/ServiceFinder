package com.tcc.tcc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcc.tcc.classe.models.Usuario;
import com.tcc.tcc.classe.utils.Processo;

public class ActivityPerfilMeuPerfil extends AppCompatActivity {

    private String keyUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        SharedPreferences preferences = getSharedPreferences("usuarioAtivo", MODE_PRIVATE);
        keyUsuario = preferences.getString("key",null);

        if (keyUsuario != null){

            ImageButton btnVoltar = findViewById(R.id.btnVoltar);
            btnVoltar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            findViewById(R.id.layoutMeuPerfil).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutBtnsMeuPerfil).setVisibility(View.VISIBLE);

            ImageView imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
            TextView textViewNome = findViewById(R.id.textViewNome);
            TextView textViewCPF = findViewById(R.id.textViewCPF);
            TextView textViewEmail = findViewById(R.id.textViewEmail);
            TextView textViewTipo = findViewById(R.id.textViewTipo);
            LinearLayout layoutPrestador = findViewById(R.id.layoutPrestador);


            Usuario.preencherDadosMeuPerfil(keyUsuario, imgFotoPerfil, textViewNome, textViewCPF, textViewEmail, textViewTipo, layoutPrestador,this);

            Button btnSair = findViewById(R.id.btnSair);
            btnSair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Usuario.logout(ActivityPerfilMeuPerfil.this);
                }
            });

            Button btnEditar = findViewById(R.id.btnEditar);
            btnEditar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Usuario.chamarActivityCadastroEditar(v.getContext(),keyUsuario);
                }
            });

            Button btnDesativar = findViewById(R.id.btnDesativar);
            btnDesativar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityPerfilMeuPerfil.this, ActivityValidarEmail.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("processo", Processo.DESATIVAR);
                    Usuario.passarUsuarioIntent(keyUsuario,intent,bundle,"bundle",ActivityPerfilMeuPerfil.this);
                }
            });

        }else {
            Intent intent = new Intent(this, ActivityLogin.class);
            startActivity(intent);
            finishAffinity();
        }
    }
}
package com.tcc.tcc;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Servico;
import com.tcc.tcc.classe.models.Usuario;
import com.tcc.tcc.classe.utils.Senha;
import com.tcc.tcc.classe.utils.VerificaCampo;
import com.tcc.tcc.cadastro.ActivityCadastro;
import com.tcc.tcc.principal.ActivityPrincipal;

public class ActivityLogin extends AppCompatActivity {

    private EditText txtEmail,txtSenha;
    private ImageButton btnVisualizarSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcherPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        SharedPreferences preferences = getSharedPreferences("usuarioAtivo", MODE_PRIVATE);
        String keyUsuario = preferences.getString("key",null);
        SharedPreferences preferencesconfig = getSharedPreferences("config", Context.MODE_PRIVATE);
        String tema = preferencesconfig.getString("tema","Tema do Dispositivo");
        switch (tema){
            case "Claro":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Escuro":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        if (keyUsuario!=null){
            Usuario.loginAutomatico(keyUsuario,this);
        }else{
            setContentView(R.layout.activity_login);
            txtEmail = findViewById(R.id.txtEmail);
            txtSenha = findViewById(R.id.txtSenha);

            btnVisualizarSenha = findViewById(R.id.btnVisualizarSenha);
            btnVisualizarSenha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Senha.visualizarSenha(view,txtSenha,btnVisualizarSenha);
                }
            });

            Button btnEntrar = findViewById(R.id.btnEntrar);
            btnEntrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (VerificaCampo.verificaVazio(v,txtEmail,"O campo de email de ser preenchido") && VerificaCampo.verificaVazio(v,txtSenha,"O campo de senha de ser preenchido")) {
                        Usuario usuario = new Usuario();
                        usuario.setEmail(txtEmail.getText().toString().toLowerCase());
                        usuario.setSenha(txtSenha.getText().toString());
                        usuario.login(ActivityLogin.this);
                    }
                }
            });

            Button btnAlterarSenha= findViewById(R.id.btnAlterarSenha);
            btnAlterarSenha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityLogin.this, ActivityAlterarSenha.class);
                    startActivity(intent);
                }
            });

            Button btnCriarConta = findViewById(R.id.btnCriarConta);
            btnCriarConta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityLogin.this, ActivityCadastro.class);
                    startActivity(intent);
                }
            });


        }

    }

    ActivityResultLauncher<String> launcherPermissaoNotificacao = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {});


}
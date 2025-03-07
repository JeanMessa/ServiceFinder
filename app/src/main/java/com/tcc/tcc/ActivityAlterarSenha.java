package com.tcc.tcc;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tcc.tcc.classe.models.Usuario;
import com.tcc.tcc.classe.utils.Email;
import com.tcc.tcc.classe.utils.Senha;
import com.tcc.tcc.classe.utils.VerificaCampo;

public class ActivityAlterarSenha extends AppCompatActivity {

    private int etapa;

    private String keyUsuario;

    private EditText txtEmail,txtCodigo;

    private RelativeLayout layoutSenha;

    private Email email;

    private TextView textViewDescricaoEditText,textViewEmail;

    private Button btnReenviar,btnVoltarEtapa;

    ImageView imgIcone;

    private LinearLayout layoutCodigo,layoutConfirmarSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha);

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layoutCodigo = findViewById(R.id.layoutCodigo);
        textViewEmail = findViewById(R.id.textViewEmail);

        imgIcone = findViewById(R.id.imgIcone);

        textViewDescricaoEditText = findViewById(R.id.textViewDescricaoEditText);

        txtEmail = findViewById(R.id.txtEmail);
        txtCodigo = findViewById(R.id.txtCodigo);
        layoutSenha = findViewById(R.id.layoutSenha);

        layoutConfirmarSenha = findViewById(R.id.layoutConfirmarSenha);

        EditText txtSenha = findViewById(R.id.txtSenha);
        ImageButton btnVisualizarSenha = findViewById(R.id.btnVisualizarSenha);
        btnVisualizarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Senha.visualizarSenha(v,txtSenha,btnVisualizarSenha);
            }
        });

        EditText txtConfirmarSenha = findViewById(R.id.txtConfirmarSenha);
        ImageButton btnVisualizarConfirmarSenha = findViewById(R.id.btnVisualizarConfirmarSenha);
        btnVisualizarConfirmarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Senha.visualizarSenha(v,txtConfirmarSenha,btnVisualizarConfirmarSenha);
            }
        });


        email = new Email();

        btnReenviar = findViewById(R.id.btnReenviar);
        btnReenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email.enviarCodigoValidacao(txtEmail.getText().toString(),"Código para validar o seu email do ServiceFinder");
            }
        });

        etapa = 0;

        btnVoltarEtapa = findViewById(R.id.btnVoltarEtapa);
        btnVoltarEtapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etapa--;
                switch (etapa){
                    case 0:
                        alterarVisibilidadeEtapaCodigo(View.GONE);
                        alterarVisibilidadeEtapaEmail(View.VISIBLE);
                        break;
                    case 1:
                        alterarVisibilidadeEtapaSenha(View.GONE);
                        alterarVisibilidadeEtapaCodigo(View.VISIBLE);
                        break;
                }
            }
        });


        Button btnConfirmar = findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (etapa){
                    case 0:
                        if (VerificaCampo.verificaVazio(v,txtEmail,"O campo de email deve ser preenchido")) {
                            if (txtEmail.getText().toString().contains("@")){
                                Usuario.encontrarUsuarioAtivoAlterarSenha(txtEmail.getText().toString().toLowerCase(),ActivityAlterarSenha.this);
                            }else{
                                Toast.makeText(ActivityAlterarSenha.this, "O email deve conter @", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case 1:
                        if (VerificaCampo.verificaVazio(v,txtCodigo,"O campo de codigo deve ser preenchido")){

                            if (email.VerificarCodigo(txtCodigo.getText().toString())){
                                alterarVisibilidadeEtapaCodigo(View.GONE);
                                alterarVisibilidadeEtapaSenha(View.VISIBLE);


                                etapa++;
                            }else{
                                Toast.makeText(v.getContext(), "Código incorreto, verifique se digitou corretamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case 2:
                        if (VerificaCampo.verificaVazio(v,txtSenha,"O campo de senha deve ser preenchido") && VerificaCampo.verificaVazio(v,txtConfirmarSenha,"O campo de confirmar senha deve ser preenchido")){
                            if (txtSenha.getText().toString().equals(txtConfirmarSenha.getText().toString())){
                                Usuario.atualizarSenha(keyUsuario,txtSenha.getText().toString());
                                ActivityAlterarSenha.this.finish();
                            }else{
                                Toast.makeText(v.getContext(), "O campo de senha deve ser igual o de confiramação de senha", Toast.LENGTH_SHORT).show();
                            }
                        }
                }
            }
        });
    }

    public void setKeyUsuario(String keyUsuario){
        this.keyUsuario = keyUsuario;
    }

    public void incrementarEtapa(){
        etapa++;
    }


    public void alterarVisibilidadeEtapaEmail(int visibility){
        txtEmail.setVisibility(visibility);
        if (visibility==View.VISIBLE){
            textViewDescricaoEditText.setText(R.string.digite_o_email_da_conta);
        }
    }

    public void alterarVisibilidadeEtapaCodigo(int visibility){

        layoutCodigo.setVisibility(visibility);

        txtCodigo.setVisibility(visibility);
        btnReenviar.setVisibility(visibility);
        btnVoltarEtapa.setVisibility(visibility);
        if (visibility==View.VISIBLE){
            email.enviarCodigoValidacao(txtEmail.getText().toString(),"Código para validar o seu email do ServiceFinder");
            textViewDescricaoEditText.setText(R.string.digite_o_codigo_recebido_por_email);
            textViewEmail.setText(txtEmail.getText().toString().toLowerCase());
        }else{
            txtCodigo.getText().clear();
        }
    }

    public void alterarVisibilidadeEtapaSenha(int visibility) {

        layoutSenha.setVisibility(visibility);
        layoutConfirmarSenha.setVisibility(visibility);
        if (visibility==View.VISIBLE){
            textViewDescricaoEditText.setText(R.string.digite_sua_nova_senha);
            imgIcone.setImageResource(R.drawable.key_24);
            btnVoltarEtapa.setVisibility(View.VISIBLE);
        }else{
            imgIcone.setImageResource(R.drawable.mail_outline_24);
        }
    }


}
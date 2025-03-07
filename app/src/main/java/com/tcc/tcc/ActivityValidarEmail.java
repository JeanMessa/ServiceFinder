package com.tcc.tcc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Prestador;
import com.tcc.tcc.classe.models.Usuario;
import com.tcc.tcc.classe.utils.Email;
import com.tcc.tcc.classe.utils.Processo;
import com.tcc.tcc.classe.utils.Senha;
import com.tcc.tcc.classe.utils.VerificaCampo;
import com.tcc.tcc.popup.PopupPergunta;

import java.io.ByteArrayOutputStream;

public class ActivityValidarEmail extends AppCompatActivity {

    private int processo;
    private String keyPessoa,keyUsuario, emailCodigo;
    private Boolean excluirFotoBD;
    private Pessoa pessoa;
    private Usuario usuario;
    private Prestador prestador;
    private ByteArrayOutputStream bytesFotoPerfil;

    private PopupPergunta popupPergunta;

    CardView cvEscurecer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validar_email);

        Email email = new Email();
        EditText txtCodigo = findViewById(R.id.txtCodigo);

        if (getIntent().getExtras()!=null){
            Bundle bundle = getIntent().getExtras().getBundle("bundle");
            if (bundle!=null){
                processo = bundle.getInt("processo");
                usuario = (Usuario) bundle.getSerializable("usuario");
                TextView textViewEmail = findViewById(R.id.textViewEmail);

                TextView textViewDescricaoProcesso = findViewById(R.id.textViewDescricaoProcesso);
                if (processo== Processo.EDITAR){
                    textViewDescricaoProcesso.setText(R.string.descricaoProcessoEditar);
                    keyUsuario = bundle.getString("keyUsuario");
                    keyPessoa = bundle.getString("keyPessoa");
                    pessoa = (Pessoa) bundle.getSerializable("pessoa");

                    byte[] byteArray = bundle.getByteArray("bytesFotoPerfil");
                    if (byteArray!=null){
                        bytesFotoPerfil = new ByteArrayOutputStream(byteArray.length);
                        bytesFotoPerfil.write(byteArray,0,byteArray.length);
                    }

                    if (pessoa!=null && pessoa.isPrestador()){

                        prestador = (Prestador) bundle.getSerializable("prestador");
                    }
                    emailCodigo = bundle.getString("emailOriginal");
                    excluirFotoBD = bundle.getBoolean("excluirFotoBD");
                }else {
                   emailCodigo = usuario.getEmail();
                    if (processo == Processo.DESATIVAR) {
                        textViewDescricaoProcesso.setText(R.string.descricaoProcessoDesativar);

                        popupPergunta = new PopupPergunta("Tem certeza que deseja desativar essa conta, caso faça isso nunca mais poderá acessar essa conta.", (ViewGroup) textViewDescricaoProcesso.getParent());
                        popupPergunta.setBtnConfirmarListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Pessoa.desativar(usuario.getKeyPessoa(),getApplicationContext());
                                Usuario.logout(ActivityValidarEmail.this);
                            }
                        });
                        popupPergunta.setBtnRecusarListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupPergunta.getPopup().dismiss();
                            }
                        });

                        cvEscurecer = findViewById(R.id.cvEscurecer);
                        popupPergunta.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                cvEscurecer.setVisibility(View.GONE);
                            }
                        });
                    }
                }
                textViewEmail.setText(emailCodigo);
                email.enviarCodigoValidacao(emailCodigo,"Código para validar o seu email do ServiceFinder");

            }
            EditText txtSenha = findViewById(R.id.txtSenha);
            ImageButton btnVisualizarSenha = findViewById(R.id.btnVisualizarSenha);
            btnVisualizarSenha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Senha.visualizarSenha(v,txtSenha,btnVisualizarSenha);
                }
            });


            Button btnConfirmar = findViewById(R.id.btnConfirmar);
            btnConfirmar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (VerificaCampo.verificaVazio(v,txtCodigo,"O campo de código deve ser preenchido")
                    && VerificaCampo.verificaVazio(v,txtSenha,"O campo de senha deve ser preenchido")){
                        boolean valido;
                        valido = email.VerificarCodigo(txtCodigo.getText().toString());
                        if (valido){
                            if (processo == Processo.EDITAR) {
                                Usuario.validarEditar(keyUsuario, keyPessoa, usuario, pessoa, prestador, bytesFotoPerfil, txtSenha.getText().toString(),excluirFotoBD, ActivityValidarEmail.this);
                            }else if (processo == Processo.DESATIVAR){
                                if (Senha.compararSenha(txtSenha.getText().toString(),usuario.getSenha())) {

                                    popupPergunta.show(null,getApplicationContext());
                                    cvEscurecer.setVisibility(View.VISIBLE);

                                }else{
                                    Toast.makeText(ActivityValidarEmail.this, "Senha Incorreta", Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else {
                            Toast.makeText(ActivityValidarEmail.this, "Código Incorreto", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            Button btnReenviar = findViewById(R.id.btnReenviar);
            btnReenviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email.enviarCodigoValidacao(emailCodigo,"Código para validar o seu email do ServiceFinder");
                }
            });

            ImageButton btnVoltar = findViewById(R.id.btnVoltar);
            btnVoltar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        if (popupPergunta !=null && popupPergunta.getPopup() !=null && popupPergunta.getPopup().isShowing()){
            popupPergunta.getPopup().dismiss();
        }
        super.onDestroy();
    }
}
package com.tcc.tcc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.tcc.tcc.adapter.AdapterHomePrestador;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Prestador;
import com.tcc.tcc.popup.PopupPergunta;
import com.tcc.tcc.principal.ActivityPrincipal;

import java.util.Objects;

public class ActivityPerfil extends AppCompatActivity {

    private String keyPessoa,keyPessoaUsuario;
    private ImageButton btnVoltar;
    private ImageView imgFotoPerfil;
    private TextView textViewNome;
    private LinearLayout layoutPrestador;
    private Button btnConversar,btnBloquear;

    private CardView cvEscurecer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        if (getIntent().getExtras()!=null){
            keyPessoa = getIntent().getExtras().getString("keyPessoa");
            keyPessoaUsuario = getIntent().getExtras().getString("keyPessoaUsuario");
        }

        if (keyPessoa != null){

            cvEscurecer = findViewById(R.id.cvEscurecer);

            btnVoltar = findViewById(R.id.btnVoltar);
            btnVoltar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            findViewById(R.id.layoutBtnsPerfil).setVisibility(View.VISIBLE);

            imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
            textViewNome = findViewById(R.id.textViewNome);
            layoutPrestador = findViewById(R.id.layoutPrestador);

            Prestador.preencherDadosPessoa(keyPessoa,imgFotoPerfil,textViewNome,layoutPrestador,this);

            btnConversar = findViewById(R.id.btnConversar);
            btnConversar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityPerfil.this, ActivityPrincipal.class);
                    intent.putExtra("KeyPessoaChat",keyPessoa);
                    startActivity(intent);
                }
            });

            PopupPergunta popupPerguntaBloquear = new PopupPergunta("Se você bloquear essa pessoa não poderá mais vê-la em nenhuma tela do aplicativo, ao menos que desbloquei ela nas configurações.\nTem certeza que deseja bloquear esta pessoa?",(ViewGroup) textViewNome.getParent());
            popupPerguntaBloquear.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    trocarEscurecer(View.GONE);
                }
            });

            btnBloquear = findViewById(R.id.btnBloquear);

            popupPerguntaBloquear.setBtnConfirmarListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pessoa.addBloqueados(keyPessoa,keyPessoaUsuario);
                    popupPerguntaBloquear.getPopup().dismiss();
                    btnBloquear.setVisibility(View.GONE);
                    btnConversar.setVisibility(View.GONE);
                }
            });

            if (keyPessoaUsuario!=null){
                btnBloquear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(popupPerguntaBloquear.show("confirmacaoBloquear",getApplicationContext())){
                            trocarEscurecer(View.VISIBLE);
                        }else {
                            Toast.makeText(ActivityPerfil.this, "Bloqueio Realizado", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }else {
            finish();
        }
    }

    public void trocarEscurecer(int visibility){
        cvEscurecer.setVisibility(visibility);
        cvEscurecer.setAlpha(0.8F);
    }
}
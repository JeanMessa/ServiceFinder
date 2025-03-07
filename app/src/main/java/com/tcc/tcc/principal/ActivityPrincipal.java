package com.tcc.tcc.principal;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;
import com.tcc.tcc.ActivityLogin;
import com.tcc.tcc.ActivityPerfilMeuPerfil;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Usuario;
import com.tcc.tcc.classe.utils.Arquivo;

import java.util.ArrayList;
import java.util.Objects;

public class ActivityPrincipal extends AppCompatActivity {
    private String keyUsuario;

    private CardView cvEscurecer;

    private Usuario usuario;

    private FragmentContainerView fragmentContainerView;

    private FragmentManager fragmentManager;

    private FragmentPrincipalHome fragmentPrincipalHome;

    private int contadorFragment;

    private boolean permissaoTrocarFragment;

    private LayoutInflater inflater;
    private ImageView imgFotoPerfil;

    private TextView textViewNome;

    private Button btnPerfil,btnSair;

    private TabLayout tabPrincipal;
    private String keyPessoaChat;

    private Arquivo arquivoDownload;

    private ArrayList<Runnable> runnablesUsuario;

    private Bundle bundleAgendamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);






        SharedPreferences preferences = getSharedPreferences("usuarioAtivo", MODE_PRIVATE);
        keyUsuario = preferences.getString("key",null);
        if (keyUsuario!=null){

            runnablesUsuario = new ArrayList<>();




            setContentView(R.layout.activity_principal);

            cvEscurecer = findViewById(R.id.cvEscurecer);

            Drawable imgCirculo = ContextCompat.getDrawable(this, android.R.drawable.radiobutton_off_background);
            imgCirculo.setTint(ContextCompat.getColor(this,R.color.AzulSecundario));

            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;

            View popupViewPerfil = inflater.inflate(R.layout.popup_perfil,(ViewGroup) getWindow().getDecorView().getRootView(),false);
            PopupWindow popupPerfil = new PopupWindow(popupViewPerfil, wrapContent, wrapContent, true);

            imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
            imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
            imgFotoPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupPerfil.showAsDropDown(imgFotoPerfil,0,15);
                }
            });

            textViewNome = popupViewPerfil.findViewById(R.id.textViewNome);

            usuario = new Usuario();
            usuario.setPessoaFromDB(keyUsuario,this,imgFotoPerfil,textViewNome);


            if (getIntent().getExtras() == null || !getIntent().getExtras().getBoolean("config")) {
                Runnable runnableSalvarTokenFCM = new Runnable() {
                    @Override
                    public void run() {
                        Pessoa.salvarTokenFCM(usuario.getKeyPessoa());
                    }
                };
                runRunnableUsuario(runnableSalvarTokenFCM);
            }




            btnPerfil = popupViewPerfil.findViewById(R.id.btnPerfil);
            btnPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ActivityPerfilMeuPerfil.class);
                    startActivity(intent);
                    popupPerfil.dismiss();
                }
            });

            btnSair = popupViewPerfil.findViewById(R.id.btnSair);
            btnSair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tabPrincipal.getSelectedTabPosition()==0){
                        FragmentPrincipalHome fragment = fragmentContainerView.getFragment();
                        if (fragment!=null) {
                            fragment.setSalvarEstado(false);
                        }
                    }
                    Usuario.logout(ActivityPrincipal.this);
                }
            });



            fragmentContainerView = findViewById(R.id.fragmentContainerView);
            fragmentManager = getSupportFragmentManager();
            contadorFragment = 0;
            permissaoTrocarFragment = true;




            fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if (fragmentManager.getBackStackEntryCount()<contadorFragment){
                        atualizarTabFragment();
                    }
                    contadorFragment = fragmentManager.getBackStackEntryCount();
                }
            });

            Runnable runnableAbrirHome = new Runnable(){
                @Override
                public void run() {

                        FragmentPrincipalHome fragment = FragmentPrincipalHome.newInstance(usuario.getKeyPessoa());
                        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment, "FragmentPrincipalHome").addToBackStack("FragmentPrincipalHome").commit();

                }
            };

            tabPrincipal = findViewById(R.id.tabPrincipal);
            boolean temaTrocou = false;
            if (getIntent().getExtras()!=null && getIntent().getExtras().getBundle("bundleConversa")!=null){
                SharedPreferences preferencesconfig = getSharedPreferences("config", Context.MODE_PRIVATE);
                String tema = preferencesconfig.getString("tema","Tema do Dispositivo");
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (tema){
                    case "Claro":
                        if (currentNightMode != Configuration.UI_MODE_NIGHT_NO){
                            finish();
                            temaTrocou = true;
                        }
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "Escuro":
                        if (currentNightMode != Configuration.UI_MODE_NIGHT_YES){
                            finish();
                            temaTrocou = true;
                        }
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }
                Bundle bundleConversa = getIntent().getExtras().getBundle("bundleConversa");
                if (bundleConversa!=null) {

                    if (temaTrocou){
                        Intent intent = new Intent(this,ActivityPrincipal.class);
                        intent.putExtra("bundleConversa",bundleConversa);
                        startActivity(intent);

                    }else {
                        setKeyPessoaChat(bundleConversa.getString("KeyPessoaChat"));
                        abrirChat();
                        tabPrincipal.getTabAt(1).view.setBackgroundColor(ContextCompat.getColor(ActivityPrincipal.this, R.color.AzulSecundario));
                    }
                }


            }else if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("config")) {
                Runnable runnableAbrirConfiguracoes = new Runnable() {
                    @Override
                    public void run() {
                        fragmentManager.beginTransaction().add(R.id.fragmentContainerView, FragmentPrincipalConfiguracoes.newInstance(usuario.getKeyPessoa()), "FragmentPrincipalConfiguracoes").addToBackStack("FragmentPrincipalConfiguracoes").commit();
                    }
                };

                runRunnableUsuario(runnableAbrirConfiguracoes);
                trocarTab(3);
                tabPrincipal.getTabAt(3).view.setBackgroundColor(ContextCompat.getColor(ActivityPrincipal.this,R.color.AzulSecundario));
            }
            else if (tabPrincipal.getSelectedTabPosition()==0){
                runRunnableUsuario(runnableAbrirHome);
                tabPrincipal.getTabAt(0).view.setBackgroundColor(ContextCompat.getColor(ActivityPrincipal.this,R.color.AzulSecundario));
            }else{
                atualizarTabFragment();
            }


            if (!temaTrocou) {
                tabPrincipal.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {

                        tab.view.setBackgroundColor(ContextCompat.getColor(ActivityPrincipal.this, R.color.AzulSecundario));
                        if (permissaoTrocarFragment) {
                            if (fragmentContainerView.getFragment() != null && Objects.equals(fragmentContainerView.getFragment().getTag(), "FragmentPrincipalHome")) {
                                fragmentPrincipalHome = fragmentContainerView.getFragment();
                            }
                            switch (tab.getPosition()) {
                                case 0:
                                    if (fragmentPrincipalHome == null) {
                                        runRunnableUsuario(runnableAbrirHome);
                                    } else {
                                        fragmentPrincipalHome.verificarAtualizacoesBloqueados();
                                        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentPrincipalHome, "FragmentPrincipalHome").addToBackStack("FragmentPrincipalHome").commit();
                                    }
                                    break;
                                case 1:
                                    if (keyPessoaChat != null) {
                                        abrirChat();
                                    } else {
                                        fragmentManager.beginTransaction().add(R.id.fragmentContainerView, new FragmentPrincipalConversas(), "FragmentPrincipalConversas").addToBackStack("FragmentPrincipalConversas").commit();
                                        break;
                                    }
                                    break;
                                case 2:
                                    Runnable runnableAbrirAgenda = new Runnable() {
                                        @Override
                                        public void run() {
                                            FragmentPrincipalAgenda fragment;
                                            if (bundleAgendamento != null) {
                                                fragment = FragmentPrincipalAgenda.newInstance(usuario.getKeyPessoa(), bundleAgendamento.getString("keyServico"), bundleAgendamento.getInt("idNotificacao"));
                                                bundleAgendamento = null;
                                            } else {
                                                fragment = FragmentPrincipalAgenda.newInstance(usuario.getKeyPessoa());
                                            }
                                            fragmentManager.beginTransaction().add(R.id.fragmentContainerView, fragment, "FragmentPrincipalAgenda").addToBackStack("FragmentPrincipalAgenda").commit();
                                        }
                                    };
                                    runRunnableUsuario(runnableAbrirAgenda);
                                    break;
                                case 3:
                                    Runnable runnableAbrirConfiguracoes = new Runnable() {
                                        @Override
                                        public void run() {
                                            fragmentManager.beginTransaction().add(R.id.fragmentContainerView, FragmentPrincipalConfiguracoes.newInstance(usuario.getKeyPessoa()), "FragmentPrincipalConfiguracoes").addToBackStack("FragmentPrincipalConfiguracoes").commit();
                                        }
                                    };
                                    runRunnableUsuario(runnableAbrirConfiguracoes);
                            }
                        } else {
                            permissaoTrocarFragment = true;
                        }

                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        tab.view.setBackgroundColor(ContextCompat.getColor(ActivityPrincipal.this, R.color.AzulPrincipal));
                    }


                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 1) {
                            if (fragmentContainerView.getFragment().getClass() == FragmentPrincipalChat.class) {
                                contadorFragment--;
                                fragmentManager.popBackStack();
                                if (fragmentManager.getBackStackEntryCount() < 2 || !Objects.equals(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 2).getName(), "FragmentPrincipalConversas")) {
                                    fragmentManager.beginTransaction().add(R.id.fragmentContainerView, new FragmentPrincipalConversas()).addToBackStack("FragmentPrincipalConversas").commit();
                                }
                                keyPessoaChat = null;
                            } else {
                                if (keyPessoaChat != null) {
                                    abrirChat();
                                }
                            }

                        }
                    }
                });


                if (getIntent().getExtras() != null) {
                    if (getIntent().getExtras().getBundle("bundleAgendamento") != null) {
                        bundleAgendamento = getIntent().getExtras().getBundle("bundleAgendamento");
                        tabPrincipal.selectTab(tabPrincipal.getTabAt(2));
                    } else if (getIntent().getExtras().getBundle("bundleConversa") != null) {
                        Bundle bundleConversa = getIntent().getExtras().getBundle("bundleConversa");
                        if (bundleConversa != null) {
                            keyPessoaChat = bundleConversa.getString("keyPessoaChat");
                            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                            notificationManager.cancel(bundleConversa.getInt("idNotificacao"));
                            tabPrincipal.selectTab(tabPrincipal.getTabAt(1));
                        }
                    }
                }

            }


        }else {
            Intent intent = new Intent(this, ActivityLogin.class);
            startActivity(intent);
            finish();
        }
    }



    public void trocarTab(int tabNum){
        TabLayout.Tab tab= tabPrincipal.getTabAt(tabNum);
        tabPrincipal.selectTab(tab);
    }

    public void setKeyPessoaChat(String keyPessoaChat){
        this.keyPessoaChat = keyPessoaChat;
        if (this.keyPessoaChat !=null && tabPrincipal.getSelectedTabPosition()!=1){
            tabPrincipal.selectTab(tabPrincipal.getTabAt(1));
        }
    }

    public String getKeyPessoaUsuario(){
        if (usuario!=null){
            return usuario.getKeyPessoa();
        }else {
            return null;
        }
    }

    public void abrirChat(){
        Runnable runnableAbrirChat = new Runnable() {
            @Override
            public void run() {
                FragmentPrincipalChat fragment = FragmentPrincipalChat.newInstance(usuario.getKeyPessoa(),keyPessoaChat);
                fragmentManager.beginTransaction().add(R.id.fragmentContainerView,fragment).addToBackStack("FragmentPrincipalChat").commit();
            }
        };
        runRunnableUsuario(runnableAbrirChat);
    }

    public void trocarEscurecer(int visibility){
        cvEscurecer.setVisibility(visibility);
        cvEscurecer.setAlpha(0.8F);
    }

    public void trocarEscurecer(int visibility,float alpha){
        cvEscurecer.setVisibility(visibility);
        cvEscurecer.setAlpha(alpha);
    }



    public void realizarDownload(Arquivo arquivo){
        arquivoDownload = arquivo;
        launcherPermissaoDownload.launch(new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
    }


    private void atualizarTabFragment(){
        TabLayout.Tab tabFragmentAtual;
        if (fragmentContainerView.getFragment()!=null){
            if(fragmentContainerView.getFragment().getClass()== FragmentPrincipalHome.class){
                tabFragmentAtual =  tabPrincipal.getTabAt(0);
            }else if (fragmentContainerView.getFragment().getClass()== FragmentPrincipalConversas.class || fragmentContainerView.getFragment().getClass()== FragmentPrincipalChat.class){
                tabFragmentAtual =  tabPrincipal.getTabAt(1);
            } else if (fragmentContainerView.getFragment().getClass()== FragmentPrincipalAgenda.class) {
                tabFragmentAtual =  tabPrincipal.getTabAt(2);
            } else  if (fragmentContainerView.getFragment().getClass()== FragmentPrincipalConfiguracoes.class){
                tabFragmentAtual =  tabPrincipal.getTabAt(3);
            }
            else{
                tabFragmentAtual = null;
            }

            if (tabFragmentAtual!=null && tabPrincipal!=null){
                if (tabPrincipal.getSelectedTabPosition() != tabFragmentAtual.getPosition()){
                    permissaoTrocarFragment = false;
                    tabPrincipal.selectTab(tabFragmentAtual);
                }
            }

        }else {
            finish();
        }


    }

    public void runRunnableUsuario(Runnable runnable){
        if(usuario.getKeyPessoa()==null){
            runnablesUsuario.add(runnable);
        }else {
            runnable.run();
        }
    }

    public void runRunnablesUsuario(){
        for (Runnable runnable: runnablesUsuario) {
            runnable.run();
        }
        runnablesUsuario.clear();
    }

    @Override
    public void onBackPressed() {
        if (fragmentPrincipalHome!=null){
            fragmentPrincipalHome.verificarAtualizacoesBloqueados();
        }
        super.onBackPressed();
    }

    ActivityResultLauncher<String[]> launcherPermissaoDownload =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean leituraPermitida = result.getOrDefault(
                                Manifest.permission.READ_EXTERNAL_STORAGE, false);
                        Boolean escritaPermitida = result.getOrDefault(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,false);
                        if ((leituraPermitida != null && leituraPermitida) || (escritaPermitida != null && escritaPermitida)) {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                arquivoDownload.download(this);
                            }
                        }
                    }

            );

}
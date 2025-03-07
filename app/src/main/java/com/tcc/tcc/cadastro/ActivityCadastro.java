package com.tcc.tcc.cadastro;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Cidade;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Prestador;
import com.tcc.tcc.classe.models.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Locale;


public class ActivityCadastro extends AppCompatActivity
        implements FragmentCadastroDados.OnDataPass,
        FragmentCadastroFoto.OnDataPass,
        FragmentCadastroPrestador.OnDataPass,
        FragmentCadastroEmail.OnDataPass{

    private TabLayout tabEtapasCadastro;

    private CardView cvEscurecer;

    private TextView textViewEscurecer;

    private ProgressBar progressEscurecer;

    private ScrollView scrollFragment;

    public static boolean permissaoTrocarFragment = true;

    private TextView textViewEtapa;

    private Pessoa pessoa;
    private Usuario usuario;

    private ByteArrayOutputStream bytesFotoPerfil;

    private boolean excluirFotoBD;

    private Prestador prestador;

    private boolean emailValidado;

    private FragmentManager fragmentManager;

    private int fragmentAtual;

    private String keyUsuarioEditar,keyPessoaEditar,emailOriginal;

    private boolean fotoAlterada;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        fragmentManager = getSupportFragmentManager();

        FragmentCadastroDados fragmentDadosInicial;

        if (getIntent().getExtras()!=null) {
                Bundle bundleEditar = getIntent().getExtras().getBundle("bundleEditar");
            if (bundleEditar != null) {
                fotoAlterada = false;
                if (bundleEditar.getSerializable("pessoa")!=null){
                    pessoa = (Pessoa) bundleEditar.getSerializable("pessoa");
                }else if (bundleEditar.getSerializable("prestador")!=null){
                    pessoa = (Pessoa) bundleEditar.getSerializable("prestador");
                    Prestador prestadorEditar = (Prestador) bundleEditar.getSerializable("prestador");


                    if (prestadorEditar!=null) {
                        prestador = new Prestador();
                        prestador.setServicos(prestadorEditar.getServicos());
                        prestador.setCidades(prestadorEditar.getCidades());
                    }
                }

                if (bundleEditar.getByteArray("bytesFotoPerfil")!=null){
                    bytesFotoPerfil = new ByteArrayOutputStream(bundleEditar.getByteArray("bytesFotoPerfil").length);
                }

                usuario = (Usuario) bundleEditar.getSerializable("usuario");
                if (usuario!=null) {
                    emailOriginal = usuario.getEmail();
                }
                keyPessoaEditar = bundleEditar.getString("keyPessoa");
                keyUsuarioEditar = bundleEditar.getString("keyUsuario");

                emailValidado = true;


            }



            if (pessoa!=null && usuario!=null){
                fragmentDadosInicial = FragmentCadastroDados.newInstance(pessoa.getNome(),pessoa.getSobrenome(),usuario.getEmail(),keyPessoaEditar);
            }else{
                fragmentDadosInicial = new FragmentCadastroDados();
            }

        }else{
            pessoa = new Pessoa();
            prestador = new Prestador();
            usuario = new Usuario();
            emailValidado = false;

            fragmentDadosInicial = new FragmentCadastroDados();
        }

        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentDadosInicial).commit();


        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        scrollFragment = findViewById(R.id.scrollFragment);

        cvEscurecer = findViewById(R.id.cvEscurecer);
        textViewEscurecer = findViewById(R.id.textViewEscurecer);
        progressEscurecer = findViewById(R.id.progressEscurecer);

        textViewEtapa = findViewById(R.id.textViewEtapa);

        tabEtapasCadastro = findViewById(R.id.tabEtapasCadastro);



        for (int i=1;i<tabEtapasCadastro.getTabCount();i++){
            TabLayout.Tab tab = tabEtapasCadastro.getTabAt(i);
            Drawable iconeTab = AppCompatResources.getDrawable(getBaseContext(), android.R.drawable.radiobutton_off_background);
            if (keyPessoaEditar==null) {
                tab.view.setEnabled(false);
                iconeTab.setAlpha(50);

            }
            iconeTab.setColorFilter(new PorterDuffColorFilter(getColor(R.color.PretoOuBranco), PorterDuff.Mode.SRC_IN));
            tab.setIcon(iconeTab);

        }

        TabLayout.Tab tabDados = tabEtapasCadastro.getTabAt(0);
        fragmentAtual = 0;
        Drawable iconeTabDados = AppCompatResources.getDrawable(getBaseContext(),android.R.drawable.radiobutton_off_background);
        iconeTabDados.setAlpha(255);
        iconeTabDados.setColorFilter(new PorterDuffColorFilter(getColor(R.color.AzulPrincipalouSecundario), PorterDuff.Mode.SRC_IN));
        tabDados.setIcon(iconeTabDados);



        tabEtapasCadastro.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (fragmentAtual == tab.getPosition() && fragmentAtual ==0){
                    iconeTabDados.setColorFilter(new PorterDuffColorFilter(getColor(R.color.AzulPrincipalouSecundario), PorterDuff.Mode.SRC_IN));
                    tabDados.setIcon(iconeTabDados);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                int tabSelecionada = tabEtapasCadastro.getSelectedTabPosition();
                switch (tab.getPosition()){
                    case 0:
                        if(fragmentAtual==0) {
                            FragmentCadastroDados fragmentCadastroDados = (FragmentCadastroDados) fragmentManager.getFragments().get(0);
                            fragmentCadastroDados.continuar(fragmentCadastroDados.getView(), tabSelecionada);
                        }
                        break;
                    case 2:
                        if(fragmentAtual==2){
                            FragmentCadastroPrestador fragmentPrestador = (FragmentCadastroPrestador)fragmentManager.getFragments().get(0);
                            permissaoTrocarFragment = fragmentPrestador.permissaoContinuar(fragmentPrestador.getView());
                            if(!permissaoTrocarFragment){
                                trocarTab(2);
                            }else {
                                trocarFragment(tabSelecionada);
                            }
                        }
                        break;
                    default:
                        if (permissaoTrocarFragment) {
                            trocarFragment(tabSelecionada);
                        }
                }
                if(permissaoTrocarFragment){
                    int CorIconeTab;
                    if (tab.getPosition()!=3 || emailValidado || !tabEtapasCadastro.getTabAt(4).view.isEnabled()){
                        CorIconeTab = getColor(R.color.PretoOuBranco);
                    }else{
                        CorIconeTab = getColor(R.color.AmareloAlerta);
                    }
                    Drawable iconeTab = AppCompatResources.getDrawable(getBaseContext(),android.R.drawable.radiobutton_off_background);
                    iconeTab.setColorFilter(new PorterDuffColorFilter(CorIconeTab, PorterDuff.Mode.SRC_IN));
                    tab.setIcon(iconeTab);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        excluirFotoBD = false;
    }


    public void rolarScrollFragment(int direction){
        scrollFragment.fullScroll(direction);
    }




    public void trocarTab(int tabNum){
        TabLayout.Tab tab= tabEtapasCadastro.getTabAt(tabNum);
        tabEtapasCadastro.selectTab(tab);
    }

    public void HabilitarTab(int tabNum){
        TabLayout.Tab tab= tabEtapasCadastro.getTabAt(tabNum);
        tab.view.setEnabled(true);
        Drawable iconeTab = tab.getIcon();
        iconeTab.setAlpha(255);
        tab.setIcon(iconeTab);
    }

    public void trocarFragment(int tabNum){
        fragmentAtual = tabNum;
        TabLayout.Tab tabAtual = tabEtapasCadastro.getTabAt(fragmentAtual);
        Drawable iconeTab = AppCompatResources.getDrawable(getBaseContext(),android.R.drawable.radiobutton_off_background);
        iconeTab.setColorFilter(new PorterDuffColorFilter(getColor(R.color.AzulPrincipalouSecundario), PorterDuff.Mode.SRC_IN));
        if (!tabAtual.view.isEnabled()) {
            tabAtual.view.setEnabled(true);
            iconeTab.setAlpha(255);
        }

        tabAtual.setIcon(iconeTab);
        rolarScrollFragment(View.FOCUS_UP);
        textViewEtapa.setText(String.format(Locale.ROOT,"Etapa %d - 5", tabAtual.getPosition() + 1));
        switch (tabNum) {
            case 0:
                FragmentCadastroDados fragmentDados;
                if (keyPessoaEditar==null){
                    fragmentDados = FragmentCadastroDados.newInstance(pessoa.getNome(), pessoa.getSobrenome(), pessoa.getCpf(), usuario.getEmail(), usuario.getSenha());
                }else{
                    fragmentDados = FragmentCadastroDados.newInstance(pessoa.getNome(), pessoa.getSobrenome(), usuario.getEmail(),keyPessoaEditar);
                }
                fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentDados).commit();
                break;
            case 1:
                if (bytesFotoPerfil!=null){
                    FragmentCadastroFoto fragmentFoto = FragmentCadastroFoto.newInstance(bytesFotoPerfil);
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentFoto).commit();
                }else if (keyPessoaEditar!=null){
                    FragmentCadastroFoto fragmentFoto = FragmentCadastroFoto.newInstance(keyPessoaEditar,pessoa.isFotoPadrao());
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentFoto).commit();
                }else{
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView,new FragmentCadastroFoto()).commit();
                }
                break;
            case 2:
                if (pessoa.isPrestador()){
                    FragmentCadastroPrestador fragmentPrestador = FragmentCadastroPrestador.newInstance(prestador);
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentPrestador).commit();
                }else {
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, new FragmentCadastroPrestador()).commit();
                }
                break;
            case 3:
                FragmentCadastroEmail fragmentEmail = FragmentCadastroEmail.newInstance(usuario.getEmail(),emailValidado);
                fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentEmail).commit();
                break;
            case 4:
                FragmentCadastroRevisar fragmentRevisar;
                if (keyPessoaEditar==null) {
                    if (!pessoa.isPrestador()) {
                        fragmentRevisar = FragmentCadastroRevisar.newInstance(pessoa, usuario, bytesFotoPerfil, emailValidado);
                    } else {
                        fragmentRevisar = FragmentCadastroRevisar.newInstance(pessoa, usuario, bytesFotoPerfil, prestador, emailValidado);
                    }
                }else{
                    if (!pessoa.isPrestador()) {
                        fragmentRevisar = FragmentCadastroRevisar.newInstance(pessoa, usuario, bytesFotoPerfil, emailValidado,keyPessoaEditar,keyUsuarioEditar,emailOriginal, fotoAlterada,excluirFotoBD);
                    } else {
                        fragmentRevisar = FragmentCadastroRevisar.newInstance(pessoa, usuario, bytesFotoPerfil, prestador, emailValidado,keyPessoaEditar,keyUsuarioEditar,emailOriginal, fotoAlterada,excluirFotoBD);
                    }
                }
                fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragmentRevisar).commit();
                break;
        }

    }

    public void trocarEscurecer(int visibility){
        cvEscurecer.setVisibility(visibility);
        textViewEscurecer.setText("");
        progressEscurecer.setVisibility(View.GONE);
    }

    public void trocarEscurecer(String text,int visibilityProgess){
        cvEscurecer.setVisibility(View.VISIBLE);
        progressEscurecer.setVisibility(visibilityProgess);
        textViewEscurecer.setText(text);
    }


    @Override
    public void onDataPassDados(Pessoa p,Usuario u) {
        pessoa.setDados(p);
        usuario = u;
    }


    public void onDataPassFoto(ByteArrayOutputStream bytes) {
        bytesFotoPerfil = bytes;
        pessoa.setFotoPadrao(bytes == null);
        if (keyPessoaEditar!=null){
            excluirFotoBD = pessoa.isFotoPadrao();
        }
    }

    public void onDataPassFotoEditada() {
        fotoAlterada = true;
    }

    public void onDataPassPrestador() {
        pessoa.setPrestador(false);
    }

    public void onDataPassPrestador(ArrayList<String> servicos, ArrayList<Cidade> cidades) {
        pessoa.setPrestador(true);
        if (prestador==null){
            prestador = new Prestador();
        }
        prestador.setServicos(servicos);
        prestador.setCidades(cidades);
    }

    public void onDataPassDadosEmailTrocado() {
        if (emailValidado) {
            emailValidado = false;
            Drawable iconeTab = AppCompatResources.getDrawable(getBaseContext(), android.R.drawable.radiobutton_off_background);
            iconeTab.setColorFilter(new PorterDuffColorFilter(getColor(R.color.AmareloAlerta), PorterDuff.Mode.SRC_IN));
            TabLayout.Tab tab = tabEtapasCadastro.getTabAt(3);
            tab.setIcon(iconeTab);
        }
    }
    public void onDataPassEmail() {
        emailValidado = true;
    }

}
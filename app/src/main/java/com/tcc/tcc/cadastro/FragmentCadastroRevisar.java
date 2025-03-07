package com.tcc.tcc.cadastro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.tcc.tcc.ActivityValidarEmail;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.utils.Imagem;
import com.tcc.tcc.classe.utils.Processo;
import com.tcc.tcc.view.ScrollListView;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.models.Prestador;
import com.tcc.tcc.classe.models.Usuario;

import java.io.ByteArrayOutputStream;


public class FragmentCadastroRevisar extends Fragment {

    private Pessoa pessoa;
    private Usuario usuario;
    private ByteArrayOutputStream bytesFotoPerfil;

    private Prestador prestador;

    private boolean emailValidado;

    private TextView textViewNome, textViewSobrenome, textViewCPF, textViewEmail,textViewPrestador,textViewEmailValidado;

    private ImageView imgFotoPerfil;

    private TableRow rowServico,rowCidade,rowAlertaEmail;

    private ScrollListView listServico, listCidade;

    private Button btnEditarDados,btnEditarFoto,btnEditarPrestador,btnConferirValidacao, btnFinalizar;

    private String keyUsuarioEditar,keyPessoaEditar,emailOriginal;
    private Boolean excluirFotoBD;

    boolean fotoAlterada,permissaoEditar;


    public static FragmentCadastroRevisar newInstance(Pessoa pessoa, Usuario usuario, ByteArrayOutputStream bytesFotoPerfil, boolean emailValidado) {
        FragmentCadastroRevisar fragment = new FragmentCadastroRevisar();
        Bundle args = new Bundle();
        args.putSerializable("pessoa",pessoa);
        args.putSerializable("usuario",usuario);
        if(bytesFotoPerfil!=null){
            args.putByteArray("bytesFotoPerfil",bytesFotoPerfil.toByteArray());
        }
        args.putBoolean("emailValidado",emailValidado);
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentCadastroRevisar newInstance(Pessoa pessoa, Usuario usuario, ByteArrayOutputStream bytesFotoPerfil, boolean emailValidado,String keyPessoaEditar,String keyUsuarioEditar,String emailOriginal,boolean fotoAlterada,boolean excluirFotobd) {
        FragmentCadastroRevisar fragment = new FragmentCadastroRevisar();
        Bundle args = new Bundle();
        args.putSerializable("pessoa",pessoa);
        args.putSerializable("usuario",usuario);
        if(bytesFotoPerfil!=null){
            args.putByteArray("bytesFotoPerfil",bytesFotoPerfil.toByteArray());
        }
        args.putBoolean("emailValidado",emailValidado);
        args.putString("keyPessoaEditar",keyPessoaEditar);
        args.putString("keyUsuarioEditar",keyUsuarioEditar);
        args.putString("emailOriginal",emailOriginal);
        args.putBoolean("fotoAlterada",fotoAlterada);
        args.putBoolean("excluirFotoBD",excluirFotobd);
        fragment.setArguments(args);
        return fragment;
    }
    public static FragmentCadastroRevisar newInstance(Pessoa pessoa, Usuario usuario, ByteArrayOutputStream bytesFotoPerfil, Prestador prestador, boolean emailValidado) {
        FragmentCadastroRevisar fragment = new FragmentCadastroRevisar();
        Bundle args = new Bundle();
        args.putSerializable("pessoa",pessoa);
        args.putSerializable("usuario",usuario);
        if(bytesFotoPerfil!=null){
            args.putByteArray("bytesFotoPerfil",bytesFotoPerfil.toByteArray());
        }
        args.putSerializable("prestador",prestador);
        args.putBoolean("emailValidado",emailValidado);
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentCadastroRevisar newInstance(Pessoa pessoa, Usuario usuario, ByteArrayOutputStream bytesFotoPerfil, Prestador prestador, boolean emailValidado,String keyPessoaEditar,String keyUsuarioEditar,String emailOriginal,boolean fotoAlterada,boolean excluirFotobd) {
        FragmentCadastroRevisar fragment = new FragmentCadastroRevisar();
        Bundle args = new Bundle();
        args.putSerializable("pessoa",pessoa);
        args.putSerializable("usuario",usuario);
        if(bytesFotoPerfil!=null){
            args.putByteArray("bytesFotoPerfil",bytesFotoPerfil.toByteArray());
        }
        args.putSerializable("prestador",prestador);
        args.putBoolean("emailValidado",emailValidado);
        args.putString("keyPessoaEditar",keyPessoaEditar);
        args.putString("keyUsuarioEditar",keyUsuarioEditar);
        args.putString("emailOriginal",emailOriginal);
        args.putBoolean("fotoAlterada",fotoAlterada);
        args.putBoolean("excluirFotoBD",excluirFotobd);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cadastro_revisar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewNome = view.findViewById(R.id.textViewNome);
        textViewSobrenome = view.findViewById(R.id.textViewSobrenome);
        textViewCPF = view.findViewById(R.id.textViewCPF);
        textViewEmail = view.findViewById(R.id.textViewEmail);

        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);

        textViewPrestador = view.findViewById(R.id.textViewPrestador);
        rowServico = view.findViewById(R.id.rowServico);
        rowCidade = view.findViewById(R.id.rowCidade);
        listServico = view.findViewById(R.id.listServico);
        listCidade = view.findViewById(R.id.listCidade);

        textViewEmailValidado = view.findViewById(R.id.textViewEmailValidado);
        rowAlertaEmail = view.findViewById(R.id.rowAlertaEmail);

        btnFinalizar = view.findViewById(R.id.btnFinalizar);


        if (getArguments()!=null){
            pessoa = (Pessoa) requireArguments().getSerializable("pessoa");
            usuario = (Usuario) requireArguments().getSerializable("usuario");
            if (pessoa != null) {
                textViewNome.setText(pessoa.getNome());
                textViewSobrenome.setText(pessoa.getSobrenome());
                textViewEmail.setText(usuario.getEmail());


                if (requireArguments().getString("keyPessoaEditar")==null){
                    textViewCPF.setText(pessoa.getCpf());
                }else{
                    TableRow tableRowCpf = view.findViewById(R.id.tableRowCpf);
                    tableRowCpf.setVisibility(View.GONE);
                    keyPessoaEditar = requireArguments().getString("keyPessoaEditar");
                    keyUsuarioEditar = requireArguments().getString("keyUsuarioEditar");
                    emailOriginal = requireArguments().getString("emailOriginal");
                    excluirFotoBD = requireArguments().getBoolean("excluirFotoBD");
                    fotoAlterada = requireArguments().getBoolean("fotoAlterada");
                }

                if (pessoa.isPrestador()){
                    prestador = (Prestador) requireArguments().getSerializable("prestador");

                    textViewPrestador.setText(R.string.sim);
                    rowServico.setVisibility(View.VISIBLE);
                    rowCidade.setVisibility(View.VISIBLE);


                    ArrayAdapter<String> adapterServico = new ArrayAdapter<>(view.getContext(), R.layout.item_simples,R.id.textViewSimples,prestador.getServicos());
                    listServico.setAdapter(adapterServico);
                    listServico.atualizarHeight(0);

                    ArrayAdapter<String> adapterCidade = new ArrayAdapter<>(view.getContext(), R.layout.item_simples,R.id.textViewSimples, prestador.cidadesToString());
                    listCidade.setAdapter(adapterCidade);
                    listCidade.atualizarHeight(0);

                }

                emailValidado = requireArguments().getBoolean("emailValidado");

                if(!emailValidado){
                    textViewEmailValidado.setText("Não");
                    rowAlertaEmail.setVisibility(View.VISIBLE);
                }
                if (keyPessoaEditar!=null) {
                    btnFinalizar.setText(R.string.finalizar_edicao);
                }

                btnFinalizar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getArguments().getBoolean("emailValidado")){
                            if (keyPessoaEditar==null) {
                                if (!pessoa.isPrestador()) {
                                    pessoa.Validainsert(view, bytesFotoPerfil, usuario);
                                } else {
                                    prestador.setPessoa(pessoa);
                                    prestador.Validainsert(view, bytesFotoPerfil, usuario);
                                }
                            }else{
                                if (permissaoEditar){
                                    Intent intent = new Intent(getContext(), ActivityValidarEmail.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("processo", Processo.EDITAR);
                                    bundle.putString("keyPessoa",keyPessoaEditar);
                                    bundle.putSerializable("pessoa",pessoa);
                                    bundle.putString("keyUsuario",keyUsuarioEditar);
                                    bundle.putSerializable("usuario",usuario);
                                    bundle.putString("emailOriginal",emailOriginal);
                                    bundle.putBoolean("excluirFotoBD",excluirFotoBD);
                                    if (pessoa.isPrestador()){
                                        bundle.putSerializable("prestador",prestador);
                                    }
                                    if (fotoAlterada){
                                        bundle.putByteArray("bytesFotoPerfil",bytesFotoPerfil.toByteArray());
                                    }
                                    intent.putExtra("bundle",bundle);
                                    requireContext().startActivity(intent);
                                }else{
                                    Toast.makeText(getContext(), "Foto de perfil carregando, aguarde e tente novamente", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((ActivityCadastro)requireActivity()).trocarTab(3);
                                }
                            }, 1000);
                            if (keyPessoaEditar==null) {
                                Toast.makeText(getContext(), "Valide seu email para poder finalizar o cadastro", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), "Valide seu email para poder finalizar a edição", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                if(!pessoa.isFotoPadrao()){
                    byte[] byteArray = getArguments().getByteArray("bytesFotoPerfil");
                    if (byteArray!=null){
                        permissaoEditar = true;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
                        imgFotoPerfil.setImageBitmap(bitmap);
                        bytesFotoPerfil = new ByteArrayOutputStream(byteArray.length);
                        bytesFotoPerfil.write(byteArray,0,byteArray.length);
                    }else if (keyPessoaEditar!=null){
                        permissaoEditar = false;
                        Task<Uri> task= Imagem.download(keyPessoaEditar+".jpg", Pessoa.obterReferenciaStorage());
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(imgFotoPerfil.getContext())
                                        .load(uri).addListener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {

                                                Bitmap bitmap = Bitmap.createBitmap(resource.getIntrinsicWidth(), resource.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

                                                Canvas canvas = new Canvas(bitmap);
                                                resource.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                                resource.draw(canvas);

                                                bytesFotoPerfil = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytesFotoPerfil);
                                                permissaoEditar = true;
                                                return false;
                                            }
                                        })
                                        .fitCenter()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .thumbnail(Glide.with(imgFotoPerfil.getContext()).load(R.drawable.replay_24))
                                        .error(R.drawable.close_24)
                                        .into(imgFotoPerfil);

                            }
                        });
                    }

                }else{
                    permissaoEditar = true;
                }
            }

        }

        btnEditarDados = view.findViewById(R.id.btnEditarDados);
        btnEditarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityCadastro)requireActivity()).trocarTab(0);
            }
        });

        btnEditarFoto = view.findViewById(R.id.btnEditarFoto);
        btnEditarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityCadastro)requireActivity()).trocarTab(1);
            }
        });

        btnEditarPrestador = view.findViewById(R.id.btnEditarPrestador);
        btnEditarPrestador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityCadastro)requireActivity()).trocarTab(2);
            }
        });

        btnConferirValidacao = view.findViewById(R.id.btnConferirValidacao);
        btnConferirValidacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityCadastro)requireActivity()).trocarTab(3);
            }
        });


    }
}
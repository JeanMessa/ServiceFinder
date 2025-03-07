package com.tcc.tcc.cadastro;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.tcc.tcc.R;
import com.tcc.tcc.classe.models.Pessoa;
import com.tcc.tcc.classe.utils.Imagem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.ByteArrayOutputStream;


public class FragmentCadastroFoto extends Fragment {

    private Button btnTrocarFoto,btnFotoGaleria,btnFotoCamera,btnContinuar;

    private ImageButton btnRemoverFoto;
    private ImageView imgFotoPerfil;

    private PopupWindow popupWindow;

    private CardView cardFotoPerfil;

    private OnDataPass dataPasser;

    private ByteArrayOutputStream bytesFotoPerfil;

    private ViewGroup parent;

    String keyPessoaEditar;

    public FragmentCadastroFoto() {}

    public static FragmentCadastroFoto newInstance(ByteArrayOutputStream bytesFotoPerfil) {
        FragmentCadastroFoto fragment = new  FragmentCadastroFoto();
        Bundle args = new Bundle();
        args.putByteArray("bytesFotoPerfil",bytesFotoPerfil.toByteArray());
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentCadastroFoto newInstance(String keyPessoaEditar,boolean isFotoPadrao) {
        FragmentCadastroFoto fragment = new  FragmentCadastroFoto();
        Bundle args = new Bundle();
        args.putString("keyPessoaEditar",keyPessoaEditar);
        args.putBoolean("isFotoPadrao",isFotoPadrao);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parent = container;
        return inflater.inflate(R.layout.fragment_cadastro_foto, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardFotoPerfil = view.findViewById(R.id.cardFotoPerfil);

        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_foto,parent,false);

        int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
        popupWindow = new PopupWindow(popupView, wrapContent, wrapContent, true);

        btnTrocarFoto = view.findViewById(R.id.btnTrocarFoto);
        btnTrocarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ((ActivityCadastro)getActivity()).trocarEscurecer(View.VISIBLE);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        ((ActivityCadastro)getActivity()).trocarEscurecer(View.GONE);
                    }
                });
            }
        });

        btnFotoGaleria = popupView.findViewById(R.id.btnFotoGaleria);
        btnFotoGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                activityResultFotoGaleria.launch(intent);
            }
        });

        btnFotoCamera = popupView.findViewById(R.id.btnFotoCamera);
        btnFotoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activityResultFotoCamera.launch(intent);
            }
        });





        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);

        btnRemoverFoto = view.findViewById(R.id.btnRemoverFoto);
        btnRemoverFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgFotoPerfil.setImageResource(R.drawable.person_24);
                btnRemoverFoto.setVisibility(View.GONE);
                cardFotoPerfil.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.AzulSecundario));
                dataPasser.onDataPassFoto(null);
            }
        });


        if (getArguments() != null) {
            if (getArguments().getByteArray("bytesFotoPerfil")!=null) {
                bytesFotoPerfil = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeByteArray(getArguments().getByteArray("bytesFotoPerfil"),0,getArguments().getByteArray("bytesFotoPerfil").length);
                imgFotoPerfil.setImageBitmap(bitmap);
                btnRemoverFoto.setVisibility(View.VISIBLE);
            }else if (getArguments().getString("keyPessoaEditar")!=null){
                if(!getArguments().getBoolean("isFotoPadrao")){
                    keyPessoaEditar = getArguments().getString("keyPessoaEditar");
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
                                            dataPasser.onDataPassFoto(bytesFotoPerfil);
                                            return false;
                                        }
                                    })
                                    .fitCenter()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .thumbnail(Glide.with(imgFotoPerfil.getContext()).load(R.drawable.replay_24))
                                    .error(R.drawable.close_24)
                                    .into(imgFotoPerfil);
                            btnRemoverFoto.setVisibility(View.VISIBLE);

                        }
                    });
                };






            }
        }

        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityCadastro)(getActivity())).trocarTab(2);
            }
        });

    }

    public interface OnDataPass {
        public void onDataPassFoto(ByteArrayOutputStream bytesFotoPerfil);

        public void onDataPassFotoEditada();
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (FragmentCadastroFoto.OnDataPass) context;
    }




    ActivityResultLauncher<Intent> activityResultFotoGaleria = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData()!=null){
                    Uri uri = result.getData().getData();
                    imgFotoPerfil.setImageURI(uri);
                    cardFotoPerfil.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.Preto));
                    BitmapDrawable drawable =(BitmapDrawable) imgFotoPerfil.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    bytesFotoPerfil = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytesFotoPerfil);
                    dataPasser.onDataPassFoto(bytesFotoPerfil);
                    if(keyPessoaEditar!=null){
                        dataPasser.onDataPassFotoEditada();
                    }
                    popupWindow.dismiss();
                    btnRemoverFoto.setVisibility(View.VISIBLE);
                }
            }
        }
    );

    ActivityResultLauncher<Intent> activityResultFotoCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()== Activity.RESULT_OK && result.getData()!=null){
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        imgFotoPerfil.setImageBitmap(bitmap);
                        bytesFotoPerfil = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytesFotoPerfil);
                        dataPasser.onDataPassFoto(bytesFotoPerfil);
                        if(keyPessoaEditar!=null){
                            dataPasser.onDataPassFotoEditada();
                        }
                        popupWindow.dismiss();
                        btnRemoverFoto.setVisibility(View.VISIBLE);
                    }


                }
            }
    );
}
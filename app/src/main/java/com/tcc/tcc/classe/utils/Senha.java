package com.tcc.tcc.classe.utils;

import android.graphics.Typeface;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.tcc.tcc.R;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Senha {
    public static void visualizarSenha(View view, EditText txtSenha, ImageButton btnVisualizarSenha){
        Typeface font = txtSenha.getTypeface();
        int cursor = txtSenha.getSelectionEnd();
        if (txtSenha.getInputType()==129){
            txtSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnVisualizarSenha.setImageResource(R.drawable.visibility_off_24);
        }else{
            txtSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnVisualizarSenha.setImageResource(R.drawable.visibility_24);
        }
        txtSenha.setTypeface(font);
        txtSenha.setSelection(cursor);
    }

    public static String criptografarSenha(String senha){
        MessageDigest algoritimo = null;
        try {
            algoritimo = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = algoritimo.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder senhaHexadecimal = new StringBuilder();
            for (byte b : messageDigest) {
                senhaHexadecimal.append(String.format("%02X", 0xFF & b));
            }
            senha = senhaHexadecimal.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return senha;
    }

    public static boolean compararSenha(String senha,String senhaCriptografada){
        return criptografarSenha(senha).equals(senhaCriptografada);
    }

}

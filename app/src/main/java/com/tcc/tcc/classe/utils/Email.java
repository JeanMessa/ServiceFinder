package com.tcc.tcc.classe.utils;

import android.util.Log;

import java.util.Properties;
import java.util.Random;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {

    Properties properties;
    String usuario,senha;

    String codigoValidacao;
    public Email() {
        properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        usuario = "servicefindertcc@gmail.com";
        senha = "ofzc cggg zuba smtn";
    }

    public void enviar(String email,String assunto,String corpo){
        new Thread(new Runnable() {
            public void run() {
                Session session = Session.getDefaultInstance(properties,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication()
                            {
                                return new PasswordAuthentication(usuario,
                                        senha);
                            }
                        }
                );
                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(usuario, "ServiceFinder"));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                    message.setSubject(assunto);
                    message.setContent(corpo,"text/html");
                    Transport.send(message);
                } catch (Exception e) {
                    Log.i("EMAIL", "Erro ao enviar email: "+e);
                }
            }
        }).start();
    }

    public void enviarCodigoValidacao(String email,String assunto){
        GerarCodigoValidacao();
        String corpoHTML =
                "<html>" +
                        "    <div style='display: flex'>" +
                        "        <h4 style='padding: 20px;" +
                        "            font-size: 25px;" +
                        "            background-color: #23089C;" +
                        "            color: #FFF;" +
                        "            margin: 0'>" +
                                        codigoValidacao +
                        "        </h4>" +
                        "    </div>" +
                "</html>";
        enviar(email,assunto,corpoHTML);
    }
    public void GerarCodigoValidacao(){
        Random random = new Random();
        random.setSeed(System.currentTimeMillis()+System.nanoTime());
        codigoValidacao = "";
        for (int i=0;i<8;i++){
            if (random.nextInt(2)==0){
                char c = (char) (random.nextInt(26)+65);
                codigoValidacao = codigoValidacao.concat(String.valueOf(c));
            }else{
                codigoValidacao = codigoValidacao.concat(String.valueOf(random.nextInt(10)));
            }
        }
        Log.i("Codigo de Validação", codigoValidacao);
    }

    public boolean VerificarCodigo(String codigo){
        return codigo.toUpperCase().equals(codigoValidacao);
    }

}

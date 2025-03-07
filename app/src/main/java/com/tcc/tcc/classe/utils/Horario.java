package com.tcc.tcc.classe.utils;

import android.os.StrictMode;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

public class Horario {
    public static long getHorarioAtual(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName("time-a.nist.gov");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        TimeInfo timeInfo = null;
        try {
            timeInfo = timeClient.getTime(inetAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return timeInfo.getMessage().getTransmitTimeStamp().getTime();
//        return System.currentTimeMillis();
    }

    public static long adicionarTempo(long tempo,int medida,int adicional){
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        date.setTime(tempo);
        calendar.setTime(date);
        calendar.add(medida,adicional);
        date = calendar.getTime();
        return date.getTime();
    }
}

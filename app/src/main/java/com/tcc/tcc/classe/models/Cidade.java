package com.tcc.tcc.classe.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Cidade implements Serializable {
    private String cidade;
    private String UF;

    public Cidade(){

    }
    public Cidade(String cidade, String uf) {
        this.cidade = cidade;
        UF = uf;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUF() {
        return UF;
    }

    public void setUF(String UF) {
        this.UF = UF;
    }

    public void setCidadeComEstado(String CidadeComEstado){
        UF = CidadeComEstado.substring(CidadeComEstado.length()-2);
        cidade = CidadeComEstado.replace(" - " + UF,"");
    }

    @NonNull
    @Override
    public String toString() {
        return cidade + " - " + UF;
    }

    public static String obterSigla(String estado){
        switch (estado){
            case "Acre":
                return "AC";
            case "Alagoas":
                return "AL";
            case "Amapá":
                return "AP";
            case "Amazonas":
                return "AM";
            case "Bahia":
                return "BA";
            case "Ceará":
                return "CE";
            case "Espírito Santo":
                return "ES";
            case "Goiás":
                return "GO";
            case "Maranhão":
                return "MA";
            case "Mato Grosso":
                return "MT";
            case "Mato Grosso do Sul":
                return "MS";
            case "Minas Gerais":
                return "MG";
            case "Pará":
                return "PA";
            case "Paraíba":
                return "PB";
            case "Paraná":
                return "PR";
            case "Pernambuco":
                return "PE";
            case "Piauí":
                return "PI";
            case "Rio de Janeiro":
                return "RJ";
            case "Rio Grande do Norte":
                return "RN";
            case "Rio Grande do Sul":
                return "RS";
            case "Rondônia":
                return "RO";
            case "Roraima":
                return "RR";
            case "Santa Catarina":
                return "SC";
            case "São Paulo":
                return "SP";
            case "Sergipe":
                return "SE";
            case "Tocantins":
                return "TO";
            case "Distrito Federal":
                return "DF";
            default:
                return null;
        }
    }
}

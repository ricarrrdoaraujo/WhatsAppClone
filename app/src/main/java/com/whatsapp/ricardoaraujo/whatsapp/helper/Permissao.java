package com.whatsapp.ricardoaraujo.whatsapp.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){

            List<String> listaPermissoes = new ArrayList<>();
            /*
            Percorre permissoes passadas,
            verificando uma a uma
            se já tem a permissão liberada
             */
            for (String permissao: permissoes){
                //checa se a permissao já foi concedida,
                //caso já foi não precisa solicitar novamente
                //abaixo: RECUPERA PERMISSÃO == VERIFICA SE JÁ FOI CONCEDIDA
                Boolean temPermissao = ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
                //Solicita apenas permissões que não foram concedidas
                if(!temPermissao) listaPermissoes.add(permissao);
            }

            //caso a lista esteja vazia, não é necessário solicitar permissão
            if(listaPermissoes.isEmpty()) return true;
            //cria array do tamanho da listaPermissoes para ser usado no segundo parâmetro do ActivityCompat.requestPermissions
            String[] novasPermissoes = new String[ listaPermissoes.size() ];
            listaPermissoes.toArray(novasPermissoes);

            //solicita permissao
            ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode );

        }

        return true;
    };

}

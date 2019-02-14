package com.whatsapp.ricardoaraujo.whatsapp.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.whatsapp.ricardoaraujo.whatsapp.config.ConfiguracaoFirebase;
import com.whatsapp.ricardoaraujo.whatsapp.helper.UsuarioFirebase;

import java.util.HashMap;
import java.util.Map;

public class Usuario {

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String foto;

    public Usuario() {
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuario = firebaseRef.child("usuarios").child(getId());
        // o this salva o objeto inteiro no firebase - id, nome, email e senha
        usuario.setValue(this);
    }

    public void atualizar(){
        String identificadorUsuario = UsuarioFirebase.getIdendificadorUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();

        DatabaseReference usuarioRef = database.child("usuarios")
                .child(identificadorUsuario);

        Map<String, Object> valoresUsuario = converterParaMap();
        //updateChilden recebe o tipo Map
        usuarioRef.updateChildren(valoresUsuario);
    }

    @Exclude
    public Map<String, Object> converterParaMap(){
        HashMap<String, Object> usuarioMap = new HashMap<>();
        //converter para Map para enviar como parâmetro do updateChildren
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("foto", getFoto());

        return usuarioMap;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

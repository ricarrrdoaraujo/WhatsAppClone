package com.whatsapp.ricardoaraujo.whatsapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.whatsapp.ricardoaraujo.whatsapp.R;
import com.whatsapp.ricardoaraujo.whatsapp.config.ConfiguracaoFirebase;
import com.whatsapp.ricardoaraujo.whatsapp.helper.Permissao;
import com.whatsapp.ricardoaraujo.whatsapp.helper.UsuarioFirebase;
import com.whatsapp.ricardoaraujo.whatsapp.model.Usuario;


import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private ImageButton imageButtomGallery;
    private ImageButton imageButtonCamera;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private CircleImageView imagePerfil;
    private EditText editNomeUsuario;
    private ImageView imageAtualizarNome;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private Usuario usuarioLogado;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        //Configurações iniciais
        storageReference = ConfiguracaoFirebase.getfirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdendificadorUsuario();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        imageButtomGallery = findViewById(R.id.imageButtonGallery);
        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imagePerfil = findViewById(R.id.profile_image);
        editNomeUsuario = findViewById(R.id.editNomeUsuario);
        imageAtualizarNome = findViewById(R.id.imageAtualizarNome);
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar); //suporte em versoes anteriores

        //botao voltar com apenas uma linha de código :)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperar dados usuário
        final FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();


        if(url != null){
            Glide.with(ConfiguracoesActivity.this)
                    .load(url)
                    .into(imagePerfil);
        } else{
            imagePerfil.setImageResource(R.drawable.default_img);
        }


        //Definir nome do usuário no perfil
        editNomeUsuario.setText(usuario.getDisplayName());

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // resolveActivity testa se foi possivel resolver a activity
                if(i.resolveActivity(getPackageManager()) != null ){
                    startActivityForResult(i, SELECAO_CAMERA);
                }

            }
        });

        imageButtomGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Caminho padrão de armazenar fotos: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //resolveActivity verifica se existe a galeria de fotod
                if(i.resolveActivity(getPackageManager()) != null ){
                    startActivityForResult(i, SELECAO_GALERIA);
                }

            }
        });

        imageAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = editNomeUsuario.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);

                if(retorno) {

                    usuarioLogado.setNome(nome);
                    usuarioLogado.atualizar();

                    Toast.makeText(ConfiguracoesActivity.this,
                            "Nome alterado com sucesso",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;

            try{
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);
                        break;
                }

                if(imagem != null){
                    imagePerfil.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //salvar imagem no firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            //.child(identificadorUsuario)
                            .child(identificadorUsuario + ".jpg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();

                            Uri url = taskSnapshot.getDownloadUrl();
                            atualizaFotoUsuario(url);
                        }
                    });
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void atualizaFotoUsuario(Uri url){
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if(retorno){
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();

            Toast.makeText(ConfiguracoesActivity.this,
                    "Sua foto foi alterada!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
            //verifica se permissão foi negada
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setCancelable(false);
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //fecha a activity se o usuário não der a permissão
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}

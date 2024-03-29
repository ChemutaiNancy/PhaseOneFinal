package com.example.alcphasetwo;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class InsertActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    EditText etTitle, etPrice, etDescription;
    TravelDeal deal;
    Button btnImage;
    ImageView imgShowAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
       // FirebaseUtil.openFbReference("traveldeals");
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        etTitle = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        btnImage = findViewById(R.id.btnImage);
        imgShowAvatar=findViewById(R.id.imgShowAvatar);
        final Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        etTitle.setText(deal.getTitle());
        etDescription.setText(deal.getDescription());
        etPrice.setText(deal.getPrice());

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/jpeg");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent1,"Insert Picture"),24);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin==true){
            menu.findItem(R.id.save).setVisible(true);
            menu.findItem(R.id.delete).setVisible(true);
            enabeled(true);
        }else{
            menu.findItem(R.id.save).setVisible(false);
            menu.findItem(R.id.delete).setVisible(false);
            enabeled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveDeal();
                Toast.makeText(InsertActivity.this, "Deals Saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete:
                deleteDeal();
                Toast.makeText(InsertActivity.this,"Deal deleted",Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void clean() {
        etTitle.getText().clear();
        etPrice.getText().clear();
        etDescription.getText().clear();
        etTitle.requestFocus();
    }

    private void saveDeal() {
        deal.setTitle(etTitle.getText().toString());
        deal.setPrice(etPrice.getText().toString());
        deal.setDescription(etDescription.getText().toString());
        if (deal.getId() == null) {
            databaseReference.push().setValue(deal);
        } else {
            databaseReference.push().setValue(deal);
        }
    }

    private void deleteDeal(){
        if (deal==null){
            //error
            return;
        }
        databaseReference.child(deal.getId()).removeValue();
    }

    private void backToList(){
        Intent intent = new Intent(InsertActivity.this,ListActivity.class);
        startActivity(intent);
    }

    private void enabeled(boolean isEnabled){
        etTitle.setEnabled(isEnabled);
        etPrice.setEnabled(isEnabled);
        etDescription.setEnabled(isEnabled);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 24 && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            final StorageReference storageReference = FirebaseUtil.storageReference.child(uri.getLastPathSegment());
            storageReference.putFile(uri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    String picName = taskSnapshot.getStorage().getPath();
                    deal.setUrl(url);
                    deal.setImgName(picName);
                    showImage(url);
                }
            });

        }
    }

    private void showImage(String url){
        if(url !=null && url.isEmpty()==false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(imgShowAvatar);
        }
    }
}

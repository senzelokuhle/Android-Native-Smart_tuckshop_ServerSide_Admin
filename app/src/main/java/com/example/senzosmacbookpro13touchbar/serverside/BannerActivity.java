package com.example.senzosmacbookpro13touchbar.serverside;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Common.Common;
import Interface.ItemClickListener;
import Model.Banner;
import Model.Food;
import Model.Token;
import ViewHolder.BannerViewHolder;
import ViewHolder.FoodViewHolder;
import info.hoang8f.widget.FButton;

public class BannerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RelativeLayout rootLayout;

    String FoodId = " ";

    FloatingActionButton fab_banner;

    //firebase
    FirebaseDatabase db;
    DatabaseReference banners;
    FirebaseStorage storage;
    StorageReference storageReference;


    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    EditText edtName, edtFoodId;
    Button btnUpload, btnSelect;

    Banner newBanner;

    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        db = FirebaseDatabase.getInstance();
        banners = db.getReference("Banner");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        recyclerView = (RecyclerView)findViewById(R.id.recycler_banner);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        fab_banner = (FloatingActionButton) findViewById(R.id.fab_banner);



        fab_banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddBanner();
            }
        });

        loadListBanner(FoodId);
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void updateToken(String token) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token data =new Token(token,true);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }


    private void showAddBanner() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Add new banner");
        alertDialog.setMessage("Please fill in");

        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.add_new_banner, null);

        edtFoodId = v.findViewById(R.id.edtFoodId);
        edtName = v.findViewById(R.id.edtFoodName);

        btnSelect = v.findViewById(R.id.btnSelect);
        btnUpload = v.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPicture();

            }
        });

        alertDialog.setView(v);
        alertDialog.setIcon(R.drawable.ic_slideshow_black_24dp);

        alertDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (newBanner != null)
                    banners.push().setValue(newBanner);
                loadListBanner(FoodId);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                newBanner = null;
            }
        });

        alertDialog.show();

    }

    private void uploadPicture() {


        if (filePath != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("Image/" + imageName);

            imageFolder.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Upload completed", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //value is set for new cat and link
                                    newBanner = new Banner();
                                    newBanner.setName(edtName.getText().toString());
                                    newBanner.setId(edtFoodId.getText().toString());
                                    newBanner.setImage(uri.toString());


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }

                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded!" + progress + "%");


                        }
                    });


        }
    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            btnSelect.setText("Image Selected");

        }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)) {
            showUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));

        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteBanner(adapter.getRef(item.getOrder()).getKey());
            Toast.makeText(BannerActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();


        }
        return super.onContextItemSelected(item);
    }

    private void deleteBanner(String key) {
        banners.child(key).removeValue();
    }

    private void showUpdateBannerDialog(final String key, final Banner item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Edit Banner");
        alertDialog.setMessage("Please fill in");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_banner, null);

        edtName = add_menu_layout.findViewById(R.id.edtFoodName);
        edtFoodId = add_menu_layout.findViewById(R.id.edtFoodId);
        edtName.setText(item.getName());
        edtFoodId.setText(item.getId());


        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //user will choose image

            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);

            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_slideshow_black_24dp);

        //set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();


                item.setId(edtFoodId.getText().toString());
                item.setName(edtName.getText().toString());



                Map<String,Object> update=new HashMap<>();
                update.put("name",item.getName());
                update.put("Image",item.getImage());
                update.put("Id",item.getId());

                banners.child(key)
                        .updateChildren(update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(BannerActivity.this, "Upload completed", Toast.LENGTH_SHORT).show();
                                loadListBanner(FoodId);

                            }
                        });



                banners.child(key).setValue(item);
                Snackbar.make(rootLayout, "" + item.getName() + "was edited", Snackbar.LENGTH_SHORT).show();
                loadListBanner(FoodId);


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                loadListBanner(FoodId);

            }
        });
        alertDialog.show();
    }

    private void changeImage(final Banner item) {
        if (filePath != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("Image/" + imageName);

            imageFolder.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Upload completed", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //value is set for new cat and link

                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity .this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }

                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded!" + progress + "%");


                        }
                    });


        }

    }

    private void loadListBanner(String FoodId) {
        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(
                Banner.class,
                R.layout.baner_layout,
                BannerViewHolder.class,
                banners

                ) {
            @Override
            protected void populateViewHolder(BannerViewHolder viewHolder, Banner model, int position) {
                viewHolder.banner_name.setText(model.getName());
                Picasso.with(BannerActivity.this).load(model.getImage())
                        .into(viewHolder.banner_image);



            }

        };
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


}

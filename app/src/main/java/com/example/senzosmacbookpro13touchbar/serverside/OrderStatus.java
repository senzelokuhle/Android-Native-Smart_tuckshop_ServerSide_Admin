package com.example.senzosmacbookpro13touchbar.serverside;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.jaredrummler.materialspinner.MaterialSpinner;

import Common.Common;
import Interface.ItemClickListener;
import Model.MyResponse;
import Model.Notification;
import Model.Order;
import Model.Request;
import Model.Sender;
import Model.Token;
import Remote.APIService;
import ViewHolder.OrderViewHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase db;
    DatabaseReference requests;

    MaterialSpinner spinner;

    APIService mService;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);


        db=FirebaseDatabase.getInstance();
        requests=db.getReference("Requests");

        mService=Common.getFCMClient();

        recyclerView=(RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();


    }

    private void loadOrders() {
        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));


                    }
                });
                        viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deleteOrder(adapter.getRef(position).getKey());


                            }
                        });
                        viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i=new Intent(OrderStatus.this,OrderDetail.class);
                                Common.currentRequest=model;
                                i.putExtra("OrderID",adapter.getRef(position).getKey());
                                startActivity(i);
                            }
                        });

                        viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent trackingOrder= new Intent(OrderStatus.this,TrackingOrder.class);
                                Common.currentRequest=model;
                                startActivity(trackingOrder);

                            }
                        });



            }

        };
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter );


    }



    private void deleteOrder(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();

    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater= this.getLayoutInflater();
        final View view=inflater.inflate(R.layout.update_order_layout,null);
        spinner=(MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","Processing","Shipped");

        alertDialog.setView(view);

        final String localKey=key;

        alertDialog.setPositiveButton("SET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);
                adapter.notifyDataSetChanged();

                sendOrderStatusToUser(localKey,item);
                Toast.makeText(OrderStatus.this,"Status changed!",Toast.LENGTH_SHORT).show();


            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();





    }

    private void sendOrderStatusToUser(final String key,final Request item) {

        DatabaseReference tokens=db.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            Token token=postSnapShot.getValue(Token.class);


                            Notification notification=new Notification("Your order status "+key+" was updated","Smart TuckShop");

                            Sender content=new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                            {
                                                Toast.makeText(OrderStatus.this,"Order was updated",Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else
                                            {
                                                Toast.makeText(OrderStatus.this,"Order was updated, notification error!",Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());

                                        }
                                    });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}

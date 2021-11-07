package com.example.ecommerceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.ecommerceapp.Model.Products;
import com.example.ecommerceapp.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity {

   // private FloatingActionButton addToCartBtn;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView productPrice, productDescription, productName;
    private String productID = "", state="Normal";
    private Button addToCartBtn;


    //DatabaseReference productRef = FirebaseDatabase.getInstance().getReference().child("Products");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);



        productID= getIntent().getStringExtra("pid");
        addToCartBtn = (Button) findViewById(R.id.pd_add_to_cart_btn);
        numberButton= (ElegantNumberButton) findViewById(R.id.number_btn);
        productImage= (ImageView) findViewById(R.id.product_image_details);
        productName= (TextView) findViewById(R.id.product_name_details);
        productDescription =(TextView) findViewById(R.id.product_description_details);
        productPrice = (TextView) findViewById(R.id.product_price_details);
        getProductDetails(productID);
        //Toast.makeText(this, productID, Toast.LENGTH_SHORT).show();


        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCartList();
                if(state.equals("Order placed") || state.equals("Order delivered"))
                {
                    Toast.makeText(ProductDetailsActivity.this, "you can purchase more products, once your order is delivered or confirmed ", Toast.LENGTH_LONG).show();
                }else{
                    addToCartList();
                }
                
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkOrderState();
    }

    private void addToCartList() {
        String saveCurrentTime, saveCurrentDate;
       Calendar calander = Calendar.getInstance();

       SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
       saveCurrentDate= currentDate.format(calander.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime= currentDate.format(calander.getTime());
        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        final HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("pid", productID);
        cartMap.put("pname", productName.getText().toString());
        cartMap.put("price", productPrice.getText().toString());
        cartMap.put("date", saveCurrentDate);
        cartMap.put("time", saveCurrentTime);
        cartMap.put("quantity", numberButton.getNumber());
        cartMap.put("discount", "");

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone())
                .child("Products").child(productID)
                .updateChildren(cartMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone())
                                    .child("Products").child(productID)
                                    .updateChildren(cartMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(ProductDetailsActivity.this, "Added to Cart List", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
                                                startActivity(intent);

                                            }

                                        }
                                    });
                        }

                    }
                });

    }

    private void getProductDetails(String productID) {
       DatabaseReference productRef = FirebaseDatabase.getInstance().getReference().child("Products");
        productRef.child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
               if (snapshot.exists()) {
                   Products products= snapshot.getValue(Products.class);

                   //Toast.makeText(ProductDetailsActivity.this, products.getPname(), Toast.LENGTH_SHORT).show();
                   productName.setText(products.getPname());
                   productPrice.setText(products.getPrice());
                   productDescription.setText(products.getDescription());
                   Picasso.get().load(products.getImage()).into(productImage);

               }else{

                   Toast.makeText(ProductDetailsActivity.this, productID, Toast.LENGTH_SHORT).show();
               }
            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });

    }


    private void checkOrderState()
    {
        DatabaseReference orderRef;
        orderRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String shippingState = snapshot.child("state").getValue().toString();

                    if(shippingState.equals("delivered"))
                    {

                        state ="Order delivered";

                    }
                    else if(shippingState.equals("not delivered"))
                    {
                        //txttotalAmount.setText("Total Amount = "+overTotalPrice);
                        state= "Order placed";

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
package com.example.app.Messaging.Customer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.Messaging.ConversationRecyclerView;
import com.example.app.R;
import com.example.app.Utilities.Communication;
import com.example.app.Utilities.PaymentHandler;
import com.example.app.Utilities.util;
import com.example.app.fragment.AddListBottomSheetFragment;
import com.example.app.fragment.FilterBottomSheetFragment;
import com.example.app.model.MessageObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class CustomerConversationActivity extends AppCompatActivity implements AddListBottomSheetFragment.BottomSheetListener{

    public RecyclerView mRecyclerView;
    public ConversationRecyclerView mAdapter;
    private EditText messageText;
    private String merchantId;
    private FirebaseUser currentUser;
    public List<MessageObject> messagesList = new ArrayList<>();
    private Communication communication;
    AddListBottomSheetFragment addListBottomSheetFragment;
    private final int MODE_CHAT = 0;
    private final int MODE_PAY = 1;
    private int flagMode = MODE_PAY;
    public static String pay_message = "";
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_conversation);
        mContext = this;

        String merchantName = getIntent().getStringExtra("merchantName");
        merchantId = getIntent().getStringExtra("merchantId");
        String chatId = getIntent().getStringExtra("chatId");

//        setupToolbarWithUpNav(R.id.toolbar, merchantName, R.drawable.ic_action_back);

        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText(merchantName);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ConversationRecyclerView(this, messagesList);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mAdapter.getItemCount()>0)
                    mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }, 1000);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        communication = new Communication(CustomerConversationActivity.this,
                messagesList, mAdapter, mRecyclerView, chatId, "customer", merchantName);

        communication.getMessages();

        if(util.listItems.length() > 0){
            communication.sendMessage("My List:\n=======\n"+util.listItems);
            util.listItems = "";
            Toast.makeText(this, "Your List has been Sent", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            //Uncomment the below code to Set the message and title from the strings.xml file
            builder.setMessage("Do you want it delivered ?") .setTitle("");
            //Setting message manually and performing action on button click
            builder.setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(getApplicationContext(),"Action for delivery",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //  Action for 'NO' Button
                            dialog.cancel();
                            Toast.makeText(getApplicationContext(),"Pickup Chosen",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            //Creating dialog box
            AlertDialog alert = builder.create();
            //Setting the title manually
            //alert.setTitle("AlertDialogExample");
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alert.show();
        }


        final FloatingActionButton fab = findViewById(R.id.bt_send);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flagMode == MODE_CHAT) {
                    if (!messageText.getText().toString().equals("")) {
                        communication.sendMessage(messageText.getText().toString());
                        messageText.setText("");
                    }
                } else if (flagMode == MODE_PAY) {

                    pay_message = "";

                    LayoutInflater li = LayoutInflater.from(mContext);
                    View promptsView = li.inflate(R.layout.dialog_pay, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.editTextAmount);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Pay",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                            //todo: for payment
                                            PaymentHandler paymentHandler = new PaymentHandler(CustomerConversationActivity.this,
                                                    "", "");
                                            paymentHandler.getTransactionStatus();

                                            //todo: register a receiver.

                                            // get user input and set it to result
                                            // edit text
                                            pay_message = pay_message.concat(userInput.getText().toString());
                                            communication.sendMessage(formatPaymentMessage("PAID:" + pay_message));
                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }

            }
        });

        messageText = (EditText) findViewById(R.id.et_message);
        messageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mAdapter.getItemCount()>0)
                            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                    }
                }, 500);
            }
        });

        /*
        * Dynamic Floating Action Button:
        * If the number of characters in the text box is 0 then: payment button
        * If the number of characters in the text box is > 0 then: send message button
        * */
        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = messageText.length();
                if(length > 0) {
                    fab.setImageResource(R.drawable.ic_send_white);
                    flagMode = MODE_CHAT;
                }
                else {
                    fab.setImageResource(R.drawable.ic_card_white);
                    flagMode = MODE_PAY;
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public class ViewDialog {

        public void showDialog(Activity activity) {

        }
    }

    private String formatPaymentMessage(String pay_message) {
        String temp_pay="";
        int left_hyphen = (int)Math.floor((18 - pay_message.length())/2.0f);
        int right_hyphen = (int)Math.ceil((18 - pay_message.length())/2.0f);

        for(int i=0; i < left_hyphen; i++)  temp_pay = temp_pay.concat("─");
        temp_pay = temp_pay.concat(pay_message);
        for(int i=0; i < right_hyphen; i++)  temp_pay = temp_pay.concat("─");

        return "┌───── •✧✧• ─────┐\n " + temp_pay + "\n" +
                "└───── •✧✧• ─────┘";
    }

    /*
    * When the customer clicks the plus button to add list(say grocery) to send to the customer
    * */
    public void addList(View view) {
        addListBottomSheetFragment = new AddListBottomSheetFragment();
        addListBottomSheetFragment.show(getSupportFragmentManager(), addListBottomSheetFragment.getTag());

    }

    @Override
    public void onButtonClicked(ArrayList<String> list) {
        Log.i("Customer Chat", "Total Items: " + list.size());
        String final_list = "My List \n======\n";
        for(int i = 0; i< list.size(); i++){
            final_list = final_list.concat(list.get(i));
            if(i!=list.size()-1)
                final_list = final_list.concat("\n");
        }
        if (!final_list.equals(""))
        {
            communication.sendMessage(final_list);
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        builder.setMessage("Do you want it delivered ?") .setTitle("");
        //Setting message manually and performing action on button click
        builder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(),"Action for delivery",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                        Toast.makeText(getApplicationContext(),"Pickup Chosen",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        //alert.setTitle("AlertDialogExample");
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.show();

        addListBottomSheetFragment.dismiss();
    }

}

package com.pakhi.clicksdigital.Event;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventParticipantsActivity extends AppCompatActivity {
    Event             event;
    DatabaseReference eventRef, currentEventRef, usersRef;
    List<String>             participantsList=new ArrayList<>();
    ArrayList<String> myParticipantName = new ArrayList<>();
    ArrayList<String> myParticipantEmail = new ArrayList<>();
    ArrayList<String> myParticipantNumber = new ArrayList<>();
    RecyclerView             user_recycler_list;
    FirebaseDatabaseInstance rootRef;
    Button button;
    Bitmap bmp, scaledbmp;
    String eventtitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_participants);

        rootRef=FirebaseDatabaseInstance.getInstance();

        event=(Event) getIntent().getSerializableExtra("Event");
        eventRef=rootRef.getEventRef();
        usersRef=rootRef.getUserRef();

        currentEventRef=eventRef.child(event.getEventType()).child(event.getEventId());
        currentEventRef.child("EventDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventtitle=snapshot.child("eventName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        initializeFields();
        currentEventRef.child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int participants=(int) snapshot.getChildrenCount();
                    // no_of_participants.setText(participants);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        participantsList.add(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeFields() {
        user_recycler_list=findViewById(R.id.user_recycler_list);
        user_recycler_list.setLayoutManager(new LinearLayoutManager(this));
        button = findViewById(R.id.button);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logowdc);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 300, 300, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //updateUserStatus("online");

        FirebaseRecyclerOptions<String> options=
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(currentEventRef.child("Participants"), String.class)
                        .build();
        FirebaseRecyclerAdapter<String, EventParticipantsActivity.EventParticipantsViewHolder> adapter=
                new FirebaseRecyclerAdapter<String, EventParticipantsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final EventParticipantsViewHolder holder, final int position, @NonNull final String model) {


                        final String visit_user_id=getRef(position).getKey();
                        usersRef.child(visit_user_id).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    holder.userName.setText(dataSnapshot.child(Const.USER_NAME).getValue().toString());

                                    myParticipantName.add(dataSnapshot.child(Const.USER_NAME).getValue().toString()+" "+dataSnapshot.child("last_name").getValue().toString());
                                    myParticipantNumber.add(dataSnapshot.child(Const.MO_NUMBER).getValue().toString());
                                    myParticipantEmail.add(dataSnapshot.child(Const.USER_EMAIL).getValue().toString());

                                    holder.userStatus.setText(dataSnapshot.child(Const.USER_BIO).getValue().toString());
                                    final String image_url=dataSnapshot.child(Const.IMAGE_URL).getValue().toString();
                                    Picasso.get()
                                            .load(image_url).placeholder(R.drawable.profile_image)
                                            .resize(120, 120)
                                            .into(holder.profile_image);

                                    holder.profile_image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            EnlargedImage.enlargeImage(image_url, getApplicationContext());

                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        holder.chat_with_friend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatActivity=new Intent(EventParticipantsActivity.this, ChatActivity.class);
                                chatActivity.putExtra("visit_user_id", getRef(position).getKey());
                                startActivity(chatActivity);
                            }
                        });
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // String visit_user_id = getRef(position).getKey();
                                Intent profileIntent=new Intent(EventParticipantsActivity.this, VisitProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public EventParticipantsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user, viewGroup, false);
                        EventParticipantsViewHolder viewHolder=new EventParticipantsViewHolder(view);
                        return viewHolder;
                    }
                };

        user_recycler_list.setAdapter(adapter);
        adapter.startListening();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PdfDocument myPdfDocument = new PdfDocument();
                Paint myPaint = new Paint();
                Paint titlePaint = new Paint();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
                PdfDocument.Page myPage = myPdfDocument.startPage(pageInfo);
                Canvas canvas = myPage.getCanvas();

                canvas.drawBitmap(scaledbmp, 100, 50,myPaint);

                titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
                titlePaint.setTextSize(50);
                canvas.drawText("World Digital Conclave", 420, 200, titlePaint);

                titlePaint.setTextAlign(Paint.Align.CENTER);
                titlePaint.setTextSize(40);
                canvas.drawText(eventtitle, 600, 400, titlePaint);

                titlePaint.setTextSize(30);
                canvas.drawText("Participant List", 600, 470, titlePaint);

                titlePaint.setTextAlign(Paint.Align.LEFT);
                titlePaint.setTextSize(20);
                canvas.drawText("|", 30,510,titlePaint);
                canvas.drawText("Sr.no", 40, 510, titlePaint);
                canvas.drawText("|", 190,510,titlePaint);
                canvas.drawText("Name", 200 , 510, titlePaint);
                canvas.drawText("|", 550,510,titlePaint);
                canvas.drawText("Mobile Number", 560, 510, titlePaint);
                canvas.drawText("|", 790,510,titlePaint);
                canvas.drawText("Email ID", 800, 510, titlePaint);
                canvas.drawText("|", 1170,510,titlePaint);
                canvas.drawText("________________________________________________________________________________________________________", 30,520,titlePaint);
                canvas.drawText("________________________________________________________________________________________________________", 30,483,titlePaint);

                titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
                int y = 545;
                for(int i=1; i<=myParticipantName.size(); ++i){
                    canvas.drawText("|", 30,y,titlePaint);
                    canvas.drawText("|", 190,y,titlePaint);
                    canvas.drawText("|", 550,y,titlePaint);
                    canvas.drawText("|", 790,y,titlePaint);
                    canvas.drawText("|", 1170,y,titlePaint);
                    canvas.drawText("________________________________________________________________________________________________________", 30,y+4,titlePaint);

                    canvas.drawText(String.valueOf(i), 40, y, titlePaint);
                    canvas.drawText(myParticipantName.get(i-1), 200 , y, titlePaint);
                    canvas.drawText(myParticipantNumber.get(i-1), 560, y, titlePaint);
                    canvas.drawText(myParticipantEmail.get(i-1), 800, y, titlePaint);
                    y=y+35;

                }
                myPdfDocument.finishPage(myPage);
                String directory_path = Environment.getExternalStorageDirectory().getPath() + "/WDC_PDF_TEST/";
                File file = new File(directory_path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String targetPdf = directory_path+ eventtitle + ".pdf";
                File filePath = new File(targetPdf);
                try {
                    myPdfDocument.writeTo(new FileOutputStream(filePath));
                    } catch (IOException e) {
                    Log.e("main", "error "+e.toString());
                    }
                myPdfDocument.close();
                Toast.makeText(getApplicationContext(),"The file is stored at "+targetPdf,Toast.LENGTH_LONG).show();




            }
        });
    }

    public static class EventParticipantsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profile_image;
        ImageView       chat_with_friend;

        public EventParticipantsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.display_name);
            userStatus=itemView.findViewById(R.id.user_status);
            chat_with_friend=itemView.findViewById(R.id.chat_with_friend);

            chat_with_friend.setVisibility(View.VISIBLE);
            userName.setTextColor(Color.BLACK);
            profile_image=itemView.findViewById(R.id.image_profile);
            userStatus.setVisibility(View.VISIBLE);
        }
    }
}
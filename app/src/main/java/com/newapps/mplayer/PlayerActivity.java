package com.newapps.mplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btnPause,btnFastForward,btnFastRewind,btnNext,btnPrevious;
    TextView txtName,txtStartTime,txtEndTime;
    SeekBar seekBar;
    BlastVisualizer barVisualizer;
    ImageView songImage;

    String songName;

    static MediaPlayer mediaPlayer;
    public static final String EXTRA_NAME = "song_name";
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekBar ;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(barVisualizer!=null){
            barVisualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Music Player");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnPause = findViewById(R.id.btnPause);
        btnFastForward = findViewById(R.id.btnFastForward);
        btnFastRewind = findViewById(R.id.btnFastRewind);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

        txtName = findViewById(R.id.txtSong);
        txtStartTime = findViewById(R.id.startTime);
        txtEndTime = findViewById(R.id.endTime);
        seekBar = findViewById(R.id.seekBar);
        songImage = findViewById(R.id.songImg);
        barVisualizer = findViewById(R.id.wave);

        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while(currentPosition<totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }catch(InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        if(mediaPlayer != null){
            mediaPlayer.start();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        position = bundle.getInt("pos");

        songName = mySongs.get(position).getName();
        txtName.setSelected(true);
        txtName.setText(songName);

        Uri uri = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_700),PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        String endTime = createTime(mediaPlayer.getDuration());
        txtEndTime.setText(endTime);

        Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtStartTime.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    btnPause.setBackgroundResource(R.drawable.baseline_play_arrow_24);
                }else{
                    mediaPlayer.start();
                    btnPause.setBackgroundResource(R.drawable.baseline_pause_24);
                    TranslateAnimation moveAnim = new TranslateAnimation(-25,25,-25,25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(600);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setRepeatCount(Animation.REVERSE);
                    moveAnim.setRepeatCount(1);
                    songImage.startAnimation(moveAnim);
                }
            }
        });


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1){
            barVisualizer.setAudioSessionId(audioSessionId);
        }

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position+1)%mySongs.size();
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
                songName = mySongs.get(position).getName().toString();
                txtName.setText(songName);
                startAnimation(songImage,360f);

                String endTime = createTime(mediaPlayer.getDuration());
                txtEndTime.setText(endTime);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1){
                    barVisualizer.setAudioSessionId(audioSessionId);
                }

            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position-1)<0 ? mySongs.size()-1 : position-1;
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
                songName = mySongs.get(position).getName().toString();
                txtName.setText(songName);
                startAnimation(songImage,360f);

                String endTime = createTime(mediaPlayer.getDuration());
                txtEndTime.setText(endTime);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1){
                    barVisualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);

                    int audioSessionId = mediaPlayer.getAudioSessionId();
                    if(audioSessionId != -1){
                        barVisualizer.setAudioSessionId(audioSessionId);
                    }
                }
            }
        });

        btnFastRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);

                    int audioSessionId = mediaPlayer.getAudioSessionId();
                    if(audioSessionId != -1){
                        barVisualizer.setAudioSessionId(audioSessionId);
                    }
                }
            }
        });
    }
    public void startAnimation(View view,float degree){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time = time+min+":";
        if(sec<10){
            time += "0";
        }
        time += sec;
        return time;
    }
}
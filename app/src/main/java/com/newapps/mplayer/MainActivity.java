package com.newapps.mplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        runtimePermission();
    }

    private void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    ArrayList<File> findSong(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        for(File singleFile : files){
            if(singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.addAll(findSong(singleFile));
            }else{
                if(singleFile.getName().endsWith(".wav") || singleFile.getName().endsWith(".mp3")){
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }

    public void displaySongs(){
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for(int i=0;i<mySongs.size();i++){
            items[i] = mySongs.get(i).getName().toString().replace(".mp3","").replace(".mp3","");
        }
        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),PlayerActivity.class)
                        .putExtra("pos",i)
                        .putExtra("songs",mySongs);
                startActivity(intent);
            }
        });
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = getLayoutInflater().inflate(R.layout.list_item,null);
            TextView songName = v.findViewById(R.id.songName);
            songName.setSelected(true);
            songName.setText(items[i]);
            return v;
        }
    }
}
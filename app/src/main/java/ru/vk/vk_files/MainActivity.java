package ru.vk.vk_files;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Names";


    ListView listView;
    FileListAdapter adapter;
    private ArrayList<File> files = new ArrayList<>();

    Button bthName, bthDate, bthSize, back;
    boolean[] downs = {true, true, true};
    File currentFolder;

    int REQUEST_ACTION_OPEN_DOCUMENT_TREE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list);
        bthName = findViewById(R.id.sort_name);
        bthDate = findViewById(R.id.sort_date);
        bthSize = findViewById(R.id.sort_size);
        back = findViewById(R.id.back);

        // доступен ли SD?
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            System.out.println("SD не доступна: " + Environment.getExternalStorageState());
            return;
        }
        // путь к SD
        currentFolder = Environment.getExternalStorageDirectory();

        createList(currentFolder);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), files.get(i).getPath(), Toast.LENGTH_LONG).show();
                if (files.get(i).isDirectory()) {
                    createList(files.get(i));
                }
            }
        });

        // sorting names
        bthName.setOnClickListener(view -> {
            if (downs[0]) {
                Collections.sort(files, Comparator.comparing(File::getName));
                view.setBackgroundResource(R.drawable.upward);
                downs[0] = false;
            } else {
                Collections.sort(files, (file1, file2) -> file2.getName().compareTo(file1.getName()));
                view.setBackgroundResource(R.drawable.downward);
                downs[0] = true;
            }
            adapter = new FileListAdapter(this, files);
            listView.setAdapter(adapter);
        });

        back.setOnClickListener(view -> {
            createList(currentFolder.getParentFile());
        });
    }

    public void createList(File folder) {
        currentFolder = folder;

        switch (folder.getName()) {
            case "Android": openStorage("Android");break;
            case "Download": openStorage("Download");break;
            case "DCIM": openStorage("DCIM");break;
            case "Camera": openStorage("DCIM/Camera");break;
            case "Documents": openStorage("Documents");break;
            case "data": openStorage("Android/data");break;
            default:{
                Log.i(TAG, folder.listFiles().length+"");
                files = new ArrayList<>();

                Collections.addAll(files, folder.listFiles());
                Collections.sort(files, Comparator.comparing(File::getName));
                adapter = new FileListAdapter(this, files);
                listView.setAdapter(adapter);
            }
        }
    }

    public void openStorage(String startDir) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            StorageManager sm = (StorageManager) getApplicationContext().getSystemService(Context.STORAGE_SERVICE);

            Intent intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
            //String startDir = "Android";
            //String startDir = "Download"; // Not choosable on an Android 11 device
            //String startDir = "DCIM";
            //String startDir = "DCIM/Camera";  // replace "/", "%2F"
            //String startDir = "DCIM%2FCamera";
            // String startDir = "Documents";
            //String startDir = "Android/data";

            Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");

            String scheme = uri.toString();

            Log.d(TAG, "INITIAL_URI scheme: " + scheme);

            scheme = scheme.replace("/root/", "/folder/");

            startDir = startDir.replace("/", "%2F");

            scheme += "%3A" + startDir;

            uri = Uri.parse(scheme);

            intent.putExtra("android.provider.extra.INITIAL_URI", uri);

            Log.d(TAG, "uri: " + uri.toString());

            startActivityForResult(intent, REQUEST_ACTION_OPEN_DOCUMENT_TREE);

            return;
        }
    }
}
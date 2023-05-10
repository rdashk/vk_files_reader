package ru.vk.vk_files;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FileListAdapter extends BaseAdapter {
    Context ctx;
    ArrayList<File> files;
    private static final String TAG = "Adapter";


    public FileListAdapter(Context ctx, ArrayList<File> files) {
        this.ctx = ctx;
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.i(TAG, getCount()+"");
        if (getCount() > 0) {
            File file = files.get(position);

            // создаём разметку (контейнер)
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item, parent, false);

            ImageView icon = convertView.findViewById(R.id.item_icon);
            TextView name = convertView.findViewById(R.id.item_name);
            TextView date = convertView.findViewById(R.id.item_date);
            TextView size = convertView.findViewById(R.id.item_size);

            name.setText(file.getName());
            date.setText(getDate(file.getAbsolutePath()));

            Long fileLength = file.length();
            if (fileLength/1024 > 0) {
                if (file.length()/(1024*1024) > 0) {
                    size.setText(getFileSizeMegaBytes(fileLength));
                }
                else {
                    size.setText(getFileSizeKiloBytes(fileLength));
                }
            } else {
                size.setText(fileLength + " bytes");
            }

            if (file.isDirectory()) {
                icon.setImageResource(R.drawable.folder);
            } else{
                icon.setImageResource(R.drawable.doc);
            }

            return convertView;
        }
        return LayoutInflater.from(ctx).inflate(R.layout.empty_item, parent, false);
    }

    // размер файла в мегабайтах
    private String getFileSizeMegaBytes(Long len) {
        return (double) len/(1024*1024)+" MB";
    }

    // размер файла в килобайтах
    private String getFileSizeKiloBytes(Long len) {
        return (double) len/1024 + " KB";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDate(String filePath) {
        SimpleDateFormat dFormat = null;
        Date d = new Date();
        try {
            Path path = Paths.get(filePath);
            FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
            d = new Date(creationTime.toMillis());
            String pattern = "dd.MM.yyyy";
            dFormat = new SimpleDateFormat(pattern);
        } catch (IOException ex) {
            // handle exception
        }
        return dFormat.format(d);
    }
}

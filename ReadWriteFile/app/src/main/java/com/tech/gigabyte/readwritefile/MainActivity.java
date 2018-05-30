package com.tech.gigabyte.readwritefile;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

//BufferedReader = Reads text from a character-input stream

public class MainActivity extends AppCompatActivity {
    EditText type;
    Button add, delete;
    TextView display;
    String filename = "tests.txt";
    private static final int PERMISSION_REQUEST_CODE = 100;

    //An abstract representation of file and directory
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the activity content from a layout resource
        setContentView(R.layout.activity_main);
        type = (EditText) findViewById(R.id.editText_typedata);
        add = (Button) findViewById(R.id.button_add);
        delete = (Button) findViewById(R.id.button_delete);
        display = (TextView) findViewById(R.id.textView_display);

        //Get a top-level shared/external storage directory for placing files of a particular type
        String path = Environment.getExternalStoragePublicDirectory("/").getAbsolutePath();

        file = new File(path, filename);
        try {
            if (file.createNewFile())
                Toast.makeText(MainActivity.this, "File created" + file, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Adding Text to File
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = type.getText().toString();
                type.setText("");
                String state = getstoragestate();

                //Storage state if the media is present and mounted at its mount point with read/write access.
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    if (checkPermission()) {
                        Toast.makeText(MainActivity.this, "Permission for Storing into External storage granted", Toast.LENGTH_SHORT).show();
                        MyAsync myAsync = new MyAsync(file);
                        myAsync.execute(value);
                    } else if (!checkPermission()) {
                        requestPermission();
                        Toast.makeText(MainActivity.this, "Requesting permission", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission granted already", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Deleting Saved File
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.exists()) {
                    file.delete();
                    display.setText("");
                    Snackbar.make(view, "File Deleted", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private class MyAsync extends AsyncTask<String, Integer, String> {
        File file;

        MyAsync(File file) {
            this.file = file;
        }

        @Override

        //computation on a background thread
        protected String doInBackground(String... strings) {
            String enter = "\n";
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file, true);
                fileWriter.append("\n").append(strings[0]);

                //For writing streams of characters
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert fileWriter != null;
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String name = "";
            //string builder with no characters in it
            StringBuilder sb = new StringBuilder();
            FileReader fileReader = null;

            try {
                fileReader = new FileReader(file);
                BufferedReader br = new BufferedReader(fileReader);
                while ((name = br.readLine()) != null) {
                    sb.append(name);
                    sb.append("\n");
                }
                br.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            display.setText(sb.toString());
        }
    }

    //Geting Storage State
    public String getstoragestate() {
        boolean isavailable = false;
        boolean iswritable = false;
        String state = Environment.getExternalStorageState();
        Toast.makeText(MainActivity.this, "State" + state, Toast.LENGTH_LONG).show();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            isavailable = iswritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            isavailable = true;
            iswritable = false;
        } else {
            isavailable = iswritable = false;
        }
        return state;
    }

    //Checking Permission
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    //Requesting Permission
    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            boolean StorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (StorageAccepted) {
                Toast.makeText(this, "Permission for Storage granted", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}

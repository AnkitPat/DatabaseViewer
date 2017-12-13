package com.example.bbim1043.databaseviewer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class SelectDatabase extends AppCompatActivity {

    private static int REQUEST_FILE = 101;

    TextView filePathText;

    ArrayList<String> str = new ArrayList<String>();

    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;

    private static final String TAG = "F_PATH";

    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory() + "");
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;

    ListAdapter adapter;

    private int REQUEST_CODE = 101;

    ListView recentListView;

    CustomListAdapter recentListAdapter;
    private MyOwnDatabase myOwnDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_select_database);


        recentListView = (ListView) findViewById(R.id.recent_list_view);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }


        myOwnDatabase = new MyOwnDatabase(this);

        recentListAdapter = new CustomListAdapter( myOwnDatabase.getRecentDatabase());

        recentListView.setAdapter(recentListAdapter);

        recentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                filePathText.setVisibility(View.VISIBLE);
                filePathText.setText(myOwnDatabase.getRecentDatabase().get(i));
            }
        });

        filePathText = (TextView) findViewById(R.id.filePath);
        filePathText.setVisibility(View.GONE);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file*//*");
                startActivityForResult(intent,REQUEST_FILE);*/

                loadFileList();
                showDialog(DIALOG_LOAD_FILE);
                //    startActivityForResult(new Intent(SelectDatabase.this,FileExplore.class),REQUEST_FILE);


            }
        });

        Button nextButton = (Button) findViewById(R.id.moveToDatabase);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!filePathText.getText().toString().isEmpty()) {
                    Intent intent = new Intent(SelectDatabase.this, MainActivity.class);
                    intent.putExtra("FilePath", filePathText.getText().toString().replace("file:", ""));

                    startActivity(intent);


                    myOwnDatabase.insertRecentAccessData(filePathText.getText().toString().replace("file:", ""));

                    myOwnDatabase.close();

                }
            }
        });

    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return ((sel.isFile() && sel.getName().contains(".sqlite")) || sel.isDirectory())
                            ;

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.folder_icon;
                    Log.d("DIRECTORY", fileList[i].file);
                } else {
                    Log.d("FILE", fileList[i].file);
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", R.drawable.up_image);
                fileList = temp;
            }
        } else {
            Log.e(TAG, "path does not exist");
        }

        adapter = new ArrayAdapter<Item>(this,
                android.R.layout.select_dialog_item, android.R.id.text1,
                fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                textView.setTextSize(15);
                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        fileList[position].icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        if (fileList == null) {
            Log.e(TAG, "No files loaded");
            dialog = builder.create();

            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            return dialog;
        }

        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenFile = fileList[which].file;
                        File sel = new File(path + "/" + chosenFile);
                        if (sel.isDirectory()) {
                            firstLvl = false;

                            // Adds chosen directory to list
                            str.add(chosenFile);
                            fileList = null;
                            path = new File(sel + "");

                            loadFileList();

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(TAG, path.getAbsolutePath());

                        }

                        // Checks if 'up' was clicked
                        else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

                            // present directory removed from list
                            String s = str.remove(str.size() - 1);

                            // path modified to exclude present directory
                            path = new File(path.toString().substring(0,
                                    path.toString().lastIndexOf(s)));
                            fileList = null;

                            // if there are no more directories in the list, then
                            // its the first level
                            if (str.isEmpty()) {
                                firstLvl = true;
                            }
                            loadFileList();

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(TAG, path.getAbsolutePath());

                        }
                        // File picked
                        else {
                            // Perform action with file picked
                            filePathText.setVisibility(View.VISIBLE);
                            filePathText.setText(sel.getPath());

                        }

                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILE && data != null) {


            String filePath = data.getStringExtra("File_Path");

            filePathText.setVisibility(View.VISIBLE);
            filePathText.setText(filePath);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

            } else {
                Toast.makeText(SelectDatabase.this, "App Will close as permission is not granted", Toast.LENGTH_SHORT).show();

                finish();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        recentListAdapter = new CustomListAdapter(myOwnDatabase
                .getRecentDatabase());

        recentListView.setAdapter(recentListAdapter);

    }


    private class CustomListAdapter extends BaseAdapter{
        
        
        ArrayList<String> recentDataListPath;
        
        CustomListAdapter(ArrayList<String> recentDataListPath)
        {
            this.recentDataListPath  =  recentDataListPath;
        }

        @Override
        public int getCount() {
            return recentDataListPath.size();
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

            LayoutInflater inflater = (LayoutInflater)getSystemService
                    (LAYOUT_INFLATER_SERVICE);
            
            if(view==null)
                view = inflater.inflate(R.layout.recent_list_item,viewGroup,
                        false);
            
            
            TextView titleText = view.findViewById(R.id.titleName);
            TextView filepathText = view.findViewById(R.id.filePathName);
            
            File file = new File(recentDataListPath.get(i));
            
            titleText.setText(file.getName());
            
            filepathText.setText(file.getPath());
            
            
            return view;
        }
    }
}

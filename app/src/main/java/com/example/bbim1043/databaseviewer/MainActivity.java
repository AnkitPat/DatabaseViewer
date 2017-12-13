package com.example.bbim1043.databaseviewer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.example.bbim1043.databaseviewer.QueryOrganiser.Query;

public class MainActivity extends AppCompatActivity implements ModifyOperationInterface, ChangeRecordInterface {

    Spinner tableSpinner;
    Button previousButton,nextButton;

    ArrayList<Cursor> cursorArrayList;

    TableLayout tableLayout;

    TextView countRecordText;

    Cursor mainCursor;

    int totalEntriesOnOnePage=10;
    int initialIndex=0;
    int finalIndex = totalEntriesOnOnePage-1;


    private int count;

    int initialTemp=0;

    int totalPages=0;
    private int extraEntries=0;

    int currentPage=1;

    TableLayout firstColumnTable;

    ArrayList<String> columnList;

    String selectedTableName;
    private ArrayList<String> schemaArray;
    private ArrayList<String> selectedRowValues;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

      Intent intent = getIntent();
        String action = intent.getAction();
        if(Intent.ACTION_VIEW.equalsIgnoreCase(action)) {

            Uri uri = intent.getData();
            String filePath = uri.getEncodedPath();
            
            
            
            DatabaseHelper.DB_PATH =filePath;

            File file = new File(DatabaseHelper.DB_PATH);

            DatabaseHelper.DB_NAME = file.getName();

        }
        else
        {
            DatabaseHelper.DB_PATH = intent.getStringExtra("FilePath");

            File file = new File(DatabaseHelper.DB_PATH);

            DatabaseHelper.DB_NAME = file.getName();
        }
        columnList = new ArrayList<>();



        countRecordText = (TextView)findViewById(R.id.countRecordText);
        tableSpinner = (Spinner)findViewById(R.id.tableSpinner);
        previousButton = (Button)findViewById(R.id.previousButton);
        nextButton = (Button)findViewById(R.id.nextButton);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.spinner_single_item,R.id.textId,getTableName());

        tableSpinner.setAdapter(arrayAdapter);




        tableLayout = (TableLayout)findViewById(R.id.tableLayout);

        firstColumnTable = (TableLayout)findViewById(R.id.tableLayoutFirst);
        firstColumnTable.bringToFront();

        tableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i!=0) {
                    initialIndex=0;
                    finalIndex=totalEntriesOnOnePage-1;
                    refreshTable(getTableName().get(i));

                    selectedTableName = getTableName().get(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                /*finalIndex=initialIndex;
                initialIndex=initialTemp-totalEntriesOnOnePage;
                if(!(initialIndex<0))
                {
                    addTableHeader();
                }
               else
                {
                    Toast.makeText(MainActivity.this,"You are on first page",Toast.LENGTH_SHORT).show();
                }*/


                if(currentPage<=totalPages && currentPage!=0 && currentPage!=1)
                {
                    initialIndex=initialTemp-totalEntriesOnOnePage;
                    finalIndex-=totalEntriesOnOnePage;
                    initialTemp=initialIndex;
                    addTableHeader();
                    currentPage-=1;
                }

                else if(currentPage==totalPages+1 && currentPage!=0 && currentPage!=1)
                {
                    initialIndex=initialTemp-totalEntriesOnOnePage;
                    finalIndex-=totalEntriesOnOnePage;
                    initialTemp=initialIndex;
                    addTableHeader();
                    currentPage-=1;
                }
                else
                {
                    Toast.makeText(MainActivity.this,"You are on first page",Toast.LENGTH_SHORT).show();
                }


            }
        });




        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            /*    finalIndex=finalIndex+totalEntriesOnOnePage;

                if (finalIndex<count-1) {
                    addTableHeader();
                }
                else
                {
                    finalIndex = finalIndex- finalIndex%(count+1);

                    addTableHeader();
                }

                initialTemp=initialIndex;*/


            if(currentPage<=totalPages)
            {
                initialIndex=initialTemp+totalEntriesOnOnePage;
                finalIndex+=totalEntriesOnOnePage;
                initialTemp=initialIndex;
                addTableHeader();
                currentPage+=1;

            }
         /*   else if(currentPage==totalPages+1)
            {
                initialIndex+=extraEntries;
                finalIndex+=extraEntries;
                initialTemp=initialIndex;
                addTableHeader();
                currentPage++;


            }*/
            else
            {
                Toast.makeText(MainActivity.this,"You are on last page",Toast.LENGTH_SHORT).show();
            }

            }
        });

    }


        public void refreshTable(String tablename)
        {
            tableLayout.removeAllViews();

            fetchTableCursor(tablename);

            addTableHeader();
        }

        public void fetchTableCursor(String tableName)
        {
            String Query2 ="select * from "+tableName;

            ArrayList<Cursor> columnArrayList = getData(Query2);

            mainCursor=columnArrayList.get(0);


            if (mainCursor!=null) {
                mainCursor.moveToFirst();
                count = mainCursor.getCount();


                countRecordText.setText(count+" ");
            }


            totalPages = count/totalEntriesOnOnePage;
            extraEntries=  count%totalEntriesOnOnePage;


        }

        public void addTableHeader()
        {

            columnList = new ArrayList<>();
            tableLayout.removeAllViews();
            firstColumnTable.removeAllViews();


            if (mainCursor!=null) {
                TableRow row = new TableRow(this);




                for(int i=0;i<mainCursor.getColumnCount();i++) {



                    TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

                    row.setLayoutParams(lp);

                    TextView textView =(TextView) LayoutInflater.from(this).inflate(R.layout.single_tupple,null);






                    textView.setText(mainCursor.getColumnName(i));

                    textView.setPadding(5, 0, 5, 0);
                    textView.setTextSize(15f);
                    textView.setGravity(Gravity.CENTER);

                    row.addView(textView);

                    View view = new View(this);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
                    view.setLayoutParams(layoutParams);
                    view.setBackgroundColor(Color.BLACK);

                    row.addView(view);

                    columnList.add(mainCursor.getColumnName(i));

                }

                TableRow firstColumnRow = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

                firstColumnRow.setLayoutParams(lp);

                TextView textView =(TextView) LayoutInflater.from(this).inflate(R.layout.single_tupple,null);






                textView.setText(mainCursor.getColumnName(0));

                textView.setPadding(5, 0, 5, 0);
                textView.setTextSize(15f);

                textView.setGravity(Gravity.CENTER);

                firstColumnRow.addView(textView);

                View view = new View(this);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(Color.BLACK);

                firstColumnRow.addView(view);

                firstColumnTable.addView(firstColumnRow);

                tableLayout.addView(row);

                addRows(mainCursor);
            }

        }

    private void addRows(Cursor cursor) {






        do{
            if (cursor.moveToPosition(initialIndex)) {
                TableRow row = new TableRow(this);
                for (int i = 0; i <cursor.getColumnCount();i++){


                    TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

                    row.setLayoutParams(lp);

                    TextView textView = new TextView(this);


                    textView.setTag(columnList.get(i));
                    textView.setText(cursor.getString(i));

                    textView.setPadding(5, 0, 5, 0);
                    textView.setTextSize(15f);
                    textView.setGravity(Gravity.CENTER);

                    row.addView(textView);

                    View view = new View(this);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
                    view.setLayoutParams(layoutParams);
                    view.setBackgroundColor(Color.BLACK);


                    row.addView(view);

                }

                initialIndex++;
                row.setTag(initialIndex);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      /*  TableRow row = (TableRow)(tableLayout.findViewWithTag(firstColumnTable.indexOfChild(firstColumnRow)));

                        selectedRowValues = new ArrayList<String>();
                        for(int i=0;i<columnList.size();i++) {

                            TextView textView1 = (TextView) row.findViewWithTag(columnList.get(i));

                            selectedRowValues.add(textView1.getText().toString());
                        }*/
                    }
                });
                tableLayout.addView(row);
            } else {
                if (initialIndex<0) {
                    Toast.makeText(MainActivity.this,"you are on first page",Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this,"you are on last page",Toast.LENGTH_SHORT).show();
                }


            }

            final TableRow firstColumnRow = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

            firstColumnRow.setLayoutParams(lp);

            TextView textView = new TextView(this);


            textView.setText(cursor.getString(0));

            textView.setTag("First_column");
            textView.setPadding(5, 0, 5, 0);
            textView.setTextSize(15f);
            textView.setGravity(Gravity.CENTER);

            firstColumnRow.addView(textView);

            View view = new View(this);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(layoutParams);
            view.setBackgroundColor(Color.BLACK);

            firstColumnRow.addView(view);

            firstColumnTable.addView(firstColumnRow);

            firstColumnRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(MainActivity.this,((TextView)firstColumnRow.findViewWithTag("First_column")).getText().toString(),Toast.LENGTH_SHORT).show();

                    TableRow row = (TableRow)(tableLayout.findViewWithTag(firstColumnTable.indexOfChild(firstColumnRow)));

                   selectedRowValues = new ArrayList<String>();
                    for(int i=0;i<columnList.size();i++) {

                        TextView textView1 = (TextView) row.findViewWithTag(columnList.get(i));

                        selectedRowValues.add(textView1.getText().toString());
                    }
                   // Toast.makeText(MainActivity.this,textView1.getText().toString(),Toast.LENGTH_SHORT).show();

                   schemaArray = getColumnSchema(columnList,selectedTableName);

                    EditFragment editFragment = new EditFragment();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("ColumnList",columnList);
                    bundle.putStringArrayList("SelectedRow",selectedRowValues);
                    bundle.putStringArrayList("Schema",schemaArray);
                    editFragment.setArguments(bundle);
                    editFragment.show(getFragmentManager(),"Edit Dialog");
                }
            });


        }while(cursor.moveToNext() && initialIndex<=finalIndex);

    }





    public ArrayList<String> getTableName()
    {
        cursorArrayList = getData(Query);


        final Cursor c=cursorArrayList.get(0);

        //the second cursor has error messages
        Cursor Message =cursorArrayList.get(1);

        Message.moveToLast();
        String msg = Message.getString(0);
        Log.d("Message from sql = ",msg);

        ArrayList<String> tablenames = new ArrayList<String>();

        if(c!=null)
        {

            c.moveToFirst();
            tablenames.add("click here");
            do{
                //add names of the table to tablenames array list
                tablenames.add(c.getString(0));
            }while(c.moveToNext());
        }

        return tablenames;
    }

    public static ArrayList<Cursor> getData(String Query){
        //get writable database

        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        SQLiteDatabase sqlDB = null;
        try {
             databaseHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            databaseHelper.openDatabase();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        sqlDB = databaseHelper.database;

        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }

    public static ArrayList<String> getColumnSchema(ArrayList<String> columnList,String selectedTableName)
    {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        SQLiteDatabase sqlDB = null;
        try {
            databaseHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            databaseHelper.openDatabase();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        sqlDB = databaseHelper.database;

        ArrayList<String> schemaArrayList = new ArrayList<>();

        for(String columnName: columnList)
        {
            Cursor typeCursor = sqlDB.rawQuery("select typeof ("+columnName+") from "+selectedTableName,null);

            typeCursor.moveToFirst();

            schemaArrayList.add(typeCursor.getString(0));

        }

        return schemaArrayList;
    }

    @Override
    public void deleteRow(ArrayList<String> selectedRow) {

        DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
        SQLiteDatabase sqlDB = null;
        try {
            databaseHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            databaseHelper.openDatabase();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        sqlDB = databaseHelper.database;




        int deleted=0;
        if (selectedRow.size()>2) {
            String where = "";
            for(int j=0;j<3;j++)
            {
                if (j!=2) {
                    if(!selectedRowValues.get(j).isEmpty())
                        where = where+columnList.get(j)+" = '" + selectedRowValues.get(j) +"' AND ";
                }
                else
                {
                    if(!selectedRowValues.get(j).isEmpty())
                        where = where+columnList.get(j)+" = '" + selectedRowValues.get(j)+"'";
                }
            }
            deleted = sqlDB.delete(selectedTableName,where,null);
        } else if(selectedRow.size()>1) {
            deleted = sqlDB.delete(selectedTableName,columnList.get(0)+"=?,"+columnList.get(1)+"=?",new String[]{selectedRowValues.get(0),selectedRowValues.get(1)});

        }

        System.out.println("Table data"+selectedTableName+deleted+"");
        initialIndex=0;
        finalIndex=totalEntriesOnOnePage;
        refreshTable(selectedTableName);

    }

    @Override
    public void modifyData(ArrayList<String> selectedRow) {

        DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);

        SQLiteDatabase sqlDB = null;
        try {
            databaseHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            databaseHelper.openDatabase();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        sqlDB = databaseHelper.database;

        ContentValues contentValues = new ContentValues();
        for(int i=0;i<columnList.size();i++)
        {
            contentValues.put(columnList.get(i),String.valueOf(selectedRow.get(i)));


        }



        int updated=0;
        if (selectedRow.size()>2) {
            String where = "";
            for(int j=0;j<3;j++)
            {
                if (j!=2) {
                    if(!selectedRowValues.get(j).isEmpty())
                    where = where+columnList.get(j)+" = '" + selectedRowValues.get(j) +"' AND ";
                }
                else
                {
                    if(!selectedRowValues.get(j).isEmpty())
                        where = where+columnList.get(j)+" = '" + selectedRowValues.get(j)+"'";
                    else
                        where = where.replace("AND","");
                }
            }
             updated = sqlDB.update(selectedTableName,contentValues,where,null);
        } else if(selectedRow.size()>1) {
            updated = sqlDB.update(selectedTableName,contentValues,columnList.get(0)+"=?,"+columnList.get(1)+"=?",new String[]{selectedRowValues.get(0),selectedRowValues.get(1)});

        }

        System.out.println("Table data"+selectedTableName+updated+"");
        initialIndex=0;
        finalIndex=totalEntriesOnOnePage;
        refreshTable(selectedTableName);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())

        {
            case R.id.changeRecordToShow:
                ChangeRecordCountFragment changeRecordCountFragment = new ChangeRecordCountFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("Current_count",totalEntriesOnOnePage);
                changeRecordCountFragment.setArguments(bundle);
                changeRecordCountFragment.show(getFragmentManager(),"Change Record");
                break;

            case R.id.query_runner:
                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);

                SQLiteDatabase sqlDB = null;
                try {
                    databaseHelper.createDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    databaseHelper.openDatabase();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                sqlDB = databaseHelper.database;
                Intent intent = new Intent(MainActivity.this,QueryRunner.class);

                intent.putStringArrayListExtra("TableArray",getTableName());


                startActivity(intent);

        }
        return true;
    }

    @Override
    public void changeRecordTo(int recordCount) {
        totalEntriesOnOnePage=recordCount;

        initialIndex=0;
        finalIndex=totalEntriesOnOnePage;

        refreshTable(selectedTableName);
    }



}

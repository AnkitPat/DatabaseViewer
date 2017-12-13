package com.example.bbim1043.databaseviewer;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.example.bbim1043.databaseviewer.MainActivity.getData;

/**
 * Created by Ankit patidar on 18/09/17.
 */

public class QueryRunner extends AppCompatActivity {

    EditText queryEditText;
    SQLiteDatabase sqlDB;

    Button selectQueryButton, deleteQueryButton, updateQueryButton, insertQueryButton;

    String selectedTableName = "";

    ArrayList<String> tableArrayList;
    private TextView toolBarText;

    LinearLayout selectView;

    StringBuilder selectQueryBuilder = null;

    ArrayList<String> selectQuery = new ArrayList<>();

    TableLayout resultTable, firstColumnTable;
    private Cursor mainSelectTableCursor;
    private int totalCountOfRowsSelect;
    private int totalEntriesOnOnePage = 10;
    private int totalPages = 0;
    private int extraEntries = 0;

    int initialIndex = 0;
    int finalIndex = totalEntriesOnOnePage - 1;


    ArrayList<String> columnList;
    private Button previousButton, nextButton;

    int initialTemp = 0, currentPage = 1;

    LinearLayout tableSelectFrame;
    private LinearLayout insertView;
    private LinearLayout contentViewAdder;

    HashMap<String, String> insertValues, deleteValues;
    private ArrayList<String> schemaList;

    String currentWorking = "";
    private LinearLayout deleteView;
    private LinearLayout contentViewDeleteAdder;

    TextView deleteInfoTextView;

    LinearLayout whereConditionLinear;
    LinearLayout setValuesLinear;
    private LinearLayout updateView;
    private ScrollView updateScroll;
    private LinearLayout contentViewUpdateLinear, setValueUpdateLinear;
    private HashMap<String, String> whereConditionValues, setUpdateValues;

    TextView updateTypeText;

    EditText updateQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        insertValues = new HashMap<>();
        deleteValues = new HashMap<>();
        columnList = new ArrayList<>();
        whereConditionValues = new HashMap<>();
        setUpdateValues = new HashMap<>();

        setContentView(R.layout.query_runner);

        selectQueryBuilder = new StringBuilder("Select ");

        tableArrayList = (ArrayList<String>) getIntent().getStringArrayListExtra("TableArray");

        DatabaseHelper databaseHelper = new DatabaseHelper(QueryRunner.this);

        sqlDB = null;
        try {
            databaseHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            databaseHelper.openDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sqlDB = databaseHelper.database;


        tableSelectFrame = (LinearLayout) findViewById(R.id.tableSelectQuery);

        previousButton = (Button) findViewById(R.id.previousButton);
        nextButton = (Button) findViewById(R.id.nextButton);

        toolBarText = (TextView) findViewById(R.id.toolbarTextView);
        toolBarText.setText(DatabaseHelper.DB_NAME.replace("/", ""));

        resultTable = (TableLayout) findViewById(R.id.resultTable);
        firstColumnTable = (TableLayout) findViewById(R.id.tableLayoutFirst);
        firstColumnTable.bringToFront();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deleteInfoTextView = (TextView) findViewById(R.id.deleteInfoTextView);

        selectQueryButton = (Button) findViewById(R.id.select);
        deleteQueryButton = (Button) findViewById(R.id.delete);
        updateQueryButton = (Button) findViewById(R.id.update);
        insertQueryButton = (Button) findViewById(R.id.insert);

        selectView = (LinearLayout) findViewById(R.id.selectView);
        insertView = (LinearLayout) findViewById(R.id.insertView);
        deleteView = (LinearLayout) findViewById(R.id.deleteView);
        updateView = (LinearLayout) findViewById(R.id.updateView);

        contentViewAdder = (LinearLayout) insertView.findViewById(R.id.contentValueLinear);
        contentViewDeleteAdder = (LinearLayout) deleteView.findViewById(R.id.contentValueDeleteLinear);
        contentViewUpdateLinear = (LinearLayout) updateView.findViewById(R.id.contentValueUpdateLinear);


        updateTypeText = (TextView) updateView.findViewById(R.id.updateTypeText);
        updateQuery = (EditText) updateView.findViewById(R.id.updateQuery);

        updateScroll = (ScrollView) updateView.findViewById(R.id.updateScroll);

        selectQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedTableName.isEmpty()) {
                    selectTableDialog("select");
                    currentWorking = "select";
                } else {
                    initialiseSelectViewLayout(selectView);

                    currentWorking = "select";
                }

                queryEditText.setText("Select ");


            }
        });

        insertQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedTableName.isEmpty()) {
                    selectTableDialog("insert");

                    currentWorking = "insert";
                } else {
                    initialiseInsertViewLayout(insertView);

                    currentWorking = "insert";
                }
            }
        });

        deleteQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedTableName.isEmpty()) {
                    selectTableDialog("delete");

                    currentWorking = "delete";
                } else {
                    initialiseDeleteViewLayout(deleteView);

                    currentWorking = "delete";
                }
            }
        });

        updateQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (selectedTableName.isEmpty()) {
                    selectTableDialog("update");

                    currentWorking = "update";
                } else {

                    initialiseUpdateViewLayout(updateView, 1);

                    currentWorking = "update";
                    setUpdateValues = new HashMap<>();
                    whereConditionValues = new HashMap<>();
                }

            }
        });


        final TextView errorTextView = (TextView) findViewById(R.id.errorTextView);

        queryEditText = (EditText) findViewById(R.id.queryEditText);

        Button runQuery = (Button) findViewById(R.id.run_query_button);
        runQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    if (currentWorking.equalsIgnoreCase("select")) {
                        Cursor cursor = sqlDB.rawQuery(queryEditText.getText().toString(), new String[]{});

                        tableSelectFrame.setVisibility(View.VISIBLE);
                        initialIndex = 0;
                        finalIndex = totalEntriesOnOnePage - 1;
                        refreshTable(cursor);

                        tableSelectFrame.setVisibility(View.VISIBLE);

                        errorTextView.setVisibility(View.GONE);
                    } else if (currentWorking.equalsIgnoreCase("insert")) {
                        ContentValues contentValues = new ContentValues();
                        insertValues.put(selectedItem, spinnerSelectedItem);
                        Set keys = insertValues.entrySet();
                        Iterator it = keys.iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            contentValues.put(entry.getValue().toString(), entry.getKey().toString());
                        }

                        long valueInserted = sqlDB.insert(selectedTableName, "", contentValues);
                        if (valueInserted > 0) {

                            tableSelectFrame.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.VISIBLE);
                            errorTextView.setText("Inserted Value");
                            errorTextView.setTextColor(Color.GREEN);
                        }


                    } else if (currentWorking.equalsIgnoreCase("delete")) {
                        ContentValues contentValues = new ContentValues();
                        deleteValues.put(deleteSelectedItem, deleteSpinnerSelectedItem);
                        Set keys = deleteValues.entrySet();
                        Iterator it = keys.iterator();
                        String whereClause = "";
                        ArrayList<String> whereArgsArrayList = new ArrayList<String>();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            // contentValues.put(entry.getValue().toString(),entry.getKey().toString());

                            whereClause = whereClause + entry.getValue().toString() + "=?,";
                            whereArgsArrayList.add(entry.getKey().toString());

                        }

                        long valueDeleted = sqlDB.delete(selectedTableName, whereClause.substring(0, whereClause.length() - 1), whereArgsArrayList.toArray(new String[whereArgsArrayList.size()]));
                        if (valueDeleted > 0) {

                            tableSelectFrame.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.VISIBLE);
                            errorTextView.setText("Value Deleted");
                            errorTextView.setTextColor(Color.GREEN);
                        }
                    } else if (currentWorking.equalsIgnoreCase("update")) {

                        //Cursor cursor = sqlDB.rawQuery(updateQuery.getText().toString(), new String[]{});

                        int updated = sqlDB.update(selectedTableName,updateContentValues,whereValues.substring(0,whereValues.length()-1),whereArg.toArray(new String[whereArg.size()]));
                      /*  if (cursor.moveToFirst()) {
                            tableSelectFrame.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.VISIBLE);
                            errorTextView.setText("Value Updated");
                            errorTextView.setTextColor(Color.GREEN);
                        }*/

                      if(updated>0)
                      {
                          tableSelectFrame.setVisibility(View.GONE);
                          errorTextView.setVisibility(View.VISIBLE);
                          errorTextView.setText("Value Updated");
                          errorTextView.setTextColor(Color.GREEN);
                      }
                      else
                      {
                          tableSelectFrame.setVisibility(View.GONE);
                          errorTextView.setVisibility(View.VISIBLE);
                          errorTextView.setText("Nothing to update");
                          errorTextView.setTextColor(Color.GREEN);
                      }
                    }


                } catch (Exception e) {
                    insertView.setVisibility(View.GONE);
                    selectView.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText(e.getMessage());
                    tableSelectFrame.setVisibility(View.GONE);
                }

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


                if (currentPage <= totalPages && currentPage != 0 && currentPage != 1) {
                    initialIndex = initialTemp - totalEntriesOnOnePage;
                    finalIndex -= totalEntriesOnOnePage;
                    initialTemp = initialIndex;
                    addTableHeader();
                    currentPage -= 1;
                } else if (currentPage == totalPages + 1 && currentPage != 0 && currentPage != 1) {
                    initialIndex = initialTemp - totalEntriesOnOnePage;
                    finalIndex -= totalEntriesOnOnePage;
                    initialTemp = initialIndex;
                    addTableHeader();
                    currentPage -= 1;
                } else {
                    Toast.makeText(QueryRunner.this, "You are on first page", Toast
                            .LENGTH_SHORT).show();
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


                if (currentPage <= totalPages) {
                    initialIndex = initialTemp + totalEntriesOnOnePage;
                    finalIndex += totalEntriesOnOnePage;
                    initialTemp = initialIndex;
                    addTableHeader();
                    currentPage += 1;

                }
         /*   else if(currentPage==totalPages+1)
            {
                initialIndex+=extraEntries;
                finalIndex+=extraEntries;
                initialTemp=initialIndex;
                addTableHeader();
                currentPage++;


            }*/
                else {
                    Toast.makeText(QueryRunner.this, "You are on last page", Toast
                            .LENGTH_SHORT).show();
                }

            }
        });


    }

    ArrayList<String> insertColumnList, insertSelectedItems;
    String selectedItem = "";
    String spinnerSelectedItem = "";

    HashMap<String, String> columnToSchema;

    private void initialiseInsertViewLayout(final LinearLayout insertView) {

        columnToSchema = new HashMap<>();


        columnList = getColumnListForTable(selectedTableName);
        schemaList = MainActivity.getColumnSchema(columnList, selectedTableName);

        deleteInfoTextView.setVisibility(View.GONE);

        selectView.setVisibility(View.GONE);
        insertView.setVisibility(View.VISIBLE);
        deleteView.setVisibility(View.GONE);
        updateView.setVisibility(View.GONE);
        for (int i = 0; i < schemaList.size(); i++) {
            columnToSchema.put(columnList.get(i), schemaList.get(i));
        }

        insertColumnList = columnList;

        insertSelectedItems = new ArrayList<>();

        ViewGroup viewGroup = null;
        final LinearLayout singleInsertView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.insert_single_item, viewGroup, false);

        contentViewAdder.addView(singleInsertView);
        final Spinner columnSpinner = (Spinner) singleInsertView.findViewById(R.id.columnSpinner);
        final EditText editText = (EditText) singleInsertView.findViewById(R.id.contentValueEditText);

        columnSpinner.setAdapter(new SpinnerAdapterWithoutCheckBox(this, insertColumnList));

        columnSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                insertSelectedItems.add(insertColumnList.get(i));

                spinnerSelectedItem = insertColumnList.get(i);

                if (columnToSchema.get(spinnerSelectedItem).equals("text") || columnToSchema.get(spinnerSelectedItem).equals("null")) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (columnToSchema.get(spinnerSelectedItem).equals("integer")) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setHint("Only true or false");
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spinnerSelectedItem = insertColumnList.get(0);
        if (columnToSchema.get(spinnerSelectedItem).equals("text") || columnToSchema.get(spinnerSelectedItem).equals("null")) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if (columnToSchema.get(spinnerSelectedItem).equals("integer")) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setHint("Only true or false");
        }


        ImageView crossImage = (ImageView) singleInsertView.findViewById(R.id.crossImage);
        crossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertColumnList.add(insertValues.get(editText.getText().toString()));
                insertSelectedItems.remove(insertValues.get(editText.getText().toString()));
                contentViewAdder.removeView(singleInsertView);


            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                selectedItem = editable.toString();
            }
        });


        ImageView rowAdder = (ImageView) insertView.findViewById(R.id.rowAdder);

        rowAdder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                insertValues.put(selectedItem, spinnerSelectedItem);
                if (!selectedItem.isEmpty() && insertColumnList.size() - 1 > 0) {
                    final LinearLayout singleInsertView1 = (LinearLayout) LayoutInflater.from(QueryRunner.this).inflate(R.layout.insert_single_item, null);

                    selectedItem = "";
                    contentViewAdder.addView(singleInsertView1);

                    final EditText editText1 = (EditText) singleInsertView1.findViewById(R.id.contentValueEditText);
                    editText1.setFocusable(true);
                    editText1.setFocusableInTouchMode(true);
                    editText1.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            selectedItem = editable.toString();
                        }
                    });

                    spinnerSelectedItem = insertColumnList.get(0);
                    if (columnToSchema.get(spinnerSelectedItem).equals("text") || columnToSchema.get(spinnerSelectedItem).equals("null")) {
                        editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else if (columnToSchema.get(spinnerSelectedItem).equals("integer")) {
                        editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                    } else {
                        editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                        editText1.setHint("Only true or false");
                    }

                    final Spinner columnSpinner1 = (Spinner) singleInsertView1.findViewById(R.id.columnSpinner);
                    insertColumnList.removeAll(insertSelectedItems);
                    columnSpinner1.setAdapter(new SpinnerAdapterWithoutCheckBox(QueryRunner.this, insertColumnList));

                    columnSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            insertSelectedItems.add(insertColumnList.get(i));
                            spinnerSelectedItem = insertColumnList.get(i);

                            if (columnToSchema.get(spinnerSelectedItem).equals("text") || columnToSchema.get(spinnerSelectedItem).equals("null")) {
                                editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                            } else if (columnToSchema.get(spinnerSelectedItem).equals("integer")) {
                                editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                            } else {
                                editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                                editText1.setHint("Only true or false");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });


                    ImageView crossImage = (ImageView) singleInsertView1.findViewById(R.id.crossImage);
                    crossImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        /*    insertColumnList.add(columnSpinner.getSelectedItem().toString());
                            insertSelectedItems.remove(columnSpinner.getSelectedItem().toString());*/

                            insertColumnList.add(insertValues.get(editText1.getText().toString()));
                            insertSelectedItems.remove(insertValues.get(editText1.getText().toString()));
                            contentViewAdder.removeView(singleInsertView1);


                        }
                    });
                }

            }
        });

    }


    ArrayList<String> deleteColumnList, deleteSelectedItems;
    String deleteSelectedItem = "";
    String deleteSpinnerSelectedItem = "";

    private void initialiseDeleteViewLayout(final LinearLayout deleteView) {

        columnToSchema = new HashMap<>();


        columnList = getColumnListForTable(selectedTableName);
        schemaList = MainActivity.getColumnSchema(columnList, selectedTableName);

        deleteInfoTextView.setVisibility(View.VISIBLE);

        selectView.setVisibility(View.GONE);
        deleteView.setVisibility(View.VISIBLE);
        insertView.setVisibility(View.GONE);
        updateView.setVisibility(View.GONE);

        for (int i = 0; i < schemaList.size(); i++) {
            columnToSchema.put(columnList.get(i), schemaList.get(i));
        }

        deleteColumnList = columnList;

        deleteSelectedItems = new ArrayList<>();

        ViewGroup viewGroup = null;
        final LinearLayout singleInsertView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.insert_single_item, viewGroup, false);

        contentViewDeleteAdder.addView(singleInsertView);
        final Spinner columnSpinner = (Spinner) singleInsertView.findViewById(R.id.columnSpinner);
        final EditText editText = (EditText) singleInsertView.findViewById(R.id.contentValueEditText);

        columnSpinner.setAdapter(new SpinnerAdapterWithoutCheckBox(this, deleteColumnList));

        columnSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                deleteSelectedItems.add(deleteColumnList.get(i));

                deleteSpinnerSelectedItem = deleteColumnList.get(i);

                if (columnToSchema.get(deleteSpinnerSelectedItem).equals("text") || columnToSchema.get(deleteSpinnerSelectedItem).equals("null")) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (columnToSchema.get(deleteSpinnerSelectedItem).equals("integer")) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setHint("Only true or false");
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        deleteSpinnerSelectedItem = deleteColumnList.get(0);
        if (columnToSchema.get(deleteSpinnerSelectedItem).equals("text") || columnToSchema.get(deleteSpinnerSelectedItem).equals("null")) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if (columnToSchema.get(deleteSpinnerSelectedItem).equals("integer")) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setHint("Only true or false");
        }


        ImageView crossImage = (ImageView) singleInsertView.findViewById(R.id.crossImage);
        crossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteColumnList.add(deleteValues.get(editText.getText().toString()));
                deleteSelectedItems.remove(deleteValues.get(editText.getText().toString()));
                contentViewDeleteAdder.removeView(singleInsertView);


            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                deleteSelectedItem = editable.toString();
            }
        });


        ImageView rowAdder = (ImageView) deleteView.findViewById(R.id.rowAdderDelete);

        rowAdder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteValues.put(deleteSelectedItem, deleteSpinnerSelectedItem);
                if (!deleteSelectedItem.isEmpty() && deleteColumnList.size() - 1 > 0) {
                    final LinearLayout singleInsertView1 = (LinearLayout) LayoutInflater.from(QueryRunner.this).inflate(R.layout.insert_single_item, null);

                    deleteSelectedItem = "";
                    contentViewDeleteAdder.addView(singleInsertView1);

                    final EditText editText1 = (EditText) singleInsertView1.findViewById(R.id.contentValueEditText);
                    editText1.setFocusable(true);
                    editText1.setFocusableInTouchMode(true);
                    editText1.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            deleteSelectedItem = editable.toString();
                        }
                    });

                    deleteSpinnerSelectedItem = deleteColumnList.get(0);
                    if (columnToSchema.get(deleteSpinnerSelectedItem).equals("text") || columnToSchema.get(deleteSpinnerSelectedItem).equals("null")) {
                        editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else if (columnToSchema.get(deleteSpinnerSelectedItem).equals("integer")) {
                        editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                    } else {
                        editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                        editText1.setHint("Only true or false");
                    }

                    final Spinner columnSpinner1 = (Spinner) singleInsertView1.findViewById(R.id.columnSpinner);
                    deleteColumnList.removeAll(deleteSelectedItems);
                    columnSpinner1.setAdapter(new SpinnerAdapterWithoutCheckBox(QueryRunner.this, deleteColumnList));

                    columnSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            deleteSelectedItems.add(deleteColumnList.get(i));
                            deleteSpinnerSelectedItem = deleteColumnList.get(i);

                            if (columnToSchema.get(deleteSpinnerSelectedItem).equals("text") || columnToSchema.get(deleteSpinnerSelectedItem).equals("null")) {
                                editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                            } else if (columnToSchema.get(deleteSpinnerSelectedItem).equals("integer")) {
                                editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                            } else {
                                editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                                editText1.setHint("Only true or false");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });


                    ImageView crossImage = (ImageView) singleInsertView1.findViewById(R.id.crossImage);
                    crossImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        /*    insertColumnList.add(columnSpinner.getSelectedItem().toString());
                            insertSelectedItems.remove(columnSpinner.getSelectedItem().toString());*/

                            deleteColumnList.add(deleteValues.get(editText1.getText().toString()));
                            deleteSelectedItems.remove(deleteValues.get(editText1.getText().toString()));
                            contentViewDeleteAdder.removeView(singleInsertView1);


                        }
                    });
                }

            }
        });

    }

    String whereValues = "";
    ArrayList<String> whereArg = new ArrayList<String>();
    ContentValues updateContentValues ;
    ArrayList<String> updateColumnList, updateSelectedItems;
    String updateSelectedItem = "";
    String updateSpinnerSelectedItem = "";

    private void initialiseUpdateViewLayout(final LinearLayout updateView, final int type) {
updateContentValues= new ContentValues();

        contentViewUpdateLinear.removeAllViews();
        queryEditText.setText("");
        updateScroll.setVisibility(View.VISIBLE);
        updateTypeText.setVisibility(View.VISIBLE);
        if (type == 1) {
            updateTypeText.setText("Set Values to Update");
        } else {
            updateTypeText.setText("Where condition to update");
        }

        columnToSchema = new HashMap<>();


        columnList = getColumnListForTable(selectedTableName);
        schemaList = MainActivity.getColumnSchema(columnList, selectedTableName);

        selectView.setVisibility(View.GONE);
        deleteView.setVisibility(View.GONE);
        insertView.setVisibility(View.GONE);
        updateView.setVisibility(View.VISIBLE);

        for (int i = 0; i < schemaList.size(); i++) {
            columnToSchema.put(columnList.get(i), schemaList.get(i));
        }

        updateColumnList = columnList;

        if (updateColumnList.size()>0) {

            updateSelectedItems = new ArrayList<>();

            ViewGroup viewGroup = null;

            Button doneButton = (Button) updateView.findViewById(R.id.doneButton);
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (type == 1) {
                        setUpdateValues.put(updateSelectedItem, updateSpinnerSelectedItem);

                        contentViewUpdateLinear.removeAllViews();
                        initialiseUpdateViewLayout(updateView, 2);
                    } else if (type == 2) {
                        whereValues="";
                        whereConditionValues.put(updateSelectedItem, updateSpinnerSelectedItem);

                        updateScroll.setVisibility(View.GONE);
                        updateTypeText.setVisibility(View.GONE);


                        String setValues="";
                        String localWhereValues ="";
                        Set keys = setUpdateValues.entrySet();
                        Iterator it = keys.iterator();


                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            // contentValues.put(entry.getValue().toString(),entry.getKey().toString());

                            setValues = setValues + entry.getValue().toString() +" = '"+entry.getKey().toString()+"',";

                            updateContentValues.put(entry.getValue().toString(),entry.getKey().toString());

                        }

                        keys = whereConditionValues.entrySet();
                        it = keys.iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            // contentValues.put(entry.getValue().toString(),entry.getKey().toString());


                            whereValues = whereValues + entry.getValue().toString() + " = ?,";
                            localWhereValues = localWhereValues + entry.getValue().toString() + " = '"+entry.getKey().toString()+"',";

                            whereArg.add(entry.getKey().toString());

                        }


                        updateQuery.setText("Update " + selectedTableName + " set " + setValues.substring(0, setValues.length() - 1) + " where " + localWhereValues.substring(0,localWhereValues.length() - 1));


                    }
                }
            });


            final LinearLayout singleInsertView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.insert_single_item, viewGroup, false);

            contentViewUpdateLinear.addView(singleInsertView);
            final Spinner columnSpinner = (Spinner) singleInsertView.findViewById(R.id.columnSpinner);
            final EditText editText = (EditText) singleInsertView.findViewById(R.id.contentValueEditText);

            columnSpinner.setAdapter(new SpinnerAdapterWithoutCheckBox(this, updateColumnList));

            columnSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    updateSelectedItems.add(updateColumnList.get(i));

                    updateSpinnerSelectedItem = updateColumnList.get(i);

                    if (columnToSchema.get(updateSpinnerSelectedItem).equals("text") || columnToSchema.get(updateSpinnerSelectedItem).equals("null")) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else if (columnToSchema.get(updateSpinnerSelectedItem).equals("integer")) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    } else {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                        editText.setHint("Only true or false");
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            updateSpinnerSelectedItem = updateColumnList.get(0);
            if (columnToSchema.get(updateSpinnerSelectedItem).equals("text") || columnToSchema.get(updateSpinnerSelectedItem).equals("null")) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
            } else if (columnToSchema.get(updateSpinnerSelectedItem).equals("integer")) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setHint("Only true or false");
            }


            ImageView crossImage = (ImageView) singleInsertView.findViewById(R.id.crossImage);
            crossImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (type == 1) {
                        updateColumnList.add(setUpdateValues.get(editText.getText().toString()));
                        updateSelectedItems.remove(setUpdateValues.get(editText.getText().toString()));
                    } else {
                        updateColumnList.add(whereConditionValues.get(editText.getText().toString()));
                        updateSelectedItems.remove(whereConditionValues.get(editText.getText().toString()));
                    }
                    contentViewUpdateLinear.removeView(singleInsertView);


                }
            });


            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    updateSelectedItem = editable.toString();
                }
            });


            ImageView rowAdder = (ImageView) updateView.findViewById(R.id.rowAdderUpdate);

            rowAdder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (type == 1) {
                        setUpdateValues.put(updateSelectedItem, updateSpinnerSelectedItem);
                    } else {
                        whereConditionValues.put(updateSelectedItem, updateSpinnerSelectedItem);
                    }

                    if ((!updateSelectedItem.isEmpty() && updateColumnList.size() - 1 > 0) || updateSelectedItems.size() == 0) {
                        final LinearLayout singleInsertView1 = (LinearLayout) LayoutInflater.from(QueryRunner.this).inflate(R.layout.insert_single_item, null);

                        updateSelectedItem = "";
                        contentViewUpdateLinear.addView(singleInsertView1);

                        final EditText editText1 = (EditText) singleInsertView1.findViewById(R.id.contentValueEditText);
                        editText1.setFocusable(true);
                        editText1.setFocusableInTouchMode(true);
                        editText1.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                updateSelectedItem = editable.toString();
                            }
                        });

                        updateSpinnerSelectedItem = updateColumnList.get(0);
                        if (columnToSchema.get(updateSpinnerSelectedItem).equals("text") || columnToSchema.get(updateSpinnerSelectedItem).equals("null")) {
                            editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                        } else if (columnToSchema.get(deleteSpinnerSelectedItem).equals("integer")) {
                            editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                        } else {
                            editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                            editText1.setHint("Only true or false");
                        }

                        final Spinner columnSpinner1 = (Spinner) singleInsertView1.findViewById(R.id.columnSpinner);
                        updateColumnList.removeAll(updateSelectedItems);
                        columnSpinner1.setAdapter(new SpinnerAdapterWithoutCheckBox(QueryRunner.this, updateColumnList));

                        columnSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                updateSelectedItems.add(updateColumnList.get(i));
                                updateSpinnerSelectedItem = updateColumnList.get(i);

                                if (columnToSchema.get(updateSpinnerSelectedItem).equals("text") || columnToSchema.get(updateSpinnerSelectedItem).equals("null")) {
                                    editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                                } else if (columnToSchema.get(updateSpinnerSelectedItem).equals("integer")) {
                                    editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                                } else {
                                    editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                                    editText1.setHint("Only true or false");
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });


                        ImageView crossImage = (ImageView) singleInsertView1.findViewById(R.id.crossImage);
                        crossImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            /*    insertColumnList.add(columnSpinner.getSelectedItem().toString());
                                insertSelectedItems.remove(columnSpinner.getSelectedItem().toString());*/

                                if (type == 1) {
                                    updateColumnList.add(setUpdateValues.get(editText1.getText().toString()));
                                    updateSelectedItems.remove(setUpdateValues.get(editText1.getText().toString()));
                                } else {
                                    updateColumnList.add(whereConditionValues.get(editText1.getText().toString()));
                                    updateSelectedItems.remove(whereConditionValues.get(editText1.getText().toString()));
                                }
                                contentViewUpdateLinear.removeView(singleInsertView1);


                            }
                        });
                    }

                }
            });
        }

    }

    private void initialiseSelectViewLayout(LinearLayout selectView) {

        deleteInfoTextView.setVisibility(View.GONE);

        selectView.setVisibility(View.VISIBLE);
        insertView.setVisibility(View.GONE);
        deleteView.setVisibility(View.GONE);
        updateView.setVisibility(View.GONE);
        
        Spinner columnSpinner = (Spinner) selectView.findViewById(R.id.selectColumn);

        columnSpinner.setAdapter(new SpinnerAdapterWithCheckBox(QueryRunner.this, getColumnListForTable(selectedTableName)));

        columnList = getColumnListForTable(selectedTableName);


    }

    private class SpinnerAdapterWithCheckBox extends BaseAdapter {

        ArrayList<String> spinnerArray;

        public SpinnerAdapterWithCheckBox(@NonNull Context context, ArrayList<String> spinnerArray) {

            this.spinnerArray = spinnerArray;
        }

        @Override
        public int getCount() {
            return spinnerArray.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        TextView titleText;
        CheckBox checkBox;

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);


            if (view == null)
                view = layoutInflater.inflate(R.layout.spinner_single_item_checkbox, viewGroup, false);

            titleText = (TextView) view.findViewById(R.id.textId);
            checkBox = (CheckBox) view.findViewById(R.id.checkBoxId);

            titleText.setText(spinnerArray.get(i));


            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        selectQueryBuilder = new StringBuilder("select ");
                        if (!selectQuery.contains(spinnerArray.get(i)))
                            selectQuery.add(spinnerArray.get(i));

                        for (int j = 0; j < selectQuery.size() - 1; j++)
                            selectQueryBuilder.append(selectQuery.get(j) + ",");

                        selectQueryBuilder.append(selectQuery.get(selectQuery
                                .size() - 1) + " from " + selectedTableName);

                    } else {
                        selectQueryBuilder = new StringBuilder("select ");

                        if (selectQuery.contains(spinnerArray.get(i)))
                            selectQuery.remove(selectQuery.indexOf(spinnerArray.get(i)));

                        for (int j = 0; j < selectQuery.size() - 1; j++)
                            selectQueryBuilder.append(selectQuery.get(j) + ",");


                        selectQueryBuilder.append(selectQuery.get(selectQuery
                                .size() - 1) + " from " + selectedTableName);

                    }


                    queryEditText.setText(selectQueryBuilder.toString());
                }
            });


            return view;
        }
    }

    private class SpinnerAdapterWithoutCheckBox extends BaseAdapter {

        ArrayList<String> spinnerArray;

        public SpinnerAdapterWithoutCheckBox(@NonNull Context context, ArrayList<String> spinnerArray) {

            this.spinnerArray = spinnerArray;
        }

        @Override
        public int getCount() {
            return spinnerArray.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        TextView titleText;
        CheckBox checkBox;

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);


            if (view == null)
                view = layoutInflater.inflate(R.layout.spinner_single_item_checkbox, viewGroup, false);

            titleText = (TextView) view.findViewById(R.id.textId);
            checkBox = (CheckBox) view.findViewById(R.id.checkBoxId);
            checkBox.setVisibility(View.GONE);

            titleText.setText(spinnerArray.get(i));


            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        selectQueryBuilder = new StringBuilder("select ");
                        if (!selectQuery.contains(spinnerArray.get(i)))
                            selectQuery.add(spinnerArray.get(i));

                        for (int j = 0; j < selectQuery.size() - 1; j++)
                            selectQueryBuilder.append(selectQuery.get(j) + ",");

                        selectQueryBuilder.append(selectQuery.get(selectQuery
                                .size() - 1) + " from " + selectedTableName);

                    } else {
                        selectQueryBuilder = new StringBuilder("select ");

                        if (selectQuery.contains(spinnerArray.get(i)))
                            selectQuery.remove(selectQuery.indexOf(spinnerArray.get(i)));

                        for (int j = 0; j < selectQuery.size() - 1; j++)
                            selectQueryBuilder.append(selectQuery.get(j) + ",");


                        selectQueryBuilder.append(selectQuery.get(selectQuery
                                .size() - 1) + " from " + selectedTableName);

                    }


                    queryEditText.setText(selectQueryBuilder.toString());
                }
            });


            return view;
        }
    }


    public void selectTableDialog(final String clickActionName) {

        final Dialog dialog = new Dialog(QueryRunner.this);

        dialog.setContentView(R.layout.guideline_chooser);

        ListView listView = (ListView) dialog.findViewById(R.id.guidelineList);


        listView.setAdapter(new ArrayAdapter<String>(QueryRunner.this, android.R.layout.simple_list_item_1, tableArrayList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTableName = tableArrayList.get(i);
                toolBarText.setText(DatabaseHelper.DB_NAME.replace("/", "") + "/" + selectedTableName);
                dialog.dismiss();

                if (clickActionName.equalsIgnoreCase("select")) {
                    initialiseSelectViewLayout(selectView);
                } else if (clickActionName.equalsIgnoreCase("insert")) {
                    initialiseInsertViewLayout(insertView);
                } else if (clickActionName.equalsIgnoreCase("delete")) {
                    initialiseDeleteViewLayout(deleteView);
                } else if (clickActionName.equalsIgnoreCase("update")) {

                    initialiseUpdateViewLayout(updateView, 1);
                }
            }
        });

        selectQueryBuilder = new StringBuilder();
        queryEditText.setText("");

        selectQuery = new ArrayList<>();

        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.run_query_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())

        {
            case R.id.changeTable:
                selectTableDialog("");
                break;
        }

        return true;
    }


    public ArrayList<String> getColumnListForTable(String tableName) {
        ArrayList<String> columnList = new ArrayList<>();
        String Query2 = "select * from " + tableName;

        ArrayList<Cursor> columnArrayList = getData(Query2);

        Cursor mainCursor = columnArrayList.get(0);
        if (mainCursor != null) {
            for (int i = 0; i < mainCursor.getColumnCount(); i++) {
                columnList.add(mainCursor.getColumnName(i));
            }
        }

        return columnList;
    }

    public void refreshTable(Cursor cursor) {
        resultTable.removeAllViews();

        fetchTableCursor(cursor);

        addTableHeader();
    }

    public void fetchTableCursor(Cursor selectCursor) {
        mainSelectTableCursor = selectCursor;

        totalCountOfRowsSelect = mainSelectTableCursor.getCount();

        totalPages = totalCountOfRowsSelect / totalEntriesOnOnePage;
        extraEntries = totalCountOfRowsSelect % totalEntriesOnOnePage;


    }

    public void addTableHeader() {


        resultTable.removeAllViews();
        firstColumnTable.removeAllViews();


        if (mainSelectTableCursor != null) {
            TableRow row = new TableRow(this);


            for (int i = 0; i < mainSelectTableCursor.getColumnCount(); i++) {


                TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

                row.setLayoutParams(lp);

                TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.single_tupple, null);


                textView.setText(mainSelectTableCursor.getColumnName(i));

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

            TableRow firstColumnRow = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

            firstColumnRow.setLayoutParams(lp);

            TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.single_tupple, null);


            textView.setText(mainSelectTableCursor.getColumnName(0));

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

            resultTable.addView(row);

            addRows(mainSelectTableCursor);
        }

    }

    private void addRows(Cursor cursor) {


        do {
            if (cursor.moveToPosition(initialIndex)) {
                TableRow row = new TableRow(this);
                for (int i = 0; i < cursor.getColumnCount(); i++) {


                    TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

                    row.setLayoutParams(lp);

                    TextView textView = new TextView(this);


                    textView.setTag(cursor.getString(i));
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

                resultTable.addView(row);
            } else {
                if (initialIndex < 0) {
                    Toast.makeText(QueryRunner.this, "you are on first page", Toast
                            .LENGTH_SHORT).show();

                } else {
                    Toast.makeText(QueryRunner.this, "you are on last page", Toast
                            .LENGTH_SHORT).show();
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


        } while (cursor.moveToNext() && initialIndex <= finalIndex);

    }

}

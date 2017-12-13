package com.example.bbim1043.databaseviewer;



import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;


public class EditFragment extends android.app.DialogFragment {

    ModifyOperationInterface modifyOperationInterface;

    ArrayList<String> columnList;
    ArrayList<String> selectedRow;
    ArrayList<String> schemaArray;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        modifyOperationInterface = (ModifyOperationInterface)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View layoutView = inflater.inflate(R.layout.fragment_dialog,container,false);

        columnList = getArguments().getStringArrayList("ColumnList");
        selectedRow = getArguments().getStringArrayList("SelectedRow");
        schemaArray = getArguments().getStringArrayList("Schema");


        LinearLayout mainLayout = layoutView.findViewById(R.id.mainLayout);

        for(int i=0;i<columnList.size();i++)
        {
           /* LinearLayout singleRowLayout = new LinearLayout(getActivity());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            singleRowLayout.setLayoutParams(layoutParams);
            singleRowLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView headingText = new TextView(getActivity());
            headingText.setText("Ankit");
            headingText.setGravity(Gravity.CENTER_HORIZONTAL);
            headingText.setTextSize(20);


            headingText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,0.5f));

            singleRowLayout.addView(headingText);

            EditText editText = new EditText(getActivity());



            editText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,0.5f));

            singleRowLayout.addView(editText);

            mainLayout.addView(singleRowLayout);
*/



           LinearLayout singleRowLayout = null;
           if(schemaArray.get(i).equals("text") || schemaArray.get(i).equals("null")) {

                singleRowLayout = (LinearLayout) inflater.inflate(R.layout.string_input_layout, null);

               TextView titleText = (TextView)singleRowLayout.findViewById(R.id.textHeading);
               titleText.setText(columnList.get(i));

               EditText editText = (EditText)singleRowLayout.findViewById(R.id.valueEditText);
               editText.setInputType(InputType.TYPE_CLASS_TEXT);

               editText.setText(selectedRow.get(i));
               editText.setTag(columnList.get(i));

           }
           else if(schemaArray.get(i).equals("integer")) {

               singleRowLayout = (LinearLayout) inflater.inflate(R.layout.string_input_layout,null);

               TextView titleText = (TextView)singleRowLayout.findViewById(R.id.textHeading);
               titleText.setText(columnList.get(i));

               EditText editText = (EditText)singleRowLayout.findViewById(R.id.valueEditText);
               editText.setInputType(InputType.TYPE_CLASS_NUMBER);

               editText.setText(selectedRow.get(i));
               editText.setTag(columnList.get(i));



           }
           else {
               singleRowLayout = (LinearLayout)inflater.inflate(R.layout.boolean_input_layout,null);

               TextView titleText = (TextView)singleRowLayout.findViewById(R.id.textHeading);
               titleText.setText(columnList.get(i));


               Spinner spinner = (Spinner)singleRowLayout.findViewById(R.id.spinner);
               ArrayList<String> booleanArrayList = new ArrayList<>();
               booleanArrayList.add("true");
               booleanArrayList.add("false");
               spinner.setAdapter(new ArrayAdapter<String>(getContext(),R.layout.spinner_single_item,R.id.textId,booleanArrayList));

               if(selectedRow.get(i).equals("true"))
               {
                   spinner.setSelection(0);
               }
               else
                   spinner.setSelection(1);

               spinner.setTag(columnList.get(i));
           }


            mainLayout.addView(singleRowLayout);


        }

        Button doneButton = (Button)layoutView.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> modifiedData = captureData(layoutView);

                modifyOperationInterface.modifyData(modifiedData);
                dismiss();
            }
        });
        final Button deleteButton = (Button)layoutView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> modifiedData = captureData(layoutView);

                modifyOperationInterface.deleteRow(modifiedData);
                dismiss();
            }
        });
        Button cancelButton = (Button)layoutView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });




        return layoutView;
    }

    public ArrayList<String> captureData(View view)
    {
        ArrayList<String> selectedRowData = new ArrayList<>();

        for(int i=0;i<columnList.size();i++)
        {
            View childView = view.findViewWithTag(columnList.get(i));
            if(childView instanceof EditText)
                selectedRowData.add(((EditText) childView).getText().toString());
            else if(childView instanceof Spinner)
                selectedRowData.add(((Spinner) childView).getSelectedItemPosition()==0?"true":"false");

        }

        return selectedRowData;
    }




    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog
                 = getDialog();

        if(dialog!=null)
        {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        }
    }
}

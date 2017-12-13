package com.example.bbim1043.databaseviewer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by BBIM1043 on 16/09/17.
 */

public class ChangeRecordCountFragment extends DialogFragment {

    ChangeRecordInterface changeRecordInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        changeRecordInterface = (ChangeRecordInterface)context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.change_record_layout,container,false);

        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(false);

        Bundle bundle = getArguments();

        int currentRecord = bundle.getInt("Current_count");

        final EditText recordEditText = layoutView.findViewById(R.id.recordCount);
        recordEditText.setText(currentRecord+"");

        recordEditText.setSelection(String.valueOf(currentRecord).length());


        Button changeButton = layoutView.findViewById(R.id.changeButton);

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int changed = Integer.parseInt(recordEditText.getText().toString());
                if(changed>20)
                    changed=20;
                changeRecordInterface.changeRecordTo(changed);


                dismiss();
            }
        });

        return layoutView;
    }

   /* @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog!=null)
        {
            dialog.getWindow().setLayout(W,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }*/
}

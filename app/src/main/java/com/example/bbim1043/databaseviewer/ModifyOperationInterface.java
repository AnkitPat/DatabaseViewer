package com.example.bbim1043.databaseviewer;

import java.util.ArrayList;

/**
 * Created by BBIM1043 on 16/09/17.
 */

public interface ModifyOperationInterface {

    public void deleteRow(ArrayList<String> selectedRow);

    public void modifyData(ArrayList<String> selectedRow);

}

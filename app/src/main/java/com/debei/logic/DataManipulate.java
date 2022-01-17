package com.debei.logic;

import android.widget.Button;

import com.debei.flightdisplay.listview.MyAdapter;

public class DataManipulate {
    public static void updateData(Button sourceButton, MyAdapter.ViewHolder viewHolder, Flight currentFlight) {
        final Button button_1 = viewHolder.button_1;
        final Button button_2 = viewHolder.button_2;
        final Button button_3 = viewHolder.button_3;
        if (sourceButton == null) {
            return;
        } else if (sourceButton.equals(button_1)) {
            responseButton_1(button_1, currentFlight);
        } else if (sourceButton.equals(button_2)) {
            responseButton_2(button_2, currentFlight);
        } else if (sourceButton.equals(button_3)) {
            responseButton_3(button_3, currentFlight);
        }

    }

    private static void responseButton_1(Button light_1, Flight currentFlight) {
        if (currentFlight.getStatus() == Flight.STAGE_SB) {
            currentFlight.setStatus(Flight.STAGE_FA);
        }
    }

    private static void responseButton_2(Button light_2, Flight currentFlight) {
        if (currentFlight.getStatus() == Flight.STAGE_FU) {
            currentFlight.setStatus(Flight.STAGE_LA);
        }
    }

    private static void responseButton_3(Button light_3, Flight currentFlight) {

        if (currentFlight.getStatus() == Flight.STAGE_FU){
            currentFlight.setStatus(Flight.STAGE_BSB);
        }
        else if (currentFlight.getStatus() == Flight.STAGE_LU){
            currentFlight.setStatus(Flight.STAGE_FR);
        }
        else if (currentFlight.getStatus() == Flight.STAGE_FEP){
            currentFlight.setStatus(Flight.STAGE_LR);
        }
    }
}

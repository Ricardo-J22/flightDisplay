package com.debei.logic;

import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import com.debei.flightdisplay.listview.MyAdapter;

public class UpdateUI {
    public static void updateDisplay(MyAdapter.ViewHolder holder, Flight currentFlight) {
        final ImageView light_1 = holder.light_1;
        final ImageView light_2 = holder.light_2;
        final ImageView light_3 = holder.light_3;
        final TextView flightText = holder.flightNumber;
        final String flight_number = currentFlight.getFlight_number();
        final int stage = currentFlight.getStatus();
        if(currentFlight.getErrorFlag()){
            flightText.setText("故障");
            flightText.setTextColor(Color.RED);
            return;
        }
        switch (stage) {
            case Flight.STAGE_EP:
            case Flight.STAGE_FEP:
                flightText.setText("NULL");
                flightText.setTextColor(Color.GREEN);
                light_1.setSelected(false);
                light_2.setSelected(false);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_SB:
                flightText.setText(flight_number);
                flightText.setTextColor(Color.RED);
                light_1.setSelected(false);
                light_2.setSelected(false);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_FU:
                flightText.setText(flight_number);
                flightText.setTextColor(Color.GREEN);
                light_1.setSelected(true);
                light_2.setSelected(false);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_LU:
                flightText.setText(flight_number);
                flightText.setTextColor(Color.YELLOW);
                light_1.setSelected(false);
                light_2.setSelected(true);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_F:
                flightText.setText("故障");
                flightText.setTextColor(Color.RED);
//                light_1.setSelected(false);
//                light_2.setSelected(false);
//                light_3.setSelected(false);
                break;
            case Flight.STAGE_FA:
                flightText.setText("正在上传");
                flightText.setTextColor(Color.RED);
                light_1.setSelected(true);
                light_2.setSelected(false);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_LA:
                flightText.setText("正在上传");
                flightText.setTextColor(Color.RED);
                light_1.setSelected(false);
                light_2.setSelected(true);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_CA:
                flightText.setText("正在上传");
                flightText.setTextColor(Color.RED);
                light_1.setSelected(false);
                light_2.setSelected(false);
                light_3.setSelected(true);
                break;
            case Flight.STAGE_LR:
                flightText.setText("正在上传");
                flightText.setTextColor(Color.RED);
                light_1.setSelected(false);
                light_2.setSelected(true);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_FR:
                flightText.setText("正在上传");
                flightText.setTextColor(Color.YELLOW);
                light_1.setSelected(true);
                light_2.setSelected(false);
                light_3.setSelected(false);
                break;
            case Flight.STAGE_BSB:
                flightText.setText("正在上传");
                flightText.setTextColor(Color.GREEN);
                light_1.setSelected(false);
                light_2.setSelected(false);
                light_3.setSelected(false);
            default:
                break;
        }
    }
}

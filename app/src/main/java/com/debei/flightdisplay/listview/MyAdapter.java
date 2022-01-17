package com.debei.flightdisplay.listview;

import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.debei.flightdisplay.R;
import com.debei.logic.DataManipulate;
import com.debei.logic.Flight;
import com.debei.logic.UpdateUI;
import com.debei.utils.Constants;
import com.debei.utils.EventMsg;
import com.debei.utils.LongClickUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MyAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;//得到一个LayoutInfalter对象用来导入布局
    private Flight[] flightArrayList;
    private Context context;

    //构造函数
    public MyAdapter(Context context, Flight[] flightArrayList) {
        this.context = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.flightArrayList = flightArrayList;
    }//声明构造函数

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int position) {
        return flightArrayList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Flight flight = flightArrayList[position];
        //初始化Viewholder
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.flightNumber = convertView.findViewById(R.id.flight_number);
            holder.light_1 = convertView.findViewById(R.id.green_light);
            holder.light_2 = convertView.findViewById(R.id.yellow_light);
            holder.light_3 = convertView.findViewById(R.id.red_light);
            holder.button_1 = convertView.findViewById(R.id.green);
            holder.button_2 = convertView.findViewById(R.id.yellow);
            holder.button_3 = convertView.findViewById(R.id.red);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //更新的逻辑区
        UpdateUI.updateDisplay(holder, flight);
        addListener(holder, flight);
        return convertView;

    }

    private void addListener(ViewHolder holder, Flight temp) {
//        final Button button_1 = holder.button_1;
//        final Button button_2 = holder.button_2;
//        final Button button_3 = holder.button_3;
        holder.button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (temp.getStatus() == Flight.STAGE_SB && !temp.getErrorFlag()) {
                    showDialogue(v, temp, holder, false);
                }

                //Toast.makeText(context, "我是按钮" + position, Toast.LENGTH_SHORT).show();
            }
        });
        holder.button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (temp.getStatus() == Flight.STAGE_FU && !temp.getErrorFlag()) {
                    showDialogue(v, temp, holder, false);
                }

                //Toast.makeText(context, "我是按钮" + position, Toast.LENGTH_SHORT).show();
            }
        });


        holder.button_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (temp.getStatus() == Flight.STAGE_FU || temp.getStatus() == Flight.STAGE_LU || temp.getStatus() == Flight.STAGE_FEP && !temp.getErrorFlag()) {
                    showDialogue(v, temp, holder, false);
                }


            }
        });
        LongClickUtils.setLongClick(new Handler(), holder.button_3, 2000, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if ((temp.getStatus() == Flight.STAGE_LU || temp.getStatus() == Flight.STAGE_SB) && !temp.getErrorFlag()) {
                    showDialogue(v, temp, holder, true);
                }
                return false;
            }
        });


    }

    private void showDialogue(View sourceView, Flight currentFlight, ViewHolder holder, boolean longClick) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("操作确认")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Add positive button action code here
                        if (sourceView.getId() == holder.button_1.getId()) {
                            DataManipulate.updateData(holder.button_1, holder, currentFlight);
                            notifyDataSetChanged();
                            //发送消息给activity发送新的消息
                            sendMessageToActivity();

                        } else if (sourceView.getId() == holder.button_2.getId()) {
                            DataManipulate.updateData(holder.button_2, holder, currentFlight);
                            notifyDataSetChanged();
                            //发送消息给activity发送新的消息
                            sendMessageToActivity();

                        } else if (sourceView.getId() == holder.button_3.getId() && !longClick) {
                            DataManipulate.updateData(holder.button_3, holder, currentFlight);
                            notifyDataSetChanged();
                            //发送消息给activity发送新的消息
                            sendMessageToActivity();

                        } else if (sourceView.getId() == holder.button_3.getId() && longClick) {
                            currentFlight.setStatus(Flight.STAGE_CA);
                            notifyDataSetChanged();
                            //发送消息给activity发送新的消息
                            sendMessageToActivity();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
        if (sourceView.getId() == holder.button_1.getId()) {
            dialog.setMessage(currentFlight.getFlight_number() + " 第一件行李上架?");
        } else if (sourceView.getId() == holder.button_2.getId()) {
            dialog.setMessage(currentFlight.getFlight_number() + " 最后一架行李上架?");
        } else if (sourceView.getId() == holder.button_3.getId() && longClick) {
            dialog.setMessage("清除航班" + currentFlight.getFlight_number() + "?");
        } else if (sourceView.getId() == holder.button_3.getId() && !longClick) {
            dialog.setMessage(currentFlight.getFlight_number() + " 回退到上一状态？");
        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            private static final int AUTO_DISMISS_MILLIS = 5000;

            @Override
            public void onShow(final DialogInterface dialog) {
                final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                final CharSequence negativeButtonText = defaultButton.getText();
                new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        defaultButton.setText(String.format(
                                Locale.getDefault(), "%s (%d)",
                                negativeButtonText,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                        ));
                    }

                    @Override
                    public void onFinish() {
                        if (((AlertDialog) dialog).isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }.start();
            }
        });
        dialog.show();
    }

    private void sendMessageToActivity() {
        EventMsg msg = new EventMsg();
        msg.setTag(Constants.REQUEST_SEND);
        EventBus.getDefault().post(msg);
    }

    public static class ViewHolder {
        public TextView flightNumber;
        public Button button_1;
        public Button button_2;
        public Button button_3;
        public ImageView light_1;
        public ImageView light_2;
        public ImageView light_3;
    }
}

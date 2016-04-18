package edu.pitt.cs.cs1635.cmn26.mobilevoting2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            // Note: a pdu (protocol data unit) is the industry format for an SMS message
            Object[] pdus  = (Object[])bundle.get("pdus");
            for(int i = 0; i < pdus.length; i++) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);

                String smsBody = sms.getDisplayMessageBody();
                String sender = sms.getOriginatingAddress();

                MainActivity inst = MainActivity.instance();
                inst.parseSMS(sender, smsBody);
            }
        }
    }
}
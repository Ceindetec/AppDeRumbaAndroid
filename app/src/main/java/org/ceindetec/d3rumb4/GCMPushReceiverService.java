package org.ceindetec.d3rumb4;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class GCMPushReceiverService extends GcmListenerService {


    @Override
    public void onMessageReceived(String from, Bundle data) {

        String message = data.getString("message");

        Intent intent = new Intent(getApplicationContext(), OnlinePlaylist.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mensaje", message);
        startActivity(intent);
    }

}
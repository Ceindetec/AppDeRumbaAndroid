package org.ceindetec.d3rumb4;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.HashMap;

public class OnlinePlaylist extends FragmentActivity
        implements SearchView.OnQueryTextListener

{
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    String token="";
    String URL_BASE = GlobalVars.getGlobalVarsInstance().getUrlBase();
    DataBaseManager manager = new DataBaseManager(this);
    int sede = GlobalVars.getGlobalVarsInstance().getSede();
    String nickname = GlobalVars.getGlobalVarsInstance().getNickname();

    ArrayList<CancionOnline> canciones = new ArrayList<>();
    CustomArrayAdapter adapter;
    SearchView search_view;
    ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_playlist);
        getOnlinePlayList(sede);

        adapter = new CustomArrayAdapter(this,canciones);
        lv = (ListView) findViewById(R.id.play_list);
        search_view = (SearchView) findViewById(R.id.search_online);
        FloatingActionButton nuevo = (FloatingActionButton) findViewById(R.id.add_cancion);
        nuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                    new AddCancion().show(fragmentManager, "AddCancion");
            }
        });


        /** RECIBE EL MENSAJE PUSH DEL SERVIDOR **************************************************************************************/
/** SI EL MENSAJE NO VIENE VACIO, OCURRIO UN CAMBIO EN LA PLAYLIST ONLINE Y SE RE-CONSTRUYE LA LISTA DE REPRODUCCION EN EL MOVIL */
        String mensaje = getIntent().getStringExtra("mensaje");
        if(mensaje != null){
                getOnlinePlayList(sede);
        }

        /*else{
            getOnlinePlayList(sede);
        }*/
            /**  SI ES LA PRIMERA VEZ, REGISTRA EL DISPOSITIVO EN LA BD DEL SERVIDOR ***********************************************/
            if(GlobalVars.getGlobalVarsInstance().getEstadoRegistroUsuario() == 0) {
                mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                        token = intent.getStringExtra("token");
                        GlobalVars.getGlobalVarsInstance().setEstadoRegistroUsuario(1);
                        registrarDispositivo(token, nickname, sede);
                    } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                        Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
                    }
                }
        };

        //Checking play service is available or not
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        //if play service is not available
        if (ConnectionResult.SUCCESS != resultCode) {
            //If play service is supported but not installed
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Displaying message that play service is not installed
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());

                //If play service is not supported
                //Displaying an error message
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }

            //If play service is available
        } else {
            //Starting intent to register device
            Intent itent = new Intent(this, GCMRegistrationIntentService.class);
            startService(itent);
        }
    }
}

    private void getOnlinePlayList(int sede) {
        String archivoPhp = "get_online_playlist.php";
        final ProgressDialog loadingb;
        loadingb = new ProgressDialog(this);
        loadingb.setMessage("Sincronizando playlist ...");
        loadingb.show();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("sede", String.valueOf(sede));
        GsonRequest jsonObjReq = new GsonRequest( Request.Method.POST, URL_BASE + archivoPhp, JsonObject.class ,parameters,

                new Response.Listener<JsonObject>() {
                    @Override
                    public void onResponse(JsonObject response) {
                        loadingb.dismiss();
                        try {
                            manager.limpiarListaOnline();
                            JsonArray jsonArray = response.getAsJsonArray("playlist");
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JsonElement data = jsonArray.get(i);
                                JsonObject respuesta = data.getAsJsonObject();
                                String codigo =  respuesta.get("codigo").getAsString();
                                String agregado_por =  respuesta.get("agregado_por").getAsString();
                                String posicion =  respuesta.get("posicion").getAsString();
                                String votos =  respuesta.get("votos").getAsString();
                                manager.insPlayListOnline(codigo,agregado_por, posicion,votos);
                            }
                            buildPlayList();
                        } catch (JsonIOException e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }
                    }
                },
                //ErrorListener errorListener
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "Error en la obtencion de la play list");
                    }
                }
        );
        // Añadir petición JSON a la cola
        SingletonDeRumba.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjReq);
    }



    private void buildPlayList(){
        Cursor listaBd = manager.getPlayList();
        canciones = new ArrayList<>();
        if (listaBd.moveToFirst()) {
            do {
                CancionOnline aux = new CancionOnline("Nombre: " + listaBd.getString(0),"Duracion: "+listaBd.getString(1) ,"Dj: "+listaBd.getString(1));
                canciones.add(aux);
            }
            while (listaBd.moveToNext());
        }
        search_view.setOnQueryTextListener(this);
        adapter = new CustomArrayAdapter(getApplicationContext(), canciones );
        lv.setAdapter(adapter);

    }

    private void registrarDispositivo(String token, String nickname, final int sede) {

        /** REGISTRA EL DISPOSITIVO EN LA BASE DE DATOS DEL SERVIDOR ************************************************************/

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("token", token);
        parameters.put("nickname", nickname);
        parameters.put("sede", String.valueOf(sede));

        String archivoPhp = "insAppUser.php";

        GsonRequest jsonObjReq = new GsonRequest( Request.Method.POST, URL_BASE + archivoPhp, JsonObject.class ,parameters,

                new Response.Listener<JsonObject>() {

                    @Override
                    public void onResponse(JsonObject response) {

                        try {
                            JsonArray jsonArray = response.getAsJsonArray("res_insert");
                                JsonElement data = jsonArray.get(0);
                                JsonObject respuesta = data.getAsJsonObject();
                                String error =  respuesta.get("error").getAsString();
                                if(error.equals("true"))
                                    Toast.makeText(getApplicationContext(), respuesta.get("message").toString(),Toast.LENGTH_LONG).show();

                        } catch (JsonIOException e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }
                    }
                },
                //ErrorListener errorListener
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "Error en la obtencion de datos o algo");
                    }
                }
        );
        // Añadir petición JSON a la cola
        SingletonDeRumba.getInstance(this).addToRequestQueue(jsonObjReq);
        /** ---------------------------------------------------------------------------------------------------------------------*/
    }

    //Registering receiver on activity resume
    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        Log.w("OnlinePlaylist", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


}

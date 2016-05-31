package org.ceindetec.d3rumb4;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainLogin extends FragmentActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap googleMap;
    DataBaseManager manager = new DataBaseManager(this);
    String ciudad = "";

    TextView tv;
    Location ubicacion_actual;
    String bienvenida = "";
    //String URL_BASE = "http://192.168.0.17/derumba/";
    String URL_BASE = "http://192.168.0.21/derumba/";
    //String URL_BASE = "http://192.168.1.3/derumba/";


    String nickname = "";
    int sedeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);


        /** RECUPERA DATOS DE FACEBOOK LOGIN Y LOS ASIGNA A LA ACTIVIDAD ********************************************************/

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Bundle datosFacebook = getIntent().getBundleExtra("facebookdata");
        String apellido = datosFacebook.get("last_name").toString();
        nickname = datosFacebook.get("first_name").toString() + "_" + apellido.substring(0, 1);
        bienvenida = "Bienvenido DJ " + nickname;

        String imageURL;
        String userID = datosFacebook.get("idFacebook").toString();
        Bitmap bitmap;
        imageURL = "https://graph.facebook.com/" + userID + "/picture?type=large";
        InputStream in = null;
        try {
            in = (InputStream) new URL(imageURL).getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = BitmapFactory.decodeStream(in);

        ImageView profile = (ImageView) findViewById(R.id.img_perfil);
        TextView bienv = (TextView) findViewById(R.id.tvBienvenida);
        bienv.setText(bienvenida);
        profile.setImageBitmap(bitmap);
        /** ---------------------------------------------------------------------------------------------------------------------*/

        /** ESTABLECE LA CONECCION AL GOOGLE APICLIENT Y UN INTERVALO DE PETICIONES DE UBICACION *********************************/
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(50000);
        mLocationRequest.setFastestInterval(20000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        /** ---------------------------------------------------------------------------------------------------------------------*/

        /** BORRA LOS DATOS DE LA BASE DE DATOS PARA SU CORRECTA SINCRONIZACION **************************************************/

        manager.truncateTables();
        /** ---------------------------------------------------------------------------------------------------------------------*/


        /** OBTIENE UN MAPA DE MANERA ASINCRONA Y LO ASIGNA AL FRAGMENT DEL LAYOUT ***********************************************/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /** ---------------------------------------------------------------------------------------------------------------------*/


        /** RECUPERA EL TEXTO INGRESADO POR EL USUARIO EN EL CAMPO CODIGO Y REALIZA VALIDACIONES DE ACCESO **********************/

        final Button button = (Button) findViewById(R.id.btn_acceder);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText obj_codigo = (EditText) findViewById(R.id.et_codigo_rumba);
                    String codigorumba = obj_codigo.getText().toString();
                    if (codigorumba.equals("")) {
                        Toast.makeText(getApplicationContext(), "No has ingresado el código !", Toast.LENGTH_LONG).show();
                    } else {
                        Cursor resultadoConsulta = manager.verificarCodigoAcceso(codigorumba);
                        int cantResultados = resultadoConsulta.getCount();
                        resultadoConsulta.moveToFirst();
                        if (cantResultados > 0) {
                            sedeId = resultadoConsulta.getInt(0);
                            double latitudBar = resultadoConsulta.getDouble(1);
                            double longitudBar = resultadoConsulta.getDouble(2);
                            double latitudUsuario = ubicacion_actual.getLatitude();
                            double longitudUsuario = ubicacion_actual.getLongitude();


                            Location bar = new Location("");
                            bar.setLatitude(latitudBar);
                            bar.setLongitude(longitudBar);
                            float margen_error = ubicacion_actual.getAccuracy();
                            float distancia = ubicacion_actual.distanceTo(bar);
                            if (distancia < 1000000000) {
                                Intent intent = new Intent(getApplicationContext(), OnlinePlaylist.class);
                                GlobalVars.getGlobalVarsInstance().setNickname(nickname);
                                GlobalVars.getGlobalVarsInstance().setSede(sedeId);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "El código ingresado no coincide con ningún establecimiento afiiado a DeRumb@ !! !", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
        /** ---------------------------------------------------------------------------------------------------------------------*/
    }

    public void syncEstablecimientos() {
        String archivoPhp = "getEstablecimientos.php";
        final ProgressDialog loadingE;
        loadingE = new ProgressDialog(this);
        loadingE.setMessage("Sincronizando establecimientos ...");
        loadingE.show();
        //Llamado al constructor de la clase GsonRequest
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(URL_BASE + archivoPhp, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingE.dismiss();
                        try {
                            JSONArray jsonArray = response.getJSONArray("establecimientos");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                manager.insEstablecimiento(Integer.parseInt(obj.get("id").toString()), obj.get("nombre").toString(), obj.get("descripcion").toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                //ErrorListener errorListener
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("", "Error obteniendo datos del servidor !!");
                    }
                }
        );

        // Añadir petición JSON a la cola
        SingletonDeRumba.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjReq);
    }

    public void syncSedes() {

        String archivoPhp = "getSedes.php";
        final ProgressDialog loadingS;
        loadingS = new ProgressDialog(this);
        loadingS.setMessage("Sincronizando sedes ...");
        loadingS.show();
        //Llamado al constructor de la clase GsonRequest
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(URL_BASE + archivoPhp, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingS.dismiss();
                        try {
                            JSONArray arr = response.getJSONArray("sedes");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                manager.insSedes(
                                        Integer.parseInt(obj.get("id").toString()),
                                        Integer.parseInt(obj.get("establecimiento_id").toString()),
                                        obj.get("ciudad").toString(),
                                        obj.get("sede").toString(),
                                        obj.get("direccion").toString(),
                                        Double.parseDouble(obj.get("latitud").toString()),
                                        Double.parseDouble(obj.get("longitud").toString()),
                                        obj.get("horario").toString(),
                                        obj.get("estado").toString(),
                                        obj.get("codigoAcceso").toString(),
                                        obj.get("maxCanciones").toString()
                                );

                            }
                            tv = (TextView) (findViewById(R.id.tvCantDrumbas));
                            Cursor datosEstablecimientos = manager.getInfoEstablecimientos();
                            int conteo = 0;

                            if (datosEstablecimientos.moveToFirst()) {
                                do {

                                    String nombre = datosEstablecimientos.getString(0);
                                    String sede = datosEstablecimientos.getString(1);
                                    Double latitud = datosEstablecimientos.getDouble(2);
                                    Double longitud = datosEstablecimientos.getDouble(3);
                                    Geocoder gcd = new Geocoder(getApplication());
                                    List<Address> addresses;
                                    String ciudadactual;
                                    addresses = gcd.getFromLocation(latitud, longitud, 1);

                                    if (addresses.size() > 0 && addresses != null) {
                                        ciudadactual = addresses.get(0).getLocality();
                                        if (ciudadactual.equals(ciudad)) {
                                            agregarMarcador(latitud, longitud, nombre, sede);
                                            conteo++;
                                        }
                                    }
                                }
                                while (datosEstablecimientos.moveToNext());
                                tv.setText("Se han encontrado " + conteo + " DeRumba en la ciudad de " + ciudad);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                //ErrorListener errorListener
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("", "Error obteniendo datos del servidor !!");
                    }
                }
        );

        // Añadir petición JSON a la cola
        SingletonDeRumba.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjReq);
    }

    public void syncBiblioteca() {

        String archivoPhp = "get_biblioteca.php";
        final ProgressDialog loadingb;
        loadingb = new ProgressDialog(this);
        loadingb.setMessage("Sincronizando biblioteca ...");
        loadingb.show();
        //Llamado al constructor de la clase GsonRequest
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(URL_BASE + archivoPhp, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingb.dismiss();
                        Iterator<?> keys = response.keys();
                        try {
                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                JSONArray jsonArray = response.getJSONArray(key);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    String tags = obj.get("tags").toString();
                                    JSONObject valoresDb = new JSONObject(tags);

                                    String nombre = "";
                                    String titulo = "";
                                    String artista = "";
                                    String genero = "";
                                    String album = "";

                                    nombre = valoresDb.get("nombre").toString();
                                    if (valoresDb.has("titulo")) {
                                        titulo = valoresDb.get("titulo").toString();
                                    }
                                    if (valoresDb.has("artista")) {
                                        artista = valoresDb.get("artista").toString();
                                    }
                                    if (valoresDb.has("genero")) {
                                        genero = valoresDb.get("genero").toString();
                                    }
                                    if (valoresDb.has("album")) {
                                        album = valoresDb.get("album").toString();
                                    }
                                    manager.insBiblioteca(nombre, titulo, artista, genero, album);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                //ErrorListener errorListener
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("", "Error obteniendo datos del servidor !!");
                    }
                }
        );

        // Añadir petición JSON a la cola
        SingletonDeRumba.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjReq);
    }

    public void onLocationChanged(Location location) {
        this.ubicacion_actual = location;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) 16.5));
    }

    public void agregarMarcador(Double latitud, Double longitud, String nombreSitio, String descripcion) {
        LatLng latLng = new LatLng(latitud, longitud);
        googleMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title(nombreSitio)
                        .snippet(descripcion)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador))
        );
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        ubicacion_actual = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,  this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "permission was granted, :)",
                            Toast.LENGTH_LONG).show();

                    try{
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
                    }catch(SecurityException e){
                        Toast.makeText(this,"SecurityException:\n" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "permission denied, ...:(", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed: \n" + connectionResult.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        this.googleMap = gMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        ubicacion_actual = locationManager.getLastKnownLocation(provider);

        if (ubicacion_actual != null) {
            onLocationChanged(ubicacion_actual);
        }

        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            if (ubicacion_actual != null) {
                addresses = gcd.getFromLocation(ubicacion_actual.getLatitude(), ubicacion_actual.getLongitude(), 1);
                if (addresses.size() > 0)
                    ciudad = addresses.get(0).getLocality();
            }
            else {
                Toast.makeText(this, "Ubicacion no establecida aun!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        /** SINCRONIZA LA BASE DE DATOS LOCAL CON DATOS OBTENIDOS DEL SERVIDOR *************************************************/
        syncEstablecimientos();
        syncSedes();
        syncBiblioteca();
        /** -------------   --------------------------------------------------------------------------------------------------------*/
      /*  tv = (TextView) (findViewById(R.id.tvCantDrumbas));
                            Cursor datosEstablecimientos = manager.getInfoEstablecimientos();
                            int conteo = 0;

                            if (datosEstablecimientos.moveToFirst()) {
                                do {

                                    String nombre = datosEstablecimientos.getString(0);
                                    String sede = datosEstablecimientos.getString(1);
                                    Double latitud = datosEstablecimientos.getDouble(2);
                                    Double longitud = datosEstablecimientos.getDouble(3);


                                    String ciudadactual;
                                    try {
                                        addresses = gcd.getFromLocation(latitud, longitud, 1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if (addresses.size() > 0 && addresses != null) {
                                        ciudadactual = addresses.get(0).getLocality();
                                        if (ciudadactual.equals(ciudad)) {
                                            agregarMarcador(latitud, longitud, nombre, sede);
                                            conteo++;
                                        }
                                    }
                                }
                                while (datosEstablecimientos.moveToNext());
                                tv.setText("Se han encontrado " + conteo + " DeRumba en la ciudad de " + ciudad);
                            }*/
    }

}

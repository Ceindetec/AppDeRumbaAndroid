package org.ceindetec.d3rumb4;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.HashMap;

class CustomArrayAdapterBiblioteca extends BaseAdapter implements Filterable {
    Context context;
    ArrayList<CancionBiblioteca> cancionesBiblioteca;
    ArrayList<CancionBiblioteca> listaFiltrada;
    ValueFilter valueFilter;
    DataBaseManager manager;
   // GlobalVars globalVars = new GlobalVars();

    public CustomArrayAdapterBiblioteca(Context context, ArrayList<CancionBiblioteca> canciones) {
        this.context = context;
        this.cancionesBiblioteca = canciones;
        listaFiltrada = canciones;
    }

    @Override
    public int getCount() {
        return cancionesBiblioteca.size();
    }

    @Override
    public Object getItem(int position) {
        return cancionesBiblioteca.get(position);
    }

    @Override
    public long getItemId(int position) {
        return cancionesBiblioteca.indexOf(getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

      //  if (convertView == null) {
            convertView = mInflater.inflate(R.layout.items_biblioteca, null);
            TextView textViewNombres = (TextView) convertView.findViewById(R.id.nombre);
            CancionBiblioteca cancion = cancionesBiblioteca.get(position);
            textViewNombres.setText(cancion.getNombre());

            ImageButton addBtn = (ImageButton) convertView.findViewById(R.id.agregar_cancion);
            addBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    CancionBiblioteca cancionTest =  cancionesBiblioteca.get(position);
                    String id = cancionTest.getId();
                    DataBaseManager manager = new DataBaseManager(context);

                    long existe = manager.checkCancionOnPlaylist(id);
                    if (existe > 0){
                        Toast.makeText(context,"La canción seleccionada ya se encuentra en la playlist online !!!",Toast.LENGTH_LONG).show();
                    }
                    else{
                        registrarCancionBD(cancionTest);
                    }
                }
            });


       // }
        return convertView;
    }

    private void registrarCancionBD(CancionBiblioteca cancionTest) {
        DataBaseManager manager = new DataBaseManager(context);
        //String URL_BASE = "http://192.168.0.21/derumba/";
        String URL_BASE = GlobalVars.getGlobalVarsInstance().getUrlBase();
        long posicion = manager.getTotalCancionesPlaylist();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("sede_id", String.valueOf(GlobalVars.getGlobalVarsInstance().getSede()));
        parameters.put("codigo", cancionTest.getId());
        parameters.put("agregado_por", GlobalVars.getGlobalVarsInstance().getNickname());
        parameters.put("posicion", String.valueOf(posicion +1));
        parameters.put("votos", "1");
        String archivoPhp = "insertCancion.php";

        GsonRequest jsonObjReq = new GsonRequest( Request.Method.POST, URL_BASE + archivoPhp, JsonObject.class ,parameters,

                new Response.Listener<JsonObject>() {

                    @Override
                    public void onResponse(JsonObject response) {

                        try {
                            JsonArray jsonArray = response.getAsJsonArray("res_insert");
                            JsonElement data = jsonArray.get(0);
                            JsonObject respuesta = data.getAsJsonObject();
                            String error =  respuesta.get("error").getAsString();
                            if(error.equals("false"))
                                Toast.makeText(context, respuesta.get("message").toString(),Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(context, respuesta.get("message").toString(),Toast.LENGTH_LONG).show();

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
        SingletonDeRumba.getInstance(context).addToRequestQueue(jsonObjReq);
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<CancionBiblioteca> filterList = new ArrayList<>();
                for (int i = 0; i < listaFiltrada.size(); i++) {
                    if ((listaFiltrada.get(i).getNombre().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        CancionBiblioteca cancion = new CancionBiblioteca(listaFiltrada.get(i).getNombre(), listaFiltrada.get(i).getId());

                        filterList.add(cancion);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = listaFiltrada.size();
                results.values = listaFiltrada;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            cancionesBiblioteca = (ArrayList<CancionBiblioteca>) results.values;
            notifyDataSetChanged();
        }

    }
}
/*
*  deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                list.remove(position); //or some other task
                notifyDataSetChanged();
            }
        });

* */
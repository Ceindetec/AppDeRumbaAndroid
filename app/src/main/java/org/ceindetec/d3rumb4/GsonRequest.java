package org.ceindetec.d3rumb4;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;


public class GsonRequest<T> extends Request<T> {

    private Gson mGson = new Gson();
    private Class<T> clazz;
    private Map<String, String> headers;
    private Map<String, String> params;
    private Listener<T> listener;

    /*
     * Make a GET request and return a parsed object from JSON.
     *
     * @param int           method          Metodo por el cual se realiza la peticion
     * @param String        url             URL donde se realiza la peticion
     * @param Class<T>      clazz           Clase o modelo en el que se formatean los datos
     * @param Map<String,   String>   headers         Encabezado de la peticion
     * @param Listener<T>   listener        Escuchador de peticion correcta
     * @param ErrorListener errorListener   Escichador de error en la peticion
     */
    public GsonRequest(int method,
                       String url,
                       Class<T> clazz,
                       Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.listener = listener;
        mGson = new Gson();
    }

    /*
     * Make a POST request and return a parsed object from JSON.
     *
     * @param int           method                  Metodo por el cual se realiza la peticion
     * @param String        url                     URL donde se realiza la peticion
     * @param Class<T>      clazz                   Clase o modelo en el que se formatean los datos
     * @param Map<String,   String>   params        Encabezado de la peticion
     * @param Listener<T>   listener                Escuchador de peticion correcta
     * @param ErrorListener errorListener           Escichador de error en la peticion
     */
    public GsonRequest(int method,
                       String url,
                       Class<T> clazz,
                       Map<String, String> params,
                       Listener<T> listener,
                       ErrorListener errorListener) {

        super(method, url, errorListener);

        this.clazz = clazz;
        this.params = params;
        this.listener = listener;
        this.headers = null;
        mGson = new Gson();
    }


    /**
     * @return
     * @throws AuthFailureError
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    /**
     * @param response The parsed response returned by
     */
    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    /**
     * Metodo que maneja la peicion GSON de la cola de peticiones
     *
     * @param response Response from the network
     * @return
     */
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {

            //Cadena de peticion JSON hacia la URL
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));


            return Response.success(mGson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}

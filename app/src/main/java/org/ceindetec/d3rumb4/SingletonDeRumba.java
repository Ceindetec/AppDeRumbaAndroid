package org.ceindetec.d3rumb4;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public final class SingletonDeRumba {

    // Atributos de la clase social media singleton

    //Instancia unica del singleton
    private static SingletonDeRumba singleton;

    //Atributo imageloader
    private ImageLoader imageLoader;

    //Cola de peticiones
    private RequestQueue requestQueue;

    //Atributo contexto
    private static Context contextoAplicacion;

    /*
     * Constructor de la clase SingletonAppRestaurante
     * recibe un context de tipo Context
     * @param context
     */
    private SingletonDeRumba(Context context) {

        //Se ingresa el contexto de la aplicacion del valor contexto enviado al constructor
        SingletonDeRumba.contextoAplicacion = context;

        //Se inicializa la cola de peticiones desde el metodo getRequestQueue
        requestQueue = getRequestQueue();

        //Se incializa el cargador de imagenes
        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    /*
     * Metodo que crea la instancia unica
     * recibe un context de tipo Context
     * @param context
     * @return
     */
    public static synchronized SingletonDeRumba getInstance(Context context) {

        //Si la instancia No esta creada la crea
        if (singleton == null) {
            singleton = new SingletonDeRumba(context);
        }
        return singleton;
    }

    /*
     * Metodo que obtiene la peticion de la cola de peticiones
     * No recibe Parametros
     * @return requestQueue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(contextoAplicacion.getApplicationContext());
        }
        return requestQueue;
    }

    /*
     * Metodo que agrega las peticiones a la cola
     * recibe una peticion Request de tipo Request (variable de la libreria volley)
     * @param req
     * No retorna data
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /*
     * Metodo que obtiene la imagen del loader
     * No recibe Parametros
     * @return imageLoader
     */
    public ImageLoader getImageLoader() {
        return imageLoader;
    }

}


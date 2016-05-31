package org.ceindetec.d3rumb4;


import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBaseManager extends SQLiteOpenHelper {

    public DataBaseManager(Context context) {
        super(context, "dbderumba1.sqlite", null, 1);

    }

    public static final String CREATE_BIBLIOTECA = "create table biblioteca (id integer primary key autoincrement, "+
            "nombre text not null, "+
            "titulo text, "+
            "artista text, "+
            "genero text, "+
            "album text);";

    public static final String CREATE_ESTABLECIMIENTOS = "CREATE TABLE establecimientos (id integer primary key, "+
            "nombre text not null, "+
            "descripcion text not null);";

    public static final String CREATE_SEDES = "create table sedes (id integer primary key, "+
            "establecimiento_id integer not null, "+
            "ciudad text not null, "+
            "sede text not null, "+
            "direccion text not null, "+
            "latitud real not null, "+
            "longitud real not null, "+
            "horario text not null, "+
            "estado integer not null, "+
            "codigoAcceso text not null, "+
            "maxCanciones integer not null, " +
            "FOREIGN KEY(establecimiento_id) REFERENCES establecimientos(id));";

    public static final String CREATE_LISTA_ONLINE = "CREATE TABLE lista_online (id integer primary key, "+
            "sede_id integer not null, "+
            "biblioteca_id integer not null, "+
            "nombre_cancion text not null, "+
            "duracion integer, "+
            "agregado_por text not null, "+
            "votos integer DEFAULT 1, "+
            "posicion integer not null, "+
            "agregado_en integer not null, "+
            "FOREIGN KEY(sede_id) REFERENCES sedes(id), " +
            "FOREIGN KEY(biblioteca_id) REFERENCES biblioteca(id));";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataBaseManager.CREATE_ESTABLECIMIENTOS);
        db.execSQL(DataBaseManager.CREATE_SEDES);
        db.execSQL(DataBaseManager.CREATE_BIBLIOTECA);
        db.execSQL(DataBaseManager.CREATE_LISTA_ONLINE);
    }


    public void insEstablecimiento(int id, String nombre, String descripcion )
    {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into establecimientos values ("+id+","+DatabaseUtils.sqlEscapeString(nombre)+","+DatabaseUtils.sqlEscapeString(descripcion)+")");
    }


    public void insSedes(int id, int establecimiento_id, String ciudad, String sede, String direccion, Double latitud, Double longitud, String horario, String estado, String codigoAcceso, String maxCanciones)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into sedes values ("+id+","+establecimiento_id+","+ DatabaseUtils.sqlEscapeString(ciudad)+","+DatabaseUtils.sqlEscapeString(sede)+","+DatabaseUtils.sqlEscapeString(direccion)+","+latitud+","+longitud+","+
                DatabaseUtils.sqlEscapeString(horario)+","+estado+","+DatabaseUtils.sqlEscapeString(codigoAcceso)+","+maxCanciones+")");
    }

    public void insBiblioteca(String nombre, String titulo, String artista, String genero, String album)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into biblioteca values (null, "
                +DatabaseUtils.sqlEscapeString(nombre)+","
                +DatabaseUtils.sqlEscapeString(titulo)+","
                +DatabaseUtils.sqlEscapeString(artista)+","
                +DatabaseUtils.sqlEscapeString(genero)+","
                +DatabaseUtils.sqlEscapeString(album)+");");
    }

    public void insPlayListOnline(String sede_id,String biblioteca_id,String nombre_cancion,String duracion,
                                  String agregado_por,String votos,String posicion, String agregado_en )
    {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into lista_online values (null,"+
                DatabaseUtils.sqlEscapeString(sede_id)+","+DatabaseUtils.sqlEscapeString(biblioteca_id)+","+
                DatabaseUtils.sqlEscapeString(nombre_cancion)+"," + DatabaseUtils.sqlEscapeString(duracion)+","+
                DatabaseUtils.sqlEscapeString(agregado_por)+"," + DatabaseUtils.sqlEscapeString(votos)+","+
                DatabaseUtils.sqlEscapeString(posicion)+"," + DatabaseUtils.sqlEscapeString(agregado_en)+")");
    }

    public void truncateTables(){

        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM sedes;");
        database.execSQL("DELETE FROM establecimientos;");
        database.execSQL("DELETE FROM biblioteca;");
        database.execSQL("DELETE FROM lista_online;");
    }

    public Cursor getInfoEstablecimientos(){

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor mCount= database.rawQuery("SELECT nombre, se.sede, se.latitud, se.longitud FROM sedes se INNER JOIN establecimientos ON establecimientos.id = se.establecimiento_id ",null);
        return  mCount;
    }


    public Cursor getPlayList(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor mCount= database.rawQuery("SELECT biblioteca_id, nombre_cancion, duracion, agregado_por, votos, posicion FROM lista_online order by posicion",null);
        return  mCount;
    }

    public Cursor getBibliotecaList(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT id, nombre FROM biblioteca order by nombre",null);
    }

    public Cursor verificarCodigoAcceso(String codigo) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT id, latitud, longitud FROM SEDES WHERE codigoAcceso = '" + codigo+"'", null);
    }

    public long checkCancionOnPlaylist(String idBiblioteca){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCount= db.rawQuery("SELECT COUNT(*) FROM lista_online WHERE biblioteca_id = '"+idBiblioteca+ "'", null);
        mCount.moveToFirst();
        long count= mCount.getInt(0);
        mCount.close();
        return count;
    }

    public long getTotalCancionesPlaylist(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCount= db.rawQuery("SELECT COUNT(*) FROM lista_online", null);
        mCount.moveToFirst();
        long count= mCount.getInt(0);
        mCount.close();
        return count;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

 /* public ContentValues generarContentValues(String nombre, String telefono) {

        ContentValues valores = new ContentValues();
        valores.put(CN_NAME, nombre);
        valores.put(CN_PHONE, telefono);
        return valores;
    }*/
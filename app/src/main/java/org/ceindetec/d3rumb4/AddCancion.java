package org.ceindetec.d3rumb4;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class AddCancion extends DialogFragment implements SearchView.OnQueryTextListener {

    DataBaseManager manager;
    SearchView search;
    CustomArrayAdapterBiblioteca adapter_biblioteca;
    Context context;

    public AddCancion() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        manager = new DataBaseManager(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.add_cancion, null);
        ListView lv = (ListView) vista.findViewById(R.id.biblioteca_list);


        Cursor listaBd = manager.getBibliotecaList();
        ArrayList<CancionBiblioteca> canciones = new ArrayList<>();
        if (listaBd.moveToFirst()) {
            do {
                CancionBiblioteca aux = new CancionBiblioteca("Nombre: " + listaBd.getString(1),listaBd.getString(0));
                canciones.add(aux);
            }
            while (listaBd.moveToNext());
        }
        search = (SearchView) vista.findViewById(R.id.search_biblioteca);
        search.setOnQueryTextListener(this);
        adapter_biblioteca = new CustomArrayAdapterBiblioteca(getContext(), canciones );

        lv.setAdapter(adapter_biblioteca);
        Button btn = (Button) vista.findViewById(R.id.dismiss);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });
        builder.setView(vista);
        return builder.create();
    }



    @Override
    public boolean onQueryTextChange(String newText) {
        adapter_biblioteca.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


}


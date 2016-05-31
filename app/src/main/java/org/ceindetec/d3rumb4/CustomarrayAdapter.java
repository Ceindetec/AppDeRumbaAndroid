package org.ceindetec.d3rumb4;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

class CustomArrayAdapter extends BaseAdapter implements Filterable {
    Context context;
    ArrayList<CancionOnline> canciones;
    ArrayList<CancionOnline> mStringFilterList;
    ValueFilter valueFilter;

    public CustomArrayAdapter(Context context, ArrayList<CancionOnline> canciones) {
        this.context = context;
        this.canciones = canciones;
        mStringFilterList = canciones;
    }

    @Override
    public int getCount() {
        return canciones.size();
    }

    @Override
    public Object getItem(int position) {
        return canciones.get(position);
    }

    @Override
    public long getItemId(int position) {
        return canciones.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.items_online, null);

            TextView textViewNombres = (TextView) convertView.findViewById(R.id.cancion);
            TextView textViewDuraciones = (TextView) convertView.findViewById(R.id.duracion);
            TextView textViewDjs = (TextView) convertView.findViewById(R.id.dj);

            CancionOnline cancion = canciones.get(position);

            textViewNombres.setText(cancion.getNombre());
            textViewDuraciones.setText(cancion.getDuracion());
            textViewDjs.setText(cancion.getAgregadoPor());
        }
        return convertView;
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
                ArrayList<CancionOnline> filterList = new ArrayList<CancionOnline>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if ((mStringFilterList.get(i).getNombre().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        CancionOnline cancion = new CancionOnline(mStringFilterList.get(i)
                                .getNombre(), mStringFilterList.get(i)
                                .getDuracion(), mStringFilterList.get(i)
                                .getAgregadoPor());

                        filterList.add(cancion);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            canciones = (ArrayList<CancionOnline>) results.values;
            notifyDataSetChanged();
        }

    }
}
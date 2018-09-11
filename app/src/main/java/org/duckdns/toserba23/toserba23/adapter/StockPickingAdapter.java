package org.duckdns.toserba23.toserba23.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.model.StockPicking;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 22/02/18.
 */

public class StockPickingAdapter extends ArrayAdapter<StockPicking> implements Filterable {
    private List<StockPicking> originalData = null;
    private List<StockPicking> filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    private Context mContext;

    public StockPickingAdapter(Activity context, ArrayList<StockPicking> data) {
        super(context, 0, data);
        mContext = context;
        this.filteredData = data ;
        this.originalData = data ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() { return filteredData.size(); }

    public StockPicking getItem(int position) { return filteredData.get(position); }

    public long getItemId(int position) { return position; }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.stock_picking_list_adapter, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.dotIndicator = (TextView) convertView.findViewById(R.id.dot_indicator);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.origin = (TextView) convertView.findViewById(R.id.origin);
            holder.ref = (TextView) convertView.findViewById(R.id.ref);
            holder.otherNotes = (TextView) convertView.findViewById(R.id.detail_other_notes);
            holder.partner = (TextView) convertView.findViewById(R.id.partner);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // If weren't re-ordering this you could rely on what you set last time
        holder.dotIndicator.setBackgroundColor(ContextCompat.getColor(mContext, DisplayFormatter.getStockPickingStateColor(filteredData.get(position).getState())));
        holder.date.setText(DisplayFormatter.formatDateTime(filteredData.get(position).getScheduledDate()));
        holder.origin.setText(DisplayFormatter.formatString(filteredData.get(position).getOrigin()));
        holder.ref.setText(DisplayFormatter.formatString(filteredData.get(position).getName()));
        holder.otherNotes.setText(DisplayFormatter.formatString(filteredData.get(position).getXNotes()));
        holder.partner.setText(DisplayFormatter.formatString(filteredData.get(position).getPartnerName()));

        return convertView;
    }

    static class ViewHolder {
        TextView date;
        TextView origin;
        TextView dotIndicator;
        TextView ref;
        TextView otherNotes;
        TextView partner;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<StockPicking> list = originalData;

            int count = list.size();
            final ArrayList<StockPicking> nlist = new ArrayList<StockPicking>(count);

            String filterableString ;
            String filterableString2 ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getName();
                if (list.get(i).getName()!=null) {
                    filterableString2 = list.get(i).getName();
                } else {
                    filterableString2 = "";
                }
                if (filterableString.toLowerCase().contains(filterString) || filterableString2.toLowerCase().contains(filterString)) {
                    nlist.add(list.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<StockPicking>) results.values;
            notifyDataSetChanged();
        }
    }
}

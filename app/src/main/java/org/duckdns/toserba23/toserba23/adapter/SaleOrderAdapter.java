package org.duckdns.toserba23.toserba23.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 22/02/18.
 */

public class SaleOrderAdapter extends ArrayAdapter<SaleOrder> implements Filterable {
    private List<SaleOrder> originalData = null;
    private List<SaleOrder> filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    private Context mContext;

    public SaleOrderAdapter(Activity context, ArrayList<SaleOrder> data) {
        super(context, 0, data);
        mContext = context;
        this.filteredData = data ;
        this.originalData = data ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() { return filteredData.size(); }

    public SaleOrder getItem(int position) { return filteredData.get(position); }

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
            convertView = mInflater.inflate(R.layout.sale_order_list_adapter, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.state = (TextView) convertView.findViewById(R.id.state);
            holder.ref = (TextView) convertView.findViewById(R.id.ref);
            holder.amount = (TextView) convertView.findViewById(R.id.amount);
            holder.partner = (TextView) convertView.findViewById(R.id.partner);
            holder.warehouse = (TextView) convertView.findViewById(R.id.warehouse);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // If weren't re-ordering this you could rely on what you set last time
        holder.date.setText(DisplayFormatter.formatDate(filteredData.get(position).getDateOrder()));
        holder.state.setText(DisplayFormatter.formatString(filteredData.get(position).getStateName()));
        holder.ref.setText(DisplayFormatter.formatString(filteredData.get(position).getName()));
        holder.amount.setText(DisplayFormatter.formatCurrency(filteredData.get(position).getAmountTotal()));
        holder.partner.setText(DisplayFormatter.formatString(filteredData.get(position).getPartner().getName()));
        holder.warehouse.setText(DisplayFormatter.formatString(filteredData.get(position).getWarehouse().getName()));

        return convertView;
    }

    static class ViewHolder {
        TextView date;
        TextView state;
        TextView ref;
        TextView amount;
        TextView partner;
        TextView warehouse;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<SaleOrder> list = originalData;

            int count = list.size();
            final ArrayList<SaleOrder> nlist = new ArrayList<SaleOrder>(count);

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
            filteredData = (ArrayList<SaleOrder>) results.values;
            notifyDataSetChanged();
        }
    }
}

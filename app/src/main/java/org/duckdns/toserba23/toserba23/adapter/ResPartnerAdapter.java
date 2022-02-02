package org.duckdns.toserba23.toserba23.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.model.ResPartner;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 30/05/19.
 */

public class ResPartnerAdapter extends ArrayAdapter<ResPartner> implements Filterable {
    private List<ResPartner> originalData = null;
    private List<ResPartner> filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    private Context mContext;

    public ResPartnerAdapter(Activity context, ArrayList<ResPartner> data) {
        super(context, 0, data);
        mContext = context;
        this.filteredData = data ;
        this.originalData = data ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() { return filteredData.size(); }

    public ResPartner getItem(int position) { return filteredData.get(position); }

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
            convertView = mInflater.inflate(R.layout.res_partner_list_adapter, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.ref = (TextView) convertView.findViewById(R.id.ref);
            holder.location = (TextView) convertView.findViewById(R.id.location);
            holder.default_pricelist = (TextView) convertView.findViewById(R.id.default_pricelist);
            holder.payment_term = (TextView) convertView.findViewById(R.id.payment_term);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // If weren't re-ordering this you could rely on what you set last time
        holder.name.setText(DisplayFormatter.formatString(filteredData.get(position).getName()));
        holder.ref.setText(DisplayFormatter.formatString(filteredData.get(position).getRef()));
        holder.location.setText(DisplayFormatter.formatString(filteredData.get(position).getCity()).concat(", ").concat(DisplayFormatter.formatString(filteredData.get(position).getState())));
        holder.default_pricelist.setText(DisplayFormatter.formatString(filteredData.get(position).getDefPricelist()));
        holder.payment_term.setText(DisplayFormatter.formatString(filteredData.get(position).getPaymentTerm()));

        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView ref;
        TextView location;
        TextView default_pricelist;
        TextView payment_term;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<ResPartner> list = originalData;

            int count = list.size();
            final ArrayList<ResPartner> nlist = new ArrayList<ResPartner>(count);

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
            filteredData = (ArrayList<ResPartner>) results.values;
            notifyDataSetChanged();
        }
    }
}

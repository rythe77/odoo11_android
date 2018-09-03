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
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 22/02/18.
 */

public class ProductTemplateAdapter extends ArrayAdapter<ProductTemplate> implements Filterable {
    private List<ProductTemplate> originalData = null;
    private List<ProductTemplate> filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    private Context mContext;

    public ProductTemplateAdapter(Activity context, ArrayList<ProductTemplate> data) {
        super(context, 0, data);
        mContext = context;
        this.filteredData = data ;
        this.originalData = data ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() { return filteredData.size(); }

    public ProductTemplate getItem(int position) { return filteredData.get(position); }

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
            convertView = mInflater.inflate(R.layout.product_template_list_adapter, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.qty_available = (TextView) convertView.findViewById(R.id.qty_available);
            holder.virtual_available = (TextView) convertView.findViewById(R.id.virtual_available);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // If weren't re-ordering this you could rely on what you set last time
        holder.code.setText(DisplayFormatter.formatString(filteredData.get(position).getRef()));
        holder.name.setText(DisplayFormatter.formatString(filteredData.get(position).getName()));
        holder.qty_available.setText(DisplayFormatter.formatQuantity(filteredData.get(position).getQty()));
        holder.virtual_available.setText(DisplayFormatter.formatQuantity(filteredData.get(position).getQtyForecast()));

        return convertView;
    }

    static class ViewHolder {
        TextView code;
        TextView name;
        TextView qty_available;
        TextView virtual_available;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<ProductTemplate> list = originalData;

            int count = list.size();
            final ArrayList<ProductTemplate> nlist = new ArrayList<ProductTemplate>(count);

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
            filteredData = (ArrayList<ProductTemplate>) results.values;
            notifyDataSetChanged();
        }
    }
}

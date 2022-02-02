package org.duckdns.toserba23.toserba23.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.model.HrEmployee;
import org.duckdns.toserba23.toserba23.model.HrHolidays;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;

import java.util.ArrayList;
import java.util.List;

public class HrHolidayAdapter extends ArrayAdapter<HrHolidays> implements Filterable {
    private List<HrHolidays> originalData = null;
    private List<HrHolidays> filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    private Context mContext;

    public HrHolidayAdapter(Activity context, ArrayList<HrHolidays> data) {
        super(context, 0, data);
        mContext = context;
        this.filteredData = data ;
        this.originalData = data ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() { return filteredData.size(); }

    public HrHolidays getItem(int position) { return filteredData.get(position); }

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
            convertView = mInflater.inflate(R.layout.hr_holidays_list_adapter, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.date_from = (TextView) convertView.findViewById(R.id.date_from);
            holder.date_to = (TextView) convertView.findViewById(R.id.date_to);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.holiday_status = (TextView) convertView.findViewById(R.id.holiday_status);
            holder.employee = (TextView) convertView.findViewById(R.id.employee);
            holder.status = (TextView) convertView.findViewById(R.id.status);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // If weren't re-ordering this you could rely on what you set last time
        holder.date_from.setText(DisplayFormatter.formatDateTime(filteredData.get(position).getDateFrom()));
        holder.date_to.setText(DisplayFormatter.formatDateTime(filteredData.get(position).getDateTo()));
        holder.name.setText(DisplayFormatter.formatString(filteredData.get(position).getName()));
        holder.holiday_status.setText(DisplayFormatter.formatString(filteredData.get(position).getHolidayStatus().getName()));
        holder.employee.setText(DisplayFormatter.formatString(filteredData.get(position).getEmployee().getName()));
        holder.status.setText(DisplayFormatter.formatString(filteredData.get(position).getStatusName()));

        return convertView;
    }

    static class ViewHolder {
        TextView date_from;
        TextView date_to;
        TextView name;
        TextView holiday_status;
        TextView employee;
        TextView status;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<HrHolidays> list = originalData;

            int count = list.size();
            final ArrayList<HrHolidays> nlist = new ArrayList<HrHolidays>(count);

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
            filteredData = (ArrayList<HrHolidays>) results.values;
            notifyDataSetChanged();
        }
    }
}

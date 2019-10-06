package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import cz.vitskalicky.lepsirozvrh.R;

public class SchoolsAdapter extends PagedListAdapter<SchoolInfo, SchoolsAdapter.ViewHolder> {

    private Context context;
    private SchoolsListFragment.OnItemClickListener listener;


    public SchoolsAdapter(Context context, SchoolsListFragment.OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_school_info, viewGroup, false);
        return new ViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SchoolInfo item = getItem(position);
        holder.bind(item);
    }

    private static DiffUtil.ItemCallback<SchoolInfo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SchoolInfo>() {

                @Override
                public boolean areItemsTheSame(SchoolInfo oldItem, SchoolInfo newItem) {
                    // The ID property identifies when items are the same.
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(SchoolInfo oldItem, SchoolInfo newItem) {
                    // Don't use the "==" operator here. Either implement and use .equals(),
                    // or write custom data comparison logic here.
                    return oldItem.url.equals(newItem.url) && oldItem.search_text.equals(newItem.search_text) && oldItem.name.equals(newItem.name) && oldItem.id.equals(newItem.id);
                }
            };


    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView twName;
        TextView twURL;
        SchoolInfo item = null;
        SchoolsListFragment.OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView, SchoolsListFragment.OnItemClickListener listener) {
            super(itemView);
            view = itemView;
            twName = view.findViewById(R.id.textViewName);
            twURL = view.findViewById(R.id.textViewURL);
            this.listener = listener;
        }

        public void bind(SchoolInfo item) {
            this.item = item;
            if (item == null) {
                clear();
            } else {
                twName.setText(item.name);
                twURL.setText(item.url);
                view.setOnClickListener(v -> listener.onClick(item.url));
            }
        }

        public void clear() {
            item = null;
            twName.setText("");
            twURL.setText("");
            view.setOnClickListener(v -> {
            });
        }
    }
}

package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;
import static com.googlecode.cqengine.query.QueryFactory.*;

import java.util.ArrayList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.R;

public class SchoolsAdapter extends RecyclerView.Adapter<SchoolsAdapter.ViewHolder> {

    Context context;
    IndexedCollection<SchoolInfo> collection;
    SchoolsListFragment.OnItemClickListener listener;
    String searchText = "";

    List<SchoolInfo> itemList = new ArrayList<>();

    public SchoolsAdapter(Context context, IndexedCollection<SchoolInfo> collection, SchoolsListFragment.OnItemClickListener listener) {
        this.context = context;
        this.collection = collection;
        this.listener = listener;

        refreshList();
    }

    public void refreshList(){
        ResultSet<SchoolInfo> results = collection.retrieve(contains(SchoolInfo.STRIPED_NAME, searchText), queryOptions(orderBy(ascending(SchoolInfo.NAME))));
        itemList = new ArrayList<>(results.size());
        for (SchoolInfo item:results) {
            itemList.add(item);
        }
        notifyDataSetChanged();
    }

    public void onSearchChange(String searchText){
        this.searchText = SchoolInfo.stripName(searchText);
        refreshList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_school_info, viewGroup, false);
        return new ViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(itemList.get(i));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

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

        public void bind(SchoolInfo item){
            this.item = item;
            if (item == null){
                clear();
            }else {
                twName.setText(item.name);
                twURL.setText(item.url);
                view.setOnClickListener(v -> listener.onClick(item.url));
            }
        }

        public void clear(){
            item = null;
            twName.setText("");
            twURL.setText("");
            view.setOnClickListener(v -> {});
        }
    }
}

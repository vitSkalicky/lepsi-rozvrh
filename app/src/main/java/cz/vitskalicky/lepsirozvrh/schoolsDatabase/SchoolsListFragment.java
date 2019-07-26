package cz.vitskalicky.lepsirozvrh.schoolsDatabase;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;

import cz.vitskalicky.lepsirozvrh.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolsListFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    SchoolsAdapter adapter = null;

    ProgressBar progressBar;
    EditText etSearch;
    TextView twError;
    ImageView ivError;

    OnItemClickListener listener = url -> {};

    RequestQueue requestQueue = null;


    public SchoolsListFragment() {
        // Required empty public constructor
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schools_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        etSearch = view.findViewById(R.id.editTextSearch);
        twError = view.findViewById(R.id.textViewError);
        ivError = view.findViewById(R.id.imageViewError);

        recyclerView.setVisibility(View.GONE);
        twError.setVisibility(View.GONE);
        ivError.setVisibility(View.GONE);


        etSearch.addTextChangedListener(new TextWatcher() {//<editor-fold desc="unused methods">
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            //</editor-fold>
            @Override
            public void afterTextChanged(Editable s) {
                if (adapter != null){
                    adapter.onSearchChange(s.toString());
                }
            }
        });



        requestQueue = SchoolsDatabaseAPI.getAllSchools(getContext(), collection -> {
            if (collection != null) {
                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                adapter = new SchoolsAdapter(getContext(), collection, listener);

                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                adapter.onSearchChange(etSearch.getText().toString());
            }else {
                progressBar.setVisibility(View.GONE);
                twError.setVisibility(View.VISIBLE);
                ivError.setVisibility(View.VISIBLE);
            }
        });

        //automatically show keyboard
        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null){
            requestQueue.cancelAll(request -> true/*all requests*/);
        }
    }

    public static interface OnItemClickListener{
        public void onClick(String url);
    }
}

package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.schoolsDatabase.SchoolsListFragment;

public class SchoolsListActivity extends CyaneaAppCompatActivity {
    public static final String EXTRA_URL = SchoolsListActivity.class.getCanonicalName() + ".url";
    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;
    SchoolsListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schools);

        fragment = (SchoolsListFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentSchools);
        fragment.setOnItemClickListener(url -> {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_URL, url);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCEL);
        super.onBackPressed();
    }
}

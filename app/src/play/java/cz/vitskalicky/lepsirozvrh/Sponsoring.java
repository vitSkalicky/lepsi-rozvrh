package cz.vitskalicky.lepsirozvrh;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class Sponsoring {
    private Context context;

    public Sponsoring(Context context) {
        this.context = context;
    }

    public boolean isEnabled(){
        return false;
    }

    public boolean inSponsor(){
        return true;
    }

    public void showDialog(AppCompatActivity activity){
        FragmentManager fm = activity.getSupportFragmentManager();
        DonateDialogFragment DonateDF = new DonateDialogFragment();
        DonateDF.show(fm, "donate_df");
    }
}

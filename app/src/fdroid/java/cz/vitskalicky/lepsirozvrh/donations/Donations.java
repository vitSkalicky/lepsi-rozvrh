package cz.vitskalicky.lepsirozvrh.donations;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.donations.PurchaseActivity;

public class Donations {
    private Context context;

    public Donations(Context context, PurchaseActivity pActivity, AppCompatActivity activity) {
        this.context = context;
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean isSponsor() {
        return true;
    }

    public void restorePurchases() {
        Toast.makeText(context, "Error (in-app purchases not enabled in " + BuildConfig.FLAVOR + " flavour)", Toast.LENGTH_SHORT).show();
    }

    public void showDialog() {
        Toast.makeText(context, "Error (no donate dialog in " + BuildConfig.FLAVOR + " flavour)", Toast.LENGTH_SHORT).show();
    }

    public void release(){

    }
}

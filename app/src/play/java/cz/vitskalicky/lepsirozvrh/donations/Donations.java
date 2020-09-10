package cz.vitskalicky.lepsirozvrh.donations;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import cz.vitskalicky.lepsirozvrh.Utils;

public class Donations {
    private Context context;
    private Billing billing;
    private AppCompatActivity activity;

    private DonateDialogFragment donateDF;

    public Donations(Context context, AppCompatActivity activity, Utils.Listener onPurchaseChangesListener) {
        this.context = context;
        this.activity = activity;
        billing = new Billing(context);
        billing.addOnPurchaseChangeListener(onPurchaseChangesListener);
        FragmentManager fm = activity.getSupportFragmentManager();
        donateDF = (DonateDialogFragment) fm.findFragmentByTag("donateDF");
        if (donateDF != null){
            donateDF.init(billing);
        }
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isSponsor() {
        return billing.isSponsor();
    }

    public void restorePurchases() {
        billing.restorePurchases();
    }

    public void showDialog() {
        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.findFragmentByTag("donateDF") == null) {
            if (donateDF == null) {
                donateDF = new DonateDialogFragment();
            }
            donateDF.init(billing);
            donateDF.show(fm, "donateDF");
        }
    }

    public void release(){
        billing.release();
    }
}

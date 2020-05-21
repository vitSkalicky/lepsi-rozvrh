package cz.vitskalicky.lepsirozvrh.donations;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class Donations {
    private Context context;
    private Billing billing;
    private PurchaseActivity pActivity;

    private DonateDialogFragment donateDF;

    public Donations(Context context, PurchaseActivity pActivity, AppCompatActivity activity) {
        this.context = context;
        this.pActivity = pActivity;
        billing = new Billing(context, pActivity);
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

    public void showDialog(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        if (donateDF == null){
            donateDF = new DonateDialogFragment();
        }
        donateDF.init(billing);
        donateDF.setIsSponsor(isSponsor());
        donateDF.show(fm, "donateDF");
    }

    public void release(){
        billing.release();
    }
}

package cz.vitskalicky.lepsirozvrh.donations;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class Donations {
    private Context context;
    private Billing billing;
    private PurchaseActivity pActivity;

    public Donations(Context context, PurchaseActivity pActivity) {
        this.context = context;
        this.pActivity = pActivity;
        billing = new Billing(context, pActivity);
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean inSponsor() {
        return billing.isSponsor();
    }

    public void restorePurchases() {
        billing.restorePurchases();
    }

    public void showDialog(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        DonateDialogFragment DonateDF = new DonateDialogFragment(billing);
        DonateDF.show(fm, "donate_df");
    }
}

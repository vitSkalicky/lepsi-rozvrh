package cz.vitskalicky.lepsirozvrh.donations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.PurchaseState;
import com.anjlab.android.iab.v3.TransactionDetails;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;

public class Billing {

    public static final String SKU_SMALL_DONATION = "cz.vitskalicky.lepsirozvrh.small-donation";
    public static final String SKU_BIG_DONATION = "cz.vitskalicky.lepsirozvrh.big-donation";

    private BillingProcessor bp;

    private String smallPrice = "";
    private String bigPrice = "";

    private Context context;
    private PurchaseActivity pActivity;

    private boolean isSponsor = false;
    private Utils.Listener onIsSponsorChangeListener = () -> {
    };

    public Billing(Context context, PurchaseActivity pActivity) {
        this.context = context;
        this.pActivity = pActivity;
        pActivity.setActivityResultListener(this::handleActivityResult);

        bp = BillingProcessor.newBillingProcessor(context, "YOUR LICENSE KEY FROM GOOGLE PLAY CONSOLE HERE", new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
                if (details != null && details.purchaseInfo.purchaseData.purchaseState == PurchaseState.PurchasedSuccessfully && (SKU_SMALL_DONATION.equals(productId)) || SKU_BIG_DONATION.equals(productId)) {
                    setIsSponsor(true);
                }
            }

            @Override
            public void onPurchaseHistoryRestored() {
                checkPurchases();
            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
                if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
                    Toast.makeText(context, R.string.purchase_cancelled, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, String.format(context.getText(R.string.purchase_error).toString(), error == null ? String.format(context.getString(R.string.error_code), Integer.toString(errorCode)) : error.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onBillingInitialized() {
                checkPurchases();
            }
        });
        bp.initialize();
    }

    public void checkPurchases() {
        for (String sku : bp.listOwnedProducts()) {
            if (SKU_SMALL_DONATION.equals(sku) || SKU_BIG_DONATION.equals(sku)) {
                setIsSponsor(true);
            }
        }
        /*smallPrice = bp.getPurchaseListingDetails(SKU_SMALL_DONATION).priceText;
        bigPrice = bp.getPurchaseListingDetails(SKU_BIG_DONATION).priceText;*/
    }

    private void setIsSponsor(boolean value) {
        boolean old = isSponsor;
        isSponsor = value;
        if (old != value) {
            pActivity.onSponsorChange(isSponsor);
            onIsSponsorChangeListener.method();
        }
    }

    public void buySmall(Activity activity) {
        bp.purchase(activity, SKU_SMALL_DONATION);
    }

    public void buyBig(Activity activity) {
        bp.purchase(activity, SKU_BIG_DONATION);
    }

    public boolean isSponsor() {
        return isSponsor;
    }

    public void restorePurchases() {
        bp.loadOwnedPurchasesFromGoogle();
    }

    public String getSmallPrice() {
        return smallPrice;
    }

    public String getBigPrice() {
        return bigPrice;
    }

    public BillingProcessor getBillingProcessor() {
        return bp;
    }

    public void setOnIsSponsorChangeListener(Utils.Listener onIsSponsorChangeListener) {
        this.onIsSponsorChangeListener = onIsSponsorChangeListener;
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        bp.handleActivityResult(requestCode, resultCode, data);
    }
}

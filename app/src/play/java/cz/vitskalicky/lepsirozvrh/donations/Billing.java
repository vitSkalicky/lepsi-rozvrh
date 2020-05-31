package cz.vitskalicky.lepsirozvrh.donations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;

public class Billing implements PurchasesUpdatedListener {

    public static final String SKU_SMALL_DONATION = "small_donation";
    public static final String SKU_BIG_DONATION = "big_donation";

    private SkuDetails smallDetails;
    private SkuDetails bigDetails;

    private BillingClient billingClient;

    private Context context;

    private boolean smallPurchased = false;
    private boolean bigPurchased = false;
    private LinkedList<Utils.Listener> onPurchaseChangeListeners = new LinkedList<>();
    private LinkedList<Utils.Listener> onInitializedListeners = new LinkedList<>();
    private boolean initialized = false;

    public Billing(Context context) {
        this.context = context;

        billingClient = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    //query details
                    List<String> skuList = new ArrayList<>();
                    skuList.add(SKU_SMALL_DONATION);
                    skuList.add(SKU_BIG_DONATION);
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                        (billingResult1, skuDetailsList) -> {
                            for (SkuDetails item : skuDetailsList) {
                                if (SKU_SMALL_DONATION.equals(item.getSku())) {
                                    smallDetails = item;
                                }
                                if (SKU_BIG_DONATION.equals(item.getSku())) {
                                    bigDetails = item;
                                }
                            }

                            //finish
                            initialized = true;
                            for (Utils.Listener listener : onInitializedListeners) {
                                listener.method();
                            }
                    });
                    checkPurchases();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

                // Dear programmer looking at this code,
                // in all tutorials they say "and don't forget to implement your reconnection logic",
                // but even the examples just have //todo: retry connection.
                // So I have no other choice then to write

                //todo: implement this when you find out how

                // Love,
                // the frustrated idiot who wrote this code
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        boolean oldsmall = smallPurchased;
        boolean oldbig = bigPurchased;

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(context, R.string.purchase_cancelled, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, String.format(context.getText(R.string.purchase_error).toString(), billingResult.getResponseCode() + " - " + billingResult.getDebugMessage()), Toast.LENGTH_LONG).show();
        }

        if (oldsmall != isSmallPurchased() || oldbig != isBigPurchased()){
            notifyOnPurchaseListeners();
        }
    }

    private void checkPurchases() {
        boolean oldsmall = smallPurchased;
        boolean oldbig = bigPurchased;

        Purchase.PurchasesResult purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        for (Purchase item : purchases.getPurchasesList()) {
            handlePurchase(item);
        }
        if (oldsmall != isSmallPurchased() || oldbig != isBigPurchased()){
            notifyOnPurchaseListeners();
        }
    }

    private void handlePurchase(Purchase p) {
        if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (SKU_SMALL_DONATION.equals(p.getSku())) {
                smallPurchased = true;
            }
            if (SKU_BIG_DONATION.equals(p.getSku())) {
                bigPurchased = true;
            }

            if (!p.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(p.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                });
            }
        }
    }

    private void notifyOnPurchaseListeners() {
        for (Utils.Listener listener : onPurchaseChangeListeners) {
            listener.method();
        }
    }

    public void buySmall(Activity activity) {
        if (!isInitialized()) return;
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(smallDetails)
                .build();
        BillingResult result = billingClient.launchBillingFlow(activity,flowParams);

    }

    public void buyBig(Activity activity) {
        if (!isInitialized()) return;
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(bigDetails)
                .build();
        BillingResult result = billingClient.launchBillingFlow(activity,flowParams);
    }

    public boolean isSponsor() {
        return isSmallPurchased() || isBigPurchased();
    }

    public void restorePurchases() {
        if (!isInitialized()) return;
        checkPurchases();
    }

    public boolean isSmallPurchased() {
        return smallPurchased;
    }

    public boolean isBigPurchased() {
        return bigPurchased;
    }

    public SkuDetails getSmallDetails() {
        return smallDetails;
    }

    public SkuDetails getBigDetails() {
        return bigDetails;
    }

    public BillingClient getBillingClient() {
        return billingClient;
    }

    public void addOnPurchaseChangeListener(Utils.Listener onPurchaseChangeListener) {
        onPurchaseChangeListeners.add(onPurchaseChangeListener);
    }

    public void removeOnPurchaseChangeListener(Utils.Listener listener) {
        onPurchaseChangeListeners.remove(listener);
    }

    public void addOnInitializedListener(Utils.Listener onInitializedListener) {
        onInitializedListeners.add(onInitializedListener);
    }

    public void removeOnInitializedListener(Utils.Listener listener) {
        onInitializedListeners.remove(listener);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void release() {
        billingClient.endConnection();
        onInitializedListeners.clear();
        onPurchaseChangeListeners.clear();
    }
}

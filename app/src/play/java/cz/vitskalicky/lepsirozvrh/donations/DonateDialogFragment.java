package cz.vitskalicky.lepsirozvrh.donations;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class DonateDialogFragment extends DialogFragment {
    private Billing billing;

    private View body;
    private View viewTitleBackground;
    private TextView twTitle;
    private ImageView iwTitle;
    private TextView twText1;
    private Button donateLittle;
    private Button donateMore;

    private boolean isSponsor = false;

    private Theme t;

    private Utils.Listener onSponsorChangeListener = this::updatePurchases;

    public void init(Billing billing){
        this.billing = billing;
        billing.addOnPurchaseChangeListener(onSponsorChangeListener);
        updatePurchases();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        body = getLayoutInflater().inflate(R.layout.dialog_sponsor_body, null);

        viewTitleBackground = body.findViewById(R.id.viewTitleBackground);
        twTitle = body.findViewById(R.id.textViewTitle);
        iwTitle = body.findViewById(R.id.imageViewTitle);
        twText1 = body.findViewById(R.id.textViewText1);


        donateLittle = body.findViewById(R.id.buttonDonateLittle);
        donateMore = body.findViewById(R.id.buttonDonateMore);

        t = Theme.of(getContext());
        updatePurchases();

        updatePrizes();
        billing.addOnInitializedListener(this::updatePrizes);

        donateLittle.setOnClickListener(v -> billing.buySmall(getActivity()));
        donateMore.setOnClickListener(v -> billing.buyBig(getActivity()));

        //DEBUG
        /*twTitle.setOnClickListener(v -> {
            Purchase.PurchasesResult result = billing.getBillingClient().queryPurchases(BillingClient.SkuType.INAPP);
            Toast.makeText(getContext(), "# of purchases: " + result.getPurchasesList().size(), Toast.LENGTH_LONG).show();
            if (result.getPurchasesList().size() > 0){
                ConsumeParams consumeParams =
                        ConsumeParams.newBuilder()
                                .setPurchaseToken(result.getPurchasesList().get(0).getPurchaseToken())
                                .build();
                billing.getBillingClient().consumeAsync(consumeParams, (billingResult1, s) -> {});
            }
        });*/

        return new AlertDialog.Builder(getActivity()).setTitle(null)
                .setPositiveButton(R.string.close, (dialogInterface, i) -> {})
                .setNeutralButton(R.string.use_promo_code, (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/redeem?code="));
                    startActivity(browserIntent);
                }).setView(body).create();
    }

    public void updatePurchases(){
        this.isSponsor = billing.isSponsor();
        if (body != null){
            int bgColor;
            if (isSponsor){
                bgColor = 0xff4CAF50;
                twTitle.setText(R.string.donate_title_ok);
                twText1.setText(R.string.donate_text1_ok);
                iwTitle.setImageResource(R.drawable.ic_check_black_48dp);
            }else {
                bgColor = t.getCPrimary();
                twTitle.setText(R.string.donate_title);
                twText1.setText(R.string.donate_text1);
                iwTitle.setImageResource(R.drawable.ic_monetization_on_black_48dp);
            }
            viewTitleBackground.setBackgroundColor(bgColor);
            twTitle.setTextColor(Theme.Utils.textColorFor(bgColor));
            iwTitle.setColorFilter(Theme.Utils.textColorFor(bgColor));

            donateLittle.setEnabled(!billing.isSmallPurchased());
            donateMore.setEnabled(!billing.isBigPurchased());
        }
    }

    public void updatePrizes(){
        donateLittle.setText(billing.isInitialized() ? String.format(getText(R.string.donate_a_little).toString(), billing.getSmallDetails().getPrice()) : getText(R.string.donate_a_little_no_price));
        donateMore.setText(billing.isInitialized() ? String.format(getText(R.string.donate_more).toString(), billing.getBigDetails().getPrice()) : getText(R.string.donate_more_no_price));
    }

    @Override
    public void onDestroy() {
        billing.removeOnPurchaseChangeListener(onSponsorChangeListener);
        super.onDestroy();
    }
}
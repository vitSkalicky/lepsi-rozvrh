package cz.vitskalicky.lepsirozvrh.donations;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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

    Theme t;

    private Utils.Listener onSponsorChangeListener = () -> setIsSponsor(billing.isSponsor());

    public void init(Billing billing){
        this.billing = billing;
        billing.addOnIsSponsorChangeListener(onSponsorChangeListener);
        setIsSponsor(billing.isSponsor());
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
        setIsSponsor(isSponsor);

        donateLittle.setText(String.format(getText(R.string.donate_a_little).toString(), billing.getSmallPrice()));
        donateMore.setText(String.format(getText(R.string.donate_more).toString(), billing.getBigPrice()));

        donateLittle.setOnClickListener(v -> billing.buySmall(getActivity()));
        donateMore.setOnClickListener(v -> billing.buyBig(getActivity()));

        return new AlertDialog.Builder(getActivity()).setTitle(null).setPositiveButton(R.string.close, (dialogInterface, i) -> {
        }).setView(body).create();
    }

    public void setIsSponsor(boolean isSponsor){
        this.isSponsor = isSponsor;
        if (body != null){
            int bgColor;
            if (isSponsor){
                bgColor = 0xff4CAF50;
                twTitle.setText(R.string.donate_title_ok);
                twText1.setText(R.string.donate_text1_ok);
                iwTitle.setImageResource(R.drawable.ic_check_black_24dp);
            }else {
                bgColor = t.getCPrimary();
                twTitle.setText(R.string.donate_title);
                twText1.setText(R.string.donate_text1);
                iwTitle.setImageResource(R.drawable.ic_monetization_on_black_24dp);
            }
            viewTitleBackground.setBackgroundColor(bgColor);
            twTitle.setTextColor(Theme.Utils.textColorFor(bgColor));
            iwTitle.setColorFilter(Theme.Utils.textColorFor(bgColor));
        }
    }

    @Override
    public void onDestroy() {
        billing.removeOnIsSponsorChangeListener(onSponsorChangeListener);
        super.onDestroy();
    }
}
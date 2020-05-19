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
import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class DonateDialogFragment extends DialogFragment {
    private Billing billing;

    private View body;
    private View viewTitleBackground;
    private TextView twTitle;
    private ImageView iwTitle;
    private Button donateLittle;
    private Button donateMore;

    public DonateDialogFragment(Billing billing) {
        this.billing = billing;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        body = getLayoutInflater().inflate(R.layout.dialog_sponsor_body, null);

        viewTitleBackground = body.findViewById(R.id.viewTitleBackground);
        twTitle = body.findViewById(R.id.textViewTitle);
        iwTitle = body.findViewById(R.id.imageViewTitle);

        donateLittle = body.findViewById(R.id.buttonDonateLittle);
        donateMore = body.findViewById(R.id.buttonDonateMore);

        Theme t = Theme.of(getContext());
        viewTitleBackground.setBackgroundColor(t.getCPrimary());
        twTitle.setTextColor(t.getCyanea().getMenuIconColor());
        iwTitle.setColorFilter(t.getCyanea().getMenuIconColor());

        donateLittle.setText(String.format(getText(R.string.donate_a_little).toString(), billing.getSmallPrice()));
        donateMore.setText(String.format(getText(R.string.donate_more).toString(), billing.getBigPrice()));

        donateLittle.setOnClickListener(v -> billing.buySmall(getActivity()));
        donateMore.setOnClickListener(v -> billing.buyBig(getActivity()));

        return new AlertDialog.Builder(getActivity()).setTitle(null).setPositiveButton(R.string.close, (dialogInterface, i) -> {
        }).setView(body).create();
    }
}
package cz.vitskalicky.lepsirozvrh;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class DonateDialogFragment extends DialogFragment {
    private View body;
    private View viewTitleBackground;
    private TextView twTitle;
    private ImageView iwTitle;

    public DonateDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        body = getLayoutInflater().inflate(R.layout.dialog_sponsor_body, null);

        viewTitleBackground = body.findViewById(R.id.viewTitleBackground);
        twTitle = body.findViewById(R.id.textViewTitle);
        iwTitle = body.findViewById(R.id.imageViewTitle);

        Theme t = Theme.of(getContext());
        viewTitleBackground.setBackgroundColor(t.getCPrimary());
        twTitle.setTextColor(t.getCyanea().getMenuIconColor());
        iwTitle.setColorFilter(t.getCyanea().getMenuIconColor());

        return new AlertDialog.Builder(getActivity()).setTitle(null).setPositiveButton(R.string.close, (dialogInterface, i) -> {
        }).setView(body).create();
    }
}
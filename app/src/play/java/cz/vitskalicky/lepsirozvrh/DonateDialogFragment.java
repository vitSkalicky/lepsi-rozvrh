package cz.vitskalicky.lepsirozvrh;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class DonateDialogFragment extends DialogFragment {

    private View viewTitleBackground;
    private TextView twTitle;
    private ImageView iwTitle;

    public DonateDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View body = getLayoutInflater().inflate(R.layout.dialog_sponsor_body, null);
        View title = getLayoutInflater().inflate(R.layout.dialog_sponsor_title, null);

        viewTitleBackground = title.findViewById(R.id.viewTitleBackground);
        twTitle = title.findViewById(R.id.textViewTitle);
        iwTitle = title.findViewById(R.id.imageViewTitle);

        Theme t = Theme.of(getContext());
        viewTitleBackground.setBackgroundColor(t.getCPrimary());
        twTitle.setTextColor(t.getCyanea().getMenuIconColor());
        iwTitle.setColorFilter(t.getCyanea().getMenuIconColor());

        AlertDialog ad = new AlertDialog.Builder(getContext())
                .setCustomTitle(title)
                .setView(body)
                .setPositiveButton(R.string.close, (dialog, which) -> {})
                .create();
        return ad;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

}
package cz.vitskalicky.lepsirozvrh.whatsnew;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.DialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cz.vitskalicky.lepsirozvrh.R;

public class WhatsNewFragment extends DialogFragment {
    public static final String TAG = DialogFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.changelog)));
        StringBuilder sb = new StringBuilder();
        try {
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = br.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load changelog");
            sb.append("ERROR");
        }

        CharSequence cs = HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT);

        TextView tw = new TextView(getContext());
        tw.setText(cs);
        return tw;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.changelog)));
        StringBuilder sb = new StringBuilder();
        try {
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = br.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load changelog");
            sb.append("ERROR");
        }

        CharSequence cs = HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT);
        return new AlertDialog.Builder(getActivity()).setPositiveButton(R.string.close, (dialogInterface, i) -> {
        }).setMessage(cs).setTitle(R.string.whats_new).create();
    }
}

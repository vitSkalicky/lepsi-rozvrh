package cz.vitskalicky.lepsirozvrh.settings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.theme.Theme;
import cz.vitskalicky.lepsirozvrh.theme.ThemeData;

public class ExportThemeFragment extends Fragment {

    View root;
    TextView textViewData;
    Button buttonCopy;
    Button buttonCopied;
    Button buttonShare;
    public ExportThemeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_export_theme, container, false);
        textViewData = root.findViewById(R.id.textViewData);
        buttonCopy = root.findViewById(R.id.buttonCopy);
        buttonCopied = root.findViewById(R.id.buttonCopied);
        buttonShare = root.findViewById(R.id.buttonShare);

        View.OnClickListener copy = v -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(textViewData.getText(), textViewData.getText());
            clipboard.setPrimaryClip(clip);
            buttonCopy.setVisibility(View.INVISIBLE);
            buttonCopied.setVisibility(View.VISIBLE);
        };
        buttonCopy.setOnClickListener(copy);
        buttonCopied.setOnClickListener(copy);

        buttonShare.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getContext().getString(R.string.share_instructions) + "\n\n" + textViewData.getText());
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_via)));
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        AsyncTask.execute(() -> {
            ThemeData td = new Theme(getContext()).getThemeData();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                Base64OutputStream b64os = new Base64OutputStream(baos, Base64.NO_WRAP);
                GZIPOutputStream gos = new GZIPOutputStream(b64os);
                Theme.writeThemeData(gos, td);
            } catch (IOException e) {
                IOException ne = new IOException("Failed to create base64 theme string. This should never happen!", e);
                ne.printStackTrace();
                Utils.somethingWrong(ne, root, getContext());
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                textViewData.setText(baos.toString());
            });
        });

    }
}

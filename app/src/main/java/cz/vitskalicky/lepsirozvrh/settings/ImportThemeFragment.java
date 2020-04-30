package cz.vitskalicky.lepsirozvrh.settings;

import android.app.Activity;
import android.content.ClipboardManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.theme.Theme;
import cz.vitskalicky.lepsirozvrh.theme.ThemeData;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImportThemeFragment extends Fragment {

    View root;
    EditText editTextData;
    Button buttonPaste;
    Button buttonOK;
    Button buttonClear;
    public ImportThemeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_import_theme, container, false);
        editTextData = root.findViewById(R.id.editTextData);
        buttonPaste = root.findViewById(R.id.buttonPaste);
        buttonOK = root.findViewById(R.id.buttonOK);
        buttonClear = root.findViewById(R.id.buttonClear);

        buttonPaste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
            if (clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemCount() > 0) {
                editTextData.setText(clipboard.getPrimaryClip().getItemAt(0).getText());
            }
        });
        buttonOK.setOnClickListener(v -> {
            AsyncTask.execute(() -> {
                ThemeData td = null;
                try {
                    td = ThemeData.parseZipped(editTextData.getText().toString().trim());
                } catch (IOException e) {
                    //try to parse as json (not zipped)
                    try {
                        td = ThemeData.parseJson(editTextData.getText().toString().trim());
                    } catch (IOException ex) {
                    }
                }
                final ThemeData ftd = td;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (ftd != null) {
                        Theme.of(getContext()).setThemeData(ftd);
                        Activity activity = getActivity();
                        if (activity instanceof Utils.RecreateWithAnimationActivity) {
                            ((Utils.RecreateWithAnimationActivity) activity).recreateWithAnimation();
                        }
                    } else {
                        Snackbar s = Snackbar.make(root, R.string.import_invalid, Snackbar.LENGTH_LONG);
                        s.show();
                    }
                });
            });
        });
        buttonClear.setOnClickListener(v -> editTextData.setText(""));
        return root;
    }
}

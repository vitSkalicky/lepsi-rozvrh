package cz.vitskalicky.lepsirozvrh;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Sponsoring {
    private Context context;

    public Sponsoring(Context context) {
        this.context = context;
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean inSponsor() {
        return true;
    }

    public void showDialog(AppCompatActivity activity) {
        Toast.makeText(context, "Error (no donate dialog in " + BuildConfig.FLAVOR+ " flavour)", Toast.LENGTH_SHORT).show();
    }
}

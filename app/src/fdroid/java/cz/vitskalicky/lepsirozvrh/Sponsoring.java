package cz.vitskalicky.lepsirozvrh;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class Sponsoring {
    private Context context;

    public Sponsoring(Context context) {
        this.context = context;
    }

    public boolean isEnabled(){
        return false;
    }

    public boolean inSponsor(){
        return true;
    }

    public void showDialog(AppCompatActivity activity){
        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
    }
}

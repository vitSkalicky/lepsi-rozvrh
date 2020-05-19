package cz.vitskalicky.lepsirozvrh.donations;

import android.content.Intent;

public interface PurchaseActivity {

    public void setActivityResultListener(Listener listener);

    public void onSponsorChange(boolean newValue);

    public static interface Listener {
        public void handleActivityResult(int requestCode, int resultCode, Intent data);
    }
}

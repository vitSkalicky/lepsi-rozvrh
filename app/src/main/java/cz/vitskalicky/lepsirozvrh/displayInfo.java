package cz.vitskalicky.lepsirozvrh;

import java.util.LinkedList;
import java.util.List;

/**
 * Information about displayed content for the user like loading status etc.
 */
public class displayInfo {
    private int loading = 0;

    public static final int LOADED = 0;
    public static final int LOADING = 1;
    public static final int ERROR = 2;

    public int getLoadingState(){
        return loading;
    }

    public void setLoadingState(int newLoadingState){
        final int oldState = newLoadingState;
        loading = newLoadingState;
        for (LoadingListener item :loadingListeners) {
            item.onChange(oldState, loading);
        }
    }




    private String message;

    public static interface LoadingListener{
        public void onChange(int oldState, int newState);
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String newMessage){
        final String oldMessage = message;
        message = newMessage;
        for (MessageListener item :messageListeners) {
            item.onChange(oldMessage, message);
        }
    }



    public static interface MessageListener{
        public void onChange(String oldMessage, String newMessage);
    }

    private List<LoadingListener> loadingListeners = new LinkedList<>();
    private List<MessageListener> messageListeners = new LinkedList<>();

    public void addOnLoadingStateChangeListener(LoadingListener listener){
        loadingListeners.add(listener);
    }

    public boolean removeOnLoadingStateChangeListener(LoadingListener listener){
        return loadingListeners.remove(listener);
    }

    public void addOnMessageChangeListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public boolean removeOnMessageChangeListener(MessageListener listener){
        return messageListeners.remove(listener);
    }
}

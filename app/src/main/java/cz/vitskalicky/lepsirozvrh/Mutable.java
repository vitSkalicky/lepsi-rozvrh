package cz.vitskalicky.lepsirozvrh;

/**
 * An utility class for mutable variables
 */
public class Mutable<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Mutable(T value) {
        this.value = value;
    }
}

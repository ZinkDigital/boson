package io.boson.valid;


import java.util.ArrayList;
import java.util.List;

public class Trace<T> extends Validation<T> {
    private final List<String> trace = new ArrayList<>(2);

    public Trace(String error) { addError(error); }

    public void addError(String error) { trace.add(error); }

    public boolean isError() { return true; }
    public boolean isResult() { return false; }

    public List<String> getTrace() { return trace; }
    public T getResult() {
        throw new IllegalStateException("Attempt to read a result from an Error");
    }
}

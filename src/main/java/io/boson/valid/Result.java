package io.boson.valid;

import java.math.BigDecimal;
import java.util.List;

public class Result<T> extends Validation<T> {
    private final T result;

    public Result(T result) { this.result = result; }

    public boolean isError() { return false; }
    public boolean isResult() { return true; }
    public T getResult() { return result; }

    public List<String> getTrace() {
        throw new IllegalStateException("Attempt to read a trace from a result");
    }
}

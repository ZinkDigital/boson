package io.boson.valid;

import java.math.BigDecimal;
import java.util.List;

abstract public class Validation<T> {

    abstract public boolean isError();

    abstract public boolean isResult();

    abstract public List<String> getTrace();

    abstract public T getResult();

}



package io.boson.json;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;


public interface Joson {

    /**
     * Make an Extractor that will call the extract function (Consumer) according to
     * the given expression.
     * @param expression
     * @param extractFunction
     * @param <T>
     * @return a BosonJava that is a BosonExtractor
     */
    static <T> Joson extractor(String expression, Consumer<T> extractFunction) {
        // TODO construct an extractor
        return null;
    }

    /**
     * Make an Injector that will call the inject function (of T -> T) according to
     * the given expression.
     * @param expression
     * @param injectFunction
     * @param <T>
     * @return
     */
    static <T> Joson injector(String expression, Function<T,T> injectFunction) {
        // TODO construct an injector
        return null;
    }

    /**
     * Apply this Joson to the String that arrives and at some point in the future complete
     * the future with the resulting String. In the case of an Extractor this will result in
     * the immutable String being returned unmodified.
     * @param the Json string.
     * @return
     */
    CompletableFuture<String> go(final String jsonStr);


    /**
     * Fuse one Joson to another. The joson that is 'this' should be executed first before the
     * joson that is the parameter in teh case of update/read conflicts.
     * the immutable String being returned unmodified.
     * @param the Joson to fuse to.
     * @return the fused Joson
     */
    Joson fuse(final Joson joson);

}

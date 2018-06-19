package io.zink.boson;


import io.zink.boson.bson.bsonImpl.CustomException;
import io.zink.boson.impl.BosonExtractor;
import io.zink.boson.impl.BosonInjector;
import io.zink.boson.impl.BosonValidate;
import net.jodah.typetools.TypeResolver;
import shapeless.HList;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;


public interface Boson {

    static <T> Boson validate(String expression, Consumer<T> validateFunction) {
        return new BosonValidate<>(expression, validateFunction);
    }

    /**
     * Make an Extractor that will call the extract function (Consumer) according to
     * the given expression.
     *
     * @param expression
     * @param extractFunction
     * @param <T>
     * @return a BosonImpl that is a BosonExtractor
     */
    static <T> Boson extractor(String expression, Consumer<T> extractFunction) {
        return new BosonExtractor<>(expression, extractFunction);
    }

    /**
     * Make an Injector that will call the inject function (of T -> T) according to
     * the given expression.
     *
     * @param expression
     * @param injectFunction
     * @param <T>
     * @return
     */
    static <T> Boson injector(String expression, Function<T, T> injectFunction) {
        return new BosonInjector<>(expression, injectFunction);
    }

    /**
     * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
     * the future with the resulting byte array. In the case of an Extractor this will result in
     * the immutable byte array being returned unmodified.
     *
     * @param bsonByteEncoding bson encoded into a byte array
     * @return CompletableFuture with original or a modified byte array.
     */
    CompletableFuture<byte[]> go(final byte[] bsonByteEncoding);

    /**
     * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
     * the future with the resulting byte array. In the case of an Extractor tis will result in
     * the immutable byte array being returned unmodified.
     *
     * @param bsonByteEncoding bson encoded into a String
     * @return CompletableFuture with original or a modified String.
     */
    CompletableFuture<String> go(final String bsonByteEncoding);

    /**
     * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
     * the future with the resulting byte array. In the case of an Extractor tis will result in
     * the immutable byte array being returned unmodified.
     *
     * @param bsonByteBufferEncoding byte array encoded wrapped in a ByteBuffer.
     * @return CompletableFuture with original or a modified ByteBuffer.
     */
    CompletableFuture<ByteBuffer> go(final ByteBuffer bsonByteBufferEncoding);


    /**
     * Fuse one BosonImpl to another. The boson that is this should be executed first before the
     * boson that is the parameter in teh case of update/read conflicts.
     * the immutable byte array being returned unmodified.
     *
     * @param boson to fuse to.
     * @return the fused BosonImpl
     */
    Boson fuse(final Boson boson);

}

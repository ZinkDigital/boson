package io.boson.json;


import io.boson.json.JsonExtractor;


//   ExtractorBuilder<T> Obj() ;
//   ExtractorBuilder<T> Obj(String name) ;
//
//   ExtractorBuilder<T> Arr() ;
//   ExtractorBuilder<T> Arr(int element) ;
//   ExtractorBuilder<T> Arr(int startElement, int endElement);
//   ExtractorBuilder<T> Arr(String name);
//   ExtractorBuilder<T> Arr(String name, int element );
//   ExtractorBuilder<T> Arr(String name, int startElement, int endElement);
//
//   ExtractorBuilder<T> Num(String name);
//   ExtractorBuilder<T> Str(String name);
//   ExtractorBuilder<T> Bool(String name);

/**
 * Extractor is the base for a structured assembly of Extractors for various types
 */
 public abstract class JsonChainedExtractor<O> implements JsonExtractor<O> {

    private final JsonExtractor<O> next;

    public JsonChainedExtractor(JsonExtractor<O> extractor) {
        this.next =  extractor;
    }

    public boolean hasNext() { return true; }

    public JsonExtractor<O> getNext() { return next; }

}

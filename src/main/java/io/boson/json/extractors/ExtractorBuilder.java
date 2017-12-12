package io.boson.json.extractors;

public interface ExtractorBuilder<T> {

    ExtractorBuilder<T> Obj() ;
    ExtractorBuilder<T> Obj(String name) ;

    ExtractorBuilder<T> Arr() ;
    ExtractorBuilder<T> Arr(int element) ;
    ExtractorBuilder<T> Arr(int startElement, int endElement);
    ExtractorBuilder<T> Arr(String name);
    ExtractorBuilder<T> Arr(String name, int element );
    ExtractorBuilder<T> Arr(String name, int startElement, int endElement);

    ExtractorBuilder<T> Num(String name);
    ExtractorBuilder<T> Str(String name);
    ExtractorBuilder<T> Bool(String name);

}

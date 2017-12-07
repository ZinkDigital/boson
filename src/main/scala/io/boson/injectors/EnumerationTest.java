package io.boson.injectors;

/**
 * Created by Ricardo Martins on 10/11/2017.
 */


public enum EnumerationTest {
   A("a"), B("b"), C("c");


   private String str;
   private EnumerationTest(String str){
      this.str=str;
   }
}
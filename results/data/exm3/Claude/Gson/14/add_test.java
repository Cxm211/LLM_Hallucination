// com/google/gson/internal/bind/RecursiveTypesResolveTest.java
public void testTripleSubtype() {
    assertEquals($Gson$Types.subtypeOf(Number.class),
            $Gson$Types.subtypeOf($Gson$Types.subtypeOf($Gson$Types.subtypeOf(Number.class))));
  }
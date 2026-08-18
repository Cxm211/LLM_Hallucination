// com/google/gson/internal/bind/RecursiveTypesResolveTest.java
public void testTripleNested() {
    assertEquals($Gson$Types.subtypeOf(Number.class),
            $Gson$Types.subtypeOf($Gson$Types.subtypeOf($Gson$Types.subtypeOf(Number.class))));
    assertEquals($Gson$Types.supertypeOf(Number.class),
            $Gson$Types.supertypeOf($Gson$Types.supertypeOf($Gson$Types.supertypeOf(Number.class))));
  }

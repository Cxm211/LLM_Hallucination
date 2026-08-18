// com/google/gson/internal/bind/RecursiveTypesResolveTest.java
public void testTripleSupertype() {
    assertEquals($Gson$Types.supertypeOf(Number.class),
            $Gson$Types.supertypeOf($Gson$Types.supertypeOf($Gson$Types.supertypeOf(Number.class))));
  }
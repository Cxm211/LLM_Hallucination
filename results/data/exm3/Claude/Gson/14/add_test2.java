// com/google/gson/internal/bind/RecursiveTypesResolveTest.java
public void testSuperSubSupertype() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.supertypeOf($Gson$Types.subtypeOf($Gson$Types.supertypeOf(Number.class))));
  }
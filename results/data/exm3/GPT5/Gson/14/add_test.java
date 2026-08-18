// com/google/gson/internal/bind/RecursiveTypesResolveTest.java::testSuperSubtypeObject
public void testSuperSubtypeObject() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.supertypeOf($Gson$Types.subtypeOf(Object.class)));
  }
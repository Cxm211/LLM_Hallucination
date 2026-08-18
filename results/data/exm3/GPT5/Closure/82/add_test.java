// com/google/javascript/rhino/jstype/FunctionTypeTest.java::testNoResolvedTypeNotEmpty
public void testNoResolvedTypeNotEmpty() {
    com.google.javascript.rhino.jstype.JSTypeRegistry registry = new com.google.javascript.rhino.jstype.JSTypeRegistry(null);
    com.google.javascript.rhino.jstype.NamedType nt = new com.google.javascript.rhino.jstype.NamedType(registry, "Foo", "source", -1, -1);
    assertFalse(nt.isEmptyType());
  }
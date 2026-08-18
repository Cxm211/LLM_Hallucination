// com/google/gson/internal/bind/RecursiveTypesResolveTest.java
public void testRecursiveTypeVariableWithParameterizedBound() throws Exception {
  class TestRecursive<T extends java.util.List<T>> {}
  com.google.gson.TypeAdapter<TestRecursive> adapter = new com.google.gson.Gson().getAdapter(TestRecursive.class);
  org.junit.Assert.assertNotNull(adapter);
}

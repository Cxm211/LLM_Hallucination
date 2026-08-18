// buggy function
  public static <T1> TypeAdapterFactory newTypeHierarchyFactory(
      final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      public <T2> TypeAdapter<T2> create(Gson gson, TypeToken<T2> typeToken) {
        final Class<? super T2> requestedType = typeToken.getRawType();
        if (!clazz.isAssignableFrom(requestedType)) {
          return null;
        }
        return (TypeAdapter<T2>) typeAdapter;

      }
      @Override public String toString() {
        return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }

// trigger testcase
// com/google/gson/functional/DefaultTypeAdaptersTest.java::testJsonElementTypeMismatch
public void testJsonElementTypeMismatch() {
    try {
      gson.fromJson("\"abc\"", JsonObject.class);
      fail();
    } catch (JsonSyntaxException expected) {
      assertEquals("Expected a com.google.gson.JsonObject but was com.google.gson.JsonPrimitive",
          expected.getMessage());
    }
  }

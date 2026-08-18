// com/google/gson/functional/TypeVariableTest.java::testGenericHolderWithParameterizedField
public void testGenericHolderWithParameterizedField() throws Exception {
    Gson gson = new Gson();

    class GenericHolder<T> {
      T value;
      GenericHolder(T v) { this.value = v; }
      @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericHolder<?> that = (GenericHolder<?>) o;
        return (value == null ? that.value == null : value.equals(that.value));
      }
      @Override public int hashCode() { return value == null ? 0 : value.hashCode(); }
    }

    java.util.ArrayList<Integer> list = new java.util.ArrayList<Integer>();
    list.add(1);
    list.add(2);
    GenericHolder<java.util.List<Integer>> h1 = new GenericHolder<java.util.List<Integer>>(list);

    java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<GenericHolder<java.util.List<Integer>>>(){}.getType();
    String json = gson.toJson(h1, type);
    GenericHolder<?> h2 = gson.fromJson(json, type);

    assertEquals(h1, h2);
  }
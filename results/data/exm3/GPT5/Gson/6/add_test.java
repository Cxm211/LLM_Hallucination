// com/google/gson/regression/JsonAdapterNullSafeTest.java::testRespectsNullSafeFlagTypeAdapter
public void testRespectsNullSafeFlagTypeAdapter() {
    class BadNullUnsafeAdapter extends TypeAdapter<String> {
      @Override public void write(JsonWriter out, String value) throws IOException {
        if (value == null) {
          throw new IllegalStateException("Adapter should receive null when nullSafe=false");
        }
        out.value(value);
      }
      @Override public String read(JsonReader in) throws IOException { return in.nextString(); }
    }
    class Holder {
      @JsonAdapter(value = BadNullUnsafeAdapter.class, nullSafe = false)
      String v;
    }
    Gson gsonLocal = new GsonBuilder().serializeNulls().create();
    Holder h = new Holder();
    h.v = null;
    try {
      gsonLocal.toJson(h);
      fail("Expected exception due to nullSafe=false TypeAdapter receiving null");
    } catch (IllegalStateException expected) {
      // expected
    }
  }
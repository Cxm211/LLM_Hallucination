// ===== FIXED com.google.gson.internal.bind.JsonTreeWriter :: value(boolean) [lines 157-160] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Gson/Gson-9-fixed/gson/src/main/java/com/google/gson/internal/bind/JsonTreeWriter.java =====
  @Override public JsonWriter value(boolean value) throws IOException {
    put(new JsonPrimitive(value));
    return this;
  }

// ===== FIXED com.google.gson.internal.bind.TypeAdapters :: write(JsonWriter, Boolean) [lines 164-166] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Gson/Gson-9-fixed/gson/src/main/java/com/google/gson/internal/bind/TypeAdapters.java =====
    public void write(JsonWriter out, Boolean value) throws IOException {
      out.value(value);
    }

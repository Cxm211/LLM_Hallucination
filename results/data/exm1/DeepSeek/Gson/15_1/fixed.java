// ===== FIXED com.google.gson.stream.JsonWriter :: value(double) [lines 493-501] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Gson/Gson-15-fixed/gson/src/main/java/com/google/gson/stream/JsonWriter.java =====
  public JsonWriter value(double value) throws IOException {
    writeDeferredName();
    if (!lenient && (Double.isNaN(value) || Double.isInfinite(value))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    out.append(Double.toString(value));
    return this;
  }

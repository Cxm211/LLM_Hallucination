// ===== FIXED com.google.gson.internal.bind.JsonTreeReader :: skipValue() [lines 256-269] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Gson/Gson-12-fixed/gson/src/main/java/com/google/gson/internal/bind/JsonTreeReader.java =====
  @Override public void skipValue() throws IOException {
    if (peek() == JsonToken.NAME) {
      nextName();
      pathNames[stackSize - 2] = "null";
    } else {
      popStack();
      if (stackSize > 0) {
        pathNames[stackSize - 1] = "null";
      }
    }
    if (stackSize > 0) {
      pathIndices[stackSize - 1]++;
    }
  }

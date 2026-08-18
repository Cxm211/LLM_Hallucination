// ===== FIXED com.google.gson.DefaultDateTypeAdapter :: read(JsonReader) [lines 98-114] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Gson/Gson-17-fixed/gson/src/main/java/com/google/gson/DefaultDateTypeAdapter.java =====
  public Date read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    Date date = deserializeToDate(in.nextString());
    if (dateType == Date.class) {
      return date;
    } else if (dateType == Timestamp.class) {
      return new Timestamp(date.getTime());
    } else if (dateType == java.sql.Date.class) {
      return new java.sql.Date(date.getTime());
    } else {
      // This must never happen: dateType is guarded in the primary constructor
      throw new AssertionError();
    }
  }

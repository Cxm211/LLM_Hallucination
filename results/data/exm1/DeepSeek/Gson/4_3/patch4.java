public JsonWriter nullValue() throws IOException {
    if (deferredName != null) {
      writeDeferredName();
    }
    beforeValue(false);
    out.write("null");
    return this;
  }
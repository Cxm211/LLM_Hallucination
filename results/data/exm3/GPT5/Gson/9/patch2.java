public JsonWriter value(Boolean value) throws IOException {
    if (value == null) {
      writeDeferredName();
      beforeValue();
      out.write("null");
      return this;
    }
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }
  public JsonWriter value(Boolean value) throws IOException {
    writeDeferredName();
    beforeValue();
    if (value == null) {
      out.write("null");
    } else {
      out.write(value ? "true" : "false");
    }
    return this;
  }
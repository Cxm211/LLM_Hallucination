  public JsonWriter value(Boolean value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    return value(value.booleanValue());
  }
// buggy code
  private void put(JsonElement value) {
    if (pendingName != null) {
      if (!value.isJsonNull() || getSerializeNulls()) {
        JsonObject object = (JsonObject) peek();
        object.add(pendingName, value);
      }
      pendingName = null;
    } else if (stack.isEmpty()) {
      product = value;
    } else {
      JsonElement element = peek();
      if (element instanceof JsonArray) {
        ((JsonArray) element).add(value);
      } else {
        throw new IllegalStateException();
      }
    }
  }

    public void write(JsonWriter out, Boolean value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.value(value);
    }

  public JsonWriter value(boolean value) throws IOException {
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }


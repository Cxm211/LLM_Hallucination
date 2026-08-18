  private void put(JsonElement value) {
    if (pendingName != null) {
      if (!value.isJsonNull() || getSerializeNulls()) {
        JsonElement container = peek();
        if (!(container instanceof JsonObject)) {
          throw new IllegalStateException();
        }
        JsonObject object = (JsonObject) container;
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
private void put(JsonElement value) {
  if (pendingName != null) {
    if (!value.isJsonNull() || getSerializeNulls()) {
      if (stack.isEmpty()) {
        throw new IllegalStateException("No object to add to");
      }
      JsonElement element = peek();
      if (!(element instanceof JsonObject)) {
        throw new IllegalStateException("Expected JsonObject, got " + element.getClass().getName());
      }
      JsonObject object = (JsonObject) element;
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
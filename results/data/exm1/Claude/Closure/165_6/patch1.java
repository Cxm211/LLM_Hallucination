public final boolean defineDeclaredProperty(String propertyName,
      JSType type, Node propertyNode) {
    registry.registerPropertyOnType(propertyName, this);
    boolean result = defineProperty(propertyName, type, false,
        propertyNode);
    return result;
  }
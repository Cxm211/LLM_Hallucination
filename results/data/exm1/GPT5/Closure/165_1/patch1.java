public final boolean defineDeclaredProperty(String propertyName,
      JSType type, Node propertyNode) {
    boolean result = defineProperty(propertyName, type, false,
        propertyNode);

    // All property definitions go through this method
    // or defineDeclaredProperty. Because the properties defined an an
    // object can affect subtyping, it's slightly more efficient
    // to register this after defining the property.
    registry.registerPropertyOnType(propertyName, this);

    return result;
  }
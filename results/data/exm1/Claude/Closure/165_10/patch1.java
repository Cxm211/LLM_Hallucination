public final boolean defineDeclaredProperty(String propertyName,
    JSType type, Node propertyNode) {
  boolean result = defineProperty(propertyName, type, false,
      propertyNode);

  
  
  
  
  registry.registerPropertyOnType(propertyName, this);

  return result;
}
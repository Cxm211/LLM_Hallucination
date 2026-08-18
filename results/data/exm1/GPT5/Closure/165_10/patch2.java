RecordType(JSTypeRegistry registry, Map<String, RecordProperty> properties) {
    super(registry, null, null);
    setPrettyPrint(true);

    // Ensure the internal properties map is initialized before use.
    this.properties = new java.util.LinkedHashMap<String, JSType>();

    for (String property : properties.keySet()) {
      RecordProperty prop = properties.get(property);
      if (prop == null) {
        throw new IllegalStateException(
            "RecordProperty associated with a property should not be null!");
      }
      defineDeclaredProperty(
          property, prop.getType(), prop.getPropertyNode());
      // Keep the local cache in sync for operations that iterate over it.
      this.properties.put(property, prop.getType());
    }

    // Freeze the record type.
    isFrozen = true;
  }
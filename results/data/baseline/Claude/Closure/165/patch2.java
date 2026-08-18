RecordType(JSTypeRegistry registry, Map<String, RecordProperty> properties) {
    super(registry, null, null);
    setPrettyPrint(true);

    for (String property : properties.keySet()) {
      RecordProperty prop = properties.get(property);
      if (prop == null) {
        throw new IllegalStateException(
            "RecordProperty associated with a property should not be null!");
      }
        defineProperty(
            property, prop.getType(), false, prop.getPropertyNode());
    }

    // Freeze the record type.
    isFrozen = true;

    // Register properties after the record type is fully constructed
    for (String property : properties.keySet()) {
      registry.registerPropertyOnType(property, this);
    }
  }
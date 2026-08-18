RecordType(JSTypeRegistry registry, Map<String, RecordProperty> properties) {
    super(registry, null, null);
    setPrettyPrint(true);

    // Store the properties and property nodes for later use.
    this.properties = new HashMap<>();
    this.propertyNodes = new HashMap<>();

    for (String property : properties.keySet()) {
      RecordProperty prop = properties.get(property);
      if (prop == null) {
        throw new IllegalStateException(
            "RecordProperty associated with a property should not be null!");
      }
      this.properties.put(property, prop.getType());
      this.propertyNodes.put(property, prop.getPropertyNode());
      defineDeclaredProperty(
          property, prop.getType(), prop.getPropertyNode());
    }

    // Freeze the record type.
    isFrozen = true;
  }
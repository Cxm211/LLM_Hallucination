  public JSType build() {
    if (isEmpty) {
       return registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    }

    return new RecordType(
        registry, Collections.unmodifiableMap(properties));
  }
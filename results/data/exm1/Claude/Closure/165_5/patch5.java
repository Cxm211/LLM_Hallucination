public JSType build() {
  if (isEmpty) {
     return registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  RecordType recType = new RecordType(
      registry, Collections.unmodifiableMap(properties));
  return recType;
}
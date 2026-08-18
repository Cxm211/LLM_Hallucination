public JSType getLeastSupertype(JSType that) {
  if (!that.isRecordType()) {
    return super.getLeastSupertype(that);
  }
  RecordTypeBuilder builder = new RecordTypeBuilder(registry);
  for (String property : properties.keySet()) {
    if (that.toMaybeRecordType().hasProperty(property) &&
        that.toMaybeRecordType().getPropertyType(property).isEquivalentTo(
            getPropertyType(property))) {
      builder.addProperty(property, getPropertyType(property),
          getPropertyNode(property));
    }
  }
  if (builder.isEmpty()) {
    return registry.createUnionType(this, that);
  }
  for (String property : that.toMaybeRecordType().properties.keySet()) {
    if (!properties.containsKey(property)) {
      return registry.createUnionType(this, that);
    }
  }
  return builder.build();
}
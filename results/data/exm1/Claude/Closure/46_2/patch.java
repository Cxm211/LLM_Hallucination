public JSType getLeastSupertype(JSType that) {
  if (!that.isRecordType()) {
    return super.getLeastSupertype(that);
  }
  RecordTypeBuilder builder = new RecordTypeBuilder(registry);
  RecordType thatRecord = that.toMaybeRecordType();
  
  for (String property : properties.keySet()) {
    if (thatRecord.hasProperty(property) &&
        thatRecord.getPropertyType(property).isEquivalentTo(
            getPropertyType(property))) {
      builder.addProperty(property, getPropertyType(property),
          getPropertyNode(property));
    }
  }
  
  for (String property : thatRecord.getOwnPropertyNames()) {
    if (!properties.containsKey(property) &&
        hasProperty(property) &&
        getPropertyType(property).isEquivalentTo(
            thatRecord.getPropertyType(property))) {
      builder.addProperty(property, getPropertyType(property),
          getPropertyNode(property));
    }
  }
  
  return builder.build();
}
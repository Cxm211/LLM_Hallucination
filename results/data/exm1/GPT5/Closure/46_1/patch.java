public JSType getLeastSupertype(JSType that) {
  if (!that.isRecordType()) {
    return super.getLeastSupertype(that);
  }
  RecordType other = that.toMaybeRecordType();
  RecordTypeBuilder builder = new RecordTypeBuilder(registry);
  for (String property : properties.keySet()) {
    if (other.hasProperty(property)) {
      JSType thisProp = getPropertyType(property);
      JSType thatProp = other.getPropertyType(property);
      JSType lub = thisProp.getLeastSupertype(thatProp);
      builder.addProperty(property, lub, getPropertyNode(property));
    }
  }
  return builder.build();
}
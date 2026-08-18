public JSType getLeastSupertype(JSType that) {
  if (!that.isRecordType()) {
    return super.getLeastSupertype(that);
  }
  RecordTypeBuilder builder = new RecordTypeBuilder(registry);
  
  for (String property : properties.keySet()) {
    if (that.toMaybeRecordType().hasProperty(property)) {
      builder.addProperty(property, 
          getPropertyType(property).getLeastSupertype(
              that.toMaybeRecordType().getPropertyType(property)),
          getPropertyNode(property));
    } else {
      builder.addProperty(property, getPropertyType(property),
          getPropertyNode(property));
    }
  }
  
  for (String property : that.toMaybeRecordType().properties.keySet()) {
    if (!properties.containsKey(property)) {
      builder.addProperty(property, 
          that.toMaybeRecordType().getPropertyType(property),
          that.toMaybeRecordType().getPropertyNode(property));
    }
  }
  
  return builder.build();
}
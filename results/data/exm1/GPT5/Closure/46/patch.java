public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    RecordType thatRecord = that.toMaybeRecordType();
    for (String property : properties.keySet()) {
      if (thatRecord.hasProperty(property)) {
        JSType thisPropType = getPropertyType(property);
        JSType thatPropType = thatRecord.getPropertyType(property);
        JSType propLeastSuper = thisPropType.getLeastSupertype(thatPropType);
        builder.addProperty(property, propLeastSuper, getPropertyNode(property));
      }
    }
    return builder.build();
  }
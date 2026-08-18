  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (that.toMaybeRecordType().hasProperty(property)) {
        JSType thisPropType = getPropertyType(property);
        JSType thatPropType = that.toMaybeRecordType().getPropertyType(property);
        JSType newPropType = thisPropType.getLeastSupertype(thatPropType);
        builder.addProperty(property, newPropType, getPropertyNode(property));
      }
    }
    return builder.build();
  }
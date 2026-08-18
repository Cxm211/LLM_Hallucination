  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (that.toMaybeRecordType().hasProperty(property)) {
        JSType thisPropType = getPropertyType(property);
        JSType thatPropType = that.toMaybeRecordType().getPropertyType(property);
        builder.addProperty(property, thisPropType.getLeastSupertype(thatPropType),
            getPropertyNode(property));
      }
    }
    return builder.build();
  }
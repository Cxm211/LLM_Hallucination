public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (that.toMaybeRecordType().hasProperty(property)) {
        JSType thisPropertyType = getPropertyType(property);
        JSType thatPropertyType = that.toMaybeRecordType().getPropertyType(property);
        JSType leastSupertype = thisPropertyType.getLeastSupertype(thatPropertyType);
        builder.addProperty(property, leastSupertype,
            getPropertyNode(property));
      }
    }
    return builder.build();
  }
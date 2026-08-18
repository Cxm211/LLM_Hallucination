public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (that.toMaybeRecordType().hasProperty(property)) {
        JSType thisType = getPropertyType(property);
        JSType thatType = that.toMaybeRecordType().getPropertyType(property);
        builder.addProperty(property,
            thisType.getLeastSupertype(thatType),
            getPropertyNode(property));
      }
    }
    return builder.build();
  }
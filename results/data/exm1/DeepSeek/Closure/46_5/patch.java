public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordType thatRecord = (RecordType) that;
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (thatRecord.properties.containsKey(property)) {
        JSType thisPropType = getPropertyType(property);
        JSType thatPropType = thatRecord.getPropertyType(property);
        JSType leastSupertype = thisPropType.getLeastSupertype(thatPropType);
        builder.addProperty(property, leastSupertype, getPropertyNode(property));
      }
    }
    return builder.build();
  }
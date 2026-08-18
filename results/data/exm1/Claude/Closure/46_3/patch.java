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
    for (String property : thatRecord.getProperties().keySet()) {
      if (!properties.containsKey(property) && this.hasProperty(property) &&
          this.getPropertyType(property).isEquivalentTo(
              thatRecord.getPropertyType(property))) {
        builder.addProperty(property, thatRecord.getPropertyType(property),
            thatRecord.getPropertyNode(property));
      }
    }
    return builder.build();
  }
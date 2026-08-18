public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    RecordType thatRecord = that.toMaybeRecordType();
    if (thatRecord != null) {
      for (String property : properties.keySet()) {
        if (thatRecord.hasProperty(property) &&
            thatRecord.getPropertyType(property).isEquivalentTo(
                getPropertyType(property))) {
          builder.addProperty(property, getPropertyType(property),
              getPropertyNode(property));
        }
      }
    }
    return builder.build();
  }
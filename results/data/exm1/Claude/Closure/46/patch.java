public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (that.toMaybeRecordType().hasProperty(property) &&
          that.toMaybeRecordType().getPropertyType(property).isEquivalentTo(
              getPropertyType(property))) {
        builder.addProperty(property, getPropertyType(property),
            getPropertyNode(property));
      }
    }
    for (String property : that.toMaybeRecordType().getOwnPropertyNames()) {
      if (!hasProperty(property) &&
          that.toMaybeRecordType().hasProperty(property)) {
        builder.addProperty(property,
            that.toMaybeRecordType().getPropertyType(property),
            that.toMaybeRecordType().getPropertyNode(property));
      }
    }
    return builder.build();
  }
  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordType other = that.toMaybeRecordType();
    if (other == null) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (other.hasProperty(property) &&
          other.getPropertyType(property).isEquivalentTo(
              getPropertyType(property))) {
        builder.addProperty(property, getPropertyType(property),
            getPropertyNode(property));
      }
    }
    return builder.build();
  }
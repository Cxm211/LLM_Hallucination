  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (that.toMaybeRecordType().hasProperty(property)) {
        builder.addProperty(property,
            this.getPropertyType(property).getLeastSupertype(
                that.toMaybeRecordType().getPropertyType(property)),
            getPropertyNode(property));
      }
    }
    return builder.build();
  }
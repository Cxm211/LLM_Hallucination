  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordType other = that.toMaybeRecordType();
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (other.hasProperty(property)) {
        JSType thisPropType = getPropertyType(property);
        JSType otherPropType = other.getPropertyType(property);
        JSType lub = thisPropType.getLeastSupertype(otherPropType);
        builder.addProperty(property, lub, getPropertyNode(property));
      }
    }
    return builder.build();
  }
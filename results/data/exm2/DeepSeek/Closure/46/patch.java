  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    // Both are record types.
    return registry.createUnionType(this, that);
  }
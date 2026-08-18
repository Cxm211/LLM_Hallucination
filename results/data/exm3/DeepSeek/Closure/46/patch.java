  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    return registry.createUnionType(this, that);
  }
  public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    if (this.isSubtype(that)) {
      return that;
    }
    if (that.isSubtype(this)) {
      return this;
    }
    return registry.createUnionType(this, that);
  }
public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    // If one record is a subtype of the other, the least supertype is the supertype.
    if (this.isSubtype(that)) {
      return that;
    }
    if (that.isSubtype(this)) {
      return this;
    }
    // Otherwise, the least supertype is the union of the two record types.
    return registry.createUnionType(this, that);
  }
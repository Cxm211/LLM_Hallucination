public JSType getLeastSupertype(JSType that) {
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    // For record types, the least supertype is a union type unless
    // one record is a subtype of the other
    if (isSubtype(that)) {
      return that;
    }
    if (that.isSubtype(this)) {
      return this;
    }
    return registry.createUnionType(this, that);
}
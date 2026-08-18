// buggy code
  public TypePair getTypesUnderInequality(JSType that) {
    // unions types
    if (that instanceof UnionType) {
      TypePair p = that.getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }

    // other types
    switch (this.testForEquality(that)) {
      case TRUE:
        // Inequality cannot hold if the types are always equal.
        return new TypePair(
            getNativeType(JSTypeNative.NO_TYPE),
            getNativeType(JSTypeNative.NO_TYPE));

      case FALSE:
        // Always not equal; no refinement.
        return new TypePair(this, that);

      case UNKNOWN:
        // Handle special JS abstract equality semantics: null == undefined.
        if (that.isNullType() || that.isVoidType()) {
          return new TypePair(this.restrictByNotNullOrUndefined(), that);
        } else if (this.isNullType() || this.isVoidType()) {
          return new TypePair(this, that.restrictByNotNullOrUndefined());
        }
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }
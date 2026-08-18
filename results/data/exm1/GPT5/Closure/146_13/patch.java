public TypePair getTypesUnderInequality(JSType that) {
    // unions types
    if (that instanceof UnionType) {
      TypePair p = that.getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }

    // Special-case null/undefined: x != null implies x is not null/undefined
    if (this.isNullType() || this.isVoidType()) {
      return new TypePair(null, that.restrictByNotNullOrUndefined());
    }
    if (that.isNullType() || that.isVoidType()) {
      return new TypePair(this.restrictByNotNullOrUndefined(), null);
    }

    // other types
    switch (this.testForEquality(that)) {
      case TRUE:
        return new TypePair(null, null);

      case FALSE:
      case UNKNOWN:
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }
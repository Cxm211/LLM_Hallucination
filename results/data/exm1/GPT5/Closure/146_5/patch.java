public TypePair getTypesUnderInequality(JSType that) {
    // unions types
    if (that instanceof UnionType) {
      TypePair p = that.getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }

    // Special-case comparisons against null/undefined to enable narrowing like (x != null)
    if (that.isNullType() || that.isVoidType()) {
      return new TypePair(this.restrictByNotNullOrUndefined(), that);
    }
    if (this.isNullType() || this.isVoidType()) {
      return new TypePair(this, that.restrictByNotNullOrUndefined());
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
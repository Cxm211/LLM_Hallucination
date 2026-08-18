  public TypePair getTypesUnderInequality(JSType that) {
    // unions types
    if (that instanceof UnionType) {
      TypePair p = that.getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }

    // other types
    switch (this.testForEquality(that)) {
      case TRUE:
        return new TypePair(null, null);

      case FALSE:
        return new TypePair(this, that);

      case UNKNOWN:
        // In cases like (T != null), we can refine T to be non-null.
        if (that != null && that.isNullType()) {
          return new TypePair(this.getNonNullType(), that);
        }
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }
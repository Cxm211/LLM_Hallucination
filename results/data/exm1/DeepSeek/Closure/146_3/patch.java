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
      case UNKNOWN:
        // If one is a subtype of the other, treat them as equal.
        if (this.isSubtype(that) || that.isSubtype(this)) {
          return new TypePair(null, null);
        }
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }
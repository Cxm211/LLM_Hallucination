public TypePair getTypesUnderInequality(JSType that) {
    // If this is a union, let it handle the inequality logic directly.
    if (this instanceof UnionType) {
      return ((UnionType) this).getTypesUnderInequality(that);
    }

    // If the other type is a union, delegate to it and swap the result.
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
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }
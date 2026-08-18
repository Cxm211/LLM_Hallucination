public TypePair getTypesUnderInequality(JSType that) {
    // unions types
    if (that instanceof UnionType) {
      UnionType union = (UnionType) that;
      JSType thisRestricted = this;
      JSType thatRestricted = that;
      for (JSType alternate : union.alternates) {
        TernaryValue result = this.testForEquality(alternate);
        if (result == TernaryValue.FALSE) {
          thisRestricted = this;
          thatRestricted = that;
          break;
        } else if (result == TernaryValue.TRUE) {
          thatRestricted = thatRestricted.restrictByNotNullOrUndefined();
          if (thatRestricted.isNoType()) {
            return new TypePair(null, null);
          }
        }
      }
      return new TypePair(thisRestricted, thatRestricted);
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
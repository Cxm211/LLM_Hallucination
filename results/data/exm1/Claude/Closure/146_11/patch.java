public TypePair getTypesUnderInequality(JSType that) {
    if (that instanceof UnionType) {
      TypePair p = that.getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }

    TernaryValue result = this.testForEquality(that);
    if (result == TernaryValue.TRUE) {
      return new TypePair(null, null);
    } else if (result == TernaryValue.FALSE || result == TernaryValue.UNKNOWN) {
      return new TypePair(this, that);
    }

    throw new IllegalStateException();
  }
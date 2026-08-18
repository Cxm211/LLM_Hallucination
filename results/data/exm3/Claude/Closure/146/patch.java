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
      JSType noType = getNativeType(JSTypeNative.NO_TYPE);
      return new TypePair(noType, noType);

    case UNKNOWN:
      return new TypePair(this, that);
  }

  // switch case is exhaustive
  throw new IllegalStateException();
}
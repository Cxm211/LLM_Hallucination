public TypePair getTypesUnderInequality(JSType that) {
  // unions types
  if (that instanceof UnionType) {
    TypePair p = that.getTypesUnderInequality(this);
    return new TypePair(p.typeB, p.typeA);
  }

  // other types
  switch (this.testForEquality(that)) {
    case FALSE:
    case UNKNOWN:
      return new TypePair(this, that);
      
    case TRUE:
      return new TypePair(null, null);
  }

  // switch case is exhaustive
  throw new IllegalStateException();
}
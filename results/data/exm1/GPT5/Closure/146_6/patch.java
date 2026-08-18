public TypePair getTypesUnderInequality(JSType that) {
  // Handle when this is a union type
  if (this instanceof UnionType) {
    return ((UnionType) this).getTypesUnderInequality(that);
  }

  // Handle when that is a union type
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
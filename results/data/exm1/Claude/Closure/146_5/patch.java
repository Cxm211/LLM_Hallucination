public TypePair getTypesUnderInequality(JSType that) {
  if (that instanceof UnionType) {
    TypePair p = that.getTypesUnderInequality(this);
    return new TypePair(p.typeA, p.typeB);
  }

  switch (this.testForEquality(that)) {
    case TRUE:
      return new TypePair(null, null);

    case FALSE:
    case UNKNOWN:
      return new TypePair(this, that);
  }

  throw new IllegalStateException();
}
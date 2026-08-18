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
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }

// trigger testcase
public void testEqCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", VOID_TYPE),
        createVar(blind, "b", VOID_TYPE),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NO_TYPE),
            new TypedName("b", NO_TYPE)));
  }

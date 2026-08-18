JSType meet(JSType that) {
  UnionTypeBuilder builder = new UnionTypeBuilder(registry);
  for (JSType alternate : alternates) {
    if (alternate.isSubtype(that)) {
      builder.addAlternate(alternate);
    }
  }

  if (that instanceof UnionType) {
    for (JSType otherAlternate : ((UnionType) that).alternates) {
      if (otherAlternate.isSubtype(this)) {
        builder.addAlternate(otherAlternate);
      }
    }
  } else if (that.isSubtype(this)) {
    builder.addAlternate(that);
  }
  JSType result = builder.build();
  if (result != null) {
    return result;
  } else {
    // If no common subtype found, but both sides are object types,
    // then the meet should be the NO_OBJECT_TYPE.
    boolean thisIsObject = true;
    for (JSType a : alternates) {
      if (!a.isObject()) {
        thisIsObject = false;
        break;
      }
    }
    boolean thatIsObject;
    if (that instanceof UnionType) {
      thatIsObject = true;
      for (JSType oa : ((UnionType) that).alternates) {
        if (!oa.isObject()) {
          thatIsObject = false;
          break;
        }
      }
    } else {
      thatIsObject = that.isObject();
    }
    if (thisIsObject && thatIsObject) {
      return getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }
}
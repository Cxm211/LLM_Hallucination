JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    for (JSType alternate : alternates) {
      if (that instanceof UnionType) {
        for (JSType otherAlternate : ((UnionType) that).alternates) {
          JSType meetType = alternate.getGreatestSubtype(otherAlternate);
          if (!meetType.isEmptyType()) {
            builder.addAlternate(meetType);
          }
        }
      } else {
        JSType meetType = alternate.getGreatestSubtype(that);
        if (!meetType.isEmptyType()) {
          builder.addAlternate(meetType);
        }
      }
    }
    JSType result = builder.build();
    if (result != null) {
      return result;
    } else if (this.isObject() && that.isObject()) {
      return getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }
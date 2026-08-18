  JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    for (JSType alternate : alternates) {
      JSType meet = alternate.meet(that);
      if (meet != null) {
        builder.addAlternate(meet);
      }
    }

    if (that instanceof UnionType) {
      for (JSType otherAlternate : ((UnionType) that).alternates) {
        JSType meet = otherAlternate.meet(this);
        if (meet != null) {
          builder.addAlternate(meet);
        }
      }
    } else {
      JSType meet = that.meet(this);
      if (meet != null) {
        builder.addAlternate(meet);
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
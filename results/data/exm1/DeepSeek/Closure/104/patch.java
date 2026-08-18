  JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    for (JSType alternate : alternates) {
      JSType meetResult = alternate.meet(that);
      if (meetResult != null) {
        builder.addAlternate(meetResult);
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
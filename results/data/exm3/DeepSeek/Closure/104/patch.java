  JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    if (that instanceof UnionType) {
      // pairwise meets
      for (JSType a : alternates) {
        for (JSType b : ((UnionType) that).alternates) {
          builder.addAlternate(a.meet(b));
        }
      }
    } else {
      for (JSType alternate : alternates) {
        builder.addAlternate(alternate.meet(that));
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
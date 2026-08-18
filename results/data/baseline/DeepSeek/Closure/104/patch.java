  JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    if (that instanceof UnionType) {
      for (JSType thisAlt : alternates) {
        for (JSType thatAlt : ((UnionType) that).alternates) {
          JSType meet = thisAlt.meet(thatAlt);
          if (!meet.isNoType() && !meet.isNoObjectType()) {
            builder.addAlternate(meet);
          }
        }
      }
    } else {
      for (JSType thisAlt : alternates) {
        JSType meet = thisAlt.meet(that);
        if (!meet.isNoType() && !meet.isNoObjectType()) {
          builder.addAlternate(meet);
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
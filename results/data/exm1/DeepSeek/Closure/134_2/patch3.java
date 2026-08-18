  private JSType getJSType(Node n) {
    JSType jsType = n.getJSType();
    if (jsType == null) {
      return compiler.getTypeRegistry().getNativeType(
          JSTypeNative.UNKNOWN_TYPE);
    } else {
      return jsType;
    }
  }
String getReadableJSTypeName(Node n, boolean dereference) {
    if (n.isGetProp()) {
      JSType childType = getJSType(n.getFirstChild());
      if (childType != null) {
        ObjectType objectType = childType.dereference();
        if (objectType != null) {
          String propName = n.getLastChild().getString();
          if (objectType.getConstructor() != null &&
              objectType.getConstructor().isInterface()) {
            objectType = FunctionType.getTopDefiningInterface(
                objectType, propName);
          } else {
            while (objectType != null && !objectType.hasOwnProperty(propName)) {
              objectType = objectType.getImplicitPrototype();
            }
          }
          if (objectType != null &&
              (objectType.getConstructor() != null ||
               objectType.isInterface() ||
               objectType.isFunctionPrototypeType())) {
            return objectType.toString() + "." + propName;
          }
        }
      }
    }
    JSType type = getJSType(n);
    if (dereference) {
      ObjectType dereferenced = type != null ? type.dereference() : null;
      if (dereferenced != null) {
        type = dereferenced;
      }
    }
    if (type != null &&
        (type.isFunctionPrototypeType() ||
         (type.toObjectType() != null &&
          type.toObjectType().getConstructor() != null))) {
      return type.toString();
    }
    String qualifiedName = n.getQualifiedName();
    if (qualifiedName != null) {
      return qualifiedName;
    } else if (type != null && type.isFunctionType()) {
      return "function";
    } else if (type != null) {
      return type.toString();
    } else {
      return null;
    }
  }
private void inferPropertyTypesToMatchConstraint(
      JSType type, JSType constraint) {
    if (type == null || constraint == null) {
      return;
    }

    // Distribute over union types for 'type'
    if (type.isUnionType()) {
      for (JSType alt : type.toMaybeUnionType().getAlternates()) {
        inferPropertyTypesToMatchConstraint(alt, constraint);
      }
      return;
    }

    // Distribute over union types for 'constraint'
    if (constraint.isUnionType()) {
      for (JSType cAlt : constraint.toMaybeUnionType().getAlternates()) {
        inferPropertyTypesToMatchConstraint(type, cAlt);
      }
      return;
    }

    ObjectType constraintObj =
        ObjectType.cast(constraint.restrictByNotNullOrUndefined());
    if (constraintObj != null && constraintObj.isRecordType()) {
      ObjectType objType = ObjectType.cast(type.restrictByNotNullOrUndefined());
      if (objType != null) {
        for (String prop : constraintObj.getOwnPropertyNames()) {
          JSType propType = constraintObj.getPropertyType(prop);
          if (!objType.isPropertyTypeDeclared(prop)) {
            JSType typeToInfer = propType;
            if (!objType.hasProperty(prop)) {
              typeToInfer =
                  getNativeType(VOID_TYPE).getLeastSupertype(propType);
            }
            objType.defineInferredProperty(prop, typeToInfer, null);
          }
        }
      }
    }
  }
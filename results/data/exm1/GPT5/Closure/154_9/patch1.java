private void checkDeclaredPropertyInheritance(
      NodeTraversal t, Node n, FunctionType ctorType, String propertyName,
      JSDocInfo info, JSType propertyType) {
    if (hasUnknownOrEmptySupertype(ctorType)) {
      return;
    }

    FunctionType superClass = ctorType.getSuperClassConstructor();
    boolean superClassHasProperty = superClass != null &&
        superClass.getPrototype().hasProperty(propertyName);
    boolean declaredOverride = info != null && info.isOverride();

    boolean foundInterfaceProperty = false;
    if (ctorType.isConstructor()) {
      for (JSType implementedInterface : ctorType.getImplementedInterfaces()) {
        if (implementedInterface.isUnknownType() ||
            implementedInterface.isEmptyType()) {
          continue;
        }
        FunctionType interfaceType =
            implementedInterface.toObjectType().getConstructor();
        Preconditions.checkNotNull(interfaceType);
        boolean interfaceHasProperty =
            interfaceType.getPrototype().hasProperty(propertyName);
        foundInterfaceProperty = foundInterfaceProperty || interfaceHasProperty;
        if (reportMissingOverride.isOn() && !declaredOverride &&
            interfaceHasProperty) {
          compiler.report(t.makeError(n, reportMissingOverride,
              HIDDEN_INTERFACE_PROPERTY, propertyName,
              interfaceType.getTopMostDefiningType(propertyName).toString()));
        }
        if (interfaceHasProperty) {
          JSType interfacePropType =
              interfaceType.getPrototype().getPropertyType(propertyName);
          if (!propertyType.canAssignTo(interfacePropType)) {
            compiler.report(t.makeError(n,
                HIDDEN_INTERFACE_PROPERTY_MISMATCH, propertyName,
                interfaceType.getTopMostDefiningType(propertyName).toString(),
                interfacePropType.toString(), propertyType.toString()));
          }
        }
      }
    }

    if (!declaredOverride && !superClassHasProperty) {
      return;
    }

    JSType topInstanceType = superClassHasProperty ?
        superClass.getTopMostDefiningType(propertyName) : null;
    if (reportMissingOverride.isOn() && ctorType.isConstructor() &&
        !declaredOverride && superClassHasProperty) {
      compiler.report(t.makeError(n, reportMissingOverride,
          HIDDEN_SUPERCLASS_PROPERTY, propertyName,
          topInstanceType.toString()));
    }
    if (!declaredOverride) {
      return;
    }
    if (superClassHasProperty) {
      JSType superClassPropType =
          superClass.getPrototype().getPropertyType(propertyName);
      if (!propertyType.canAssignTo(superClassPropType)) {
        compiler.report(
            t.makeError(n, HIDDEN_SUPERCLASS_PROPERTY_MISMATCH,
                propertyName, topInstanceType.toString(),
                superClassPropType.toString(), propertyType.toString()));
      }
    } else if (!foundInterfaceProperty) {
      compiler.report(
          t.makeError(n, UNKNOWN_OVERRIDE,
              propertyName, ctorType.getInstanceType().toString()));
    }
  }

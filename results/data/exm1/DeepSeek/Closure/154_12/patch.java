  static final DiagnosticGroup ALL_DIAGNOSTICS = new DiagnosticGroup(
      DETERMINISTIC_TEST,
      DETERMINISTIC_TEST_NO_RESULT,
      INEXISTENT_ENUM_ELEMENT,
      INEXISTENT_PROPERTY,
      NOT_A_CONSTRUCTOR,
      BIT_OPERATION,
      NOT_CALLABLE,
      CONSTRUCTOR_NOT_CALLABLE,
      FUNCTION_MASKS_VARIABLE,
      MULTIPLE_VAR_DEF,
      ENUM_DUP,
      ENUM_NOT_CONSTANT,
      INVALID_INTERFACE_MEMBER_DECLARATION,
      INTERFACE_FUNCTION_NOT_EMPTY,
      CONFLICTING_EXTENDED_TYPE,
      BAD_IMPLEMENTED_TYPE,
      HIDDEN_SUPERCLASS_PROPERTY,
      HIDDEN_INTERFACE_PROPERTY,
      HIDDEN_SUPERCLASS_PROPERTY_MISMATCH,
      HIDDEN_INTERFACE_PROPERTY_MISMATCH,
      UNKNOWN_OVERRIDE,
      INTERFACE_METHOD_OVERRIDE,
      UNKNOWN_EXPR_TYPE,
      UNRESOLVED_TYPE,
      WRONG_ARGUMENT_COUNT,
      ILLEGAL_IMPLICIT_CAST,
      TypedScopeCreator.UNKNOWN_LENDS,
      TypedScopeCreator.LENDS_ON_NON_OBJECT,
      TypedScopeCreator.CTOR_INITIALIZER,
      TypedScopeCreator.IFACE_INITIALIZER,
      FunctionTypeBuilder.THIS_TYPE_NON_OBJECT);

  private void checkDeclaredPropertyInheritance(
      NodeTraversal t, Node n, FunctionType ctorType, String propertyName,
      JSDocInfo info, JSType propertyType) {
    // If the supertype doesn't resolve correctly, we've warned about this
    // already.
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
          // @override not present, but the property does override an interface
          // property
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
      // nothing to do here, it's just a plain new property
      return;
    }

    JSType topInstanceType = superClassHasProperty ?
        superClass.getTopMostDefiningType(propertyName) : null;
    if (reportMissingOverride.isOn() && ctorType.isConstructor() &&
        !declaredOverride && superClassHasProperty) {
      // @override not present, but the property does override a superclass
      // property
      compiler.report(t.makeError(n, reportMissingOverride,
          HIDDEN_SUPERCLASS_PROPERTY, propertyName,
          topInstanceType.toString()));
    }
    if (!declaredOverride) {
      // there's no @override to check
      return;
    }
    // @override is present and we have to check that it is ok
    if (superClassHasProperty) {
      // there is a superclass implementation
      JSType superClassPropType =
          superClass.getPrototype().getPropertyType(propertyName);
      if (!propertyType.canAssignTo(superClassPropType)) {
        compiler.report(
            t.makeError(n, HIDDEN_SUPERCLASS_PROPERTY_MISMATCH,
                propertyName, topInstanceType.toString(),
                superClassPropType.toString(), propertyType.toString()));
      }
    } else if (!foundInterfaceProperty) {
      // there is no superclass nor interface implementation
      compiler.report(
          t.makeError(n, UNKNOWN_OVERRIDE,
              propertyName, ctorType.getInstanceType().toString()));
    }
  }

  static final DiagnosticGroup ALL_TYPE_DIAGNOSTICS = new DiagnosticGroup(
      INVALID_CAST,
      TYPE_MISMATCH_WARNING,
      MISSING_EXTENDS_TAG_WARNING,
      DUP_VAR_DECLARATION,
      HIDDEN_PROPERTY_MISMATCH,
      INTERFACE_METHOD_NOT_IMPLEMENTED);

  private void expectInterfaceProperty(NodeTraversal t, Node n,
      ObjectType instance, ObjectType implementedInterface, String prop) {
    if (!instance.hasProperty(prop)) {
      // Not implemented
      String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
      sourceName = sourceName == null ? "" : sourceName;
      if (shouldReport) {
        compiler.report(JSError.make(sourceName, n,
            INTERFACE_METHOD_NOT_IMPLEMENTED,
            prop, implementedInterface.toString(), instance.toString()));
      }
      registerMismatch(instance, implementedInterface);
        // Implemented, but not correctly typed
    }
  }
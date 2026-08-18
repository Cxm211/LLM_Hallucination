private void expectInterfaceProperty(NodeTraversal t, Node n,
      ObjectType instance, ObjectType implementedInterface, String prop) {
    String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
    sourceName = sourceName == null ? "" : sourceName;
    if (!instance.hasProperty(prop)) {
      // Not implemented
      if (shouldReport) {
        compiler.report(JSError.make(sourceName, n,
            INTERFACE_METHOD_NOT_IMPLEMENTED,
            prop, implementedInterface.toString(), instance.toString()));
      }
      registerMismatch(instance, implementedInterface);
    } else {
      // Implemented, check typing
      JSType interfacePropType = implementedInterface.getPropertyType(prop);
      JSType instancePropType = instance.getPropertyType(prop);
      if (!instancePropType.canAssignTo(interfacePropType)) {
        if (shouldReport) {
          compiler.report(JSError.make(sourceName, n,
              HIDDEN_PROPERTY_MISMATCH,
              prop, implementedInterface.toString(),
              interfacePropType.toString(), instancePropType.toString()));
        }
        registerMismatch(instance, implementedInterface);
      }
    }
  }
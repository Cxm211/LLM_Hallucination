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
    } else {
      // Implemented, check if correctly typed
      JSType instancePropType = instance.getPropertyType(prop);
      JSType interfacePropType = implementedInterface.getPropertyType(prop);
      if (interfacePropType != null && instancePropType != null) {
        if (!instancePropType.canAssignTo(interfacePropType)) {
          String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
          sourceName = sourceName == null ? "" : sourceName;
          if (shouldReport) {
            compiler.report(JSError.make(sourceName, n,
                HIDDEN_INTERFACE_PROPERTY_MISMATCH,
                prop, implementedInterface.toString(),
                interfacePropType.toString(), instancePropType.toString()));
          }
          registerMismatch(instance, implementedInterface);
        }
      }
    }
  }
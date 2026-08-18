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
      // Implemented, check type
      JSType instancePropType = instance.getPropertyType(prop);
      JSType interfacePropType = implementedInterface.getPropertyType(prop);
      if (!instancePropType.canAssignTo(interfacePropType)) {
        compiler.report(t.makeError(n,
            HIDDEN_INTERFACE_PROPERTY_MISMATCH, prop,
            implementedInterface.toString(),
            interfacePropType.toString(), instancePropType.toString()));
      }
    }
  }
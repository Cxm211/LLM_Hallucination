  private void expectInterfaceProperty(NodeTraversal t, Node n,
      ObjectType instance, ObjectType implementedInterface, String prop) {
    if (!instance.hasProperty(prop)) {
      String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
      sourceName = sourceName == null ? "" : sourceName;
      if (shouldReport) {
        compiler.report(JSError.make(sourceName, n,
            INTERFACE_METHOD_NOT_IMPLEMENTED,
            prop, implementedInterface.toString(), instance.toString()));
      }
      registerMismatch(instance, implementedInterface);
    } else {
      // Check type mismatch
      JSType instancePropType = instance.getPropertyType(prop);
      JSType interfacePropType = implementedInterface.getPropertyType(prop);
      if (!instancePropType.canAssignTo(interfacePropType)) {
        String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
        sourceName = sourceName == null ? "" : sourceName;
        if (shouldReport) {
          compiler.report(JSError.make(sourceName, n,
              INTERFACE_METHOD_NOT_IMPLEMENTED, // or a separate type mismatch diagnostic
              prop, implementedInterface.toString(), instance.toString()));
        }
        registerMismatch(instance, implementedInterface);
      }
    }
  }
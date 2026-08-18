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
      JSType propType = instance.getPropertyType(prop);
      JSType interfacePropType = implementedInterface.getPropertyType(prop);
      if (interfacePropType != null && propType != null &&
          !propType.canAssignTo(interfacePropType)) {
        String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
        sourceName = sourceName == null ? "" : sourceName;
        if (shouldReport) {
          compiler.report(JSError.make(sourceName, n,
              HIDDEN_PROPERTY_MISMATCH, prop,
              interfacePropType.toString(), propType.toString()));
        }
        registerMismatch(instance, implementedInterface);
      }
    }
  }
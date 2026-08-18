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
      JSType found = instance.getPropertyType(prop);
      JSType required = implementedInterface.getPropertyType(prop);
      if (!found.canAssignTo(required)) {
        if (shouldReport) {
          String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
          sourceName = sourceName == null ? "" : sourceName;
          compiler.report(JSError.make(sourceName, n,
              HIDDEN_PROPERTY_MISMATCH,
              prop, implementedInterface.toString(),
              required.toString(), found.toString()));
        }
        registerMismatch(instance, implementedInterface);
      }
    }
  }
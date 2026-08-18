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
      return;
    }
    // Implemented, check type compatibility
    JSType foundType = instance.getPropertyType(prop);
    JSType requiredType = implementedInterface.getPropertyType(prop);
    if (foundType != null && requiredType != null && !foundType.canAssignTo(requiredType)) {
      String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
      sourceName = sourceName == null ? "" : sourceName;
      if (shouldReport) {
        compiler.report(JSError.make(sourceName, n,
            HIDDEN_PROPERTY_MISMATCH,
            prop,
            implementedInterface.toString(),
            requiredType.toString(),
            foundType.toString()));
      }
      registerMismatch(instance, implementedInterface);
    }
  }
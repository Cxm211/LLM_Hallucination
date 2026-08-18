  Node getNodeForCodeInsertion(JSModule module) {
    if (module == null) {
      if (inputs.length == 0) {
        // If there are no source inputs, fall back to the first extern.
        if (externs != null && externs.length > 0) {
          return externs[0].getAstRoot(this);
        }
        throw new IllegalStateException("No inputs");
      }

      return inputs[0].getAstRoot(this);
    }

    List<CompilerInput> inputs = module.getInputs();
    if (inputs.size() > 0) {
      return inputs.get(0).getAstRoot(this);
    }
    for (JSModule m : getModuleGraph().getTransitiveDepsDeepestFirst(module)) {
      inputs = m.getInputs();
      if (inputs.size() > 0) {
        return inputs.get(0).getAstRoot(this);
      }
    }
    throw new IllegalStateException("Root module has no inputs");
  }
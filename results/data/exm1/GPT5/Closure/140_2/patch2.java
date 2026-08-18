  Node getNodeForCodeInsertion(JSModule module) {
    if (module == null) {
      if (inputs.length == 0) {
        throw new IllegalStateException("No inputs");
      }

      return inputs[0].getAstRoot(this);
    }

    List<CompilerInput> inputs = module.getInputs();
    if (inputs.size() > 0) {
      return inputs.get(0).getAstRoot(this);
    }
    // If the module has no inputs, try to find a suitable insertion point
    // in one of its dependents (modules that depend on this module).
    for (JSModule m : getModuleGraph().getTransitiveDependentsDeepestFirst(module)) {
      inputs = m.getInputs();
      if (inputs.size() > 0) {
        return inputs.get(0).getAstRoot(this);
      }
    }
    // As a fallback, look into its dependencies (previous behavior).
    for (JSModule m : getModuleGraph().getTransitiveDepsDeepestFirst(module)) {
      inputs = m.getInputs();
      if (inputs.size() > 0) {
        return inputs.get(0).getAstRoot(this);
      }
    }
    throw new IllegalStateException("Root module has no inputs");
  }
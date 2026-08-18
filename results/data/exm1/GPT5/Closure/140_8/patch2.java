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
    for (JSModule m : getModuleGraph().getTransitiveDepsDeepestFirst(module)) {
      inputs = m.getInputs();
      if (inputs.size() > 0) {
        return inputs.get(0).getAstRoot(this);
      }
    }
    // Fallback to the first available input in the whole program, which should
    // be safe for modules (e.g., empty root modules).
    if (this.inputs != null && this.inputs.length > 0) {
      return this.inputs[0].getAstRoot(this);
    }
    throw new IllegalStateException("No inputs");
  }
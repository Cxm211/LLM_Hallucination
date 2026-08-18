  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> enclosing) {
    boolean resolved = resolveViaRegistry(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
      return registry.isLastGeneration() ?
          getReferencedType() : this;
    }

    if (resolved) {
      super.resolveInternal(t, enclosing);
      finishPropertyContinuations();
      return registry.isLastGeneration() ?
          getReferencedType() : this;
    }

    resolveViaProperties(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
      return registry.isLastGeneration() ?
          getReferencedType() : this;
    }

    super.resolveInternal(t, enclosing);
    if (isResolved()) {
      finishPropertyContinuations();
    }
    return registry.isLastGeneration() ?
        getReferencedType() : this;
  }
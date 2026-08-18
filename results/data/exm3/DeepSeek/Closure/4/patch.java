  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> enclosing) {
    // TODO(user): Investigate whether it is really necessary to keep two
    // different mechanisms for resolving named types, and if so, which order
    // makes more sense. Now, resolution via registry is first in order to
    // avoid triggering the warnings built into the resolution via properties.
    boolean resolved = resolveViaRegistry(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
      // Once a cycle is detected, no need to continue resolution.
      return registry.isLastGeneration() ? getReferencedType() : this;
    }

    if (resolved) {
      super.resolveInternal(t, enclosing);
      if (detectImplicitPrototypeCycle()) {
        handleTypeCycle(t);
        return registry.isLastGeneration() ? getReferencedType() : this;
      }
      finishPropertyContinuations();
      return registry.isLastGeneration() ?
          getReferencedType() : this;
    }

    resolveViaProperties(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
      // Once a cycle is detected, no need to continue resolution.
      return registry.isLastGeneration() ? getReferencedType() : this;
    }

    super.resolveInternal(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
      return registry.isLastGeneration() ? getReferencedType() : this;
    }
    if (isResolved()) {
      finishPropertyContinuations();
    }
    return registry.isLastGeneration() ?
        getReferencedType() : this;
  }
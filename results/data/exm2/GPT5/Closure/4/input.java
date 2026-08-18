  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> enclosing) {
    // TODO(user): Investigate whether it is really necessary to keep two
    // different mechanisms for resolving named types, and if so, which order
    // makes more sense. Now, resolution via registry is first in order to
    // avoid triggering the warnings built into the resolution via properties.
    boolean resolved = resolveViaRegistry(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
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
    }

    super.resolveInternal(t, enclosing);
    if (isResolved()) {
      finishPropertyContinuations();
    }
    return registry.isLastGeneration() ?
        getReferencedType() : this;
  }

// trigger testcase
public void testConversionFromInterfaceToRecursiveConstructor()
      throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "/** @interface */ var OtherType = function() {}\n" +
            "/** @implements {MyType} \n * @constructor */\n" +
            "var MyType = function() {}\n" +
            "/** @type {MyType} */\n" +
            "var x = /** @type {!OtherType} */ (new Object());",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type MyType",
            "initializing variable\n" +
            "found   : OtherType\n" +
            "required: (MyType|null)"));
  }

public void testImplementsExtendsLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "/** @constructor \n * @implements {F} */var G = function() {};" +
            "/** @constructor \n * @extends {G} */var F = function() {};" +
        "alert((new F).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type F"));
  }

public void testImplementsLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "/** @constructor \n * @implements {T} */var T = function() {};" +
        "alert((new T).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type T"));
  }

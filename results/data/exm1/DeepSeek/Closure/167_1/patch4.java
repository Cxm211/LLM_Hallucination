  private FlowScope caseNameOrGetProp(Node name, FlowScope blindScope,
      boolean outcome) {
    JSType type = getTypeIfRefinable(name, blindScope);
    if (type != null) {
      JSType restrictedType =
          type.getRestrictedTypeGivenToBooleanOutcome(outcome);
      JSType noType = getNativeType(JSTypeNative.NO_TYPE);
      if (!noType.equals(restrictedType)) {
        FlowScope informed = blindScope.createChildFlowScope();
        declareNameInScope(informed, name, restrictedType);
        return informed;
      }
    }
    return blindScope;
  }
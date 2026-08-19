// ===== FIXED com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter :: caseAndOrNotShortCircuiting(Node, Node, FlowScope, boolean) [lines 307-352] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-167-fixed/src/com/google/javascript/jscomp/type/SemanticReverseAbstractInterpreter.java =====
  private FlowScope caseAndOrNotShortCircuiting(Node left, Node right,
        FlowScope blindScope, boolean condition) {
    // left type
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    if (leftType != null) {
      leftIsRefineable = true;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
      blindScope = firstPreciserScopeKnowingConditionOutcome(
          left, blindScope, condition);
    }

    // restricting left type
    JSType restrictedLeftType = (leftType == null) ? null :
        leftType.getRestrictedTypeGivenToBooleanOutcome(condition);
    if (restrictedLeftType == null) {
      return firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    // right type
    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
      blindScope = firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    if (condition) {
      JSType restrictedRightType = (rightType == null) ? null :
          rightType.getRestrictedTypeGivenToBooleanOutcome(condition);

      // creating new scope
      return maybeRestrictTwoNames(
          blindScope,
          left, leftType, leftIsRefineable ? restrictedLeftType : null,
          right, rightType, rightIsRefineable ? restrictedRightType : null);
    }
    return blindScope;
  }

// ===== FIXED com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter :: caseEquality(Node, Node, FlowScope, Function) [lines 272-305] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-167-fixed/src/com/google/javascript/jscomp/type/SemanticReverseAbstractInterpreter.java =====
  private FlowScope caseEquality(Node left, Node right, FlowScope blindScope,
      Function<TypePair, TypePair> merging) {
    // left type
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    if (leftType != null) {
      leftIsRefineable = true;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
    }

    // right type
    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
    }

    // merged types
    TypePair merged = merging.apply(new TypePair(leftType, rightType));

    // creating new scope
    if (merged != null) {
      return maybeRestrictTwoNames(
          blindScope,
          left, leftType, leftIsRefineable ? merged.typeA : null,
          right, rightType, rightIsRefineable ? merged.typeB : null);
    }
    return blindScope;
  }

// ===== FIXED com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter :: caseNameOrGetProp(Node, FlowScope, boolean) [lines 426-435] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-167-fixed/src/com/google/javascript/jscomp/type/SemanticReverseAbstractInterpreter.java =====
  private FlowScope caseNameOrGetProp(Node name, FlowScope blindScope,
      boolean outcome) {
    JSType type = getTypeIfRefinable(name, blindScope);
    if (type != null) {
      return maybeRestrictName(
          blindScope, name, type,
          type.getRestrictedTypeGivenToBooleanOutcome(outcome));
    }
    return blindScope;
  }

// ===== FIXED com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter :: maybeRestrictName(FlowScope, Node, JSType, JSType) [lines 392-400] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-167-fixed/src/com/google/javascript/jscomp/type/SemanticReverseAbstractInterpreter.java =====
  private FlowScope maybeRestrictName(
      FlowScope blindScope, Node node, JSType originalType, JSType restrictedType) {
    if (restrictedType != null && restrictedType != originalType) {
      FlowScope informed = blindScope.createChildFlowScope();
      declareNameInScope(informed, node, restrictedType);
      return informed;
    }
    return blindScope;
  }

// ===== FIXED com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter :: maybeRestrictTwoNames(FlowScope, Node, boolean, JSType, Node, boolean, JSType) [lines 405-424] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-167-fixed/src/com/google/javascript/jscomp/type/SemanticReverseAbstractInterpreter.java =====
  private FlowScope maybeRestrictTwoNames(
      FlowScope blindScope,
      Node left, JSType originalLeftType, JSType restrictedLeftType,
      Node right, JSType originalRightType, JSType restrictedRightType) {
    boolean shouldRefineLeft =
        restrictedLeftType != null && restrictedLeftType != originalLeftType;
    boolean shouldRefineRight =
        restrictedRightType != null && restrictedRightType != originalRightType;
    if (shouldRefineLeft || shouldRefineRight) {
      FlowScope informed = blindScope.createChildFlowScope();
      if (shouldRefineLeft) {
        declareNameInScope(informed, left, restrictedLeftType);
      }
      if (shouldRefineRight) {
        declareNameInScope(informed, right, restrictedRightType);
      }
      return informed;
    }
    return blindScope;
  }

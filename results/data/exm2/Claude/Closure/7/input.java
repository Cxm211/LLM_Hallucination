    public JSType caseObjectType(ObjectType type) {
      if (value.equals("function")) {
        JSType ctorType = getNativeType(U2U_CONSTRUCTOR_TYPE);
        return resultEqualsValue && ctorType.isSubtype(type) ? ctorType : null;
          // Objects are restricted to "Function", subtypes are left
          // Only filter out subtypes of "function"
      }
      return matchesExpectation("object") ? type : null;
    }

// trigger testcase
public void testGoogIsFunction2() throws Exception {
    testClosureFunction("goog.isFunction",
        OBJECT_NUMBER_STRING_BOOLEAN,
        U2U_CONSTRUCTOR_TYPE,
        OBJECT_NUMBER_STRING_BOOLEAN);
  }

public void testTypeof3() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(
            blind, "a", OBJECT_NUMBER_STRING_BOOLEAN)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_NUMBER_STRING_BOOLEAN)));
  }

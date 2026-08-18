// buggy code
  public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        JSType greatestSubtype = alt.getGreatestSubtype(type);
        if (!greatestSubtype.isEmptyType()) {
          // We've found a type with this property. Now we just have to make
          // sure it's not a type used for internal bookkeeping.

          return true;
        }
      }
    }
    return false;
  }

  public final boolean defineDeclaredProperty(String propertyName,
      JSType type, Node propertyNode) {
    boolean result = defineProperty(propertyName, type, false,
        propertyNode);

    // All property definitions go through this method
    // or defineDeclaredProperty. Because the properties defined an an
    // object can affect subtyping, it's slightly more efficient
    // to register this after defining the property.
    registry.registerPropertyOnType(propertyName, this);

    return result;
  }

  RecordType(JSTypeRegistry registry, Map<String, RecordProperty> properties) {
    super(registry, null, null);
    setPrettyPrint(true);

    for (String property : properties.keySet()) {
      RecordProperty prop = properties.get(property);
      if (prop == null) {
        throw new IllegalStateException(
            "RecordProperty associated with a property should not be null!");
      }
        defineDeclaredProperty(
            property, prop.getType(), prop.getPropertyNode());
    }

    // Freeze the record type.
    isFrozen = true;
  }

  JSType getGreatestSubtypeHelper(JSType that) {
    if (that.isRecordType()) {
      RecordType thatRecord = that.toMaybeRecordType();
      RecordTypeBuilder builder = new RecordTypeBuilder(registry);

      // The greatest subtype consists of those *unique* properties of both
      // record types. If any property conflicts, then the NO_TYPE type
      // is returned.
      for (String property : properties.keySet()) {
        if (thatRecord.hasProperty(property) &&
            !thatRecord.getPropertyType(property).isEquivalentTo(
                getPropertyType(property))) {
          return registry.getNativeObjectType(JSTypeNative.NO_TYPE);
        }

        builder.addProperty(property, getPropertyType(property),
            getPropertyNode(property));
      }

      for (String property : thatRecord.properties.keySet()) {
        if (!hasProperty(property)) {
          builder.addProperty(property, thatRecord.getPropertyType(property),
              thatRecord.getPropertyNode(property));
        }
      }

      return builder.build();
    }

    JSType greatestSubtype = registry.getNativeType(
        JSTypeNative.NO_OBJECT_TYPE);
    JSType thatRestrictedToObj =
        registry.getNativeType(JSTypeNative.OBJECT_TYPE)
        .getGreatestSubtype(that);
    if (!thatRestrictedToObj.isEmptyType()) {
      // In this branch, the other type is some object type. We find
      // the greatest subtype with the following algorithm:
      // 1) For each property "x" of this record type, take the union
      //    of all classes with a property "x" with a compatible property type.
      //    and which are a subtype of {@code that}.
      // 2) Take the intersection of all of these unions.
      for (Map.Entry<String, JSType> entry : properties.entrySet()) {
        String propName = entry.getKey();
        JSType propType = entry.getValue();
        UnionTypeBuilder builder = new UnionTypeBuilder(registry);
        for (ObjectType alt :
                 registry.getEachReferenceTypeWithProperty(propName)) {
          JSType altPropType = alt.getPropertyType(propName);
          if (altPropType != null && !alt.isEquivalentTo(this) &&
              alt.isSubtype(that) &&
              (propType.isUnknownType() || altPropType.isUnknownType() ||
                  altPropType.isEquivalentTo(propType))) {
            builder.addAlternate(alt);
          }
        }
        greatestSubtype = greatestSubtype.getLeastSupertype(builder.build());
      }
    }
    return greatestSubtype;
  }

  public RecordTypeBuilder(JSTypeRegistry registry) {
    this.registry = registry;
  }

  public JSType build() {
     // If we have an empty record, simply return the object type.
    if (isEmpty) {
       return registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    }

    return new RecordType(
        registry, Collections.unmodifiableMap(properties));
  }

// relevant test
// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference5
  public void testReturnTypeInference5() {
    testSame("function f() { if (true) { return 1; } }");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testLiteralTypesInferred
  public void testLiteralTypesInferred() {
    testSame("null + true + false + 0 + '' + {}");
    assertEquals(
        "null", findTokenType(Token.NULL, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.TRUE, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.FALSE, globalScope).toString());
    assertEquals(
        "number", findTokenType(Token.NUMBER, globalScope).toString());
    assertEquals(
        "string", findTokenType(Token.STRING, globalScope).toString());
    assertEquals(
        "{}", findTokenType(Token.OBJECTLIT, globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalQualifiedNameInLocalScope
  public void testGlobalQualifiedNameInLocalScope() {
    testSame(
        "var ns = {}; " +
        "(function() { " +
        "     ns.foo = function(x) {}; })();" +
        "(function() { ns.foo(3); })();");
    assertNotNull(globalScope.getVar("ns.foo"));
    assertEquals(
        "function (number): undefined",
        globalScope.getVar("ns.foo").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty1
  public void testDeclaredObjectLitProperty1() throws Exception {
    testSame("var x = { y: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty2
  public void testDeclaredObjectLitProperty2() throws Exception {
    testSame("var x = { y: function(z){}};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "function (number): undefined",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (number): undefined}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty3
  public void testDeclaredObjectLitProperty3() throws Exception {
    testSame("function f() {" +
        "  var x = { y: function(z){ return 3; }};" +
        "}");
    ObjectType xType = ObjectType.cast(lastLocalScope.getVar("x").getType());
    assertEquals(
        "function (?): number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (?): number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty4
  public void testDeclaredObjectLitProperty4() throws Exception {
    testSame("var x = {y: 5,  z: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number", xType.getPropertyType("y").toString());
    assertFalse(xType.isPropertyTypeDeclared("y"));
    assertTrue(xType.isPropertyTypeDeclared("z"));
    assertEquals(
        "{y: number, z: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty5
  public void testDeclaredObjectLitProperty5() throws Exception {
    testSame("var x = { prop: 3};" +
             "function f() { var y = x.prop; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty6
  public void testDeclaredObjectLitProperty6() throws Exception {
    testSame("var x = { prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertFalse(prop.isTypeInferred());
    assertFalse(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty1
  public void testInferredObjectLitProperty1() throws Exception {
    testSame("var x = {prop: 3};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("number", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty2
  public void testInferredObjectLitProperty2() throws Exception {
    testSame("var x = {prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType1
  public void testDeclaredConstType1() throws Exception {
    testSame(
        " var x = 3;" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType2
  public void testDeclaredConstType2() throws Exception {
    testSame(
        " var x = {};" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType3
  public void testDeclaredConstType3() throws Exception {
    testSame(
        " var x = {};" +
        " x.z = 'hi';" +
        "function f() { var y = x.z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType4
  public void testDeclaredConstType4() throws Exception {
    testSame(
        " function Foo() {}" +
        " Foo.prototype.z = 'hi';" +
        "function f() { var y = (new Foo()).z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());

    ObjectType fooType =
        ((FunctionType) globalScope.getVar("Foo").getType()).getInstanceType();
    assertTrue(fooType.isPropertyTypeDeclared("z"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType5
  public void testDeclaredConstType5() throws Exception {
    testSame(
        " var goog = goog || {};" +
        " var foo = goog || {};" +
        "function f() { var y = goog; var z = foo; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());

    JSType zType = lastLocalScope.getVar("z").getType();
    assertEquals("?", zType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadCtorInit1
  public void testBadCtorInit1() throws Exception {
    testSame(" var f;", CTOR_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadCtorInit2
  public void testBadCtorInit2() throws Exception {
    testSame("var x = {};  x.f;", CTOR_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadIfaceInit1
  public void testBadIfaceInit1() throws Exception {
    testSame(" var f;", IFACE_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadIfaceInit2
  public void testBadIfaceInit2() throws Exception {
    testSame("var x = {};  x.f;", IFACE_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInHook
  public void testFunctionInHook() throws Exception {
    testSame(" var f = Math.random() ? " +
        "function(x) {} : function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInAnd
  public void testFunctionInAnd() throws Exception {
    testSame(" var f = Math.random() && " +
        "function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInOr
  public void testFunctionInOr() throws Exception {
    testSame(" var f = Math.random() || " +
        "function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInComma
  public void testFunctionInComma() throws Exception {
    testSame(" var f = (Math.random(), " +
        "function(x) {});");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUnreachableCode
  public void testRemoveUnreachableCode() {
    
    test("function foo(){switch(foo){case 1:x=1;return;break;" +
         "case 2:{x=2;return;break}default:}}",
         "function foo(){switch(foo){case 1:x=1;return;" +
         "case 2:{x=2}default:}}");

    
    test("function bar(){if(foo)x=1;else if(bar){return;x=2}" +
         "else{x=3;return;x=4}return 5;x=5}",
         "function bar(){if(foo)x=1;else if(bar){return}" +
         "else{x=3;return}return 5}");

    
    test("function foo(){if(x==3)return;x=4;y++;while(y==4){return;x=3}}",
         "function foo(){if(x==3)return;x=4;y++;while(y==4){return}}");

    
    test("function baz(){for(i=0;i<n;i++){x=3;break;x=4}" +
         "do{x=2;break;x=4}while(x==4);" +
         "while(i<4){x=3;return;x=6}}",
         "function baz(){for(i=0;i<n;){x=3;break}" +
         "do{x=2;break}while(x==4);" +
         "while(i<4){x=3;return}}");

    
    test("function foo(){if(x==3){return}return 5;while(y==4){x++;return;x=4}}",
         "function foo(){if(x==3){return}return 5}");

    
    test("function foo(){return 3;for(;y==4;){x++;return;x=4}}",
         "function foo(){return 3}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=4;return 5;x=5}}",
         "function foo(){try{x=3;return x+1}catch(e){x=4;return 5}}");

    
    test("function foo(){try{x=3;return x+1;x=5}finally{x=4;return 5;x=5}}",
         "function foo(){try{x=3;return x+1}finally{x=4;return 5}}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=3;return;x=2}" +
         "finally{x=4;return 5;x=5}}",

         "function foo(){try{x=3;return x+1}catch(e){x=3;return}" +
         "finally{x=4;return 5}}");

    
    test("function foo(){x=3;if(x==4){x=5;return;x=6}else{x=7}return 5;x=3}",
         "function foo(){x=3;if(x==4){x=5;return}else{x=7}return 5}");

    
    test("function foo() { return 1; var x = 2; var y = 10; return 2;}",
         "function foo() { var y; var x; return 1}");

    test("function foo() { return 1; x = 2; y = 10; return 2;}",
         "function foo(){ return 1}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessNameStatements
  public void testRemoveUselessNameStatements() {
    test("a;", "");
    test("a.b;", "");
    test("a.b.MyClass.prototype.memberName;", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessStrings
  public void testRemoveUselessStrings() {
    test("'a';", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testNoRemoveUseStrict
  public void testNoRemoveUseStrict() {
    test("'use strict';", "'use strict'");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testNoRemoveUselessNameStatements
  public void testNoRemoveUselessNameStatements() {
    removeNoOpStatements = false;
    testSame("a;");
    testSame("a.b;");
    testSame("a.b.MyClass.prototype.memberName;");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveDo
  public void testRemoveDo() {
    test("do { print(1); break } while(1)", "do { print(1); break } while(1)");
    test("while(1) { break; do { print(1); break } while(1) }",
         "while(1) { break; do {} while(1) }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessLiteralValueStatements
  public void testRemoveUselessLiteralValueStatements() {
    test("true;", "");
    test("'hi';", "");
    test("if (x) 1;", "");
    test("while (x) 1;", "while (x);");
    test("do 1; while (x);", "do ; while (x);");
    test("for (;;) 1;", "for (;;);");
    test("switch(x){case 1:true;case 2:'hi';default:true}",
         "switch(x){case 1:case 2:default:}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testConditionalDeadCode
  public void testConditionalDeadCode() {
    test("function f() { if (1) return 5; else return 5; x = 1}",
        "function f() { if (1) return 5; else return 5; }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testSwitchCase
  public void testSwitchCase() {
    test("function f() { switch(x) { default: return 5; foo()}}",
         "function f() { switch(x) { default: return 5;}}");
    test("function f() { switch(x) { default: return; case 1: foo(); bar()}}",
         "function f() { switch(x) { default: return; case 1: foo(); bar()}}");
    test("function f() { switch(x) { default: return; case 1: return 5;bar()}}",
         "function f() { switch(x) { default: return; case 1: return 5;}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testTryCatchFinally
  public void testTryCatchFinally() {
    testSame("try {foo()} catch (e) {bar()}");
    testSame("try { try {foo()} catch (e) {bar()}} catch (x) {bar()}");
    test("try {var x = 1} catch (e) {e()}", "try {var x = 1} finally {}");
    test("try {var x = 1} catch (e) {e()} finally {x()}",
        " try {var x = 1}                 finally {x()}");
    test("try {var x = 1} catch (e) {e()} finally {}",
        "try {var x = 1} finally {}");
    testSame("try {var x = 1} finally {x()}");
    testSame("try {var x = 1} finally {}");
    test("function f() {return; try{var x = 1}catch(e){} }",
         "function f() {var x;}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemovalRequiresRedeclaration
  public void testRemovalRequiresRedeclaration() {
    test("while(1) { break; var x = 1}", "var x; while(1) { break } ");
    test("while(1) { break; var x=1; var y=1}",
        "var y; var x; while(1) { break } ");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testAssignPropertyOnCreatedObject
  public void testAssignPropertyOnCreatedObject() {
    testSame("this.foo = 3;");
    testSame("a.foo = 3;");
    testSame("bar().foo = 3;");
    testSame("({}).foo = bar();");
    testSame("(new X()).foo = 3;");

    test("({}).foo = 3;", "");
    test("(function() {}).prototype.toString = function(){};", "");
    test("(function() {}).prototype['toString'] = function(){};", "");
    test("(function() {}).prototype[f] = function(){};", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testUselessUnconditionalReturn
  public void testUselessUnconditionalReturn() {
    test("function foo() { return }", " function foo() { }");
    test("function foo() { return; return; x=1 }", "function foo() { }");
    test("function foo() { return; return; var x=1}", "function foo() {var x}");
    test("function foo() { return; function bar() {} }",
         "function foo() {         function bar() {} }" );
    testSame("function foo() { return 5 }");

    test("function f() {switch (a) { case 'a': return}}",
         "function f() {switch (a) { case 'a': }}");
    testSame("function f() {switch (a) { case 'a': case foo(): }}");
    testSame("function f() {switch (a) {" +
             " default: return; case 'a': alert(1)}}");
    testSame("function f() {switch (a) {" +
             " case 'a': return; default: alert(1)}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testUnlessUnconditionalContinue
  public void testUnlessUnconditionalContinue() {
    test("for(;1;) {continue}", " for(;1;) {}");
    test("for(;0;) {continue}", " for(;0;) {}");

    testSame("X: for(;1;) { for(;1;) { if (x()) {continue X} x = 1}}");
    test("for(;1;) { X: for(;1;) { if (x()) {continue X} }}",
         "for(;1;) { X: for(;1;) { if (x()) {}}}");

    test("do { continue } while(1);", "do {  } while(1);");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testUnlessUnconditonalBreak
  public void testUnlessUnconditonalBreak() {
    test("switch (a) { case 'a': break }", "switch (a) { case 'a': }");
    test("switch (a) { case 'a': break; case foo(): }",
         "switch (a) { case 'a':        case foo(): }");
    test("switch (a) { default: break; case 'a': }",
         "switch (a) { default:        case 'a': }");

    testSame("switch (a) { case 'a': alert(a); break; default: alert(a); }");
    testSame("switch (a) { default: alert(a); break; case 'a': alert(a); }");

    test("X: {switch (a) { case 'a': break X}}",
         "X: {switch (a) { case 'a': }}");

    testSame("X: {switch (a) { case 'a': if (a()) {break X}  a = 1}}");
    test("X: {switch (a) { case 'a': if (a()) {break X}}}",
         "X: {switch (a) { case 'a': if (a()) {}}}");

    test("X: {switch (a) { case 'a': if (a()) {break X}}}",
         "X: {switch (a) { case 'a': if (a()) {}}}");

    testSame("do { break } while(1);");
    testSame("for(;1;) { break }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testCascadedRemovalOfUnlessUnconditonalJumps
  public void testCascadedRemovalOfUnlessUnconditonalJumps() {
    test("switch (a) { case 'a': break; case 'b': break; case 'c': break }",
         "switch (a) { case 'a': break; case 'b': case 'c': }");
    
    test("switch (a) { case 'a': break; case 'b': case 'c': }",
         "switch (a) { case 'a': case 'b': case 'c': }");

    test("function foo() {" +
      "  switch (a) { case 'a':return; case 'b':return; case 'c':return }}",
      "function foo() { switch (a) { case 'a':return; case 'b': case 'c': }}");
    test("function foo() {" +
      "  switch (a) { case 'a':return; case 'b': case 'c': }}",
      "function foo() { switch (a) { case 'a': case 'b': case 'c': }}");

    testSame("function foo() {" +
             "switch (a) { case 'a':return 2; case 'b':return 1}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue311
  public void testIssue311() {
    test("function a(b) {\n" +
         "  switch (b.v) {\n" +
         "    case 'SWITCH':\n" +
         "      if (b.i >= 0) {\n" +
         "        return b.o;\n" +
         "      } else {\n" +
         "        return;\n" +
         "      }\n" +
         "      break;\n" +
         "  }\n" +
         "}",
         "function a(b) {\n" +
         "  switch (b.v) {\n" +
         "    case 'SWITCH':\n" +
         "      if (b.i >= 0) {\n" +
         "        return b.o;\n" +
         "      } else {\n" +
         "      }\n" +
         "  }\n" +
         "}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue4177428a
  public void testIssue4177428a() {
    test(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + 
        "};",
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" +  
        "};"
        );
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue4177428b
  public void testIssue4177428b() {
    test(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  
        "    }\n" +
        "    } finally {\n" +
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + 
        "};",
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  
        "    }\n" +
        "    } finally {\n" +
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" +  
        "};"
        );
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue4177428c
  public void testIssue4177428c() {
    test(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "    } finally {\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  
        "    }\n" +
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + 
        "};",
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "    } finally {\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "    }\n" +
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" +  
        "};"
        );
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue4177428_continue
  public void testIssue4177428_continue() {
    test(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: do {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      continue a\n" +  
        "    }\n" +
        "  } while(false)\n" +
        "  alert(action)\n" + 
        "};",
        "f = function() {\n" +
        "  var action;\n" +
        "  a: do {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "    }\n" +
        "  } while (false)\n" +
        "  alert(action)\n" +
        "};"
        );
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue4177428_return
  public void testIssue4177428_return() {
    test(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      return\n" +  
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + 
        "};",
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "    }\n" +
        "  }\n" +
        "};"
        );
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue4177428_multifinally
  public void testIssue4177428_multifinally() {
    testSame(
        "a: {\n" +
        " try {\n" +
        " try {\n" +
        " } finally {\n" +
        "   break a;\n" +
        " }\n" +
        " } finally {\n" +
        "   x = 1;\n" +
        " }\n" +
        "}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testIssue5215541_deadVarDeclar
  public void testIssue5215541_deadVarDeclar() {
    testSame("throw 1; var x");
    testSame("throw 1; function x() {}");
    testSame("throw 1; var x; var y;");
    test("throw 1; var x = foo", "var x; throw 1");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testForInLoop
  public void testForInLoop() {
    testSame("for(var x in y) {}");
  }

// com.google.javascript.jscomp.VarCheckTest::testBreak
  public void testBreak() {
    testSame("a: while(1) break a;");
  }

// com.google.javascript.jscomp.VarCheckTest::testContinue
  public void testContinue() {
    testSame("a: while(1) continue a;");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarNotDefined
  public void testReferencedVarNotDefined() {
    test("x = 0;", null, VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarDefined1
  public void testReferencedVarDefined1() {
    testSame("var x, y; x=1;");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarDefined2
  public void testReferencedVarDefined2() {
    testSame("var x; function y() {x=1;}");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarsExternallyDefined
  public void testReferencedVarsExternallyDefined() {
    testSame("var x = window; alert(x);");
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars1
  public void testMultiplyDeclaredVars1() {
    test("var x = 1; var x = 2;", null,
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars2
  public void testMultiplyDeclaredVars2() {
    test("var y; try { y=1 } catch (x) {}" +
         "try { y=1 } catch (x) {}",
         "var y;try{y=1}catch(x){}try{y=1}catch(x){}");
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars3
  public void testMultiplyDeclaredVars3() {
    test("try { var x = 1; x *=2; } catch (x) {}", null,
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars4
  public void testMultiplyDeclaredVars4() {
    testSame("x;", "var x = 1; var x = 2;",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR, true);
  }

// com.google.javascript.jscomp.VarCheckTest::testVarReferenceInExterns
  public void testVarReferenceInExterns() {
    testSame("asdf;", "var asdf;",
        VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testCallInExterns
  public void testCallInExterns() {
    testSame("yz();", "function yz() {}",
        VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns1
  public void testPropReferenceInExterns1() {
    testSame("asdf.foo;", "var asdf;",
        VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns2
  public void testPropReferenceInExterns2() {
    testSame("asdf.foo;", "",
        VarCheck.UNDEFINED_VAR_ERROR, true);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns3
  public void testPropReferenceInExterns3() {
    testSame("asdf.foo;", "var asdf;",
        VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    externValidationErrorLevel = CheckLevel.ERROR;
    test(
        "asdf.foo;", "var asdf;", "",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR, null);

    externValidationErrorLevel = CheckLevel.OFF;
    test("asdf.foo;", "var asdf;", "var asdf;", null, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testVarInWithBlock
  public void testVarInWithBlock() {
    test("var a = {b:5}; with (a){b;}", null, VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testValidFunctionExpr
  public void testValidFunctionExpr() {
    testSame("(function() {});");
  }

// com.google.javascript.jscomp.VarCheckTest::testRecursiveFunction
  public void testRecursiveFunction() {
    testSame("(function a() { return a(); })();");
  }

// com.google.javascript.jscomp.VarCheckTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    testSame("var a = 3; (function a() { return a(); })();");
  }

// com.google.javascript.jscomp.VarCheckTest::testLegalVarReferenceBetweenModules
  public void testLegalVarReferenceBetweenModules() {
    testDependentModules("var x = 10;", "var y = x++;", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testMissingModuleDependencyDefault
  public void testMissingModuleDependencyDefault() {
    testIndependentModules("var x = 10;", "var y = x++;",
                           null, VarCheck.MISSING_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testViolatedModuleDependencyDefault
  public void testViolatedModuleDependencyDefault() {
    testDependentModules("var y = x++;", "var x = 10;",
                         VarCheck.VIOLATED_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMissingModuleDependencySkipNonStrict
  public void testMissingModuleDependencySkipNonStrict() {
    sanityCheck = true;
    testIndependentModules("var x = 10;", "var y = x++;",
                           null, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testViolatedModuleDependencySkipNonStrict
  public void testViolatedModuleDependencySkipNonStrict() {
    sanityCheck = true;
    testDependentModules("var y = x++;", "var x = 10;",
                         null);
  }

// com.google.javascript.jscomp.VarCheckTest::testMissingModuleDependencySkipNonStrictNotPromoted
  public void testMissingModuleDependencySkipNonStrictNotPromoted() {
    sanityCheck = true;
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testIndependentModules("var x = 10;", "var y = x++;", null, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testViolatedModuleDependencyNonStrictNotPromoted
  public void testViolatedModuleDependencyNonStrictNotPromoted() {
    sanityCheck = true;
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testDependentModules("var y = x++;", "var x = 10;", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testDependentStrictModuleDependencyCheck
  public void testDependentStrictModuleDependencyCheck() {
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testDependentModules("var f = function() {return new B();};",
        "var B = function() {}",
        VarCheck.STRICT_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testIndependentStrictModuleDependencyCheck
  public void testIndependentStrictModuleDependencyCheck() {
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testIndependentModules("var f = function() {return new B();};",
        "var B = function() {}",
        VarCheck.STRICT_MODULE_DEP_ERROR, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testStarStrictModuleDependencyCheck
  public void testStarStrictModuleDependencyCheck() {
    strictModuleDepErrorLevel = CheckLevel.WARNING;
    testSame(createModuleStar("function a() {}", "function b() { a(); c(); }",
        "function c() { a(); }"),
        VarCheck.STRICT_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testForwardVarReferenceInLocalScope1
  public void testForwardVarReferenceInLocalScope1() {
    testDependentModules("var x = 10; function a() {y++;}",
                         "var y = 11; a();", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testForwardVarReferenceInLocalScope2
  public void testForwardVarReferenceInLocalScope2() {
    
    
    testDependentModules("var x = 10; function a() {y++;} a();",
                         "var y = 11;", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testSimple
  public void testSimple() {
    checkSynthesizedExtern("x", "var x;");
    checkSynthesizedExtern("var x", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testSimpleSanityCheck
  public void testSimpleSanityCheck() {
    sanityCheck = true;
    try {
      checkSynthesizedExtern("x", "");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().indexOf("Unexpected variable x") != -1);
    }
  }

// com.google.javascript.jscomp.VarCheckTest::testParameter
  public void testParameter() {
    checkSynthesizedExtern("function f(x){}", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testLocalVar
  public void testLocalVar() {
    checkSynthesizedExtern("function f(){x}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testTwoLocalVars
  public void testTwoLocalVars() {
    checkSynthesizedExtern("function f(){x}function g() {x}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testInnerFunctionLocalVar
  public void testInnerFunctionLocalVar() {
    checkSynthesizedExtern("function f(){function g() {x}}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testNoCreateVarsForLabels
  public void testNoCreateVarsForLabels() {
    checkSynthesizedExtern("x:var y", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns1
  public void testVariableInNormalCodeUsedInExterns1() {
    checkSynthesizedExtern(
        "x.foo;", "var x;", "var x; x.foo;");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns2
  public void testVariableInNormalCodeUsedInExterns2() {
    checkSynthesizedExtern(
        "x;", "var x;", "var x; x;");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns3
  public void testVariableInNormalCodeUsedInExterns3() {
    checkSynthesizedExtern(
        "x.foo;", "function x() {}", "var x; x.foo; ");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns4
  public void testVariableInNormalCodeUsedInExterns4() {
    checkSynthesizedExtern(
        "x;", "function x() {}", "var x; x; ");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCode
  public void testCorrectCode() {
    assertNoWarning("function foo(d) { (function() { d.foo(); }); d.bar(); } ");
    assertNoWarning("function foo() { bar(); } function bar() { foo(); } ");
    assertNoWarning("function f(d) { d = 3; }");
    assertNoWarning(VARIABLE_RUN);
    assertNoWarning("function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectShadowing
  public void testCorrectShadowing() {
    assertNoWarning(VARIABLE_RUN + "function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRedeclare
  public void testCorrectRedeclare() {
    assertNoWarning(
        "function f() { if (1) { var a = 2; } else { var a = 3; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRecursion
  public void testCorrectRecursion() {
    assertNoWarning("function f() { var x = function() { x(); }; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCatch
  public void testCorrectCatch() {
    assertNoWarning("function f() { try { var x = 2; } catch (x) {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testRedeclare
  public void testRedeclare() {
    
    assertRedeclare("function f() { var a = 2; var a = 3; }");
    assertRedeclare("function f(a) { var a = 2; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testEarlyReference
  public void testEarlyReference() {
    assertUndeclared("function f() { a = 2; var a = 3; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectEarlyReference
  public void testCorrectEarlyReference() {
    assertNoWarning("var goog = goog || {}");
    assertNoWarning("function f() { a = 2; } var a = 2;");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testUnreferencedBleedingFunction
  public void testUnreferencedBleedingFunction() {
    assertNoWarning("var x = function y() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testReferencedBleedingFunction
  public void testReferencedBleedingFunction() {
    assertNoWarning("var x = function y() { return y(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration
  public void testDoubleDeclaration() {
    assertRedeclare("function x(y) { if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration2
  public void testDoubleDeclaration2() {
    assertRedeclare("function x() { var y; if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction1
  public void testHoistedFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("f(); function f() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction2
  public void testHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction
  public void testNonHoistedFunction() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (true) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction2
  public void testNonHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() {} f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction3
  public void testNonHoistedFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() {} f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction4
  public void testNonHoistedFunction4() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("if (false) { function f() {} }  f();");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction5
  public void testNonHoistedFunction5() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("function g() { if (false) { function f() {} }  f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction6
  public void testNonHoistedFunction6() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (false) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction7
  public void testNonHoistedFunction7() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("function g() { if (false) { f(); function f() {} }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction1
  public void testNonHoistedRecursiveFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() { f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction2
  public void testNonHoistedRecursiveFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction3
  public void testNonHoistedRecursiveFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); g(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns1
  public void testNoWarnInExterns1() {
    
    String externs =
       "var google;" +
       " var google";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns2
  public void testNoWarnInExterns2() {
    
    String externs =
       "window;" +
       "var window;";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testCapturedVariables
  public void testCapturedVariables() {
    String source =
        "global:var global;\n" +
        "function Outer() {\n" +
        "  captured:var captured;\n" +
        "  notcaptured:var notCaptured;\n" +
        "  function Inner() {\n" +
        "    alert(captured);" +
        "   }\n" +
        "}\n";

    analyze(source);

    assertIsCapturedLocal("captured");
    assertIsUncapturedLocal("notcaptured");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testGlobals
  public void testGlobals() {
    String source =
      "global:var global;";

    analyze(source);

    assertIsGlobal("global");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testParameters
  public void testParameters() {
    String source =
      "function A(a,b,c) {\n" +
      "}\n";

    analyze(source);

    assertIsParameter("a");
    assertIsParameter("b");
    assertIsParameter("c");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testFunctions
  public void testFunctions() {
    String source =
        "function global() {\n" +
        "  function inner() {\n" +
        "  }\n" +
        "  function innerCaptured() {\n" +
        "    (function(){innerCaptured()})()\n" +
        "  }\n" +
        "}\n";

    analyze(source);

    assertFunctionHasVisibility("global",
        VariableVisibility.GLOBAL);

    assertFunctionHasVisibility("inner",
        VariableVisibility.LOCAL);

    assertFunctionHasVisibility("innerCaptured",
        VariableVisibility.CAPTURED_LOCAL);
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testArray
  public void testArray() throws Exception {
    testConversion("[,]");
    testConversion("[]");
    testConversion("[function (x) {}]");
    testConversion("[[], [a, [], [[[]], 1], f([a])], 1];");
    testConversion("x = [1, 2, 3]");
    testConversion("var x = [1, 2, 3]");
    testConversion("[, 1, Object(), , , 2]");
    testConversion("[{x: 'abc', y: 1}]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testArray1
  public void testArray1() throws Exception {
    testConversion("[,]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testAssignOperators
  public void testAssignOperators() throws Exception {
    testConversion("x += 1, x -= 1, x *= 1, x /= 1, x %= 1");
    testConversion("x |= 1, x ^= x, x &= 0");
    testConversion("x <<= 1, x >>= 1, x >>>= 1");
    testConversion("y = x += 1");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testCalls
  public void testCalls() throws Exception {
    testConversion("f()");
    testConversion("f(1)");
    testConversion("f('a')");
    testConversion("f(true)");
    testConversion("f(null)");
    testConversion("f(undefined)");

    testConversion("f(a + b)");
    testConversion("f(g(h(a)) * h(g(u(z('a')))))");

    testConversion("x = f()");
    testConversion("x = f(1)");
    testConversion("x = f(a + b)");
    testConversion("x = f(g(h(a)) * h(g(u(z('a')))))");

    testConversion("String('a')");
    testConversion("Number(1)");
    testConversion("Boolean(0)");
    testConversion("Object()");
    testConversion("Array('a', 1, false, null, Object(), String('a'))");

    testConversion("(function() {})()");
    testConversion("(function(x) {})(x)");
    testConversion("(function(x) {var y = x << 1; return y})(x)");
    testConversion("(function(x) {y = x << 1; return y})(x)");
    testConversion("var x = (function(x) {y = x << 1; return y})(x)");
    testConversion("var x = (function(x) {return x << 1})(x)");

    testConversion("eval()");
    testConversion("eval('x')");
    testConversion("x = eval('x')");
    testConversion("var x = eval('x')");
    testConversion("eval(Template('foo${bar}baz')); var Template;");

    testConversion("a.x()");
    testConversion("a[x]()");
    testConversion("z = a.x()");
    testConversion("var z = a.x()");
    testConversion("z = a[x]()");
    testConversion("z = a['x']()");
    testConversion("var z = a[x]()");
    testConversion("var z = a['x']()");
    testConversion("a.x(y)");
    testConversion("a[x](y)");
    testConversion("a['x'](y)");
    testConversion("a[x](y, z, 'a', null, true, f(y))");
    testConversion("a['x'](y, z, 'a', null, true, f(y))");
    testConversion("a[b[c[d]]()].x");

    testConversion("(f())()");
    testConversion("(f(x))(y)");
    testConversion("(f = getFn())()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testConditionals
  public void testConditionals() throws Exception {
    testConversion("x ? y : z");
    testConversion("result = x ? y : z");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDecIncOperators
  public void testDecIncOperators() throws Exception {
    testConversion("x--");
    testConversion("--x");
    testConversion("x++");
    testConversion("++x");
    testConversion("var y=x++, z=++x; var s=y--, r=++y;");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDelete
  public void testDelete() throws Exception {
    testConversion("delete a");
    testConversion("delete a.x");
    testConversion("delete a[0]");
    testConversion("delete a.x[0]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDirectives
  public void testDirectives() throws Exception {
    testConversion("'use strict'");
    testConversion("function foo() {'use strict'}");
    testConversion("'use strict'; function foo() {'use strict'}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDoWhile
  public void testDoWhile() throws Exception {
  
     testConversion("do {} while (true)");
     testConversion("do {;} while (true)");
     testConversion("do {} while (f(x, y))");
     testConversion("do {} while (f(f(f(x, y))))");
     testConversion("do {} while ((f(f(f(x, y))))())");
     testConversion("do {2 + 3; q = 2 + 3; var v = y * z;"
         + "g = function(a) {true; var b = a + 1; return a * a}} while (--x)");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testFor
  public void testFor() throws Exception {
     testConversion("for (;true;) {;}");
     testJsonMLToAstConversion("for (i = 0; i < 10; ++i) x++");
     testConversion("for (i = 0; i < 10; ++i) {x++}");
     testConversion("for (i = 0; i < 10; ++i) {2 + 3; q = 2 + 3; "
         + "var v = y * z; g = function(a) {true; var b = a + 1;"
         + "return a * a}}");

     testConversion("for(;true;) {break}");
     testConversion("for(i = 0; i < 10; ++i) {if (i > 5) {break}}");
     testConversion("s: for(i = 0; i < 10; ++i) {if (i > 5) {break s}}");
     testConversion("for (i = 0;true; ++i) {"
         + "if (i % 2) {continue} else {var x = i / 3; f(x)}}");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testForIn
  public void testForIn() throws Exception {
    testConversion("for (var i in x) {}");
    testConversion("for (var i in x) {;}");
    testConversion("for (var i in x) {f(x)}");
    testConversion("s: for(var i in x) {if (i > 5) {break s}}");
    testConversion("for (var i in x) {if (i % 2) {"
        + "continue} else {var x = i / 3; f(x)}}");
    testConversion("for (var i in x) {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}");

    testConversion("for (i in x) {}");
    testConversion("for (i in x) {;}");
    testConversion("for (i in x) {f(x)}");
    testConversion("s: for (i in x) {if (i > 5) {break s}}");
    testConversion("for (i in x) {if (i % 2) {"
        + "continue} else {var x = i / 3; f(x)}}");
    testConversion("for (i in x) {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}");

  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testFunctions
  public void testFunctions() throws Exception {
    testConversion("(function () {})");
    testConversion("(function (x, y) {})");
    testConversion("(function () {})()");
    testConversion("(function (x, y) {})()");
    testConversion("[ function f() {} ]");
    testConversion("var f = function f() {};");
    testConversion("for (function f() {};true;) {}");
    testConversion("x = (function (x, y) {})");

    testConversion("function f() {}");
    testConversion("for (;true;) { function f() {} }");

    testConversion("function f() {;}");
    testConversion("function f() {x}");
    testConversion("function f() {x;y;z}");
    testConversion("function f() {{}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testIfElse1
  public void testIfElse1() throws Exception {
    testConversion("if (true) {x = 1}");
    testConversion("if (true) {x = 1} else {x = 2}");
    testConversion("if (f(f(f()))) {x = 1} else {x = 2}");
    testConversion("if ((f(f(f())))()) {x = 1} else {x = 2}");
    testConversion("if (true) {x = 1}; x = 1;");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testLabels
  public void testLabels() throws Exception {
    testConversion("s: ;");
    testConversion("s: {;}");
    testConversion("s: while(true) {;}");
    testConversion("s: switch (x) {case 'a': break s;}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testLogicalExpr
  public void testLogicalExpr() throws Exception {
    testConversion("a && b");
    testConversion("a || b");
    testConversion("a && b || c");
    testConversion("a && (b || c)");
    testConversion("f(x) && (function (x) {"
        + "return x % 2 == 0 })(z) || z % 3 == 0 ? true : false");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testMathExpr
  public void testMathExpr() throws Exception {
    testConversion("2 + 3 * 4");
    testConversion("(2 + 3) * 4");
    testConversion("2 * (3 + 4)");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testMember
  public void testMember() throws Exception {
    testConversion("o.x");
    testConversion("a.b.c");
    testConversion("a.b.c.d");
    testConversion("o[x]");
    testConversion("o[0]");
    testConversion("o[2 + 3 * 4]");
    testConversion("o[(function (x){var y = g(x) << 1; return y * x})()]");
    testConversion("o[o.x]");
    testConversion("o.x[x]");
    testConversion("a.b[o.x]");
    testConversion("a.b[1]");
    testConversion("a[b[c[d]]].x");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testNew
  public void testNew() throws Exception {
    testConversion("new A");
    testConversion("new A()");

    testConversion("new A(x, y, z)");
    testConversion("new A(f(x), g(y), h(z))");
    testConversion("new A(x, new B(x, y), z)");
    testConversion("new A(1), new B()");
    testConversion("new A, B");

    testConversion("x = new A(a)");
    testConversion("var x = new A(a, b)");
    testConversion("var x = new A(1), y = new B()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testObject0
  public void testObject0() throws Exception {
    
    
    
    
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testObject
  public void testObject() throws Exception {
    testConversion("x = {}");
    testConversion("var x = {}");
    testConversion("x = {x: 1, y: 2}");
    
    
    testConversion("x = {x: null}");
    testConversion("x = {a: function f() {}}");
    
    testConversion("x = {a: f()}");
    
    testConversion("x = {a: function f() {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}}");
    
    
    testConversion("x = {get a() {return 1}}");
    testConversion("x = {set a(b) {}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testOperators
  public void testOperators() throws Exception {
    testConversion("x instanceof Null");
    testConversion("!x instanceof A");
    testConversion("!(x instanceof A)");

    testConversion("'a' in x");
    testConversion("if('a' in x) {f(x)}");
    testConversion("undefined in A");
    testConversion("!(Number(1) in [2, 3, 4])");

    testConversion("true ? x : y");
    testConversion("(function() {var y = 2 + 3 * 4; return y >> 1})() ? x : y");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testReturnStatement
  public void testReturnStatement() throws Exception {
    testConversion("x = function f() {return}");
    testConversion("x = function f() {return 1}");
    testConversion("x = function f() {return 2 + 3 / 4}");
    testConversion("x = function f() {return function() {}}");
    testConversion("x = function f() {var y = 2; "
        + "return function() {return y * 3}}");
    testConversion("x = function f() {z = 2 + 3; "
        + "return (function(z) {return z * y})(z)}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testRegExp
  public void testRegExp() throws Exception {
    testConversion("/ab/");
    testConversion("/ab/g");
    testConversion("x = /ab/");
    testConversion("x = /ab/g");
    testConversion("var x = /ab/");
    testConversion("var x = /ab/g");
    testConversion("function f() {"
        + "/ab/; var x = /ab/; (function g() {/ab/; var x = /ab/})()}");
    testConversion("var f = function () {return /ab/g;}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testSimplePrograms
  public void testSimplePrograms() throws Exception {
    testConversion(";");
    testConversion("1");
    testConversion("x");
    testConversion("x=1");
    testConversion("{}");
    testConversion("{;}");
    testConversion("{x=1}");
    testConversion("x='a'");

    testConversion("true");
    testConversion("false");
    testConversion("x=true");
    testConversion("x=false");

    testConversion("undefined");
    testConversion("x=undefined");

    testConversion("null");
    testConversion("x = null");

    testConversion("this");
    testConversion("2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}");

    testConversion("a; b");
    testConversion("a; b; c; d");

    testConversion("x = function () {}");
    testConversion("x = function f() {}");

    testConversion("x = function (arg1, arg2) {}");
    testConversion("x = function f(arg1, arg2) {}");

    testConversion("x = function f(arg1, arg2) {1}");
    testConversion("x = function f(arg1, arg2) {x}");

    testConversion("x = function f(arg1, arg2) {x = 1 + 1}");

    testConversion("var re = new RegExp(document.a.b.c);"
        + "var m = re.exec(document.a.b.c);");

  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testSwitch
  public void testSwitch() throws Exception {
  testConversion("switch (x) {}");
  testConversion("switch (x) {case 'a':}");
  testConversion("switch (x) {case 'a':case 'b':}");
  testConversion("switch (x) {case 'a':case 'b': x}");
  testConversion("switch (x) {case 'a':case 'b': {;}}");
  testConversion("switch (x) {case 'a':case 'b': f()}");
  testConversion("switch (x) {case 'x': case 'y': {;} case 'a':case 'b': f()}");
  testConversion("switch (x) {case 'a': f(x)}");
  testConversion("switch (x) {case 'a': {f()} {g(x)}}");
  testConversion("switch (x) {case 'a': f(); g(x)}");
  testConversion("switch (x) {default: ;}");
  testConversion("switch (x) {default:case 'a': ;}");
  testConversion("switch (x) {case 'a':case'b':default: f()}");
  testConversion("switch (x) {default:f(x); g(); case 'a': ; case 'b': g(x)}");
  testConversion("switch (x) {case 'a': default: {f(x); g(z)} case 'b': g(x)}");
  testConversion("switch (x) {case x: {;}}");
}

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testType
  public void testType() throws Exception {
    testConversion("undefined");
    testConversion("null");

    testConversion("0");
    testConversion("+0");
    testConversion("0.0");

    testConversion("3.14");
    testConversion("+3.14");

    testConversion("true");
    testConversion("false");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testThis
  public void testThis() throws Exception {
    testConversion("this");
    testConversion("var x = this");
    testConversion("this.foo()");
    testConversion("var x = this.foo()");
    testConversion("this.bar");
    testConversion("var x = this.bar()");
    testConversion("switch(this) {}");
    testConversion("x + this");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testThrow
  public void testThrow() throws Exception {
    testConversion("throw e");
    testConversion("throw 2 + 3 * 4");
    testConversion("throw (function () {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}})()");
    testConversion("throw f(x)");
    testConversion("throw f(f(f(x)))");
    testConversion("throw (f(f(x), y))()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTry
  public void testTry() throws Exception {
    testConversion("try {} catch (e) {}");
    testConversion("try {;} catch (e) {;}");
    testConversion("try {var x = 0; y / x} catch (e) {f(e)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} catch (e) {f(x)}");

    testConversion("try {} finally {}");
    testConversion("try {;} finally {;}");
    testConversion("try {var x = 0; y / x} finally {f(y)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} finally {f(x)}");

    testConversion("try {} catch (e) {} finally {}");
    testConversion("try {;} catch (e) {;} finally {;}");
    testConversion("try {var x = 0; y / x} catch (e) {;} finally {;}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; h(q)} "
        + "catch (e) {f(x)} finally {f(x)}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTypeof
  public void testTypeof() throws Exception {
    testConversion("typeof undefined");
    testConversion("typeof null");
    testConversion("typeof 1");
    testConversion("typeof 'a'");
    testConversion("typeof false");

    testConversion("typeof Null()");
    testConversion("typeof Number(1)");
    testConversion("typeof String('a')");
    testConversion("typeof Boolean(0)");

    testConversion("typeof x");
    testConversion("typeof new A()");
    testConversion("typeof new A(x)");
    testConversion("typeof f(x)");
    testConversion("typeof (function() {})()");
    testConversion("typeof 2 + 3 * 4");

    testConversion("typeof typeof x");
    testConversion("typeof typeof typeof x");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testUnaryExpressions
  public void testUnaryExpressions() throws Exception {
    testConversion("!x");
    testConversion("!null");
    testConversion("!3.14");
    testConversion("!true");

    testConversion("~x");
    testConversion("~null");
    testConversion("~3.14");
    testConversion("~true");

    testConversion("+x");
    testConversion("+null");
    testConversion("+3.14");
    testConversion("+true");

    testConversion("-x");
    testConversion("-null");
    testConversion("-true");

    testConversion("!~+-z");
    testConversion("void x");
    testConversion("void null");
    testConversion("void void !x");
    testConversion("void (x + 1)");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testVarDeclarations
  public void testVarDeclarations() throws Exception {
    testConversion("var x");
    testConversion("var x = 1");
    testConversion("var x = 1 + 1");
    testConversion("var x = 'a' + 'b'");

    testConversion("var x, y, z");
    testConversion("var x = 2, y = 2 * x, z");

    testConversion("var x = function () {}");
    testConversion("var x = function f() {}");
    testConversion("var x = function f(arg1, arg2) {}");

    testConversion("var x = function f(arg1, arg2) {1}");
    testConversion("var x = function f(arg1, arg2) {x}");
    testConversion("var x = function f(arg1, arg2) {x = 2 * 3}");

    testConversion("var x = function f() {var x}");
    testConversion("var x = function f() {var y = (z + 2) * q}");

    testConversion("var x = function f(a, b) {"
        + "var y = function g(a, b) {z = a + b}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWhile
  public void testWhile() throws Exception {
     testConversion("while (true) {;}");
     testConversion("while (true) {f()}");
     testConversion("while (f(x, y)) {break;}");
     testConversion("while (f(f(f(x, y)))) {}");
     testConversion("while ((f(f(f(x, y))))()) {}");

     testConversion("while (x--) {2 + 3; q = 2 + 3; var v = y * z; "
         + "g = function(a) {true; var b = a + 1; return a * a}}");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWith
  public void testWith() throws Exception {
     testConversion("with ({}) {}");
     testConversion("with ({}) {;}");
     testConversion("with (x) {}");
     testConversion("with (x) {f(x)}");
     testConversion("with ({a: function f() {}}) {f(1)}");
     testConversion("with ({z: function f() {2 + 3; q = 2 + 3; var v = y * z;"
         + "g = function(a) {true; var b = a + 1; return a * a}}}) {f(1)}");
     testConversion("with (x in X) {x++}");
   }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testStrictScript
  public void testStrictScript() throws Exception {
    assertNull(newParse("").getDirectives());
    assertEquals(
        Sets.newHashSet("use strict"),
        newParse("'use strict'").getDirectives());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral2
  public void testArrayLiteral2() throws Exception {
    testNewParser("[a, , b]",
      "SCRIPT 1 [source_file: FileName.js] [length: 8]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 8]\n" +
      "        ARRAYLIT 1 [source_file: FileName.js] [length: 8]\n" +
      "            NAME a 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME b 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral4
  public void testArrayLiteral4() throws Exception {
    testNewParser("[,,,a,,b]",
      "SCRIPT 1 [source_file: FileName.js] [length: 9]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 9]\n" +
      "        ARRAYLIT 1 [source_file: FileName.js] [length: 9]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME a 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME b 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral
  public void testObjectLiteral() {
    newParse("var o = {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral2
  public void testObjectLiteral2() {
    newParse("var o = {a: 1}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral3
  public void testObjectLiteral3() {
    newParse("var o = {a: 1, b: 2}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral4
  public void testObjectLiteral4() {
    newParse("var o = {1: 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral5
  public void testObjectLiteral5() {
    newParse("var o = {'a': 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral6
  public void testObjectLiteral6() {
    testNewParser("({1: true})",
      "SCRIPT 1 [source_file: FileName.js] [length: 11]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 10]\n" +
      "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 9]\n" +
      "            STRING_KEY 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
      "                TRUE 1 [source_file: FileName.js] [length: 4]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral7
  public void testObjectLiteral7() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({get 1() {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 14]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 13]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 12]\n" +
        "            GETTER_DEF 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 6]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral8
  public void testObjectLiteral8() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({set 1(a) {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 15]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 14]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 13]\n" +
        "            SETTER_DEF 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 7]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                        NAME a 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel
  public void testLabel() {
    testNewParser("foo: bar",
        "SCRIPT 1 [source_file: FileName.js] [length: 8]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL_NAME foo 1 [source_file: FileName.js] [length: 4]\n" +
        "        EXPR_RESULT 1 [source_file: FileName.js] [length: 3]\n" +
        "            NAME bar 1 [source_file: FileName.js] [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel2
  public void testLabel2() {
    testNewParser("l: while (f()) { if (g()) { continue l; } }",
        "SCRIPT 1 [source_file: FileName.js] [length: 43]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 2]\n" +
        "        LABEL_NAME l 1 [source_file: FileName.js] [length: 2]\n" +
        "        WHILE 1 [source_file: FileName.js] [length: 40]\n" +
        "            CALL 1 [source_file: FileName.js] [length: 3]\n" +
        "                NAME f 1 [source_file: FileName.js] [length: 1]\n" +
        "            BLOCK 1 [source_file: FileName.js] [length: 28]\n" +
        "                IF 1 [source_file: FileName.js] [length: 24]\n" +
        "                    CALL 1 [source_file: FileName.js] [length: 3]\n" +
        "                        NAME g 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 15]\n" +
        "                        CONTINUE 1 [source_file: FileName.js] [length: 11]\n" +
        "                            LABEL_NAME l 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel3
  public void testLabel3() {
    testNewParser("Foo:Bar:X:{ break Bar; }",
        "SCRIPT 1 [source_file: FileName.js] [length: 24]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL_NAME Foo 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "            LABEL_NAME Bar 1 [source_file: FileName.js] [length: 4]\n" +
        "            LABEL 1 [source_file: FileName.js] [length: 2]\n" +
        "                LABEL_NAME X 1 [source_file: FileName.js] [length: 2]\n" +
        "                BLOCK 1 [source_file: FileName.js] [length: 14]\n" +
        "                    BREAK 1 [source_file: FileName.js] [length: 10]\n" +
        "                        LABEL_NAME Bar 1 [source_file: FileName.js] [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation1
  public void testNegation1() {
    testNewParser("-a",
        "SCRIPT 1 [source_file: FileName.js] [length: 2]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 2]\n" +
        "        NEG 1 [source_file: FileName.js] [length: 2]\n" +
        "            NAME a 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation2
  public void testNegation2() {
    testNewParser("-2",
        "SCRIPT 1 [source_file: FileName.js] [length: 2]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 2]\n" +
        "        NUMBER -2.0 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation3
  public void testNegation3() {
    testNewParser("1 - -2",
        "SCRIPT 1 [source_file: FileName.js] [length: 6]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 6]\n" +
        "        SUB 1 [source_file: FileName.js] [length: 6]\n" +
        "            NUMBER 1.0 1 [source_file: FileName.js] [length: 1]\n" +
        "            NUMBER -2.0 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testGetter
  public void testGetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({get a() {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 14]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 13]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 12]\n" +
        "            GETTER_DEF a 1 [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 6]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSetter
  public void testSetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({set a(x) {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 15]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 14]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 13]\n" +
        "            SETTER_DEF a 1 [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 7]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                        NAME x 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete1
  public void testDelete1() {
    testNoParseError("delete a.b;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete2
  public void testDelete2() {
    testNoParseError("delete a['b'];");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete3
  public void testDelete3() {
    
    
    testNoParseError("delete a;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete4
  public void testDelete4() {
    testParseError("delete 'x';",
        "Invalid delete operand. Only properties can be deleted.");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions1
  public void testCommentPositions1() {
    Node root = newParse("function a(x) {};" +
        "function b(x) {}");
    Node a = root.getFirstChild();
    Node b = root.getLastChild();
    assertMarkerPosition(a, 1, 4);
    assertMarkerPosition(b, 1, 45);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions2
  public void testCommentPositions2() {
    Node root = newParse(
        "\n" +
        "\n" +
        "function a(x) {};\n" +
        "\n" +
        "\n" +
        "\n" +
        "\n" +
        "function b(x) {};");
    assertMarkerPosition(root.getFirstChild(), 4, 4);
    assertMarkerPosition(root.getFirstChild().getNext().getNext(), 11, 6);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLiteralLocation
   public void testLiteralLocation() {
    Node root = newParse(
        "var d =\n" +
        "    \"foo\";\n" +
        "var e =\n" +
        "    1;\n" +
        "var f = \n" +
        "    1.2;\n" +
        "var g = \n" +
        "    2e5;\n" +
        "var h = \n" +
        "    'bar';\n");

    Node firstStmt = root.getFirstChild();
    Node firstLiteral = firstStmt.getFirstChild().getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node secondLiteral = secondStmt.getFirstChild().getFirstChild();
    Node thirdStmt = secondStmt.getNext();
    Node thirdLiteral = thirdStmt.getFirstChild().getFirstChild();
    Node fourthStmt = thirdStmt.getNext();
    Node fourthLiteral = fourthStmt.getFirstChild().getFirstChild();
    Node fifthStmt = fourthStmt.getNext();
    Node fifthLiteral = fifthStmt.getFirstChild().getFirstChild();

    assertNodePosition(2, 4, firstLiteral);
    assertNodePosition(4, 4, secondLiteral);
    assertNodePosition(6, 4, thirdLiteral);
    assertNodePosition(8, 4, fourthLiteral);
    assertNodePosition(10, 4, fifthLiteral);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitchLocation
  public void testSwitchLocation() {
    Node root = newParse(
        "switch (a) {\n" +
        "  
        "   case 1:\n" +
        "     b++;\n" +
        "   case 2:\n" +
        "   default:\n" +
        "     b--;\n" +
        "  }\n");

    Node switchStmt = root.getFirstChild();
    Node switchVar = switchStmt.getFirstChild();
    Node firstCase = switchVar.getNext();
    Node caseArg = firstCase.getFirstChild();
    Node caseBody = caseArg.getNext();
    Node caseExprStmt = caseBody.getFirstChild();
    Node incrExpr = caseExprStmt.getFirstChild();
    Node incrVar = incrExpr.getFirstChild();
    Node secondCase = firstCase.getNext();
    Node defaultCase = secondCase.getNext();

    assertNodePosition(1, 0, switchStmt);
    assertNodePosition(1, 8, switchVar);
    assertNodePosition(3, 3, firstCase);
    assertNodePosition(3, 8, caseArg);
    assertNodePosition(3, 3, caseBody);
    assertNodePosition(4, 5, caseExprStmt);
    assertNodePosition(4, 5, incrExpr);
    assertNodePosition(4, 5, incrVar);
    assertNodePosition(5, 3, secondCase);
    assertNodePosition(6, 3, defaultCase);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionParamLocation
  public void testFunctionParamLocation() {
    Node root = newParse(
        "function\n" +
        "     foo(a,\n" +
        "     b,\n" +
        "     c)\n" +
        "{}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node param1 = params.getFirstChild();
    Node param2 = param1.getNext();
    Node param3 = param2.getNext();
    Node body = params.getNext();

    assertNodePosition(1, 0, function);
    assertNodePosition(2, 5, functionName);
    
    
    
    assertNodePosition(2, 8, params);
    assertNodePosition(2, 9, param1);
    assertNodePosition(3, 5, param2);
    assertNodePosition(4, 5, param3);
    assertNodePosition(5, 0, body);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVarDeclLocation
  public void testVarDeclLocation() {
    Node root = newParse(
        "var\n" +
        "    a =\n" +
        "    3\n");
    Node varDecl = root.getFirstChild();
    Node varName = varDecl.getFirstChild();
    Node varExpr = varName.getFirstChild();

    assertNodePosition(1, 0, varDecl);
    assertNodePosition(2, 4, 1, varName);
    assertNodePosition(3, 4, 1, varExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturnLocation
  public void testReturnLocation() {
    Node root = newParse(
        "function\n" +
        "    foo(\n" +
        "    a,\n" +
        "    b,\n" +
        "    c) {\n" +
        "    return\n" +
        "    4;\n" +
        "}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node body = params.getNext();
    Node returnStmt = body.getFirstChild();
    Node exprStmt = returnStmt.getNext();
    Node returnVal = exprStmt.getFirstChild();

    assertNodePosition(6, 4, returnStmt);
    assertNodePosition(7, 4, exprStmt);
    assertNodePosition(7, 4, returnVal);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoFor
  public void testLinenoFor() {
    Node root = newParse(
        "for(\n" +
        ";\n" +
        ";\n" +
        ") {\n" +
        "}\n");

    Node forNode = root.getFirstChild();
    Node initClause= forNode.getFirstChild();
    Node condClause = initClause.getNext();
    Node incrClause = condClause.getNext();

    assertNodePosition(1, 0, forNode);
    assertNodePosition(2, 0, initClause);
    assertNodePosition(3, 0, condClause);
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBinaryExprLocation
  public void testBinaryExprLocation() {
    Node root = newParse(
        "var d = a\n" +
        "    + \n" +
        "    b;\n" +
        "var\n" +
        "    e =\n" +
        "    a +\n" +
        "    c;\n" +
        "var f = b\n" +
        "    / c;\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstVarAdd = firstVar.getFirstChild();

    Node secondVarDecl = firstVarDecl.getNext();
    Node secondVar = secondVarDecl.getFirstChild();
    Node secondVarAdd = secondVar.getFirstChild();

    Node thirdVarDecl = secondVarDecl.getNext();
    Node thirdVar = thirdVarDecl.getFirstChild();
    Node thirdVarAdd = thirdVar.getFirstChild();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, firstVar);
    assertNodePosition(1, 8, firstVarAdd);
    assertNodePosition(1, 8, firstVarAdd.getFirstChild());
    assertNodePosition(3, 4, firstVarAdd.getLastChild());

    assertNodePosition(4, 0, secondVarDecl);
    assertNodePosition(5, 4, secondVar);
    assertNodePosition(6, 4, secondVarAdd);
    assertNodePosition(6, 4, secondVarAdd.getFirstChild());
    assertNodePosition(7, 4, secondVarAdd.getLastChild());

    assertNodePosition(8, 0, thirdVarDecl);
    assertNodePosition(8, 4, thirdVar);
    assertNodePosition(8, 8, thirdVarAdd);
    assertNodePosition(8, 8, thirdVarAdd.getFirstChild());
    assertNodePosition(9, 6, thirdVarAdd.getLastChild());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPrefixLocation
  public void testPrefixLocation() {
    Node root = newParse(
         "a++;\n" +
         "--\n" +
         "b;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node firstOp = firstStmt.getFirstChild();
    Node secondOp = secondStmt.getFirstChild();

    assertNodePosition(1, 0, firstOp);
    assertNodePosition(2, 0, secondOp);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIfLocation
  public void testIfLocation() {
    Node root = newParse(
        "if\n" +
        "  (a == 3)\n" +
        "{\n" +
        "  b = 0;\n" +
        "}\n" +
        "  else\n" +
        "{\n" +
        "  c = 1;\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node eqClause = ifStmt.getFirstChild();
    Node thenClause = eqClause.getNext();
    Node elseClause = thenClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 3, eqClause);
    assertNodePosition(3, 0, thenClause);
    assertNodePosition(7, 0, elseClause);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryLocation
  public void testTryLocation() {
     Node root = newParse(
         "try {\n" +
         "  var x = 1;\n" +
         "} catch\n" +
         "   (err)\n" +
         "{\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchVarBlock = catchBlock.getFirstChild();
    Node catchVar = catchVarBlock.getFirstChild();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 2, catchVarBlock);
    assertNodePosition(4, 4, catchVar);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(6, 10, finallyBlock);
    assertNodePosition(7, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testHookLocation
  public void testHookLocation() {
    Node root = newParse(
        "a\n" +
        "?\n" +
        "b\n" +
        ":\n" +
        "c\n" +
        ";\n");

    Node hookExpr = root.getFirstChild().getFirstChild();
    Node condExpr = hookExpr.getFirstChild();
    Node thenExpr = condExpr.getNext();
    Node elseExpr = thenExpr.getNext();

    assertNodePosition(2, 0, hookExpr);
    assertNodePosition(1, 0, condExpr);
    assertNodePosition(3, 0, thenExpr);
    assertNodePosition(5, 0, elseExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabelLocation
  public void testLabelLocation() {
    Node root = newParse(
        "foo:\n" +
        "a = 1;\n" +
        "bar:\n" +
        "b = 2;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();

    assertNodePosition(1, 0, firstStmt);
    assertNodePosition(3, 0, secondStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCompareLocation
  public void testCompareLocation() {
    Node root = newParse(
        "a\n" +
        "<\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
   }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testEqualityLocation
  public void testEqualityLocation() {
    Node root = newParse(
        "a\n" +
        "==\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPlusEqLocation
  public void testPlusEqLocation() {
    Node root = newParse(
        "a\n" +
        "+=\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommaLocation
  public void testCommaLocation() {
    Node root = newParse(
        "a,\n" +
        "b,\n" +
        "c;\n");

    Node statement = root.getFirstChild();
    Node comma1 = statement.getFirstChild();
    Node comma2 = comma1.getFirstChild();
    Node cRef = comma2.getNext();
    Node aRef = comma2.getFirstChild();
    Node bRef = aRef.getNext();

    assertNodePosition(1, 0, comma2);
    assertNodePosition(1, 0, aRef);
    assertNodePosition(2, 0, bRef);
    assertNodePosition(3, 0, cRef);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexpLocation
  public void testRegexpLocation() {
    Node root = newParse(
        "var path =\n" +
        "replace(\n" +
        "/a/g," +
        "'/');\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node callNode = firstVar.getFirstChild();
    Node fnName = callNode.getFirstChild();
    Node regexObject = fnName.getNext();
    Node aString = regexObject.getFirstChild();
    Node endRegexString = regexObject.getNext();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, 4, firstVar);
    assertNodePosition(2, 0, 18, callNode);
    assertNodePosition(2, 0, 7, fnName);
    assertNodePosition(3, 0, regexObject);
    assertNodePosition(3, 0, aString);
    assertNodePosition(3, 5, endRegexString);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNestedOr
  public void testNestedOr() {
    Node root = newParse(
        "if (a && \n" +
        "    b() || \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node orClause = ifStmt.getFirstChild();
    Node andClause = orClause.getFirstChild();
    Node cName = andClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(1, 4, orClause);
    assertNodePosition(1, 4, andClause);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBitwiseOps
  public void testBitwiseOps() {
      Node root = newParse(
        "if (a & \n" +
        "    b() | \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node bitOr = ifStmt.getFirstChild();
    Node bitAnd = bitOr.getFirstChild();
    Node cName = bitAnd.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(1, 4, bitOr);
    assertNodePosition(1, 4, bitAnd);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLitLocation
  public void testObjectLitLocation() {
    Node root = newParse(
        "var foo =\n" +
        "{ \n" +
        "'A' : 'A', \n" +
        "'B' : 'B', \n" +
        "'C' :\n" +
        "    'C' \n" +
        "};\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstObjectLit = firstVar.getFirstChild();
    Node firstKey = firstObjectLit.getFirstChild();
    Node firstValue = firstKey.getFirstChild();

    Node secondKey = firstKey.getNext();
    Node secondValue = secondKey.getFirstChild();

    Node thirdKey = secondKey.getNext();
    Node thirdValue = thirdKey.getFirstChild();

    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 0, firstObjectLit);

    assertNodePosition(3, 0, firstKey);
    assertNodePosition(3, 6, firstValue);

    assertNodePosition(4, 0, secondKey);
    assertNodePosition(4, 6, secondValue);

    assertNodePosition(5, 0, thirdKey);
    assertNodePosition(6, 4, thirdValue);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutCatchLocation
  public void testTryWithoutCatchLocation() {
     Node root = newParse(
         "try {\n" +
         "  var x = 1;\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 10, finallyBlock);
    assertNodePosition(4, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutFinallyLocation
  public void testTryWithoutFinallyLocation() {
     Node root = newParse(
         "try {\n" +
         "  var x = 1;\n" +
         "} catch (ex) {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchStmt = catchBlock.getFirstChild();
    Node exceptionVar = catchStmt.getFirstChild();
    Node exceptionBlock = exceptionVar.getNext();
    Node varDecl = exceptionBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 2, catchStmt);
    assertNodePosition(3, 9, exceptionVar);
    assertNodePosition(3, 13, exceptionBlock);
    assertNodePosition(4, 2, varDecl);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineEqLocation
  public void testMultilineEqLocation() {
    Node  root = newParse(
        "if\n" +
        "    (((a == \n" +
        "  3) && \n" +
        "  (b == 2)) || \n" +
        " (c == 1)) {\n" +
        "}\n");
    Node ifStmt = root.getFirstChild();
    Node orTest = ifStmt.getFirstChild();
    Node andTest = orTest.getFirstChild();
    Node cTest = andTest.getNext();
    Node aTest = andTest.getFirstChild();
    Node bTest = aTest.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 7, orTest);
    assertNodePosition(2, 7, andTest);
    assertNodePosition(2, 7, aTest);
    assertNodePosition(4, 3, bTest);
    assertNodePosition(5, 2, cTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineBitTestLocation
  public void testMultilineBitTestLocation() {
    Node root = newParse(
        "if (\n" +
        "      ((a \n" +
        "        | 3 \n" +
        "       ) == \n" +
        "       (b \n" +
        "        & 2)) && \n" +
        "      ((a \n" +
        "         ^ 0xffff) \n" +
        "       != \n" +
        "       (c \n" +
        "        << 1))) {\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node andTest = ifStmt.getFirstChild();
    Node eqTest = andTest.getFirstChild();
    Node notEqTest = eqTest.getNext();

    Node bitOrTest = eqTest.getFirstChild();
    Node bitAndTest = bitOrTest.getNext();

    Node bitXorTest = notEqTest.getFirstChild();
    Node bitShiftTest = bitXorTest.getNext();

    assertNodePosition(1, 0, ifStmt);

    assertNodePosition(2, 8, eqTest);
    assertNodePosition(7, 8, notEqTest);

    assertNodePosition(2, 8, bitOrTest);
    assertNodePosition(5, 8, bitAndTest);
    assertNodePosition(7, 8, bitXorTest);
    assertNodePosition(10, 8, bitShiftTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCallLocation
  public void testCallLocation() {
    Node root = newParse(
        "a.\n" +
        "b.\n" +
        "cccc(1);\n");

    Node exprStmt = root.getFirstChild();
    Node functionCall = exprStmt.getFirstChild();
    Node functionProp = functionCall.getFirstChild();
    Node firstNameComponent = functionProp.getFirstChild();
    Node lastNameComponent = firstNameComponent.getNext();
    Node aNameComponent = firstNameComponent.getFirstChild();
    Node bNameComponent = aNameComponent.getNext();

    assertNodePosition(1, 0, 13, functionCall);
    assertNodePosition(1, 0, 10, functionProp);
    
    
    
    assertNodePosition(1, 0, 4, firstNameComponent);
    assertNodePosition(3, 0, 4, lastNameComponent);

    assertNodePosition(1, 0, 1, aNameComponent);
    assertNodePosition(2, 0, 1, bNameComponent);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoDeclaration
  public void testLinenoDeclaration() {
    Node root = newParse(
        "a.\n" +
        "b=\n" +
        "function() {};\n");

    Node exprStmt = root.getFirstChild();
    Node fnAssignment =  exprStmt.getFirstChild();
    Node aDotbName = fnAssignment.getFirstChild();
    Node aName = aDotbName.getFirstChild();
    Node bName = aName.getNext();
    Node fnNode = aDotbName.getNext();
    Node fnName = fnNode.getFirstChild();

    assertNodePosition(1, 0, fnAssignment);
    
    
    assertNodePosition(1, 0, aName);
    assertNodePosition(2, 0, bName);
    assertNodePosition(3, 0, fnNode);
    assertNodePosition(3, 8, fnName);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignmentValidation
  public void testAssignmentValidation() {
    testNoParseError("x=1");
    testNoParseError("x.y=1");
    testNoParseError("f().y=1");
    testParseError("(x||y)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()=1", INVALID_ASSIGNMENT_TARGET);

    testNoParseError("x+=1");
    testNoParseError("x.y+=1");
    testNoParseError("f().y+=1");
    testParseError("(x||y)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()+=1", INVALID_ASSIGNMENT_TARGET);

    testParseError("f()++", INVALID_INCREMENT_TARGET);
    testParseError("f()--", INVALID_DECREMENT_TARGET);
    testParseError("++f()", INVALID_INCREMENT_TARGET);
    testParseError("--f()", INVALID_DECREMENT_TARGET);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic1
  public void testParseTypeViaStatic1() throws Exception {
    Node typeNode = parseType("null");
    assertTypeEquals(NULL_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic2
  public void testParseTypeViaStatic2() throws Exception {
    Node typeNode = parseType("string");
    assertTypeEquals(STRING_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic3
  public void testParseTypeViaStatic3() throws Exception {
    Node typeNode = parseType("!Date");
    assertTypeEquals(DATE_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic4
  public void testParseTypeViaStatic4() throws Exception {
    Node typeNode = parseType("boolean|string");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, STRING_TYPE), typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic
  public void testParseInvalidTypeViaStatic() throws Exception {
    Node typeNode = parseType("sometype.<anothertype");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic2
  public void testParseInvalidTypeViaStatic2() throws Exception {
    Node typeNode = parseType("");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType1
  public void testParseNamedType1() throws Exception {
    assertNull(parse("@type null", "Unexpected end of file"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType2
  public void testParseNamedType2() throws Exception {
    JSDocInfo info = parse("@type null*/");
    assertTypeEquals(NULL_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType3
  public void testParseNamedType3() throws Exception {
    JSDocInfo info = parse("@type {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType4
  public void testParseNamedType4() throws Exception {
    
    JSDocInfo info = parse("@type \n {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType5
  public void testParseNamedType5() throws Exception {
    JSDocInfo info = parse("@type {!goog.\nBar}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType6
  public void testParseNamedType6() throws Exception {
    JSDocInfo info = parse("@type {!goog.\n * Bar.\n * Baz}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar.Baz", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError1
  public void testParseNamedTypeError1() throws Exception {
    
    
    parse("@type {!goog\n * .Bar} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError2
  public void testParseNamedTypeError2() throws Exception {
    parse("@type {!goog.\n * Bar\n * .Baz} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType1
  public void testTypedefType1() throws Exception {
    JSDocInfo info = parse("@typedef string */");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType2
  public void testTypedefType2() throws Exception {
    JSDocInfo info = parse("@typedef \n {string}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType3
  public void testTypedefType3() throws Exception {
    JSDocInfo info = parse("@typedef \n {(string|number)}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType1
  public void testParseStringType1() throws Exception {
    assertTypeEquals(STRING_TYPE, parse("@type {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType2
  public void testParseStringType2() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE, parse("@type {!String}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType1
  public void testParseBooleanType1() throws Exception {
    assertTypeEquals(BOOLEAN_TYPE, parse("@type {boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType2
  public void testParseBooleanType2() throws Exception {
    assertTypeEquals(
        BOOLEAN_OBJECT_TYPE, parse("@type {!Boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType1
  public void testParseNumberType1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@type {number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType2
  public void testParseNumberType2() throws Exception {
    assertTypeEquals(NUMBER_OBJECT_TYPE, parse("@type {!Number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType1
  public void testParseNullType1() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType2
  public void testParseNullType2() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {Null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType1
  public void testParseAllType1() throws Exception {
    testParseType("*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType2
  public void testParseAllType2() throws Exception {
    testParseType("*?", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseObjectType
  public void testParseObjectType() throws Exception {
    assertTypeEquals(OBJECT_TYPE, parse("@type {!Object}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDateType
  public void testParseDateType() throws Exception {
    assertTypeEquals(DATE_TYPE, parse("@type {!Date}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionType
  public void testParseFunctionType() throws Exception {
    assertTypeEquals(
        createNullableType(U2U_CONSTRUCTOR_TYPE),
        parse("@type {Function}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRegExpType
  public void testParseRegExpType() throws Exception {
    assertTypeEquals(REGEXP_TYPE, parse("@type {!RegExp}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseErrorTypes
  public void testParseErrorTypes() throws Exception {
    assertTypeEquals(ERROR_TYPE, parse("@type {!Error}*/").getType());
    assertTypeEquals(URI_ERROR_TYPE, parse("@type {!URIError}*/").getType());
    assertTypeEquals(EVAL_ERROR_TYPE, parse("@type {!EvalError}*/").getType());
    assertTypeEquals(REFERENCE_ERROR_TYPE,
        parse("@type {!ReferenceError}*/").getType());
    assertTypeEquals(TYPE_ERROR_TYPE, parse("@type {!TypeError}*/").getType());
    assertTypeEquals(
        RANGE_ERROR_TYPE, parse("@type {!RangeError}*/").getType());
    assertTypeEquals(
        SYNTAX_ERROR_TYPE, parse("@type {!SyntaxError}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType1
  public void testParseUndefinedType1() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType2
  public void testParseUndefinedType2() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {Undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType3
  public void testParseUndefinedType3() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {void}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType1
  public void testParseParametrizedType1() throws Exception {
    JSDocInfo info = parse("@type !Array.<number> */");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType2
  public void testParseParametrizedType2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType3
  public void testParseParametrizedType3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType4
  public void testParseParametrizedType4() throws Exception {
    JSDocInfo info = parse("@type {!Array.<(number|null)>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType5
  public void testParseParametrizedType5() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType6
  public void testParseParametrizedType6() throws Exception {
    JSDocInfo info = parse("@type {!Array.<!Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType7
  public void testParseParametrizedType7() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType8
  public void testParseParametrizedType8() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():!Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType9
  public void testParseParametrizedType9() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType10
  public void testParseParametrizedType10() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number|boolean>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType11
  public void testParseParamterizedType11() throws Exception {
    JSDocInfo info = parse("@type {!Object.<number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType12
  public void testParseParamterizedType12() throws Exception {
    JSDocInfo info = parse("@type {!Object.<string,number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
    assertIndexTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType1
  public void testParseUnionType1() throws Exception {
    JSDocInfo info = parse("@type {(boolean,null)}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType2
  public void testParseUnionType2() throws Exception {
    JSDocInfo info = parse("@type {boolean|null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType3
  public void testParseUnionType3() throws Exception {
    JSDocInfo info = parse("@type {boolean||null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType4
  public void testParseUnionType4() throws Exception {
    JSDocInfo info = parse("@type {(Array.<boolean>,null)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType5
  public void testParseUnionType5() throws Exception {
    JSDocInfo info = parse("@type {(null, Array.<boolean>)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType6
  public void testParseUnionType6() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>|null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType7
  public void testParseUnionType7() throws Exception {
    JSDocInfo info = parse("@type {null|Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType8
  public void testParseUnionType8() throws Exception {
    JSDocInfo info = parse("@type {null||Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType9
  public void testParseUnionType9() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>||null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType10
  public void testParseUnionType10() throws Exception {
    parse("@type {string|}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType11
  public void testParseUnionType11() throws Exception {
    parse("@type {(string,)}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType12
  public void testParseUnionType12() throws Exception {
    parse("@type {()}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType13
  public void testParseUnionType13() throws Exception {
    testParseType(
        "(function(this:Date),function(this:String):number)",
        "Function");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType14
  public void testParseUnionType14() throws Exception {
    testParseType(
        "(function(...[function(number):boolean]):number)|" +
        "function(this:String, string):number",
        "Function");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType15
  public void testParseUnionType15() throws Exception {
    testParseType("*|number", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType16
  public void testParseUnionType16() throws Exception {
    testParseType("number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType17
  public void testParseUnionType17() throws Exception {
    testParseType("string|number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType18
  public void testParseUnionType18() throws Exception {
    testParseType("(string,*,number)", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionTypeError1
  public void testParseUnionTypeError1() throws Exception {
    parse("@type {(string,|number)} */",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType1
  public void testParseUnknownType1() throws Exception {
    testParseType("?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType2
  public void testParseUnknownType2() throws Exception {
    testParseType("(?|number)", "?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType3
  public void testParseUnknownType3() throws Exception {
    testParseType("(number|?)", "?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType1
  public void testParseFunctionalType1() throws Exception {
    testParseType("function (): number");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType2
  public void testParseFunctionalType2() throws Exception {
    testParseType("function (number, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType3
  public void testParseFunctionalType3() throws Exception {
    testParseType(
        "function(this:Array)", "function (this:Array): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType4
  public void testParseFunctionalType4() throws Exception {
    testParseType("function (...[number]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType5
  public void testParseFunctionalType5() throws Exception {
    testParseType("function (number, ...[string]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType6
  public void testParseFunctionalType6() throws Exception {
    testParseType(
        "function (this:Date, number): (boolean|number|string)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType7
  public void testParseFunctionalType7() throws Exception {
    testParseType("function()", "function (): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType8
  public void testParseFunctionalType8() throws Exception {
    testParseType(
        "function(this:Array,...[boolean])",
        "function (this:Array, ...[boolean]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType9
  public void testParseFunctionalType9() throws Exception {
    testParseType(
        "function(this:Array,!Date,...[boolean?])",
        "function (this:Array, Date, ...[(boolean|null)]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType10
  public void testParseFunctionalType10() throws Exception {
    testParseType(
        "function(...[Object?]):boolean?",
        "function (...[(Object|null)]): (boolean|null)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType11
  public void testParseFunctionalType11() throws Exception {
    testParseType(
        "function(...[[number]]):[number?]",
        "function (...[Array]): Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType12
  public void testParseFunctionalType12() throws Exception {
    testParseType(
        "function(...)",
        "function (...[?]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType13
  public void testParseFunctionalType13() throws Exception {
    testParseType(
        "function(...): void",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType14
  public void testParseFunctionalType14() throws Exception {
    testParseType("function (*, string, number): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType15
  public void testParseFunctionalType15() throws Exception {
    testParseType("function (?, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType16
  public void testParseFunctionalType16() throws Exception {
    testParseType("function (string, ?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType17
  public void testParseFunctionalType17() throws Exception {
    testParseType("(function (?): ?|number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType18
  public void testParseFunctionalType18() throws Exception {
    testParseType("function (?): (?|number)", "function (?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType19
  public void testParseFunctionalType19() throws Exception {
    testParseType(
        "function(...[?]): void",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testStructuralConstructor
  public void testStructuralConstructor() throws Exception {
    JSType type = testParseType(
        "function (new:Object)", "function (new:Object): ?");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNominalConstructor
  public void testNominalConstructor() throws Exception {
    ObjectType type = testParseType("Array", "(Array|null)").dereference();
    assertTrue(type.getConstructor().isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug1419535
  public void testBug1419535() throws Exception {
    parse("@type {function(Object, string, *)?} */");
    parse("@type {function(Object, string, *)|null} */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIssue477
  public void testIssue477() throws Exception {
    parse("@type function */",
        "Bad type annotation. missing opening (");
  }

// buggy code
  private void findDeclaredNames(Node n, Node parent, Renamer renamer) {
    // Do a shallow traversal, so don't traverse into function declarations,
    // except for the name of the function itself.
    if (parent == null
        || parent.getType() != Token.FUNCTION
        || n == parent.getFirstChild()) {
      if (NodeUtil.isVarDeclaration(n)) {
        renamer.addDeclaredName(n.getString());
      } else if (NodeUtil.isFunctionDeclaration(n)) {
        Node nameNode = n.getFirstChild();
        renamer.addDeclaredName(nameNode.getString());
      }

      for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
        findDeclaredNames(c, n, renamer);
      }
    }
  }

    private static String getOrginalNameInternal(String name, int index) {
      return name.substring(0, index);
    }

    private static String getNameSuffix(String name, int index) {
      return name.substring(
          index + ContextualRenamer.UNIQUE_ID_SEPARATOR.length(),
          name.length());
    }

    public void visit(NodeTraversal t, Node node, Node parent) {
      if (node.getType() == Token.NAME) {
        String oldName = node.getString();
        if (containsSeparator(oldName)) {
          Scope scope = t.getScope();
          Var var = t.getScope().getVar(oldName);
          if (var == null || var.isGlobal()) {
        return;
      }

          if (nameMap.containsKey(var)) {
            node.setString(nameMap.get(var));
          } else {
            int index = indexOfSeparator(oldName);
            String newName = getOrginalNameInternal(oldName, index);
            String suffix = getNameSuffix(oldName, index);

      // Merge any names that were referenced but not declared in the current
      // scope.
      // If there isn't anything left in the stack we will be going into the
      // global scope: don't try to build a set of referenced names for the
      // global scope.
            boolean recurseScopes = false;
            if (!suffix.matches("\\d+")) {
              recurseScopes = true;
            }

    /**
     * For the Var declared in the current scope determine if it is possible
     * to revert the name to its orginal form without conflicting with other
     * values.
     */
        // Check if the new name is valid and if it would cause conflicts.
            if (var.scope.isDeclared(newName, recurseScopes) ||
                !TokenStream.isJSIdentifier(newName)) {
              newName = oldName;
            } else {
              var.scope.declare(newName, var.nameNode, null, null);
          // Adding a reference to the new name to prevent either the parent
          // scopes or the current scope renaming another var to this new name.
              Node parentNode = var.getParentNode();
              if (parentNode.getType() == Token.FUNCTION &&
                  parentNode == var.scope.getRootNode()) {
                var.getNameNode().setString(newName);
              }
              node.setString(newName);
          compiler.reportCodeChange();
        }

            nameMap.put(var, newName);

      }

        // Add all referenced names to the set so it is possible to check for
        // conflicts.
        // Store only references to candidate names in the node map.
        }
      }
    }

    public void addDeclaredName(String name) {
        if (global) {
          reserveName(name);
        } else {
          // It hasn't been declared locally yet, so increment the count.
          if (!declarations.containsKey(name)) {
            int id = incrementNameCount(name);
            String newName = null;
            if (id != 0) {
              newName = getUniqueName(name, id);
          }
          declarations.put(name, newName);
        }
      }
    }

    public void addDeclaredName(String name) {
      if (!declarations.containsKey(name)) {
        declarations.put(name, getUniqueName(name));
      }
    }

  static boolean isSwitchCase(Node n) {
    return n.getType() == Token.CASE || n.getType() == Token.DEFAULT;
  }

  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root,
        new NormalizeStatements(compiler, assertOnChange));
    removeDuplicateDeclarations(root);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    // It is important that removeDuplicateDeclarations runs after
    // MakeDeclaredNamesUnique in order for catch block exception names to be
    // handled properly. Specifically, catch block exception names are
    // only valid within the catch block, but our currect Scope logic
    // has no concept of this and includes it in the containing function 
    // (or global scope). MakeDeclaredNamesUnique makes the catch exception
    // names unique so that removeDuplicateDeclarations() will properly handle
    // cases where a function scope variable conflict with a exception name:
    //   function f() {
    //      try {throw 0;} catch(e) {e; /* catch scope 'e'*/}
    //      var e = 1; // f scope 'e'
    //   }
    // otherwise 'var e = 1' would be rewritten as 'e = 1'.
    // TODO(johnlenz): Introduce a seperate scope for catch nodes. 
    new PropogateConstantAnnotations(compiler, assertOnChange)
        .process(externs, root);
  }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testBang3
  public void testBang3() throws Exception {
    testTypes("\n" +
        "function f(x) { return  (x); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang4
  public void testBang4() throws Exception {
    testTypes("\n" +
        "function f(x, y) {\n" +
        "if (typeof x != 'undefined') { return x == y; }\n" +
        "else { return x != y; }\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang5
  public void testBang5() throws Exception {
    testTypes("\n" +
        "function f(x, y) { return !!x && x == y; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang6
  public void testBang6() throws Exception {
    testTypes("\n" +
        "function f(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang7
  public void testBang7() throws Exception {
    testTypes("function f(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDefinePropertyOnNullableObject1
  public void testDefinePropertyOnNullableObject1() throws Exception {
    testTypes(" var n = {};\n" +
        " n.x = 1;\n" +
        "function f() { return n.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDefinePropertyOnNullableObject2
  public void testDefinePropertyOnNullableObject2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "function f(t) {\n" +
        "t.x = 1; return t.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownConstructorInstanceType1
  public void testUnknownConstructorInstanceType1() throws Exception {
    testTypes(" function g(f) { return new f(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownConstructorInstanceType2
  public void testUnknownConstructorInstanceType2() throws Exception {
    testTypes("function g(f) { return  new f(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownConstructorInstanceType3
  public void testUnknownConstructorInstanceType3() throws Exception {
    testTypes("function g(f) { var x = new f(); x.a = 1; return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownPrototypeChain
  public void testUnknownPrototypeChain() throws Exception {
    testTypes("\n" +
              "function inst(co) {\n" +
              " \n" +
              " var c = function() {};\n" +
              " c.prototype = co.prototype;\n" +
              " return new c;\n" +
              "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNamespacedConstructor
  public void testNamespacedConstructor() throws Exception {
    Node root = parseAndTypeCheck(
        "var goog = {};" +
        " goog.MyClass = function() {};" +
        " " +
        "function foo() { return new goog.MyClass(); }");

    JSType typeOfFoo = root.getLastChild().getJSType();
    assert(typeOfFoo instanceof FunctionType);

    JSType retType = ((FunctionType) typeOfFoo).getReturnType();
    assert(retType instanceof ObjectType);
    assertEquals("goog.MyClass", ((ObjectType) retType).getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testComplexNamespace
  public void testComplexNamespace() throws Exception {
    String js =
      "var goog = {};" +
      "goog.foo = {};" +
      "goog.foo.bar = 5;";

    TypeCheckResult p = parseAndTypeCheckWithScope(js);

    
    JSType googScopeType = p.scope.getVar("goog").getType();
    assertTrue(googScopeType instanceof ObjectType);
    assertTrue("foo property not present on goog type",
        ((ObjectType) googScopeType).hasProperty("foo"));
    assertFalse("bar property present on goog type",
        ((ObjectType) googScopeType).hasProperty("bar"));

    
    Node varNode = p.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    JSType googNodeType = varNode.getFirstChild().getJSType();
    assertTrue(googNodeType instanceof ObjectType);

    
    assertTrue(googScopeType == googNodeType);

    
    Node getpropFoo1 = varNode.getNext().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo1.getType());
    assertEquals("goog", getpropFoo1.getFirstChild().getString());
    JSType googGetpropFoo1Type = getpropFoo1.getFirstChild().getJSType();
    assertTrue(googGetpropFoo1Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo1Type == googScopeType);

    
    JSType googFooType = ((ObjectType) googScopeType).getPropertyType("foo");
    assertTrue(googFooType instanceof ObjectType);

    
    
    Node getpropFoo2 = varNode.getNext().getNext()
        .getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo2.getType());
    assertEquals("goog", getpropFoo2.getFirstChild().getString());
    JSType googGetpropFoo2Type = getpropFoo2.getFirstChild().getJSType();
    assertTrue(googGetpropFoo2Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo2Type == googScopeType);

    
    
    JSType googFooGetprop2Type = getpropFoo2.getJSType();
    assertTrue("goog.foo incorrectly annotated in goog.foo.bar selection",
        googFooGetprop2Type instanceof ObjectType);
    ObjectType googFooGetprop2ObjectType = (ObjectType) googFooGetprop2Type;
    assertFalse("foo property present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("foo"));
    assertTrue("bar property not present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("bar"));
    assertEquals("bar property on goog.foo type incorrectly inferred",
        NUMBER_TYPE, googFooGetprop2ObjectType.getPropertyType("bar"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomSimpleNamespace
  public void testAddingMethodsUsingPrototypeIdiomSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype.m1 = 5");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 1,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace1
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace1()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace2
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace2()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace
  public void testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 2,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testDontAddMethodsIfNoConstructor
  public void testDontAddMethodsIfNoConstructor()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    JSType functionAType = js1Node.getFirstChild().getJSType();
    assertEquals("function (): ?", functionAType.toString());
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m1"));
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m2"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionAssignement
  public void testFunctionAssignement() throws Exception {
    testTypes("" +
        "function MSG_CALENDAR_ACCESS_ERROR(ph0, ph1) {return ''}" +
        "" +
        "var MSG_CALENDAR_ADD_ERROR = MSG_CALENDAR_ACCESS_ERROR;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals("A", instanceType.toString());
    assertEquals(NATIVE_PROPERTIES_COUNT + 3,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m3", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypePropertyTypes
  public void testPrototypePropertyTypes() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {\n" +
        "   this.m1;\n" +
        "   this.m2 = {};\n" +
        "   this.m3;\n" +
        "}\n" +
        " A.prototype.m4;\n" +
        " A.prototype.m5 = 0;\n" +
        " A.prototype.m6;\n");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 6,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", STRING_TYPE);
    checkObjectType(instanceType, "m2",
        createUnionType(OBJECT_TYPE, NULL_TYPE));
    checkObjectType(instanceType, "m3", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m4", STRING_TYPE);
    checkObjectType(instanceType, "m5", NUMBER_TYPE);
    checkObjectType(instanceType, "m6", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueTypeBuiltInPrototypePropertyType
  public void testValueTypeBuiltInPrototypePropertyType() throws Exception {
    Node node = parseAndTypeCheck("\"x\".charAt(0)");
    assertEquals(STRING_TYPE, node.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeclareBuiltInConstructor
  public void testDeclareBuiltInConstructor() throws Exception {
    
    
    Node node = parseAndTypeCheck(
        " var String = function(opt_str) {};\n" +
        "(new String(\"x\")).charAt(0)");
    assertEquals(STRING_TYPE, node.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendBuiltInType1
  public void testExtendBuiltInType1() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n1 = parseAndTypeCheck(externs + "(new String(\"x\")).substr(0,1);");
    assertEquals(STRING_TYPE, n1.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendBuiltInType2
  public void testExtendBuiltInType2() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n2 = parseAndTypeCheck(externs + "\"x\".substr(0,1);");
    assertEquals(STRING_TYPE, n2.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendFunction1
  public void testExtendFunction1() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(new Function()).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendFunction2
  public void testExtendFunction2() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(function() {}).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck1
  public void testInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck2
  public void testInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo not defined on any superclass of Sub");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck3
  public void testInheritanceCheck3() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck4
  public void testInheritanceCheck4() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck5
  public void testInheritanceCheck5() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Root; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck6
  public void testInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck7
  public void testInheritanceCheck7() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;",
        "property foo already defined on superclass goog.Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck8
  public void testInheritanceCheck8() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_1
  public void testInheritanceCheck9_1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_2
  public void testInheritanceCheck9_2() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_3
  public void testInheritanceCheck9_3() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_1
  public void testInheritanceCheck10_1() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_2
  public void testInheritanceCheck10_2() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_3
  public void testInheritanceCheck10_3() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Root\n" +
        "original: function (this:Root): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck11
  public void testInterfaceInheritanceCheck11() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super, number): ?\n" +
        "override: function (this:Sub, string): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck12
  public void testInheritanceCheck12() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = \"some string\";",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass goog.Super\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck13
  public void testInheritanceCheck13() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Parse error. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck14
  public void testInheritanceCheck14() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "\n" +
        "goog.Super = function() {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Parse error. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck1
  public void testInterfaceInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on interface Super; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck2
  public void testInterfaceInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck3
  public void testInterfaceInheritanceCheck3() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() { return 1;};",
        "property foo already defined on interface Root; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck4
  public void testInterfaceInheritanceCheck4() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1;};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck5
  public void testInterfaceInheritanceCheck5() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck6
  public void testInterfaceInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Root\n" +
        "original: function (this:Root): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck7
  public void testInterfaceInheritanceCheck7() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super, number): ?\n" +
        "override: function (this:Sub, string): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck8
  public void testInterfaceInheritanceCheck8() throws Exception {
    testTypes(
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        new String[] {
          "Parse error. Unknown type Super",
          "property foo not defined on any superclass of Sub"
        });
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented
  public void testInterfacePropertyNotImplemented() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented2
  public void testInterfacePropertyNotImplemented2() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubConstructorImplementingInterface
  public void testStubConstructorImplementingInterface() throws Exception {
    
    
    testTypes(" function Int() {}\n" +
        "Int.prototype.foo = function() {};" +
        " var Foo;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteral
  public void testObjectLiteral() throws Exception {
    Node n = parseAndTypeCheck("var a = {m1: 7, m2: 'hello'}");

    Node nameNode = n.getFirstChild().getFirstChild();
    Node objectNode = nameNode.getFirstChild();

    
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals(Token.OBJECTLIT, objectNode.getType());

    
    ObjectType objectType =
        (ObjectType) objectNode.getJSType();
    assertEquals(NUMBER_TYPE, objectType.getPropertyType("m1"));
    assertEquals(STRING_TYPE, objectType.getPropertyType("m2"));

    
    assertEquals(objectType, nameNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration1
  public void testObjectLiteralDeclaration1() throws Exception {
    testTypes(
        "var x = {" +
        " abc: true," +
        " 'def': 0," +
        " 3: 'fgh'" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallDateConstructorAsFunction
  public void testCallDateConstructorAsFunction() throws Exception {
    
    
    Node n = parseAndTypeCheck("Date()");
    assertEquals(STRING_TYPE, n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallErrorConstructorAsFunction
  public void testCallErrorConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Error('x')");
    assertEquals(ERROR_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallArrayConstructorAsFunction
  public void testCallArrayConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Array()");
    assertEquals(ARRAY_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyTypeOfUnionType
  public void testPropertyTypeOfUnionType() throws Exception {
    testTypes("var a = {};" +
        " a.N = function() {};\n" +
        "a.N.prototype.p = 1;\n" +
        " a.S = function() {};\n" +
        "a.S.prototype.p = 'a';\n" +
        "\n" +
        "var f = function(x) { return x.p; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface1
  public void testAnnotatedPropertyOnInterface1() throws Exception {
    
    
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface2
  public void testAnnotatedPropertyOnInterface2() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface3
  public void testAnnotatedPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        " T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface4
  public void testAnnotatedPropertyOnInterface4() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function T() {};\n" +
        " T.prototype.f = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface5
  public void testWarnUnannotatedPropertyOnInterface5() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface6
  public void testWarnUnannotatedPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface3
  public void testWarnDataPropertyOnInterface3() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = 1;",
        "interface members can only be plain functions" +
        " or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface4
  public void testWarnDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1;",
        "interface members can only be plain functions" +
        " or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface4
  public void testErrorMismatchingPropertyOnInterface4() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x =\n" +
        "function() {};",
        "parameter foo does not appear in u.T.prototype.x's parameter list");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface5
  public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): ?\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface6
  public void testErrorMismatchingPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1",
        "interface members can only be plain functions or goog.abstractMethod"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceNonEmptyFunction
  public void testInterfaceNonEmptyFunction() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { return 'foo'; }",
        "interface member functions must have an empty body"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testDoubleNestedInterface
  public void testDoubleNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.I3 = function() {};\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticDataPropertyOnNestedInterface
  public void testStaticDataPropertyOnNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.x = 1;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInstantiation
  public void testInterfaceInstantiation() throws Exception {
    testTypes("var f; new f",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypeLoop
  public void testPrototypeLoop() throws Exception {
    testTypes(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        "Parse error. Cycle detected in inheritance chain of type T");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssign
  public void testDirectPrototypeAssign() throws Exception {
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " Bar.prototype = new Foo()",
        "assignment to property prototype of Bar\n" +
        "found   : Foo\n" +
        "required: (Array|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry1
  public void testResolutionViaRegistry1() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry2
  public void testResolutionViaRegistry2() throws Exception {
    testTypes(
        " u.T = function() {" +
        "  this.a = 0; };\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry3
  public void testResolutionViaRegistry3() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a = 0;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry4
  public void testResolutionViaRegistry4() throws Exception {
    testTypes(" u.A = function() {};\n" +
        "\nu.A.A = function() {}\n;" +
        "\nu.A.B = function() {};\n" +
        "var ab = new u.A.B();\n" +
        " var a = ab;\n" +
        " var aa = ab;\n",
        "initializing variable\n" +
        "found   : u.A.B\n" +
        "required: u.A.A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry5
  public void testResolutionViaRegistry5() throws Exception {
    Node n = parseAndTypeCheck(" u.T = function() {}; u.T");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof FunctionType);
    assertEquals("u.T",
        ((FunctionType) type).getInstanceType().getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation1
  public void testGatherProperyWithoutAnnotation1() throws Exception {
    Node n = parseAndTypeCheck(" var T = function() {};" +
        "var t; t.x; t;");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    assertEquals(
        Sets.newHashSet(objectType),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation2
  public void testGatherProperyWithoutAnnotation2() throws Exception {
    TypeCheckResult ns =
        parseAndTypeCheckWithScope("var t; t.x; t;");
    Node n = ns.root;
    Scope s = ns.scope;
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertEquals(type, OBJECT_TYPE);
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    assertEquals(
        Sets.newHashSet(OBJECT_TYPE),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionMasksVariableBug
  public void testFunctionMasksVariableBug() throws Exception {
    testTypes("var x = 4; var f = function x(b) { return b ? 1 : x(true); };",
        "function x masks variable (IE bug)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa1
  public void testDfa1() throws Exception {
    testTypes("var x = null;\n x = 1;\n  var y = x;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa2
  public void testDfa2() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\nvar x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa3
  public void testDfa3() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\n" +
        " var x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa4
  public void testDfa4() throws Exception {
    testTypes(" function f(d) {\n" +
        "if (!d) { return; }\n" +
        " var e = d;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa5
  public void testDfa5() throws Exception {
    testTypes(" function u() {return 'a';}\n" +
        " function f(x) {\n" +
        "while (!x) { x = u(); }\nreturn x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa6
  public void testDfa6() throws Exception {
    testTypes(" function u() {return {};}\n" +
        " function f(x) {\n" +
        "while (x) { x = u(); if (!x) { x = u(); } }\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa7
  public void testDfa7() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = null;\n" +
        " function f(t) {\n" +
        "if (!t.x) { return; }\n" +
        " var e = t.x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa8
  public void testDfa8() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = '';\n" +
        "function u() {}\n" +
        " function f(t) {\n" +
        "if (u()) { t.x = 1; } else { t.x = 2; } return t.x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa9
  public void testDfa9() throws Exception {
    testTypes("function f() {\nvar x;\nx = null;\n" +
        "if (x == null) { return 0; } else { return 1; } }",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa10
  public void testDfa10() throws Exception {
    testTypes(" function g(x) {}" +
        "function f(x) {\n" +
        "if (!x) { x = ''; }\n" +
        "if (g(x)) { return 0; } else { return 1; } }",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa11
  public void testDfa11() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { if (!opt_x) { " +
        "throw new Error('x cannot be empty'); } return opt_x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa12
  public void testDfa12() throws Exception {
    testTypes("" +
        "var Bar = function(x) {};" +
        " function g(x) {}" +
        " " +
        "function f(opt_x) { " +
        "  if (opt_x) { new Bar(g(opt_x) && 'x'); }" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa13
  public void testDfa13() throws Exception {
    testTypes(
        "" +
        "function g(x, y, z) {}" +
        "function f() { " +
        "  var x = 'a'; g(x, x = 3, x);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast1
  public void testTypeInferenceWithCast1() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast2
  public void testTypeInferenceWithCast2() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y; y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast3
  public void testTypeInferenceWithCast3() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x));}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast4
  public void testTypeInferenceWithCast4() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x)) && 1;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast5
  public void testTypeInferenceWithCast5() throws Exception {
    testTypes(
        " function foo(x) {}" +
        " function bar(y) {" +
        "   y.length;" +
        "  foo(y.length);" +
        "}",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithClosure1
  public void testTypeInferenceWithClosure1() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x == null;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithClosure2
  public void testTypeInferenceWithClosure2() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x === 3;" +
        "}",
        "condition always evaluates to the same value\n" +
        "left : (null|string)\n" +
        "right: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardPropertyReference
  public void testForwardPropertyReference() throws Exception {
    testTypes(" var Foo = function() { this.init(); };" +
        "" +
        "Foo.prototype.getString = function() {" +
        "  return this.number_;" +
        "};" +
        "Foo.prototype.init = function() {" +
        "  " +
        "  this.number_ = 3;" +
        "};",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoForwardTypeDeclaration
  public void testNoForwardTypeDeclaration() throws Exception {
    testTypes(
        " function f(x) {}",
        "Parse error. Unknown type MyType");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoForwardTypeDeclarationAndNoBraces
  public void testNoForwardTypeDeclarationAndNoBraces() throws Exception {
    
    
    testTypes(" function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration1
  public void testForwardTypeDeclaration1() throws Exception {
    testClosureTypes(
        
        "goog.addDependency();" +
        "goog.addDependency('y', [goog]);" +

        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return x; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration2
  public void testForwardTypeDeclaration2() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }" +
        "f(3);", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration3
  public void testForwardTypeDeclaration3() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { return x; }" +
        " var MyType = function() {};" +
        "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMalformedOldTypeDef
  public void testMalformedOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        "goog.Bar = goog.typedef",
        "Typedef for goog.Bar does not have any type information");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMalformedOldTypeDef2
  public void testMalformedOldTypeDef2() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef",
        "Typedef for goog.Bar does not have any type information");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateOldTypeDef
  public void testDuplicateOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = function() {};" +
        " goog.Bar = goog.typedef",
        "variable goog.Bar redefined with type number, " +
        "original definition at  [testcode] :1 " +
        "with type function (this:goog.Bar): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOldTypeDef1
  public void testOldTypeDef1() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef;" +
        " function f(x) {}" +
        "f(3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOldTypeDef2
  public void testOldTypeDef2() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOldTypeDef3
  public void testOldTypeDef3() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " var Bar = goog.typedef;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCircularOldTypeDef
  public void testCircularOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef;" +
        " function f(x) {}" +
        "f(3); f([3]); f([[3]]);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateTypeDef
  public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar = function() {};" +
        " goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at  [testcode] :1 " +
        "with type function (this:goog.Bar): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef1
  public void testTypeDef1() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef2
  public void testTypeDef2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef3
  public void testTypeDef3() throws Exception {
    testTypes(
        "var goog = {};" +
        " var Bar;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCircularTypeDef
  public void testCircularTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3); f([3]); f([[3]]);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent1
  public void testGetTypedPercent1() throws Exception {
    String js = "var id = function(x) { return x; }\n" +
                "var id2 = function(x) { return id(x); }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent2
  public void testGetTypedPercent2() throws Exception {
    String js = "var x = {}; x.y = 1;";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent3
  public void testGetTypedPercent3() throws Exception {
    String js = "var f = function(x) { x.a = x.b; }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent4
  public void testGetTypedPercent4() throws Exception {
    String js = "var n = {};\n  n.T = function() {};\n" +
        " var x = new n.T();";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypePropertyReference
  public void testPrototypePropertyReference() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(""
        + "\n"
        + "function Foo() {}\n"
        + "\n"
        + "Foo.prototype.bar = function(a){};\n"
        + "\n"
        + "function baz(f) {\n"
        + "  Foo.prototype.bar.call(f, 3);\n"
        + "}");
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());

    assertTrue(p.scope.getVar("Foo").getType() instanceof FunctionType);
    FunctionType fooType = (FunctionType) p.scope.getVar("Foo").getType();
    assertEquals("function (this:Foo, number): ?",
                 fooType.getPrototype().getPropertyType("bar").toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolvingNamedTypes
  public void testResolvingNamedTypes() throws Exception {
    String js = ""
        + "\n"
        + "var Foo = function() {}\n"
        + "\n"
        + "Foo.prototype.foo = function(a) {\n"
        + "  return this.baz().toString();\n"
        + "};\n"
        + "\n"
        + "Foo.prototype.baz = function() { return new Baz(); };\n"
        + "\n"
        + "var Bar = function() {};"
        + "\n"
        + "var Baz = function() {};";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty1
  public void testMissingProperty1() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty2
  public void testMissingProperty2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.b = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty3
  public void testMissingProperty3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).a = 3;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty4
  public void testMissingProperty4() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).b = 3;",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty5
  public void testMissingProperty5() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " function Bar() { this.a = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty6
  public void testMissingProperty6() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " " +
        "function Bar() { this.a = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty7
  public void testMissingProperty7() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return obj.impossible; }",
        "Property impossible never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty8
  public void testMissingProperty8() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return typeof obj.impossible; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty9
  public void testMissingProperty9() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { if (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty10
  public void testMissingProperty10() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { while (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty11
  public void testMissingProperty11() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { for (;obj.impossible;) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty12
  public void testMissingProperty12() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { do { } while (obj.impossible); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty13
  public void testMissingProperty13() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isDef(obj.impossible); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty14
  public void testMissingProperty14() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isNull(obj.impossible); }",
        "Property isNull never defined on goog");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty15
  public void testMissingProperty15() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo) { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty16
  public void testMissingProperty16() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo(); if (x.foo) {} }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty17
  public void testMissingProperty17() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (typeof x.foo == 'function') { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty18
  public void testMissingProperty18() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo instanceof Function) { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty19
  public void testMissingProperty19() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.bar) { if (x.foo) {} } else { x.foo(); } }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty20
  public void testMissingProperty20() throws Exception {
    
    
    
    
    
    
    
    
    testTypes(
        "" +
        "function f(x) { if (x.foo) { } else { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty21
  public void testMissingProperty21() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo && x.foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty22
  public void testMissingProperty22() throws Exception {
    testTypes(
        "" +
        "function f(x) { return x.foo ? x.foo() : true; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty23
  public void testMissingProperty23() throws Exception {
    testTypes(
        "function f(x) { x.impossible(); }",
        "Property impossible never defined on x");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty24
  public void testMissingProperty24() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) { x.impossible(); }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty25
  public void testMissingProperty25() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "Foo.prototype.bar = function() {};" +
        " var FooAlias = Foo;" +
        "(new FooAlias()).bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty26
  public void testMissingProperty26() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        "FooAlias.prototype.bar = function() {};" +
        "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty27
  public void testMissingProperty27() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) {" +
        "  for (var parent = x; parent; parent = parent.getParent()) {}" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty28
  public void testMissingProperty28() throws Exception {
    testTypes(
        "function f(obj) {" +
        "   obj.foo;" +
        "  return obj.foo;" +
        "}");
    testTypes(
        "function f(obj) {" +
        "   obj.foo;" +
        "  return obj.foox;" +
        "}",
        "Property foox never defined on obj");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty29
  public void testMissingProperty29() throws Exception {
    
    testTypes(
        
        " var Foo;" +
        "Foo.prototype.opera;" +
        "Foo.prototype.opera.postError;",
        "",
        null,
        false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeclaredNativeTypeEquality
  public void testDeclaredNativeTypeEquality() throws Exception {
    Node n = parseAndTypeCheck(" function Object() {};");
    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE),
                 n.getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedVar
  public void testUndefinedVar() throws Exception {
    Node n = parseAndTypeCheck("var undefined;");
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug1
  public void testFlowScopeBug1() throws Exception {
    Node n = parseAndTypeCheck("\n"
        + "function f(a, b) {\n"
        + ""
        + "var i = 0;"
        + "for (; (i + a) < b; ++i) {}}");

    
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        n.getFirstChild().getLastChild().getLastChild().getFirstChild()
        .getNext().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug2
  public void testFlowScopeBug2() throws Exception {
    Node n = parseAndTypeCheck(" function Foo() {};\n"
        + "Foo.prototype.hi = false;"
        + "function foo(a, b) {\n"
        + "  "
        + "  var arr;"
        + "  "
        + "  var iter;"
        + "  for (iter = 0; iter < arr.length; ++ iter) {"
        + "    "
        + "    var afoo = arr[iter];"
        + "    afoo;"
        + "  }"
        + "}");

    
    assertEquals(registry.createNullableType(registry.getType("Foo")),
        n.getLastChild().getLastChild().getLastChild().getLastChild()
        .getLastChild().getLastChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddSingletonGetter
  public void testAddSingletonGetter() {
    Node n = parseAndTypeCheck(
        " function Foo() {};\n" +
        "goog.addSingletonGetter(Foo);");
    ObjectType o = (ObjectType) n.getFirstChild().getJSType();
    assertEquals("function (): Foo",
        o.getPropertyType("getInstance").toString());
    assertEquals("Foo", o.getPropertyType("instance_").toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckStandaloneAST
  public void testTypeCheckStandaloneAST() throws Exception {
    Node n = compiler.parseTestCode("function Foo() { }");
    typeCheck(n);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope topScope = scopeCreator.createScope(n, null);

    Node second = compiler.parseTestCode("new Foo");

    Node externs = new Node(Token.BLOCK);
    Node externAndJsRoot = new Node(Token.BLOCK, externs, second);
    externAndJsRoot.setIsSyntheticBlock(true);

    new TypeCheck(
        compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), registry),
        registry, topScope, scopeCreator, CheckLevel.WARNING, CheckLevel.OFF)
        .process(null, second);

    assertEquals(1, compiler.getWarningCount());
    assertEquals("cannot instantiate non-constructor",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType1
  public void testBadTemplateType1() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y, z) {}\n" +
        "f(this, this, function() {});",
        FunctionTypeBuilder.TEMPLATE_TYPE_DUPLICATED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType2
  public void testBadTemplateType2() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y) {}\n" +
        "f(0, function() {});",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType3
  public void testBadTemplateType3() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {}\n" +
        "f(this);",
        TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType4
  public void testBadTemplateType4() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType5
  public void testBadTemplateType5() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssumption
  public void testAssumption() {
    assuming("x", NUMBER_TYPE);
    inFunction("");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testVar
  public void testVar() {
    inFunction("var x = 1;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEmptyVar
  public void testEmptyVar() {
    inFunction("var x;");
    verify("x", VOID_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignment
  public void testAssignment() {
    assuming("x", OBJECT_TYPE);
    inFunction("x = 1;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetProp
  public void testGetProp() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x.y();");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetElemDereference
  public void testGetElemDereference() {
    assuming("x", createUndefinableType(OBJECT_TYPE));
    inFunction("x['z'] = 3;");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf1
  public void testIf1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {}; if (x) { y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf2
  public void testIf2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = x; if (x) { y = x; } else { y = {}; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf3
  public void testIf3() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 1; if (x) { y = x; }");
    verify("y", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testReturn1
  public void testReturn1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("if (x) { return x; }\nx = {};\nreturn x;");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testReturn2
  public void testReturn2() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("if (!x) { x = 0; }\nreturn x;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testWhile1
  public void testWhile1() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("while (!x) { if (x == null) { x = 0; } else { x = 1; } }");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testWhile2
  public void testWhile2() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("while (!x) { x = {}; }");
    verifySubtypeOf("x", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testDo
  public void testDo() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("do { x = 1; } while (!x);");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor1
  public void testFor1() {
    assuming("y", NUMBER_TYPE);
    inFunction("var x = null; var i = null; for (i=y; !i; i=1) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor2
  public void testFor2() {
    assuming("y", OBJECT_TYPE);
    inFunction("var x = null; var i = null; for (i in y) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", createNullableType(STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor3
  public void testFor3() {
    assuming("y", OBJECT_TYPE);
    inFunction("var x = null; var i = null; for (var i in y) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", createNullableType(STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor4
  public void testFor4() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {};\n"  +
        "if (x) { for (var i = 0; i < 10; i++) { break; } y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch1
  public void testSwitch1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; switch(x) {\n" +
        "case 1: y = 1; break;\n" +
        "case 2: y = {};\n" +
        "case 3: y = {};\n" +
        "default: y = 0;}");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch2
  public void testSwitch2() {
    assuming("x", ALL_TYPE);
    inFunction("var y = null; switch (typeof x) {\n" +
        "case 'string':\n" +
        "  y = x;\n" +
        "  return;" +
        "default:\n" +
        "  y = 'a';\n" +
        "}");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch3
  public void testSwitch3() {
    assuming("x",
        createNullableType(createUnionType(NUMBER_TYPE, STRING_TYPE)));
    inFunction("var y; var z; switch (typeof x) {\n" +
        "case 'string':\n" +
        "  y = 1; z = null;\n" +
        "  return;\n" +
        "case 'number':\n" +
        "  y = x; z = null;\n" +
        "  return;" +
        "default:\n" +
        "  y = 1; z = x;\n" +
        "}");
    verify("y", NUMBER_TYPE);
    verify("z", NULL_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch4
  public void testSwitch4() {
    assuming("x", ALL_TYPE);
    inFunction("var y = null; switch (typeof x) {\n" +
        "case 'string':\n" +
        "case 'number':\n" +
        "  y = x;\n" +
        "  return;\n" +
        "default:\n" +
        "  y = 1;\n" +
        "}\n");
    verify("y", createUnionType(NUMBER_TYPE, STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCall1
  public void testCall1() {
    assuming("x",
        createNullableType(
            new FunctionType(registry, "x", null, null,
                registry.getNativeType(NUMBER_TYPE), null)));
    inFunction("var y = x();");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testNew1
  public void testNew1() {
    assuming("x",
        createNullableType(
            registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE)));
    inFunction("var y = new x();");
    verify("y", JSTypeNative.NO_OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInnerFunction1
  public void testInnerFunction1() {
    inFunction("var x = 1; function f() { x = null; };");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInnerFunction2
  public void testInnerFunction2() {
    inFunction("var x = 1; var f = function() { x = null; };");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testHook
  public void testHook() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = x ? x : {};");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testThrow
  public void testThrow() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("var y = 1;\n" +
        "if (x == null) { throw new Error('x is null') }\n" +
        "y = x;");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry1
  public void testTry1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; try { y = null; } finally { y = x; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry2
  public void testTry2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null;\n" +
        "try {  } catch (e) { y = null; } finally { y = x; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry3
  public void testTry3() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; try { y = x; } catch (e) { }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCatch1
  public void testCatch1() {
    inFunction("var y = null; try { foo(); } catch (e) { y = e; }");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCatch2
  public void testCatch2() {
    inFunction("var y = null; var e = 3; try { foo(); } catch (e) { y = e; }");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnknownType1
  public void testUnknownType1() {
    inFunction("var y = 3; y = x;");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnknownType2
  public void testUnknownType2() {
    assuming("x", ARRAY_TYPE);
    inFunction("var y = 5; y = x[0];");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInfiniteLoop1
  public void testInfiniteLoop1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x = {}; while(x != null) { x = {}; }");
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInfiniteLoop2
  public void testInfiniteLoop2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x = {}; do { x = null; } while (x == null);");
  }

// com.google.javascript.jscomp.TypeInferenceTest::testJoin1
  public void testJoin1() {
    JSType unknownOrNull = createUnionType(NULL_TYPE, UNKNOWN_TYPE);
    assuming("x", BOOLEAN_TYPE);
    assuming("unknownOrNull", unknownOrNull);
    inFunction("var y; if (x) y = unknownOrNull; else y = null;");
    verify("y", unknownOrNull);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testJoin2
  public void testJoin2() {
    JSType unknownOrNull = createUnionType(NULL_TYPE, UNKNOWN_TYPE);
    assuming("x", BOOLEAN_TYPE);
    assuming("unknownOrNull", unknownOrNull);
    inFunction("var y; if (x) y = null; else y = unknownOrNull;");
    verify("y", unknownOrNull);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testArrayLit
  public void testArrayLit() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 3; if (x) { x = [y = x]; }");
    verify("x", createUnionType(NULL_TYPE, ARRAY_TYPE));
    verify("y", createUnionType(NUMBER_TYPE, OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetElem
  public void testGetElem() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 3; if (x) { x = x[y = x]; }");
    verify("x", UNKNOWN_TYPE);
    verify("y", createUnionType(NUMBER_TYPE, OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI1
  public void testEnumRAI1() {
    JSType enumType = createEnumType("MyEnum", ARRAY_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (x) y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI2
  public void testEnumRAI2() {
    JSType enumType = createEnumType("MyEnum", NUMBER_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (typeof x == 'number') y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI3
  public void testEnumRAI3() {
    JSType enumType = createEnumType("MyEnum", NUMBER_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (x && typeof x == 'number') y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI4
  public void testEnumRAI4() {
    JSType enumType = createEnumType("MyEnum",
        createUnionType(STRING_TYPE, NUMBER_TYPE)).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (typeof x == 'number') y = x;");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingAnd
  public void testShortCircuitingAnd() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; if (x && (y = 3)) { }");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingAnd2
  public void testShortCircuitingAnd2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; var z = 4; if (x && (y = 3)) { z = y; }");
    verify("z", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingOr
  public void testShortCircuitingOr() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; if (x || (y = 3)) { }");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingOr2
  public void testShortCircuitingOr2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; var z = 4; if (x || (y = 3)) { z = y; }");
    verify("z", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignInCondition
  public void testAssignInCondition() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("var y; if (!(y = x)) { y = 3; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf1
  public void testInstanceOf1() {
    assuming("x", OBJECT_TYPE);
    assuming("String", STRING_OBJECT_FUNCTION_TYPE);
    inFunction("var y = null; if (x instanceof String) y = x;");
    verify("y", createNullableType(STRING_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf2
  public void testInstanceOf2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    assuming("String", STRING_OBJECT_FUNCTION_TYPE);
    inFunction("var y = 1; if (x instanceof String) y = x;");
    verify("y", createUnionType(STRING_OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf3
  public void testInstanceOf3() {
    assuming("x", createUnionType(STRING_OBJECT_TYPE, NUMBER_OBJECT_TYPE));
    assuming("String", STRING_OBJECT_FUNCTION_TYPE);
    inFunction("var y = null; if (x instanceof String) y = x;");
    verify("y", createNullableType(STRING_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf4
  public void testInstanceOf4() {
    assuming("x", createUnionType(STRING_OBJECT_TYPE, NUMBER_OBJECT_TYPE));
    assuming("String", STRING_OBJECT_FUNCTION_TYPE);
    inFunction("var y = null; if (x instanceof String); else y = x;");
    verify("y", createNullableType(NUMBER_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf5
  public void testInstanceOf5() {
    assuming("x", OBJECT_TYPE);
    assuming("String", STRING_OBJECT_FUNCTION_TYPE);
    inFunction("var y = null; if (x instanceof String); else y = x;");
    verify("y", createNullableType(OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFlattening
  public void testFlattening() {
    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      assuming("s" + i, ALL_TYPE);
    }
    assuming("b", JSTypeNative.BOOLEAN_TYPE);
    StringBuilder body = new StringBuilder();
    body.append("if (b) {");
    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      body.append("s");
      body.append(i);
      body.append(" = 1;\n");
    }
    body.append(" } else { ");
    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      body.append("s");
      body.append(i);
      body.append(" = 'ONE';\n");
    }
    body.append("}");
    JSType numberORString = createUnionType(NUMBER_TYPE, STRING_TYPE);
    inFunction(body.toString());

    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      verify("s" + i, numberORString);
    }
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnary
  public void testUnary() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = +x;");
    verify("y", NUMBER_TYPE);
    inFunction("var z = -x;");
    verify("z", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAdd1
  public void testAdd1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = x + 5;");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAdd2
  public void testAdd2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = x + '5';");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAdd3
  public void testAdd3() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = '5' + x;");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignAdd
  public void testAssignAdd() {
    assuming("x", NUMBER_TYPE);
    inFunction("x += '5';");
    verify("x", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testComparison
  public void testComparison() {
    inFunction("var x = 'foo'; var y = (x = 3) < 4;");
    verify("x", NUMBER_TYPE);
    inFunction("var x = 'foo'; var y = (x = 3) > 4;");
    verify("x", NUMBER_TYPE);
    inFunction("var x = 'foo'; var y = (x = 3) <= 4;");
    verify("x", NUMBER_TYPE);
    inFunction("var x = 'foo'; var y = (x = 3) >= 4;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testThrownExpression
  public void testThrownExpression() {
    inFunction("var x = 'foo'; "
               + "try { throw new Error(x = 3); } catch (ex) {}");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeValidatorTest::testBasicMismatch
  public void testBasicMismatch() throws Exception {
    testSame(" function f(x) {} f('a');",
        TYPE_MISMATCH_WARNING);
    assertMismatches(Lists.newArrayList(fromNatives(STRING_TYPE, NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testFunctionMismatch
  public void testFunctionMismatch() throws Exception {
    testSame(
        " function f(x) { return x; }",
        TYPE_MISMATCH_WARNING);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType string = registry.getNativeType(STRING_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);
    JSType number = registry.getNativeType(NUMBER_TYPE);
    JSType firstFunction = registry.createFunctionType(number, string);
    JSType secondFunction = registry.createFunctionType(string, bool);

    assertMismatches(
        Lists.newArrayList(
            new TypeMismatch(firstFunction, secondFunction),
            fromNatives(STRING_TYPE, BOOLEAN_TYPE),
            fromNatives(NUMBER_TYPE, STRING_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testFunctionMismatch2
  public void testFunctionMismatch2() throws Exception {
    testSame(
        " function f(x) { return x; }",
        TYPE_MISMATCH_WARNING);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType string = registry.getNativeType(STRING_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);
    JSType number = registry.getNativeType(NUMBER_TYPE);
    JSType firstFunction = registry.createFunctionType(number, string);
    JSType secondFunction = registry.createFunctionType(number, bool);

    assertMismatches(
        Lists.newArrayList(
            new TypeMismatch(firstFunction, secondFunction),
            fromNatives(STRING_TYPE, BOOLEAN_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testNullUndefined
  public void testNullUndefined() {
    testSame(" function f(x) {}\n" +
             "f( ('a'));",
             TYPE_MISMATCH_WARNING);
    assertMismatches(Collections.<TypeMismatch>emptyList());
  }

// com.google.javascript.jscomp.TypeValidatorTest::testSubclass
  public void testSubclass() {
    testSame("\n"  +
             "function Super() {}\n" +
             "\n" +
             "function Sub() {}\n" +
             " function f(x) {}\n" +
             "f( (new Sub));",
             TYPE_MISMATCH_WARNING);
    assertMismatches(Collections.<TypeMismatch>emptyList());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubProperty
  public void testStubProperty() {
    testSame("function Foo() {}; Foo.bar;");
    ObjectType foo = (ObjectType) globalScope.getVar("Foo").getType();
    assertFalse(foo.hasProperty("bar"));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        foo.getPropertyType("bar"));
    assertEquals(Sets.newHashSet(foo), registry.getTypesWithProperty("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorProperty
  public void testConstructorProperty() {
    testSame("var foo = {};  foo.Bar = function() {};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("function (this:foo.Bar): ?", fooBar.toString());
    assertEquals(Sets.newHashSet(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumProperty
  public void testEnumProperty() {
    testSame("var foo = {};  foo.Bar = {XXX: 'xxx'};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));
    assertTrue(foo.isPropertyTypeDeclared("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("enum{foo.Bar}", fooBar.toString());
    assertEquals(Sets.newHashSet(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty
  public void testInferredProperty() {
    testSame("var foo = {}; foo.Bar = 3;");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPrototypeInit
  public void testPrototypeInit() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype = {bar: 1}; var foo = new Foo();");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("bar"));
    assertEquals("number", foo.getPropertyType("bar").toString());
    assertTrue(foo.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredPrototypeProperty
  public void testInferredPrototypeProperty() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype.bar = 1; var x = new Foo();");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.hasProperty("bar"));
    assertEquals("number", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnum
  public void testEnum() {
    testSame(" var Foo = {BAR: 1}; var f = Foo;");
    ObjectType f = (ObjectType) findNameType("f", globalScope);
    assertTrue(f.hasProperty("BAR"));
    assertEquals("Foo.<number>", f.getPropertyType("BAR").toString());
    assertTrue(f instanceof EnumType);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedEnum
  public void testNamespacedEnum() {
    testSame("var goog = {}; goog.ui = {};" +
        "goog.ui.Zippy = function() {};" +
        "goog.ui.Zippy.EventType = { TOGGLE: 'toggle' };" +
        "var x = goog.ui.Zippy.EventType;" +
        "var y = goog.ui.Zippy.EventType.TOGGLE;");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.isEnumType());
    assertTrue(x.hasProperty("TOGGLE"));
    assertEquals("enum{goog.ui.Zippy.EventType}", x.getReferenceName());

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertTrue(y.isSubtype(getNativeType(STRING_TYPE)));
    assertTrue(y.isEnumElementType());
    assertEquals("goog.ui.Zippy.EventType", y.getReferenceName());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumAlias
  public void testEnumAlias() {
    testSame(" var Foo = {BAR: 1}; " +
        " var FooAlias = Foo; var f = FooAlias;");

    assertEquals("Foo.<number>",
        registry.getType("FooAlias").toString());
    assertEquals(registry.getType("FooAlias"),
        registry.getType("Foo"));

    ObjectType f = (ObjectType) findNameType("f", globalScope);
    assertTrue(f.hasProperty("BAR"));
    assertEquals("Foo.<number>", f.getPropertyType("BAR").toString());
    assertTrue(f instanceof EnumType);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacesEnumAlias
  public void testNamespacesEnumAlias() {
    testSame("var goog = {};  goog.Foo = {BAR: 1}; " +
        " goog.FooAlias = goog.Foo;");

    assertEquals("goog.Foo.<number>",
        registry.getType("goog.FooAlias").toString());
    assertEquals(registry.getType("goog.Foo"),
        registry.getType("goog.FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedFunctionStub
  public void testCollectedFunctionStub() {
    testSame(
        " function f() { " +
        "   this.foo;" +
        "}" +
        "var x = new f();");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("function (this:f): number",
        x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedFunctionStub
  public void testNamespacedFunctionStub() {
    testSame(
        "var goog = {};" +
        " goog.foo;");

    ObjectType goog = (ObjectType) findNameType("goog", globalScope);
    assertTrue(goog.hasProperty("foo"));
    assertEquals("function (number): ?",
        goog.getPropertyType("foo").toString());
    assertTrue(goog.isPropertyTypeDeclared("foo"));

    assertEquals(globalScope.getVar("goog.foo").getType(),
        goog.getPropertyType("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedCtorProperty
  public void testCollectedCtorProperty() {
    testSame(
        " function f() { " +
        "   this.foo = 3;" +
        "}" +
        "var x = new f();");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("number", x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyOnUnknownSuperClass
  public void testPropertyOnUnknownSuperClass() {
    testSame(
        "var goog = this.foo();" +
        "" +
        "function Foo() {}" +
        "Foo.prototype.bar = 1;" +
        "var x = new Foo();",
        RhinoErrorReporter.PARSE_ERROR);
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Foo", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("bar"));
    assertEquals("?", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testMethodBeforeFunction
  public void testMethodBeforeFunction() throws Exception {
    testSame(
        "var y = Window.prototype;" +
        "Window.prototype.alert = function(message) {};" +
        " function Window() {}\n" +
        "var window = new Window(); \n" +
        "var x = window;");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Window", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("alert"));
    assertEquals("function (this:Window, ?): ?",
        x.getPropertyType("alert").toString());
    assertTrue(x.isPropertyTypeDeclared("alert"));

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertEquals("function (this:Window, ?): ?",
        y.getPropertyType("alert").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    testSame(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';" +
        "var x = new A();");

    ObjectType instanceType = (ObjectType) findNameType("x", globalScope);
    assertEquals(
        getNativeObjectType(OBJECT_TYPE).getPropertiesCount() + 3,
        instanceType.getPropertiesCount());
    assertEquals(getNativeType(NUMBER_TYPE),
        instanceType.getPropertyType("m1"));
    assertEquals(getNativeType(BOOLEAN_TYPE),
        instanceType.getPropertyType("m2"));
    assertEquals(getNativeType(STRING_TYPE),
        instanceType.getPropertyType("m3"));

    
    
    
    
    
    assertFalse(instanceType.hasOwnProperty("m1"));
    assertFalse(instanceType.hasOwnProperty("m2"));
    assertFalse(instanceType.hasOwnProperty("m3"));

    ObjectType proto1 = instanceType.getImplicitPrototype();
    assertFalse(proto1.hasOwnProperty("m1"));
    assertFalse(proto1.hasOwnProperty("m2"));
    assertTrue(proto1.hasOwnProperty("m3"));

    ObjectType proto2 = proto1.getImplicitPrototype();
    assertTrue(proto2.hasOwnProperty("m1"));
    assertTrue(proto2.hasOwnProperty("m2"));
    assertFalse(proto2.hasProperty("m3"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredVar
  public void testInferredVar() throws Exception {
    testSame("var x = 3; x = 'x'; x = true;");

    Var x = globalScope.getVar("x");
    assertEquals("(boolean|number|string)", x.getType().toString());
    assertTrue(x.isTypeInferred());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredVar
  public void testDeclaredVar() throws Exception {
    testSame(" var x = 3; var y = x;");

    Var x = globalScope.getVar("x");
    assertEquals("(null|number)", x.getType().toString());
    assertFalse(x.isTypeInferred());

    JSType y = findNameType("y", globalScope);
    assertEquals("(null|number)", y.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertiesOnInterface
  public void testPropertiesOnInterface() throws Exception {
    testSame(" var I = function() {};" +
        " I.prototype.bar;" +
        "I.prototype.baz = function(){};");

    Var i = globalScope.getVar("I");
    assertEquals("function (this:I)", i.getType().toString());
    assertTrue(i.getType().isInterface());

    ObjectType iPrototype = (ObjectType)
        ((ObjectType) i.getType()).getPropertyType("prototype");
    assertEquals("I.prototype", iPrototype.toString());
    assertTrue(iPrototype.isFunctionPrototypeType());

    assertEquals("number", iPrototype.getPropertyType("bar").toString());
    assertEquals("function (this:I): ?",
        iPrototype.getPropertyType("baz").toString());

    assertEquals(iPrototype, globalScope.getVar("I.prototype").getType());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns
  public void testStubsInExterns() {
    testSame(
        " function Extern() {}" +
        "Extern.prototype.bar;" +
        "var e = new Extern(); e.baz;",
        " function Foo() {}" +
        "Foo.prototype.bar;" +
        "var f = new Foo(); f.baz;", null);

    ObjectType e = (ObjectType) globalScope.getVar("e").getType();
    assertEquals("?", e.getPropertyType("bar").toString());
    assertEquals("?", e.getPropertyType("baz").toString());

    ObjectType f = (ObjectType) globalScope.getVar("f").getType();
    assertEquals("?", f.getPropertyType("bar").toString());
    assertFalse(f.hasProperty("baz"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns2
  public void testStubsInExterns2() {
    testSame(
        " function Extern() {}" +
        " var myExtern;" +
        " myExtern.foo;",
        "", null);

    JSType e = globalScope.getVar("myExtern").getType();
    assertEquals("(Extern|null)", e.toString());

    ObjectType externType = (ObjectType) e.restrictByNotNullOrUndefined();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externType.hasOwnProperty("foo"));
    assertTrue(externType.isPropertyTypeDeclared("foo"));
    assertEquals("number", externType.getPropertyType("foo").toString());
    assertTrue(externType.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns3
  public void testStubsInExterns3() {
    testSame(
        " myExtern.foo;" +
        " var myExtern;" +
        " function Extern() {}",
        "", null);

    JSType e = globalScope.getVar("myExtern").getType();
    assertEquals("(Extern|null)", e.toString());

    ObjectType externType = (ObjectType) e.restrictByNotNullOrUndefined();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externType.hasOwnProperty("foo"));
    assertTrue(externType.isPropertyTypeDeclared("foo"));
    assertEquals("number", externType.getPropertyType("foo").toString());
    assertTrue(externType.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns4
  public void testStubsInExterns4() {
    testSame(
        "Extern.prototype.foo;" +
        " function Extern() {}",
        "", null);

    JSType e = globalScope.getVar("Extern").getType();
    assertEquals("function (this:Extern): ?", e.toString());

    ObjectType externProto = ((FunctionType) e).getPrototype();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externProto.hasOwnProperty("foo"));
    assertTrue(externProto.isPropertyTypeInferred("foo"));
    assertEquals("?", externProto.getPropertyType("foo").toString());
    assertTrue(externProto.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns1
  public void testPropertyInExterns1() {
    testSame(
        " function Extern() {}" +
        " var extern;" +
        " extern.one;",
        " function Normal() {}" +
        " var normal;" +
        " normal.one;", null);

    JSType e = globalScope.getVar("Extern").getType();
    ObjectType externInstance = ((FunctionType) e).getInstanceType();
    assertTrue(externInstance.hasOwnProperty("one"));
    assertTrue(externInstance.isPropertyTypeDeclared("one"));
    assertTypeEquals("function (): number",
        externInstance.getPropertyType("one"));

    JSType n = globalScope.getVar("Normal").getType();
    ObjectType normalInstance = ((FunctionType) n).getInstanceType();
    assertFalse(normalInstance.hasOwnProperty("one"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns2
  public void testPropertyInExterns2() {
    testSame(
        " var extern;" +
        " extern.one;",
        " var normal;" +
        " normal.one;", null);

    JSType e = globalScope.getVar("extern").getType();
    assertFalse(e.dereference().hasOwnProperty("one"));

    JSType normal = globalScope.getVar("normal").getType();
    assertFalse(normal.dereference().hasOwnProperty("one"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns3
  public void testPropertyInExterns3() {
    testSame(
        " function Object(x) {}" +
        " Object.one;", null);

    ObjectType obj = globalScope.getVar("Object").getType().dereference();
    assertTrue(obj.hasOwnProperty("one"));
    assertTypeEquals("number", obj.getPropertyType("one"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTypedStubsInExterns
  public void testTypedStubsInExterns() {
    testSame(
        " " +
        "function Function(var_args) {}" +
        " Function.prototype.apply;",
        "var f = new Function();", null);

    ObjectType f = (ObjectType) globalScope.getVar("f").getType();

    
    
    assertEquals(
        "function ((Object|null|undefined), (Object|null|undefined)): ?",
        f.getPropertyType("apply").toString());

    
    
    FunctionType func = (FunctionType) globalScope.getVar("Function").getType();
    assertEquals("Function",
        func.getPrototype().getPropertyType("apply").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyDeclarationOnInstanceType
  public void testPropertyDeclarationOnInstanceType() {
    testSame(
        " var a = {};" +
        " a.name = 0;");

    assertEquals("number", globalScope.getVar("a.name").getType().toString());

    ObjectType a = (ObjectType) (globalScope.getVar("a").getType());
    assertFalse(a.hasProperty("name"));
    assertFalse(getNativeObjectType(OBJECT_TYPE).hasProperty("name"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyDeclarationOnRecordType
  public void testPropertyDeclarationOnRecordType() {
    testSame(
        " var a = {foo: 3};" +
        " a.name = 0;");

    assertEquals("number", globalScope.getVar("a.name").getType().toString());

    ObjectType a = (ObjectType) (globalScope.getVar("a").getType());
    assertEquals("{ foo : number }", a.toString());
    assertFalse(a.hasProperty("name"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalThis
  public void testGlobalThis() {
    testSame(
        " function Window() {}" +
        "Window.prototype.alert = function() {};" +
        "var x = this;");

    ObjectType x = (ObjectType) (globalScope.getVar("x").getType());
    FunctionType windowCtor =
        (FunctionType) (globalScope.getVar("Window").getType());
    assertEquals("global this", x.toString());
    assertTrue(x.isSubtype(windowCtor.getInstanceType()));
    assertFalse(x.equals(windowCtor.getInstanceType()));
    assertTrue(x.hasProperty("alert"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testObjectLiteralCast
  public void testObjectLiteralCast() {
    testSame(" A.B = function() {}\n" +
             "goog.reflect.object(A.B, {})");

    assertEquals("A.B",
                 globalScope.getRootNode().getLastChild().getFirstChild().
                 getLastChild().getFirstChild().getLastChild().getJSType().
                 toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadObjectLiteralCast1
  public void testBadObjectLiteralCast1() {
    testSame(" A.B = function() {}\n" +
             "goog.reflect.object(A.B, 1)",
             ClosureCodingConvention.OBJECTLIT_EXPECTED);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadObjectLiteralCast2
  public void testBadObjectLiteralCast2() {
    testSame("goog.reflect.object(A.B, {})",
             TypedScopeCreator.CONSTRUCTOR_EXPECTED);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorNode
  public void testConstructorNode() {
    testSame("var goog = {};  goog.Foo = function() {};");

    ObjectType ctor = (ObjectType) (findNameType("goog.Foo", globalScope));
    assertNotNull(ctor);
    assertTrue(ctor.isConstructor());
    assertEquals("function (this:goog.Foo): ?", ctor.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testForLoopIntegration
  public void testForLoopIntegration() {
    testSame("var y = 3; for (var x = true; x; y = x) {}");

    Var y = globalScope.getVar("y");
    assertTrue(y.isTypeInferred());
    assertEquals("(boolean|number)", y.getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorAlias
  public void testConstructorAlias() {
    testSame(
        " var Foo = function() {};" +
        " var FooAlias = Foo;");
    assertEquals("Foo", registry.getType("FooAlias").toString());
    assertEquals(registry.getType("Foo"), registry.getType("FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedConstructorAlias
  public void testNamespacedConstructorAlias() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " goog.FooAlias = goog.Foo;");
    assertEquals("goog.Foo", registry.getType("goog.FooAlias").toString());
    assertEquals(registry.getType("goog.Foo"),
        registry.getType("goog.FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType
  public void testTemplateType() {
    testSame(
        "\n" +
        "function bind(fn, thisObj) {}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.baz = function() {};\n" +
        "bind(function() { var f = this.baz(); }, new Foo());");
    assertEquals("number", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithoutJSDoc
  public void testClosureParameterTypesWithoutJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo(function(baz) { var f = baz; })\n");
    assertEquals("Object", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithJSDoc
  public void testClosureParameterTypesWithJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo((" +
        "function(baz) { var f = baz; }))\n");
    assertEquals("string", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDuplicateExternProperty1
  public void testDuplicateExternProperty1() {
    testSame(
        " function Foo() {}" +
        "Foo.prototype.bar;" +
        " Foo.prototype.bar; var x = (new Foo).bar;",
        null);
    assertEquals("number", findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDuplicateExternProperty2
  public void testDuplicateExternProperty2() {
    testSame(
        " function Foo() {}" +
        " Foo.prototype.bar;" +
        "Foo.prototype.bar; var x = (new Foo).bar;", null);
    assertEquals("number", findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod
  public void testAbstractMethod() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        " Foo.prototype.bar = abstractMethod;");
    assertEquals(
        "Function", findNameType("abstractMethod", globalScope).toString());

    FunctionType ctor = (FunctionType) findNameType("Foo", globalScope);
    ObjectType instance = ctor.getInstanceType();
    assertEquals("Foo", instance.toString());

    ObjectType proto = instance.getImplicitPrototype();
    assertEquals("Foo.prototype", proto.toString());

    assertEquals(
        "function (this:Foo, number): ?",
        proto.getPropertyType("bar").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod2
  public void testAbstractMethod2() {
    testSame(
        " var abstractMethod;" +
        " var y = abstractMethod;");
    assertEquals(
        "Function",
        findNameType("y", globalScope).toString());
    assertEquals(
        "function (number): ?",
        globalScope.getVar("y").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod3
  public void testAbstractMethod3() {
    testSame(
        " var abstractMethod;" +
        " var y = abstractMethod; y;");
    assertEquals(
        "function (number): ?",
        findNameType("y", globalScope).toString());
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUnreachableCode
  public void testRemoveUnreachableCode() {
    
    test("function foo(){switch(foo){case 1:x=1;return;break;" +
         "case 2:{x=2;return;break}default:}}",
         "function foo(){switch(foo){case 1:x=1;return;" +
         "case 2:{x=2;return}default:}}");

    
    test("function bar(){if(foo)x=1;else if(bar){return;x=2}" +
         "else{x=3;return;x=4}return;x=5}",
         "function bar(){if(foo)x=1;else if(bar){return}" +
         "else{x=3;return}return}");

    
    test("function foo(){if(x==3)return;x=4;y++;while(y==4){return;x=3}}",
         "function foo(){if(x==3)return;x=4;y++;while(y==4){return}}");

    
    test("function baz(){for(i=0;i<n;i++){x=3;break;x=4}" +
         "do{x=2;break;x=4}while(x==4);" +
         "while(i<4){x=3;return;x=6}}",
         "function baz(){for(i=0;i<n;){x=3;break}" +
         "do{x=2;break}while(x==4);" +
         "while(i<4){x=3;return}}");

    
    test("function foo(){if(x==3){return}return;while(y==4){x++;return;x=4}}",
         "function foo(){if(x==3){return}return}");

    
    test("function foo(){return 3;for(;y==4;){x++;return;x=4}}",
         "function foo(){return 3}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=4;return;x=5}}",
         "function foo(){try{x=3;return x+1}catch(e){x=4;return}}");

    
    test("function foo(){try{x=3;return x+1;x=5}finally{x=4;return;x=5}}",
         "function foo(){try{x=3;return x+1}finally{x=4;return}}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=3;return;x=2}" +
         "finally{x=4;return;x=5}}",

         "function foo(){try{x=3;return x+1}catch(e){x=3;return}" +
         "finally{x=4;return}}");

    
    test("function foo(){x=3;if(x==4){x=5;return;x=6}else{x=7}return;x=3}",
         "function foo(){x=3;if(x==4){x=5;return}else{x=7}return}");

    
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
    test("function f() { if (1) return; else return; x = 1}",
        "function f() { if (1) return; else return; }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testSwitchCase
  public void testSwitchCase() {
    test("function f() { switch(x) { default: return; foo()}}",
         "function f() { switch(x) { default: return;}}");
    test("function f() { switch(x) { default: return; case 1: foo(); bar()}}",
         "function f() { switch(x) { default: return; case 1: foo(); bar()}}");
    test("function f() { switch(x) { default: return; case 1: return; bar()}}",
         "function f() { switch(x) { default: return; case 1: return;}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testTryCatchFinally
  public void testTryCatchFinally() {
    testSame("try {foo()} catch (e) {bar()}");
    testSame("try { try {foo()} catch (e) {bar()}} catch (x) {bar()}");
    test("try {var x = 1} catch (e) {e()}", "{var x = 1}");
    test("try {var x = 1} catch (e) {e()} finally {x()}",
        " try {var x = 1}                 finally {x()}");
    test("try {var x = 1} catch (e) {e()} finally {}",
        "     {var x = 1}");
    testSame("try {var x = 1} finally {x()}");
    test("try {var x = 1} finally {}", "{var x = 1}");
    test("function f() { return; try{var x = 1}catch(e){} }",
        "function f() { var x; return; {}}");
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

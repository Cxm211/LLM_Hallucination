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
// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames
  public void testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames() {
    test("Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
         "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSamePropertyNameQuotedAndUnquoted
  public void testSamePropertyNameQuotedAndUnquoted() {
    test("Bar.prototype.prop = function(){}; y = {'prop': 0};",
         "Bar.prototype.a = function(){}; y = {'prop': 0};");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStaticAndInstanceMethodWithSameName
  public void testStaticAndInstanceMethodWithSameName() {
    test("Bar = function(){}; Bar.getA = function(){}; " +
         "Bar.prototype.getA = function(){}; Bar.getA(); bar.getA();",
         "Bar = function(){}; Bar.a = function(){}; " +
         "Bar.prototype.a = function(){}; Bar.a(); bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall1
  public void testRenamePropertiesFunctionCall1() {
    test("var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall2
  public void testRenamePropertiesFunctionCall2() {
    test("var foo = {myProp: 0}; " +
         "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
         "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
         "var foo = {a: 0}; f('b.a.c'); " +
         "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs1
  public void testRemoveRenameFunctionStubs1() {
    test("function JSCompiler_renameProperty(x) { return x; }",
         "");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs2
  public void testRemoveRenameFunctionStubs2() {
    test("function() { function JSCompiler_renameProperty(x) {} }" +
         "var JSCompiler_renameProperty = function(x) { return x; }; " +
         "var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "function() {} var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testGeneratePseudoNames
  public void testGeneratePseudoNames() {
    generatePseudoNames = true;
    test("var foo={}; foo.bar=1; foo['abc']=2",
         "var foo={}; foo.$bar$=1; foo['abc']=2");
    generatePseudoNames = false;
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testModules
  public void testModules() {
    String module1Js = "function Bar(){} Bar.prototype.getA=function(x){};" +
                       "var foo;foo.getA(foo);foo.doo=foo;foo.bloo=foo;";

    String module2Js = "function Far(){} Far.prototype.getB=function(x){};" +
                       "var too;too.getB(too);too.woo=too;too.bloo=too;";

    String module3Js = "function Car(){} Car.prototype.getC=function(x){};" +
                       "var noo;noo.getC(noo);noo.zoo=noo;noo.cloo=noo;";

    JSModule module1 = new JSModule("m1");
    module1.add(JSSourceFile.fromCode("input1", module1Js));

    JSModule module2 = new JSModule("m2");
    module2.add(JSSourceFile.fromCode("input2", module2Js));

    JSModule module3 = new JSModule("m3");
    module3.add(JSSourceFile.fromCode("input3", module3Js));

    JSModule[] modules = new JSModule[] { module1, module2, module3 };
    Compiler compiler = compileModules("", modules);

    Result result = compiler.getResult();
    assertTrue(result.success);

    assertEquals("function Bar(){}Bar.prototype.b=function(x){};" +
                 "var foo;foo.b(foo);foo.f=foo;foo.a=foo;",
                 compiler.toSource(module1));

    assertEquals("function Far(){}Far.prototype.c=function(x){};" +
                 "var too;too.c(too);too.g=too;too.a=too;",
                 compiler.toSource(module2));

    
    
    
    
    
    
    assertEquals("function Car(){}Car.prototype.d=function(x){};" +
                 "var noo;noo.d(noo);noo.h=noo;noo.e=noo;",
                 compiler.toSource(module3));
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesStable
  public void testPrototypePropertiesStable() {
    testStableRenaming(
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}",
        "Bar.prototype.get = function(){}; bar.get();" +
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.c = function(){}; bar.c();" +
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeysStable
  public void testPrototypePropertiesAsObjLitKeysStable() {
    testStableRenaming(
        "Bar.prototype = {2: function(){}, getA: function(){}}; bar[2]();",
        "Bar.prototype = {2: function(){}, a: function(){}}; bar[2]();",
        "Bar.prototype = {getB: function(){},getA: function(){}}; bar.getB();",
        "Bar.prototype = {b: function(){},a: function(){}}; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeysStable
  public void testMixedQuotedAndUnquotedObjLitKeysStable() {
    testStableRenaming(
        "Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
        "Bar = {a: function(){}, 'getB': function(){}}; bar.a();",
        "Bar = {get: function(){}, getA: function(){}, 'getB': function(){}};" +
        "bar.getA();bar.get();",
        "Bar = {b: function(){}, a: function(){}, 'getB': function(){}};" +
        "bar.a();bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testOverlappingOriginalAndGeneratedNamesStable
  public void testOverlappingOriginalAndGeneratedNamesStable() {
    testStableRenaming(
        "Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
        "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();",
        "Bar.prototype = {c: function(){}, b: function(){}, a: function(){}};" +
        "bar.b();",
        "Bar.prototype = {c: function(){}, a: function(){}, b: function(){}};" +
        "bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStableWithTrickyExternsChanges
  public void testStableWithTrickyExternsChanges() {
    test("Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
         "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();");
    prevUsedPropertyMap = renameProperties.getPropertyMap();
    String externs = EXTERNS + "prop.b;";
    test(externs,
         "Bar.prototype = {new_f: function(){}, b: function(){}, " +
         "a: function(){}};bar.b();",
         "Bar.prototype = {c:function(){}, b:function(){}, a:function(){}};" +
         "bar.b();", null, null);
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesWithLeadingUnderscoresStable
  public void testRenamePropertiesWithLeadingUnderscoresStable() {
    testStableRenaming(
        "Bar.prototype = {_getA: function(){}, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, b: 0}; bar.a();",
        "Bar.prototype = {_getA: function(){}, _c: 1, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, c: 1,  b: 0}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToObjectStable
  public void testPropertyAddedToObjectStable() {
    testStableRenaming("var foo = {}; foo.prop = '';",
                       "var foo = {}; foo.a = '';",
                       "var foo = {}; foo.prop = ''; foo.a='';",
                       "var foo = {}; foo.a = ''; foo.b='';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable
  public void testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable() {
    testStableRenaming(
        "Bar.prototype.foo = function(){}; Bar.prototype['b'] = 0; bar.foo();",
        "Bar.prototype.a = function(){}; Bar.prototype['b'] = 0; bar.a();",
        "Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
        "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCallStable
  public void testRenamePropertiesFunctionCallStable() {
    testStableRenaming(
        "var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;",
        "var bar = {newProp: 0}; var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var bar = {f: 0}; var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenamePrototypes
  public void testRenamePrototypes() {
    
    test("Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
         "Bar.prototype.getBaz=function(){}",
         "Bar.prototype.a=function(){};Bar.a(b);" +
         "Bar.prototype.b=function(){}");
    test("Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
         "Bar.prototype['getBaz']=function(){}",
         "Bar.prototype['a']=function(){};Bar.a(b);" +
         "Bar.prototype['b']=function(){}");
    test("Bar.prototype={'getFoo':function(){},2:function(){}}",
         "Bar.prototype={a:function(){},2:function(){}}");
    test("Bar.prototype={'getFoo':function(){}," +
         "'getBar':function(){}};b.getFoo()",
         "Bar.prototype={a:function(){}," +
         "b:function(){}};b.a()");

    test("Bar.prototype={'B':function(){}," +
         "'getBar':function(){}};b.getBar()",
         "Bar.prototype={b:function(){}," +
         "a:function(){}};b.a()");

    
    test("Bar.prototype={'a':function(){}," +
         "'b':function(){}};b.b()",
         "Bar.prototype={b:function(){}," +
         "a:function(){}};b.a()");

    
    test("Bar.prototype={'_getFoo':function(){}," +
         "'getBar':function(){}};b._getFoo()",
         "Bar.prototype={_getFoo:function(){}," +
         "a:function(){}};b._getFoo()");

    
    test("Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={toString:function(){}," +
         "a:function(){}};b.toString()");

    
    test("Bar.prototype.foo=function(){}" +
         ";bar.foo();bar.a",
         "Bar.prototype.b=function(){}" +
         ";bar.b();bar.a");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenameProperties
  public void testRenameProperties() {
    test("var foo; foo.prop_='bar'", "var foo;foo.a='bar'");
    test("this.prop_='bar'", "this.a='bar'");
    test("this.prop='bar'", "this.prop='bar'");
    test("this['prop_']='bar'", "this['a']='bar'");
    test("this['prop']='bar'", "this['prop']='bar'");
    test("var foo={prop1_: 'bar',prop2_: 'baz'};",
         "var foo={a:'bar',b:'baz'}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testBoth
  public void testBoth() {
    test("Bar.prototype.getFoo_=function(){};Bar.getFoo_(b);" +
         "Bar.prototype.getBaz_=function(){}",
         "Bar.prototype.a=function(){};Bar.a(b);" +
         "Bar.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testPropertyNameThatIsBothObjLitKeyAndPrototypeProperty
  public void testPropertyNameThatIsBothObjLitKeyAndPrototypeProperty() {
    
    
    
    
    test("x.prototype.myprop=function(){};y={myprop:0};z.myprop",
         "x.prototype.myprop=function(){};y={myprop:0};z.myprop");

    
    
    
    test("x.prototype.myprop_=function(){};y={myprop_:0};z.myprop_",
         "x.prototype.a=function(){};y={a:0};z.a");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testModule
  public void testModule() {
    JSModule[] modules = createModules(
        "function Bar(){} var foo; Bar.prototype.getFoo_=function(x){};" +
        "foo.getFoo_(foo);foo.doo_=foo;foo.bloo_=foo;",
        "function Far(){} var too; Far.prototype.getGoo_=function(x){};" +
        "too.getGoo_(too);too.troo_=too;too.bloo_=too;");

    test(modules, new String[] {
        "function Bar(){}var foo; Bar.prototype.a=function(x){};" +
        "foo.a(foo);foo.d=foo;foo.c=foo;",
        "function Far(){}var too; Far.prototype.b=function(x){};" +
        "too.b(too);too.e=too;too.c=too;"
    });
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple1
  public void testStableSimple1() {
    testStable(
        "Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
        "Bar.prototype.getBaz=function(){}",
        "Bar.prototype.a=function(){};Bar.a(b);" +
        "Bar.prototype.b=function(){}",
        "Bar.prototype.getBar=function(){};Bar.getBar(b);" +
        "Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
        "Bar.prototype.getBaz=function(){}",
        "Bar.prototype.c=function(){};Bar.c(b);" +
        "Bar.prototype.a=function(){};Bar.a(b);" +
        "Bar.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple2
  public void testStableSimple2() {
    testStable(
        "Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
        "Bar.prototype['getBaz']=function(){}",
        "Bar.prototype['a']=function(){};Bar.a(b);" +
        "Bar.prototype['b']=function(){}",
        "Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
        "Bar.prototype['getBar']=function(){};" +
        "Bar.prototype['getBaz']=function(){}",
        "Bar.prototype['a']=function(){};Bar.a(b);" +
        "Bar.prototype['c']=function(){};" +
        "Bar.prototype['b']=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple3
  public void testStableSimple3() {
    testStable(
        "Bar.prototype={'getFoo':function(){}," +
        "'getBar':function(){}};b.getFoo()",
        "Bar.prototype={a:function(){}, b:function(){}};b.a()",
        "Bar.prototype={'getFoo':function(){}," +
        "'getBaz':function(){},'getBar':function(){}};b.getFoo()",
        "Bar.prototype={a:function(){}, c:function(){}, b:function(){}};b.a()");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableOverlap
  public void testStableOverlap() {
    testStable(
        "Bar.prototype={'a':function(){},'b':function(){}};b.b()",
        "Bar.prototype={b:function(){},a:function(){}};b.a()",
        "Bar.prototype={'a':function(){},'b':function(){}};b.b()",
        "Bar.prototype={b:function(){},a:function(){}};b.a()");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableTrickyExternedMethods
  public void testStableTrickyExternedMethods() {
    test("Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={toString:function(){}," +
         "a:function(){}};b.toString()");
    prevUsedRenameMap = renamePrototypes.getPropertyMap();
    String externs = EXTERNS + "prop.a;";
    test(externs,
         "Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={toString:function(){}," +
         "b:function(){}};b.toString()", null, null);
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStable
  public void testStable(String input1, String expected1,
                         String input2, String expected2) {
    test(input1, expected1);
    prevUsedRenameMap = renamePrototypes.getPropertyMap();
    test(input2, expected2);
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameSimple
  public void testRenameSimple() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameGlobals
  public void testRenameGlobals() {
    test("var Foo; var Bar, y; function x() { Bar++; }",
         "var a; var b, c; function d() { b++; }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocals
  public void testRenameLocals() {
    test("(function (v1, v2) {}); (function (v3, v4) {});",
        "(function (a, b) {}); (function (a, b) {});");
    test("function f1(v1, v2) {}; function f2(v3, v4) {};",
        "function c(a, b) {}; function d(a, b) {};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameRedeclaredGlobals
  public void testRenameRedeclaredGlobals() {
    test("function f1(v1, v2) {f1()};" +
         "" +
         "function f1(v3, v4) {f1()};",
         "function a(b, c) {a()};" +
         "function a(b, c) {a()};");

    localRenamingOnly = true;

    test("function f1(v1, v2) {f1()};" +
        "" +
        "function f1(v3, v4) {f1()};",
        "function f1(a, b) {f1()};" +
        "function f1(a, b) {f1()};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions1
  public void testRecursiveFunctions1() {
    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var d = function a(b, c) {" +
         "  a(b, c);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var walk = function a(b, c) {" +
         "  a(b, c);" +
         "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions2
  public void testRecursiveFunctions2() {
    preserveAnonymousFunctionNames = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var c = function walk(a, b) {" +
         "  walk(a, b);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
        "  walk(node, aFunction);" +
        "};",
        "var walk = function walk(a, b) {" +
        "  walk(a, b);" +
        "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocalsClashingWithGlobals
  public void testRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
        "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameNested
  public void testRenameNested() {
    test("function f1(v1, v2) { (function(v3, v4) {}) }",
         "function a(b, c) { (function(d, e) {}) }");
    test("function f1(v1, v2) { function f2(v3, v4) {} }",
         "function a(b, c) { function d(e, f) {} }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns1
  public void testRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns2
  public void testRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testDoNotRenameExportedName
  public void testDoNotRenameExportedName() {
    test("_foo()", "_foo()");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithNameOverlap
  public void testRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix1
  public void testRenameWithPrefix1() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
        "function PRE_(a, b) {return a} PRE_();");
    prefix = DEFAULT_PREFIX;

  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix2
  public void testRenameWithPrefix2() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {var v3 = v1 + v2; return v3;} Foo();",
        "function PRE_(a, b) {var c = a + b; return c;} PRE_();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix3
  public void testRenameWithPrefix3() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

        "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testNamingBasedOnOrderOfOccurrence
  public void testNamingBasedOnOrderOfOccurrence() {
    test("var q,p,m,n,l,k; " +
             "(function (r) {}); try { } catch(s) {}; var t = q + q;",
         "var a,b,c,d,e,f; " +
             "(function(g) {}); try { } catch(h) {}; var i = a + a;"
         );
    test("function(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z," +
         "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,$){};" +
         "var a4,a3,a2,a1,b4,b3,b2,b1,ab,ac,ad,fg;function foo(){};",
         "function(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$){};" +
         "var aa,ba,ca,da,ea,fa,ga,ha,ia,ja,ka,la;function ma(){};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimple
  public void testStableRenameSimple() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c", "L 2", "d");
    testRenameMapUsingOldMap("function Foo(v1, v2, v3) {return v1;} Foo();",
         "function a(b, c, d) {return b;} a();", expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameGlobals
  public void testStableRenameGlobals() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d");
    testRenameMap("var Foo; var Bar, y; function x() { Bar++; }",
                  "var a; var b, c; function d() { b++; }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d", "Baz", "f", "L 0" , "e");
    testRenameMapUsingOldMap(
        "var Foo, Baz; var Bar, y; function x(R) { return R + Bar++; }",
        "var a, f; var b, c; function d(e) { return e + b++; }",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPointlesslyAnonymousFunctions
  public void testStableRenameWithPointlesslyAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b");
    testRenameMap("function (v1, v2) {}; function (v3, v4) {};",
                  "function (a, b) {}; function (a, b) {};",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b", "L 2", "c");
    testRenameMapUsingOldMap("function (v0, v1, v2) {}; function (v3, v4) {};",
                             "function (a, b, c) {}; function (a, b) {};",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameLocalsClashingWithGlobals
  public void testStableRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
         "function a(b, c) {return b;} a();");
    previouslyUsedMap = renameVars.getVariableMap();
    test("function bar(){return;}function a(v1, v2) {return v1;} a();",
         "function d(){return;}function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameNested
  public void testStableRenameNested() {
    VariableMap expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e");
    testRenameMap("function f1(v1, v2) { (function(v3, v4) {}) }",
                  "function a(b, c) { (function(d, e) {}) }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e", "L 4", "f");
    testRenameMapUsingOldMap("function f1(v1, v2) { (function(v3, v4, v5) {}) }",
                             "function a(b, c) { (function(d, e, f) {}) }",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns1
  public void testStableRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var bar, baz; foo(bar, baz);",
         "var a, b; foo(a, b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns2
  public void testStableRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var b = 5, catty = 9;", "var b = 5, c=9;", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithNameOverlap
  public void testStableRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
    previouslyUsedMap = renameVars.getVariableMap();
    test("var a = 1; var c, b = 2; b + b;",
         "var a = 1; var c, b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithAnonymousFunctions
  public void testStableRenameWithAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "foo", "b");
    testRenameMap("function foo(bar){return bar;}foo(function(h){return h;});",
                  "function b(a){return a}b(function(a){return a;})",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("foo", "b", "L 0", "a", "L 1", "c");
    testRenameMapUsingOldMap(
        "function foo(bar) {return bar;}foo(function(g,h) {return g+h;});",
        "function b(a){return a}b(function(a,c){return a+c;})",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleExternsChanges
  public void testStableRenameSimpleExternsChanges() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "b", "L 1", "c", "L 2", "a");
    String externs = "var Foo;";
    testRenameMapUsingOldMap(externs,
                             "function Foo(v1, v2, v0) {return v1;} Foo();",
                             "function Foo(b, c, a) {return b;} Foo();",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleLocalNameExterned
  public void testStableRenameSimpleLocalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var b;";
    test(externs, "function Foo(v1, v2) {return v1;} Foo(b);",
         "function a(d, c) {return d;} a(b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleGlobalNameExterned
  public void testStableRenameSimpleGlobalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var Foo;";
    test(externs, "function Foo(v1, v2, v0) {return v1;} Foo();",
         "function Foo(b, c, a) {return b;} Foo();", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix1AndUnstableLocalNames
  public void testStableRenameWithPrefix1AndUnstableLocalNames() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
         "function PRE_(a, b) {return a} PRE_();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "PRE_";
    test("function Foo(v0, v1, v2) {return v1} Foo();",
         "function PRE_(a, b, c) {return b} PRE_();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix2
  public void testStableRenameWithPrefix2() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Baz() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function ab() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testContrivedExampleWhereConsistentRenamingIsWorse
  public void testContrivedExampleWhereConsistentRenamingIsWorse() {
    previouslyUsedMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");

    test("function Foo(v1, v2) {return v1;} Foo();",
         "function LongString(b, c) {return b;} LongString();");

    previouslyUsedMap = renameVars.getVariableMap();
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");
    assertVariableMapsEqual(expectedVariableMap, previouslyUsedMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportSimpleSymbolReservesName
  public void testExportSimpleSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a', x);",
         "var a, b; a.exportSymbol('a', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a', x);",
         "var b, c; b.exportSymbol('a', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportComplexSymbolReservesName
  public void testExportComplexSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var a, b; a.exportSymbol('a.b', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var b, c; b.exportSymbol('a.b', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportToNonStringDoesntExplode
  public void testExportToNonStringDoesntExplode() {
    withClosurePass = true;
    test("var goog, a, b; goog.exportSymbol(a, b);",
         "var a, b, c; a.exportSymbol(b, c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport1
  public void testDollarSignSuperExport1() {
    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){}",
         "var c = function($super,    a,        b){}");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){}",
         "var d = function(a,     b,    c        ){}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport2
  public void testDollarSignSuperExport2() {
    boolean normalizedExpectedJs = false;
    super.enableNormalize(false);

    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var c = function($super,    a,         b){};" +
            "var d = function($super,    a){};");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var d = function(a,     b,    c         ){};" +
            "var e = function(     a,    b){};");

    super.disableNormalize();
  }

// com.google.javascript.jscomp.RenameVarsTest::testPseudoNames
  public void testPseudoNames() {
    generatePseudoNames = false;
    
    test("var foo = function(a, b, c){}",
         "var d = function(a, b, c){}");

    generatePseudoNames = true;
    test("var foo = function(a, b, c){}",
         "var $foo$$ = function($a$$, $b$$, $c$$){}");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testDoNotUseReplacementMap
  public void testDoNotUseReplacementMap() {
    useReplacementMap = false;
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'goog-footer-active'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'goog-colorswatch-disabled'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('active-buttonbar')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithUnknownStringLiterals
  public void testOneArgWithUnknownStringLiterals() {
    test("var x = goog.getCssName('unknown')",
         "var x = 'unknown'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('ooo')",
         "el.className = 'ooo'", null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('ab'))",
         "setClass('ab')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithSimpleStringLiterals
  public void testOneArgWithSimpleStringLiterals() {
    test("var x = goog.getCssName('buttonbar')",
         "var x = 'b'");
    test("el.className = goog.getCssName('colorswatch')",
         "el.className = 'c'");
    test("setClass(goog.getCssName('elephant'))",
         "setClass('e')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("buttonbar", 1)
        .put("colorswatch", 1)
        .put("elephant", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNames
  public void testOneArgWithCompositeClassNames() {
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'g-f-a'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'g-c-d'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('a-b')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNamesWithUnknownParts
  public void testOneArgWithCompositeClassNamesWithUnknownParts() {
    test("var x = goog.getCssName('goog-header-active')",
         "var x = 'goog-header-active'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('goog-colorswatch-focussed')",
         "el.className = 'goog-colorswatch-focussed'",
         null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('inactive-buttonbar'))",
         "setClass('inactive-buttonbar')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArgsWithStringLiterals
  public void testTwoArgsWithStringLiterals() {
    test("var x = goog.getCssName('header', 'active')",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("el.className = goog.getCssName('footer', window)",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName('buttonbar', 'disabled'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName(goog.getCssName('buttonbar'), 'active'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArsWithVariableFirstArg
  public void testTwoArsWithVariableFirstArg() {
    test("var x = goog.getCssName(baseClass, 'active')",
         "var x = baseClass + '-a'");
    test("el.className = goog.getCssName(this.getClass(), 'disabled')",
         "el.className = this.getClass() + '-d'");
    test("setClass(goog.getCssName(BASE_CLASS, 'disabled'))",
         "setClass(BASE_CLASS + '-d')");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testZeroArguments
  public void testZeroArguments() {
    test("goog.getCssName()", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testManyArguments
  public void testManyArguments() {
    test("goog.getCssName('a', 'b', 'c')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNonStringArgument
  public void testNonStringArgument() {
    test("goog.getCssName(window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName([]);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName({});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);

    test("goog.getCssName(baseClass, window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, 555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, []);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, {});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNoSymbolMapStripsCallAndDoesntIssueWarnings
  public void testNoSymbolMapStripsCallAndDoesntIssueWarnings() {
    String input = "[goog.getCssName('test'), goog.getCssName(base, 'active')]";
    Compiler compiler = new Compiler();
    ErrorManager errorMan = new BasicErrorManager() {
      @Override protected void printSummary() {}
      @Override public void println(CheckLevel level, JSError error) {}
    };
    compiler.setErrorManager(errorMan);
    Node root = compiler.parseTestCode(input);
    useReplacementMap = false;
    ReplaceCssNames replacer = new ReplaceCssNames(compiler, null);
    replacer.process(null, root);
    assertEquals("[\"test\",base+\"-active\"]", compiler.toSource(root));
    assertEquals("There should be no errors", 0, errorMan.getErrorCount());
    assertEquals("There should be no warnings", 0, errorMan.getWarningCount());
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testAssign
  public void testAssign() {
    test("foo.bar = goog.events.getUniqueId('foo_bar')",
         "foo.bar = 'a'");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testObjectLiteral
  public void testObjectLiteral() {
    test("foo = { bar : goog.events.getUniqueId('foo_bar')}",
         "foo = { bar : 'a' }");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testTwoNamespaces
  public void testTwoNamespaces() {
    test("foo.bar = goog.events.getUniqueId('foo_bar');\n"
         + "baz.blah = goog.place.getUniqueId('baz_blah');\n",
         "foo.bar = 'a';\n"
         + "baz.blah = 'a'\n");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testLocalCall
  public void testLocalCall() {
    testSame(new String[] {
          "function Foo() { goog.events.getUniqueId('foo'); }"
        },
        ReplaceIdGenerators.NON_GLOBAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testConditionalCall
  public void testConditionalCall() {
    testSame(new String[] {"if (x) foo = goog.events.getUniqueId('foo')"},
             ReplaceIdGenerators.CONDITIONAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNullValue
  public void testNullValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.nullChecker]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValues
  public void testValues() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testSkipParamOK
  public void testSkipParamOK() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUnion
  public void testUnion() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "      jscomp.typecheck.valueChecker('number'), " +
        "      jscomp.typecheck.valueChecker('string')" +
        "]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUntypedParam
  public void testUntypedParam() {
    testChecks(" function f(x) {}", "function f(x) {}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturn
  public void testReturn() {
    testChecks(" function f() { return 'x'; }",
        "function f() {" +
        "  return jscomp.typecheck.checkType('x', " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNativeClass
  public void testNativeClass() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "      [jscomp.typecheck.externClassChecker('String')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testQualifiedClass
  public void testQualifiedClass() {
    testChecks("var goog = {}; goog.Foo = function() {};" +
        " function f(x) {}",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype['instance_of__goog.Foo'] = true;" +
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "    [jscomp.typecheck.classChecker('goog.Foo')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInterface
  public void testInterface() {
    testChecks("function I() {}" +
        "function f(i) {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "    [jscomp.typecheck.interfaceChecker('I')])" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterface
  public void testImplementedInterface() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testExtendedInterface
  public void testExtendedInterface() {
    testChecks("function I() {}" +
        "function J() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function J() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype['implements__J'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrdering
  public void testImplementedInterfaceOrdering() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}" +
        "C.prototype.f = function() {};",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrderingGoogInherits
  public void testImplementedInterfaceOrderingGoogInherits() {
    testChecks("var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {}" +
        "function B() {}" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype.f = function() {};",
        "var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function B() {}" +
        "B.prototype['instance_of__B'] = true;" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerConstructor
  public void testInnerConstructor() {
    testChecks("(function() {  function C() {} })()",
        "(function() {" +
        "  function C() {} C.prototype['instance_of__C'] = true;" +
        "})()");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturnNothing
  public void testReturnNothing() {
    testChecks("function f() { return; }", "function f() { return; }");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testFunctionType
  public void testFunctionType() {
    testChecks("function f() {}", "function f() {}");
  }

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalizeNodeTypes
  public void testUnnormalizeNodeTypes() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        root.addChildToBack(new Node(Token.EXPR_VOID, Node.newNumber(0)));
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x = 3;", "var x=3;0;0");
    } catch (IllegalStateException e) {
      assertEquals("normalizeNodeType constraints violated",
          e.getMessage());
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalized
  public void testUnnormalized() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().setNormalized();
      }
    };

    boolean exceptionCaught = false;
    try {
      test("while(1){}", "while(1){}");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "Normalize constraints violated:\nWHILE node"));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testConstantAnnotationMismatch
  public void testConstantAnnotationMismatch() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        Node name = Node.newString(Token.NAME, "x");
        name.putBooleanProp(Node.IS_CONSTANT_NAME, true);
        root.addChildToBack(new Node(Token.EXPR_RESULT, name));
        getLastCompiler().setNormalized();
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x;", "var x; x;");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "The name x is not consistently annotated as constant."));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testSymbolTable
  public void testSymbolTable() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        SymbolTable st = getLastCompiler().acquireSymbolTable();
        st.createScope(root.getParent(), null);
        Node script = root.getFirstChild();
        script.removeChild(script.getFirstChild());
        st.release();
      }
    };

    test("var x;", null, SymbolTable.VARIABLE_COUNT_MISMATCH);
  }

// com.google.javascript.jscomp.SanityCheckTest::testSymbolTableWrongRoot
  public void testSymbolTableWrongRoot() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        SymbolTable st = getLastCompiler().acquireSymbolTable();
        st.createScope(root, null);
        st.release();
      }
    };

    try {
      testSame("var x;");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "May only create scopes for the global node and functions",
          e.getMessage());
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testNameCondition
  public void testNameCondition() throws Exception {
    FlowScope blind = newScope();
    Node condition = createVar(blind, "a", createNullableType(STRING_TYPE));

    
    FlowScope informedTrue = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, true);
    assertEquals(STRING_TYPE, getVarType(informedTrue, "a"));

    
    FlowScope informedFalse = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, false);
    assertEquals(createNullableType(STRING_TYPE),
        getVarType(informedFalse, "a"));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testNegatedNameCondition
  public void testNegatedNameCondition() throws Exception {
    FlowScope blind = newScope();
    Node a = createVar(blind, "a", createNullableType(STRING_TYPE));
    Node condition = new Node(Token.NOT);
    condition.addChildToBack(a);

    
    FlowScope informedTrue = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, true);
    assertEquals(createNullableType(STRING_TYPE),
        getVarType(informedTrue, "a"));

    
    FlowScope informedFalse = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, false);
    assertEquals(STRING_TYPE, getVarType(informedFalse, "a"));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testAssignCondition1
  public void testAssignCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.ASSIGN,
        createVar(blind, "a", createNullableType(OBJECT_TYPE)),
        createVar(blind, "b", createNullableType(OBJECT_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_TYPE),
            new TypedName("b", OBJECT_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NULL_TYPE),
            new TypedName("b", NULL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition1
  public void testSheqCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        createNumber(56),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition2
  public void testSheqCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createNumber(56),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition3
  public void testSheqCondition3() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "b", createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", STRING_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE)),
            new TypedName("b",
                createUnionType(STRING_TYPE, BOOLEAN_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition4
  public void testSheqCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition5
  public void testSheqCondition5() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(NULL_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", NULL_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition6
  public void testSheqCondition6() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NUMBER_TYPE, VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a",
                createUnionType(STRING_TYPE, VOID_TYPE)),
            new TypedName("b",
                createUnionType(NUMBER_TYPE, VOID_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition1
  public void testShneCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        createNumber(56),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition2
  public void testShneCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createNumber(56),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition3
  public void testShneCondition3() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "b", createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE)),
            new TypedName("b",
                createUnionType(STRING_TYPE, BOOLEAN_TYPE))),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", STRING_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition4
  public void testShneCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition5
  public void testShneCondition5() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(NULL_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NULL_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", NULL_TYPE)),
        Sets.newHashSet(new TypedName("a", NULL_TYPE),
            new TypedName("b", NULL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition6
  public void testShneCondition6() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NUMBER_TYPE, VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a",
                createUnionType(STRING_TYPE, VOID_TYPE)),
            new TypedName("b",
                createUnionType(NUMBER_TYPE, VOID_TYPE))),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition1
  public void testEqCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", createUnionType(BOOLEAN_TYPE, VOID_TYPE)),
        createNull(),
        Sets.newHashSet(new TypedName("a", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", BOOLEAN_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition2
  public void testEqCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.NE,
        createNull(),
        createVar(blind, "a", createUnionType(BOOLEAN_TYPE, VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", BOOLEAN_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition3
  public void testEqCondition3() throws Exception {
    FlowScope blind = newScope();
    
    JSType nullableOptionalNumber =
        createUnionType(NULL_TYPE, VOID_TYPE, NUMBER_TYPE);
    
    JSType nullUndefined =
        createUnionType(VOID_TYPE, NULL_TYPE);
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", nullableOptionalNumber),
        createNull(),
        Sets.newHashSet(new TypedName("a", nullUndefined)),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition1
  public void testInequalitiesCondition1() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
          createNumber(8),
          Sets.newHashSet(
              new TypedName("a", STRING_TYPE)),
          Sets.newHashSet(new TypedName("a",
              createUnionType(STRING_TYPE, VOID_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition2
  public void testInequalitiesCondition2() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createVar(blind, "a",
              createUnionType(STRING_TYPE, NUMBER_TYPE, VOID_TYPE)),
          createVar(blind, "b",
              createUnionType(NUMBER_TYPE, NULL_TYPE)),
          Sets.newHashSet(
              new TypedName("a",
              createUnionType(STRING_TYPE, NUMBER_TYPE)),
              new TypedName("b",
              createUnionType(NUMBER_TYPE, NULL_TYPE))),
          Sets.newHashSet(
              new TypedName("a",
              createUnionType(STRING_TYPE, NUMBER_TYPE, VOID_TYPE)),
              new TypedName("b",
              createUnionType(NUMBER_TYPE, NULL_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition3
  public void testInequalitiesCondition3() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createUntypedNumber(8),
          createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
          Sets.newHashSet(
              new TypedName("a", STRING_TYPE)),
          Sets.newHashSet(new TypedName("a",
              createUnionType(STRING_TYPE, VOID_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testAnd
  public void testAnd() {
    FlowScope blind = newScope();
    testBinop(blind,
      Token.AND,
      createVar(blind, "b", createUnionType(STRING_TYPE, NULL_TYPE)),
      createVar(blind, "a", createUnionType(NUMBER_TYPE, VOID_TYPE)),
      Sets.newHashSet(new TypedName("a", NUMBER_TYPE),
          new TypedName("b", STRING_TYPE)),
      Sets.newHashSet(new TypedName("a",
          createUnionType(NUMBER_TYPE, VOID_TYPE)),
          new TypedName("b",
          createUnionType(STRING_TYPE, NULL_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof1
  public void testTypeof1() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(blind, "a", OBJECT_TYPE)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof2
  public void testTypeof2() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(blind, "a", ALL_TYPE)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", ALL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf
  public void testInstanceOf() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", UNKNOWN_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf2
  public void testInstanceOf2() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x",
            createUnionType(STRING_OBJECT_TYPE, NUMBER_OBJECT_TYPE)),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("x", NUMBER_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf3
  public void testInstanceOf3() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", OBJECT_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("x", OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf4
  public void testInstanceOf4() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", ALL_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineNumber
  public void testDefineNumber() throws Exception {
    checkDefinitionsInJs(
        "var a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = 1",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = 1",
        ImmutableSet.of("DEF GETPROP null -> NUMBER"));

    checkDefinitionsInJs(
        "({a : 1}); o.a",
        ImmutableSet.of("DEF STRING null -> NUMBER",
                        "USE GETPROP o.a -> [NUMBER]"));

    checkDefinitionsInJs(
        "var a = {b : 1}; a.b",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF STRING null -> NUMBER",
                        "USE NAME a -> [<null>]",
                        "USE GETPROP a.b -> [NUMBER]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineFunction
  public void testDefineFunction() throws Exception {
    checkDefinitionsInJs(
        "var a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = function f(){}",
        ImmutableSet.of("DEF NAME f -> FUNCTION", "DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "function a(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a.b = function(){}",
        ImmutableSet.of("DEF GETPROP a.b -> FUNCTION"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = function(){}",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = function(){}",
        ImmutableSet.of("DEF GETPROP null -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsBasic
  public void testFunctionArgumentsBasic() throws Exception {
    checkDefinitionsInJs(
        "function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]",
                        "DEF NAME f -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = 1; function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> <null>",
                        "USE NAME a -> [<null>, NUMBER]",
                        "DEF NAME f -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsInExterns
  public void testFunctionArgumentsInExterns() throws Exception {
    final String DEF = "var f = function(arg1, arg2){}";
    final String USE = "f(1, 2)";

    
    checkDefinitionsInJs(
        DEF + ";" + USE,
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "DEF NAME arg1 -> <null>",
                        "DEF NAME arg2 -> <null>",
                        "USE NAME f -> [FUNCTION]"));

    
    checkDefinitions(
        DEF, USE,
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testMultipleDefinition
  public void testMultipleDefinition() throws Exception {
    checkDefinitionsInJs(
        "a = 1; a = 2; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "USE NAME a -> [NUMBER x 2]"));

    checkDefinitionsInJs(
        "a = 1; a = 'a'; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [NUMBER, STRING]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; a = b; a",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "USE NAME a -> [<null>, NUMBER]",
                        "USE NAME b -> [NUMBER]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; c = b; c = a; c",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "DEF NAME c -> <null>",
                        "USE NAME a -> [NUMBER]",
                        "USE NAME b -> [NUMBER]",
                        "USE NAME c -> [<null> x 2]"));

    checkDefinitionsInJs(
        "function f(){} f()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.apply(null, [])",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.apply -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.foobar()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f(); f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefinitionInExterns
  public void testDefinitionInExterns() throws Exception {
    String externs = "var a = 1";

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitions(
        externs,
        "var b = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER", "DEF NAME b -> NUMBER"));

    checkDefinitions(
        externs,
        "a = \"foo\"; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [NUMBER, STRING]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b = 10",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER",
                        "DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b",
        ImmutableSet.of("DEF GETPROP a.b -> <null>",
                        "DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER",
                        "DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]"));

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> NUMBER"));
  }

// com.google.javascript.jscomp.SourceMapTest::testBasicMapping
  public void testBasicMapping() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.javascript.jscomp.SourceMapTest::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.javascript.jscomp.SourceMapTest::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.javascript.jscomp.SourceMapTest::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{ \"file\" : \"testMap\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,2,2,2,4,4,4,4,5,5,5,5,3,8,8,8,8,9,9,9,9," +
                   "10,10,10,10,11,11,12,12,12,12,12,12,13,13,13,13,13,6]\n" +

                   "\n" +
                   "[\"testcode\"]\n" +
                   "\n" +
                   "[\"testcode\",1,0]\n" +
                   "[\"testcode\",1,9]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",1,23]\n" +
                   "[\"testcode\",1,23,\"foo\"]\n" +
                   "[\"testcode\",1,29,\"foo\"]\n" +
                   "[\"testcode\",1,35,\"bar\"]\n" +
                   "[\"testcode\",1,41]\n" +
                   "[\"testcode\",1,44]\n" +
                   "[\"testcode\",1,51,\"foo\"]\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{ \"file\" : \"testMap\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,2,2,2,4,4,4,4,5,5,5,5,3,8,8,8,8,9,9,9," +
                   "9,10,10,10,10,11,11,11,11,12,12,12,12,12,12,13,13,13," +
                   "13,13,6]\n" +

                   "\n" +
                   "[\"testcode\"]\n" +
                   "\n" +
                   "[\"testcode\",1,0]\n" +
                   "[\"testcode\",1,9]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",5,0]\n" +
                   "[\"testcode\",5,0,\"foo\"]\n" +
                   "[\"testcode\",5,6,\"foo\"]\n" +
                   "[\"testcode\",5,12,\"bar\"]\n" +
                   "[\"testcode\",5,18,\"foo\"]\n" +
                   "[\"testcode\",6,0]\n" +
                   "[\"testcode\",6,7,\"foo\"]\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{ \"file\" : \"testMap\", " +
                   "\"count\": 1 }\n" +

                   "[2,2,2,2]\n" +

                   "\n" +
                   "[\"c:\\\\myfile.js\"]\n" +
                   "\n" +
                   "[\"c:\\\\myfile.js\",1,0]\n" +
                   "[\"c:\\\\myfile.js\",1,0]\n" +
                   "[\"c:\\\\myfile.js\",1,0,\"foo\"]\n");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testWith
  public void testWith() {
    test("var a; function foo(obj) { with (obj) { a = 3; }}", null,
         StrictModeCheck.WITH_DISALLOWED);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval
  public void testEval() {
    test("function foo() { eval('a'); }", null,
         StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval2
  public void testEval2() {
    test("function foo(eval) {}", null,
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval3
  public void testEval3() {
    testSame("function foo() {} foo.eval = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval4
  public void testEval4() {
    test("function foo() { var eval = 3; }", null,
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval5
  public void testEval5() {
    test("function eval() {}", null, StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval6
  public void testEval6() {
    test("try {} catch (eval) {}", null, StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval7
  public void testEval7() {
    testSame("var o = {eval: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval8
  public void testEval8() {
    testSame("var a; eval: while (true) { a = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable
  public void testUnknownVariable() {
    test("function foo(a) { a = b; }", null, StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable2
  public void testUnknownVariable2() {
    test("a: while (true) { a = 3; }", null, StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable3
  public void testUnknownVariable3() {
    testSame("try {} catch (ex) { ex = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments
  public void testArguments() {
    test("function foo(arguments) {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments2
  public void testArguments2() {
    test("function foo() { var arguments = 3; }", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments3
  public void testArguments3() {
    test("function arguments() {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments4
  public void testArguments4() {
    test("try {} catch (arguments) {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments5
  public void testArguments5() {
    testSame("var o = {arguments: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment
  public void testEvalAssignment() {
    noCajaChecks = true;
    test("function foo() { eval = []; }", null,
         StrictModeCheck.EVAL_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment2
  public void testEvalAssignment2() {
    test("function foo() { eval = []; }", null, StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testAssignToArguments
  public void testAssignToArguments() {
    test("function foo() { arguments = []; }", null,
         StrictModeCheck.ARGUMENTS_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteVar
  public void testDeleteVar() {
    test("var a; delete a", null, StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteFunction
  public void testDeleteFunction() {
    test("function a() {} delete a", null, StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteArgument
  public void testDeleteArgument() {
    test("function b(a) { delete a; }", null, StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteProperty
  public void testDeleteProperty() {
    testSame("function f(obj) { delete obj.a; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName
  public void testIllegalName() {
    test("var a__ = 3;", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName2
  public void testIllegalName2() {
    test("function a__() {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName3
  public void testIllegalName3() {
    test("function f(a__) {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName4
  public void testIllegalName4() {
    test("try {} catch (a__) {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName5
  public void testIllegalName5() {
    noVarCheck = true;
    test("var a = b__;", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName6
  public void testIllegalName6() {
    test("function f(obj) { return obj.a__; }", null,
         StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName7
  public void testIllegalName7() {
    noCajaChecks = true;
    testSame("var a__ = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName8
  public void testIllegalName8() {
    test("var o = {a__: 3};", null, StrictModeCheck.ILLEGAL_NAME);
    test("var o = {b: 3, a__: 4};", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName9
  public void testIllegalName9() {
    test("a__: while (true) { var b = 3; }", null,
         StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInConstructor
  public void testLoggerDefinedInConstructor() {
    test("a.b.c = function() {" +
         "  this.logger = goog.debug.Logger.getLogger('a.b.c');" +
         "};",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype
  public void testLoggerDefinedInPrototype() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedStatically
  public void testLoggerDefinedStatically() {
    test("a.b.c = function() {};" +
         "a.b.c.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral
  public void testLoggerDefinedInObjectLiteral() {
    test("a.b.c = {" +
         "  x: 0," +
         "  logger: goog.debug.Logger.getLogger('a.b.c')" +
         "};",
         "a.b.c={x:0}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototypeAndUsedInConstructor
  public void testLoggerDefinedInPrototypeAndUsedInConstructor() {
    test("a.b.c = function(level) {" +
         "  if (!this.logger.isLoggable(level)) {" +
         "    this.logger.setLevel(level);" +
         "  }" +
         "  this.logger.log(level, 'hi');" +
         "};" +
         "a.b.c.prototype.logger = goog.debug.Logger.getLogger('a.b.c');" +
         "a.b.c.prototype.go = function() { this.logger.finer('x'); };",
         "a.b.c=function(level){if(!null);};" +
         "a.b.c.prototype.go=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedStaticallyAndUsedInConstructor
  public void testLoggerDefinedStaticallyAndUsedInConstructor() {
    test("a.b.c = function(level) {" +
         "  if (!a.b.c.logger.isLoggable(level)) {" +
         "    a.b.c.logger.setLevel(level);" +
         "  }" +
         "  a.b.c.logger.log(level, 'hi');" +
         "};" +
         "a.b.c.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(level){if(!null);}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerVarDeclaration
  public void testLoggerVarDeclaration() {
    test("var logger = opt_logger || goog.debug.LogManager.getRoot();", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerMethodCallByVariableType
  public void testLoggerMethodCallByVariableType() {
    test("var x = goog.debug.Logger.getLogger('a.b.c'); y.info(a); x.info(a);",
         "y.info(a)");
  }

// com.google.javascript.jscomp.StripCodeTest::testSubPropertyAccessByVariableName
  public void testSubPropertyAccessByVariableName() {
    test("var x, y = goog.debug.Logger.getLogger('a.b.c');" +
         "var logger = x;" +
         "var curlevel = logger.level_ ? logger.getLevel().name : 3;",
         "var x;var curlevel=null?null:3");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedVariableName
  public void testPrefixedVariableName() {
    test("this.blcLogger_ = goog.debug.Logger.getLogger('a.b.c');" +
         "this.blcLogger_.fine('Raised dirty states.');", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedPropertyName
  public void testPrefixedPropertyName() {
    test("a.b.c.staticLogger_ = goog.debug.Logger.getLogger('a.b.c');" +
         "a.b.c.staticLogger_.fine('-' + a.b.c.d_())", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedClassName
  public void testPrefixedClassName() {
    test("a.b.MyLogger = function(logger) {" +
         "  this.logger_ = logger;" +
         "};" +
         "a.b.MyLogger.prototype.shout = function(msg, opt_x) {" +
         "  this.logger_.log(goog.debug.Logger.Level.SHOUT, msg, opt_x);" +
         "};",
         "a.b.MyLogger=function(logger){};" +
         "a.b.MyLogger.prototype.shout=function(msg,opt_x){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerClassDefinition
  public void testLoggerClassDefinition() {
    test("goog.debug.Logger=function(name){this.name_=name}", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStaticLoggerPropertyDefinition
  public void testStaticLoggerPropertyDefinition() {
    test("goog.debug.Logger.Level.SHOUT=" +
         "new goog.debug.Logger.Level(x,1200)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStaticLoggerMethodDefinition
  public void testStaticLoggerMethodDefinition() {
    test("goog.debug.Logger.getLogger=function(name){" +
         "return goog.debug.LogManager.getLogger(name)" +
         "};", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeFieldDefinition
  public void testPrototypeFieldDefinition() {
    test("goog.debug.Logger.prototype.level_=null;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeFieldDefinitionWithoutAssignment
  public void testPrototypeFieldDefinitionWithoutAssignment() {
    test("goog.debug.Logger.prototype.level_;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeMethodDefinition
  public void testPrototypeMethodDefinition() {
    test("goog.debug.Logger.prototype.addHandler=" +
         "function(handler){this.handlers_.push(handler)};", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPublicPropertyAssignment
  public void testPublicPropertyAssignment() {
    
    
    
    testSame("rootLogger.someProperty=3");
    testSame("this.blcLogger_.level=x");
    testSame("goog.ui.Component.logger.prop=y");
  }

// com.google.javascript.jscomp.StripCodeTest::testGlobalCallWithStrippedType
  public void testGlobalCallWithStrippedType() {
    testSame("window.alert(goog.debug.Logger)");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType1
  public void testClassDefiningCallWithStripType1() {
    test("goog.debug.Logger.inherits(Object)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType2
  public void testClassDefiningCallWithStripType2() {
    test("goog.formatter=function(){};" +
         "goog.inherits(goog.debug.Formatter,goog.formatter)",
         "goog.formatter=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType3
  public void testClassDefiningCallWithStripType3() {
    test("goog.formatter=function(){};" +
         "goog.inherits(goog.formatter,goog.debug.Formatter)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType4
  public void testClassDefiningCallWithStripType4() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType5
  public void testClassDefiningCallWithStripType5() {
    testSame("goog.formatter=function(){};" +
             "goog.formatter.inherits(goog.debug.FormatterFoo)");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType6
  public void testClassDefiningCallWithStripType6() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter.Foo)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType7
  public void testClassDefiningCallWithStripType7() {
    test("goog.inherits(goog.debug.TextFormatter,goog.debug.Formatter)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType8
  public void testClassDefiningCallWithStripType8() {
    
    test("goog.debug.DebugWindow = function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow,Base)", "");

    
    
    testSame("goog.debug.DebugWindowFoo=function(){}");
    testSame("goog.inherits(goog.debug.DebugWindowFoo,Base)");
    testSame("goog.debug.DebugWindowFoo");
    testSame("goog.debug.DebugWindowFoo=1");

    
    test("goog.debug.DebugWindow.Foo=function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow.Foo,Base)", "");
    test("goog.debug.DebugWindow.Foo", "");
    test("goog.debug.DebugWindow.Foo=1", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPropertyWithEmptyStringKey
  public void testPropertyWithEmptyStringKey() {
    test("goog.format.NUMERIC_SCALES_BINARY_ = {'': 1};",
         "goog.format.NUMERIC_SCALES_BINARY_={\"\":1}");
  }

// com.google.javascript.jscomp.StripCodeTest::testVarinIf
  public void testVarinIf() {
    test("if(x)var logger=null;else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testGetElemInIf
  public void testGetElemInIf() {
    test("var logger=null;if(x)logger[f];else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testAssignInIf
  public void testAssignInIf() {
    test("var logger=null;if(x)logger=1;else foo()",
         "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testNamePrefix
  public void testNamePrefix() {
    test("a = function(traceZZZ) {}; a.prototype.traceXXX = {x: 1};" +
         "a.prototype.z = function() { this.traceXXX.f(); };" +
         "var traceYYY = 0;",
         "a=function(traceZZZ){};a.prototype.z=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testTypePrefix
  public void testTypePrefix() {
    test("e.f.TraceXXX = function() {}; " +
         "e.f.TraceXXX.prototype.yyy = 2;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames
  public void testStripCallsToStrippedNames() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames
  public void testStripVarsInitializedFromStrippedNames() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_(); if (one) foo() }",
          "a=function(){};a.prototype.b=function(){if(null)foo()}");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testBadRead
  public void testBadRead() {
    badRead("window.doStuff();");
    badRead("window.Alert('case-sensitive');");
    badRead("function foo(x) { return 'wee' + x.bad }; foo(5);");
    badRead("var p = {x:1, y:2}; alert(p.z);");

    
    
    badRead("window._unknownExportedMethod()");

    
    badRead("var p = {x:1, y:1}; alert(p.y.z.x);");
    badRead("var p = {x:1, y:1}; alert(p.z.y.x);");

    
    badRead("var p = {x:1}; p.bad.x = 2; alert(p.x);");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testBadWrite
  public void testBadWrite() {
    badWrite("function F() { this.x = 1; this.y = 2; } alert((new F()).x);");
    badWrite("var x = {}; x.a = 1; x.b = 2; alert(x.b);");
    badWrite("var p = {x:1}; p.x.y = 2;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testNoProblem
  public void testNoProblem() {
    
    
    
    
    noProb("function foo(a, b) {" +
           "  a.x = b.y;" +
           "}" +
           "var aa = {};" +
           "var bb = {};" +
           "bb.y = 2;" +
           "foo(aa, bb);" +
           "alert(aa.x);");

    
    noProb("var x = {}; x.f = 'foo'; alert(x.f);");

    
    noProb("function P() { this.x = 0;} alert((new P()).x);");
    noProb("alert((new P()).x); function P() { this.x = 0;}");

    
    noProb("function foo(win) { win.alert('foo') }");

    
    noProb("function Foo(){}" +
           "foo.prototype.baz = function(){ alert(99) };" +
           "var f = new Foo();" +
           "f.baz();");
    noProb("var x = 'apples'; alert(x.indexOf(e));");
    noProb("window.alert(1999)");

    
    noProb("var x = {a:1, b:2}; alert(x.a + x.b);");

    
    noProb("var x = {a:1, b:2}; alert(x.a);");

    
    noProb("var x = {}; x.y = {}; x.y.z = ':-)'; alert(x.y.z);");

    noProb("");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testNoWarningForDuckProperty
  public void testNoWarningForDuckProperty() {
    noProb("var x = {}; x.prop; if (x.prop) {}");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testReadPropertySetByGeneratedCode
  public void testReadPropertySetByGeneratedCode() {
    noProb("var o = {}; o[JSCompiler_renameProperty('x')] = 1; o.x;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testReadPropertyReferencedByGeneratedCode
  public void testReadPropertyReferencedByGeneratedCode() {
    
    noProb("var o = {}; JSCompiler_renameProperty('x'); o.x;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testSetPropertyReadByGeneratedCode
  public void testSetPropertyReadByGeneratedCode() {
    noProb("var o = {x: 1}; o[JSCompiler_renameProperty('x')];");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testSetPropertyReferencedByGeneratedCode
  public void testSetPropertyReferencedByGeneratedCode() {
    
    noProb("var o = {x: 1}; JSCompiler_renameProperty('x');");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testPropertiesReferencedByGeneratedCode
  public void testPropertiesReferencedByGeneratedCode() {
    
    
    noProb("var o = {x: 1}; JSCompiler_renameProperty('x.y'); o.y;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testReadPropertySetByExternObjectLiteral
  public void testReadPropertySetByExternObjectLiteral() {
    noProb("var g = google.gears.workerPool;");
  }

// com.google.javascript.jscomp.SymbolTableTest::testOk
  public void testOk() throws Exception {
    
    
    test("var x = 3;", "");
  }

// com.google.javascript.jscomp.SymbolTableTest::testCountMismatch
  public void testCountMismatch() throws Exception {
    setExpectedSymbolTableError(VARIABLE_COUNT_MISMATCH);
    test("var x = 3, y = 5;", "");
  }

// com.google.javascript.jscomp.SymbolTableTest::testMovedVariable
  public void testMovedVariable() throws Exception {
    setExpectedSymbolTableError(MOVED_VARIABLE);
    test("var x = 3, y = 5;", "var x, y = 5;");
  }

// com.google.javascript.jscomp.SymbolTableTest::testMissingVariable
  public void testMissingVariable() throws Exception {
    setExpectedSymbolTableError(MISSING_VARIABLE);
    test("var x = 3, y = 5;", "var z = 3, y = 5;");
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testFunctionScope
  public void testFunctionScope() {
    Scope scope = getScope("function foo() {}\n" +
                           "var x = function bar(a1) {};" +
                           "[function bar2() { var y; }];" +
                           "if (true) { function z() {} }"
                           );
    assertTrue(scope.isDeclared("foo", false));
    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("z", false));

    
    assertFalse(scope.isDeclared("a1", false));
    assertFalse(scope.isDeclared("bar", false));
    assertFalse(scope.isDeclared("bar2", false));
    assertFalse(scope.isDeclared("y", false));
    assertFalse(scope.isDeclared("", false));
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testScopeRootNode
  public void testScopeRootNode() {
    String js = "function foo() {\n" +
        " var x = 10;" +
        "}";
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());

    Scope globalScope =
        new SyntacticScopeCreator(compiler).createScope(root, null);
    assertEquals(root, globalScope.getRootNode());

    Node fooNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fooNode.getType());
    Scope fooScope =
        new SyntacticScopeCreator(compiler).createScope(fooNode, null);
    assertEquals(fooNode, fooScope.getRootNode());
    assertTrue(fooScope.isDeclared("x", false));
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testRedeclaration1
  public void testRedeclaration1() {
     String js = "var a; var a;";
     int errors = createGlobalScopeHelper(js);
     assertEquals(1, errors);
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testRedeclaration2
  public void testRedeclaration2() {
    String js = "var a;  var a;";
    int errors = createGlobalScopeHelper(js);
    assertEquals(0, errors);
 }

// com.google.javascript.jscomp.TightenTypesTest::testTopLevelVariables
  public void testTopLevelVariables() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo();\n"
             + "var b = a;\n");

    assertTrue(getType("Foo").isFunction());
    assertTrue(getType("a").isInstance());
    assertType("function (this:Foo): ()", getType("Foo"));
    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "a = new Bar();\n"
             + "var b = a;\n");

    assertTrue(getType("a").isUnion());
    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNamespacedVariables
  public void testNamespacedVariables() {
    testSame("var goog = goog || {}; goog.foo = {};\n"
             + " goog.foo.Foo = function() {};\n"
             + "goog.foo.Foo.prototype.blah = function() {};\n"
             + " goog.foo.Bar = function() {};\n"
             + "goog.foo.Bar.prototype.blah = function() {};\n"
             + "function bar(a) { a.blah(); }\n"
             + "var baz = bar;\n"
             + "bar(new goog.foo.Foo);\n"
             + "baz(new goog.foo.Bar);\n");

    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("bar"), 0));
    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testReturnSlot
  public void testReturnSlot() {
    testSame(" function Foo() {}\n"
             + "function bar() {\n"
             + "  var a = new Foo();\n"
             + "  return a;\n"
             + "}\n"
             + "var b = bar();\n");

    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testParameterSlots
  public void testParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a, b) {}\n"
             + "bar(new Foo, new Foo);\n"
             + "bar(new Bar, null);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("Foo", getParamType(getType("bar"), 1));
    assertNull(getParamVar(getType("bar"), 2));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAliasedFunction
  public void testAliasedFunction() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a) {}\n"
             + "var baz = bar;\n"
             + "bar(new Foo);\n"
             + "baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("(Bar,Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCatchStatement
  public void testCatchStatement() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Bar() {}\n"
             + "function bar() { try { } catch (e) { return e; } }\n"
             + " function ID10TError() {}\n"
             + "var a = bar(); throw new ID10TError();\n", null, null);

    assertType("(Error,EvalError,ID10TError,RangeError,ReferenceError,"
        + "SyntaxError,TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testConstructorParameterSlots
  public void testConstructorParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz(a) {}\n"
             + "new Baz(new Foo);\n"
             + "new Baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("Baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallSlot
  public void testCallSlot() {
    testSame("function foo() {}\n"
             + "function bar() {}\n"
             + "function baz() {}\n"
             + "var a = foo;\n"
             + "a = bar;\n"
             + "a();\n");

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertFalse(isCalled(getType("baz")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testObjectLiteralTraversal
  public void testObjectLiteralTraversal() {
    testSame("var foo = function() {}\n"
             + "function bar() { return { 'a': foo()} };\n"
             + "bar();");
    assertTrue(isCalled(getType("foo")));
   }

// com.google.javascript.jscomp.TightenTypesTest::testThis
  public void testThis() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this; }\n"
             + "var a = new Foo();\n"
             + "var b = a.foo();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAssign
  public void testAssign() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "var b = a = new Bar();\n");

    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testComma
  public void testComma() {
    testSame(" function Foo() {b=new Foo()}\n"
             + "var b;"
             + " function Bar() {}\n"
             + "var a = (new Foo, new Bar);\n");

    assertType("Bar", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAnd
  public void testAnd() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (new Foo && new Bar);\n");

    assertType("Bar", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testOr
  public void testOr() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " var f = new Foo();\n"
             + " var b = new Bar();\n"
             + "var a = (f || b);\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testHook
  public void testHook() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (1+1 == 2) ? new Foo : new Bar;\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionLiteral
  public void testFunctionLiteral() {
    testSame(" function Foo() {}\n"
             + "var a = (function() { return new Foo; })();\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNameLookup
  public void testNameLookup() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo;\n"
             + "var b = (function() { return a; })();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetProp
  public void testGetProp() {
    testSame(" function Foo() {\n"
             + "  this.foo = new A();\n"
             + "}\n"
             + " function Bar() {\n"
             + "  this.foo = new B();\n"
             + "}\n"
             + " function Baz() {}\n"
             + " function A() {}\n"
             + " function B() {}\n"
             
             + " var foo = new Foo();\n"
             + " var bar = new Bar();\n"
             + " var baz = new Baz();\n" 
             + "var a = foo || bar || baz\n"
             + "var b = a.foo;\n");

    assertType("(A,B)", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetPrototypeProperty
  public void testGetPrototypeProperty() {
    testSame(" function Foo() {};\n"
             + " function Bar() {};\n"
             + "Bar.prototype.a = new Foo();\n"
             + "var a = Bar.prototype.a;\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem
  public void testGetElem() {
    testSame(""
             + "function Array(var_args) {}\n",
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = [];\n"
             + "a[0] = new Foo;\n"
             + "a[1] = new Bar;\n"
             + "var b = a[0];\n"
             + "var c = [new Foo, new Bar];\n", null);

    assertType("Array", getType("a"));
    assertType("(Array,Bar,Foo)", getType("b"));
    assertType("Array", getType("c"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "var b = new Baz;\n"
             + "b.arr[0] = new Foo;\n"
             + "b.arr[1] = new Bar;\n"
             + "var c = b.arr;\n");

    assertType("Array", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem3
  public void testGetElem3() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "function foo(anarr) {"
             + "}\n"
             + "var ar = [];\n"
             + "foo(ar);\n", null);

    assertType("Array", getType("ar"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testScopeDiscovery
  public void testScopeDiscovery() {
    testSame("function spam() {}\n"
             + "function foo() {}\n"
             + "function bar() {\n"
             + "  return function() { foo(); };\n"
             + "}"
             + "function baz() {\n"
             + "  return function() { bar()(); };\n"
             + "}"
             + "baz()()();\n");

    assertFalse(isCalled(getType("spam")));
    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSheqDiscovery
  public void testSheqDiscovery() {
    testSame("function spam() {}\n"
             + "\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo1 = function() { f1(); }\n"
             + "Foo.prototype.foo2 = function() { f2(); }\n"
             + "Foo.prototype.foo3 = function() { f3(); }\n"
             + "function baz(a) {\n"
             + "  a === null || a instanceof Foo ?\n"
             + "  Foo.prototype.foo1.call(this) :\n"
             + "  Foo.prototype.foo2.call(this);\n"
             + "}\n"
             + "function f1() {}\n"
             + "function f2() {}\n"
             + "function f3() {}\n"
             + "baz(3);\n");

    assertFalse(isCalled(getType("spam")));
    assertFalse(isCalled(getType("f3")));
    assertTrue(isCalled(getType("f1")));
    assertTrue(isCalled(getType("f2")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSubclass
  public void testSubclass() {
    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Foo()).foo()();\n"
             + "a = (new Bar()).foo()();\n");

    ConcreteType fooType =
        getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("(Bar,Foo)", getThisType(fooType));
    assertType("(A,B)", getType("a"));

    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Bar()).foo()();\n");

    fooType = getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("Bar", getThisType(fooType));
    assertType("B", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testArrayAssignments
  public void testArrayAssignments() {
    testSame(" function Foo() {}\n"
             + "var a = [];\n"
             + "function foo() { return []; }\n"
             + "(a.length == 0 ? a : foo())[0] = new Foo;\n"
             + "var b = a[0];\n"
             + "var c = foo()[0];\n");

    assertType("(Array,Foo)", getType("b"));
    assertType("(Array,Foo)", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAllPropertyReference
  public void testAllPropertyReference() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.prop = function() { this.prop2(); }\n"
             + "Foo.prototype.prop2 = function() { b = new Foo; }\n"
             + "var a = new Foo;\n"
             + "a = [][0];\n"
             + "function fun(a) {\n"
             + "  return a.prop();\n"
             + "}\n"
             + "var b;\n"
             + "fun(a);\n"
             );

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunction
  public void testCallFunction() {
    testSame(" function Foo() { this.a = new A; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this);\n"
             + "}\n"
             + " function A() {};\n"
             + "new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));
    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunctionWithArgs
  public void testCallFunctionWithArgs() {
    testSame(" function Foo(o) { this.a = o; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this, new A());\n"
             + "}\n"
             + " function A() {};\n"
             + "var b = new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));

    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunction
  public void testCallPrototypeFunction() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.a = function() { return new A; }\n"
             + "Foo.prototype.a = function() { return new A; };\n"
             + " function Bar() {}\n"
             + ""
             + "Bar.prototype.a = function() { return new B; };\n"
             + " function A() {};\n"
             + " function B() {};\n"
             + "var ret = Foo.prototype.a.call(new Bar);");

    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunctionWithArgs
  public void testCallPrototypeFunctionWithArgs() {
    testSame(" function Foo() { this.p = null }\n"
             + "Foo.prototype.set = function(arg) { this.p = arg; };\n"
             + "Foo.prototype.get = function() { return this.p; };\n"
             + " function A() {};\n"
             + "Foo.prototype.set.call(new Foo, new A);\n"
             + "var ret = Foo.prototype.get.call(new Foo);");

    ConcreteType fooP = getFunctionPrototype(getType("Foo"));
    ConcreteFunctionType gFun = getPropertyType(fooP, "get").toFunction();
    ConcreteFunctionType sFun = getPropertyType(fooP, "set").toFunction();

    assertTrue(isCalled(sFun));
    assertTrue(isCalled(gFun));
    assertTrue(isCalled(getType("A")));
    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSetTimeout
  public void testSetTimeout() {
    testSame(" function Window() {};\n"
             + "Window.prototype.setTimeout = function(f, t) {};\n"
             + " var window;",
             " function A() {}\n"
             + "A.prototype.handle = function() { foo(); };\n"
             + "function foo() {}\n"
             + "window.setTimeout((new A).handle, 3);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternType
  public void testExternType() {
    testSame(" function T() {};\n"
             + " function Ext() {};\n"
             + "\n"
             + "Ext.prototype.getT = function() {};\n"
             + " Ext.prototype.prop;\n"
             + " var ext;",
             "var b = ext.getT();\n"
             + "var p = ext.prop;", null);

    assertType("Ext", getType("ext"));
    assertType("T", getType("b"));
    assertType("T", getType("p"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypes
  public void testExternSubTypes() {
    testSame(" function A() {};\n"
             + " function B() {};\n"
             + " function C() {};\n"
             + " function D() {};\n"
             + " function Ext() {};\n"
             + " Ext.prototype.a;\n"
             + " Ext.prototype.b;\n"
             + " Ext.prototype.d;\n"
             + " Ext.prototype.getA = function() {};\n"
             + " Ext.prototype.getB = function() {};\n",
             "var a = (new Ext).a;\n"
             + "var a2 = (new Ext).getA();\n"
             + "var b = (new Ext).b;\n"
             + "var b2 = (new Ext).getB();\n"
             + "var d = (new Ext).d;\n", null);

    assertType("(A,B,C,D)", getType("a"));
    assertType("(A,B,C,D)", getType("a2"));
    assertType("(B,D)", getType("b"));
    assertType("(B,D)", getType("b2"));
    assertType("D", getType("d"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypesForObject
  public void testExternSubTypesForObject() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
             + " function A() {};\n"
             + " function B() {};\n"
             + " Object.prototype.eval = function(code) {};\n"
             + "\n"
             + "A.prototype.a;\n"
             + "\n"
             + "A.prototype.b = function(){};\n",
             "var a = (new A).b()", null, null);
    assertType("(A,Array,B,Boolean,Date,Error,EvalError,Function,Number,Object,"
               + "RangeError,ReferenceError,RegExp,String,SyntaxError,"
               + "TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCall
  public void testImplicitPropCall() {
    testSame(" function Window() {};\n"
             + "\n"
             + "Window.prototype.setTimeout = function(f, d) {};",
             "function foo() {};\n"
             + "(new Window).setTimeout(foo, 20);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCallWithArgs
  public void testImplicitPropCallWithArgs() {
    testSame(" function Window() {};\n"
             + " function EventListener() {};\n"
             + "\n"
             + "Window.prototype.addEventListener = function(t, f) {};\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Window).addEventListener('click', foo);", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testUntypedImplicitCallFromProperty
  public void testUntypedImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};"
             + " Event.prototype.erv;",
             " function foo(evt) { return bar(evt); };\n"
             + "function bar(a) { return a.type() }\n"
             + " var ar = new Element;\n"
             + "ar.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Event", getParamType(getType("bar"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromProperty
  public void testImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfUnion
  public void testImplicitCallFromPropertyOfUnion() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfAllType
  public void testImplicitCallFromPropertyOfAllType() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "var elems = [];\n"
             + "var elem = elems[0];\n" 
             + "elem.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCast
  public void testRestrictToCast() {
    testSame(" function Foo() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "var u = a[0];\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
    assertType("(Array,Foo)", getType("u"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToInterfaceCast
  public void testRestrictToInterfaceCast() {
    testSame(" function Foo() {};\n"
             + " function Int() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCastWithNonInstantiatedTypes
  public void testRestrictToCastWithNonInstantiatedTypes() {
    testSame(
             " function Super() {}\n"
             + " function Foo() {};\n"
             + "Foo.prototype.blah = function() { foofunc() };\n"
             + " function Bar() {};\n"
             + "Bar.prototype.blah = function() { barfunc() };\n"
             + "function barfunc() {}\n"
             + "function foofunc() {}\n"
             + "var a = [];\n"
             + "var u =  (a[0]);\n"
             + "u.blah()\n"
             + "new Foo");

    assertTrue(isCalled(getType("foofunc")));
    assertFalse(isCalled(getType("barfunc")));
    assertType("Array", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionToString
  public void testFunctionToString() {
    testSame(" function Foo() {}\n"
             + "\n"
             + "function Bar() { Foo.call(this); }\n"
             + "var a = function(a) { return new Foo; };\n;"
             + "a(new Foo);\n"
             + "a(new Bar);\n"
             + "new Bar;");

    assertType("function ((Bar,Foo)): Foo", getType("a"));
    assertType("function (this:(Bar,Foo)): ()", getType("Foo"));
    assertType("function (this:Bar): ()", getType("Bar"));
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionAritySimple
  public void testFunctionAritySimple() {
    assertOk("", "");
    assertOk("a", "'a'");
    assertOk("a,b", "10, 20");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithOptionalArgs
  public void testFunctionArityWithOptionalArgs() {
    assertOk("a,b,opt_c", "1,2");
    assertOk("a,b,opt_c", "1,2,3");
    assertOk("a,opt_b,opt_c", "1");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithVarArgs
  public void testFunctionArityWithVarArgs() {
    assertOk("var_args", "");
    assertOk("var_args", "1,2");
    assertOk("a,b,var_args", "1,2");
    assertOk("a,b,var_args", "1,2,3");
    assertOk("a,b,var_args", "1,2,3,4,5");
    assertOk("a,opt_b,var_args", "1");
    assertOk("a,opt_b,var_args", "1,2");
    assertOk("a,opt_b,var_args", "1,2,3");
    assertOk("a,opt_b,var_args", "1,2,3,4,5");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testWrongNumberOfArgs
  public void testWrongNumberOfArgs() {
    assertWarning("a,b,opt_c", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c", "1,2,3,4",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,c,d", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,var_args", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c,var_args", "1",
        WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testVarArgsLast
  public void testVarArgsLast() {
    assertWarning("a,b,var_args,c", "1,2,3,4",
        VAR_ARGS_MUST_BE_LAST);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testOptArgsLast
  public void testOptArgsLast() {
    assertWarning("a,b,opt_d,c", "1, 2, 3",
        OPTIONAL_ARG_AT_END);
    assertWarning("a,b,opt_d,c", "1, 2",
        OPTIONAL_ARG_AT_END);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc1
  public void testFunctionsWithJsDoc1() {
    testSame(" function foo(a,b,c) {} foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc2
  public void testFunctionsWithJsDoc2() {
    testSame(" function foo(a,b,c) {} foo(1,2,3);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc3
  public void testFunctionsWithJsDoc3() {
    testSame(" " +
             "function foo(a,b,c) {} foo(1);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc4
  public void testFunctionsWithJsDoc4() {
    testSame(" var foo = function(a) {}; foo();");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc5
  public void testFunctionsWithJsDoc5() {
    testSame(" var foo = function(a) {}; foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc6
  public void testFunctionsWithJsDoc6() {
    testSame(" var foo = function(a, b) {}; foo();",
             WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc7
  public void testFunctionsWithJsDoc7() {
    String fooDfn = " var foo = function(b) {};";
    testSame(fooDfn + "foo();");
    testSame(fooDfn + "foo(1);");
    testSame(fooDfn + "foo(1, 2);", WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionWithDefaultCodingConvention
  public void testFunctionWithDefaultCodingConvention() {
    convention = new DefaultCodingConvention();
    testSame("var foo = function(x) {}; foo(1, 2);");
    testSame("var foo = function(opt_x) {}; foo(1, 2);");
    testSame("var foo = function(var_args) {}; foo(1, 2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testMethodCalls
  public void testMethodCalls() {
    final String METHOD_DEFS =
      "\n" +
      "function Foo() {}" +
      
      "function twoArg(arg1, arg2) {};" +
      "Foo.prototype.prototypeMethod = twoArg;" +
      "Foo.staticMethod = twoArg;";
    
    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);

    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        new DefaultCodingConvention()).createInitialScope(null);

    assertEquals(ARRAY_FUNCTION_TYPE, s.getVar("Array").getType());
    assertEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        s.getVar("Boolean").getType());
    assertEquals(DATE_FUNCTION_TYPE, s.getVar("Date").getType());
    assertEquals(ERROR_FUNCTION_TYPE, s.getVar("Error").getType());
    assertEquals(EVAL_ERROR_FUNCTION_TYPE,
        s.getVar("EvalError").getType());
    assertEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        s.getVar("Number").getType());
    assertEquals(OBJECT_FUNCTION_TYPE, s.getVar("Object").getType());
    assertEquals(RANGE_ERROR_FUNCTION_TYPE,
        s.getVar("RangeError").getType());
    assertEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        s.getVar("ReferenceError").getType());
    assertEquals(REGEXP_FUNCTION_TYPE, s.getVar("RegExp").getType());
    assertEquals(STRING_OBJECT_FUNCTION_TYPE,
        s.getVar("String").getType());
    assertEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        s.getVar("SyntaxError").getType());
    assertEquals(TYPE_ERROR_FUNCTION_TYPE,
        s.getVar("TypeError").getType());
    assertEquals(URI_ERROR_FUNCTION_TYPE,
        s.getVar("URIError").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ foo()--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck7
  public void testTypeCheck7() throws Exception {
    testTypes("function foo() {delete 'abc';}",
        TypeCheck.BAD_DELETE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

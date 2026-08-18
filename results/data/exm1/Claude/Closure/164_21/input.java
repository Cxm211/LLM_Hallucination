// buggy code
  public boolean isSubtype(JSType other) {
    if (!(other instanceof ArrowType)) {
      return false;
    }

    ArrowType that = (ArrowType) other;

    // This is described in Draft 2 of the ES4 spec,
    // Section 3.4.7: Subtyping Function Types.

    // this.returnType <: that.returnType (covariant)
    if (!this.returnType.isSubtype(that.returnType)) {
      return false;
    }

    // that.paramType[i] <: this.paramType[i] (contravariant)
    //
    // If this.paramType[i] is required,
    // then that.paramType[i] is required.
    //
    // In theory, the "required-ness" should work in the other direction as
    // well. In other words, if we have
    //
    // function f(number, number) {}
    // function g(number) {}
    //
    // Then f *should* not be a subtype of g, and g *should* not be
    // a subtype of f. But in practice, we do not implement it this way.
    // We want to support the use case where you can pass g where f is
    // expected, and pretend that g ignores the second argument.
    // That way, you can have a single "no-op" function, and you don't have
    // to create a new no-op function for every possible type signature.
    //
    // So, in this case, g < f, but f !< g
    Node thisParam = parameters.getFirstChild();
    Node thatParam = that.parameters.getFirstChild();
    while (thisParam != null && thatParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType thatParamType = thatParam.getJSType();
      if (thisParamType != null) {
        if (thatParamType == null ||
            !thatParamType.isSubtype(thisParamType)) {
          return false;
        }
      }

      boolean thisIsVarArgs = thisParam.isVarArgs();
      boolean thatIsVarArgs = thatParam.isVarArgs();

      // "that" can't be a supertype, because it's missing a required argument.
        // NOTE(nicksantos): In our type system, we use {function(...?)} and
        // {function(...NoType)} to to indicate that arity should not be
        // checked. Strictly speaking, this is not a correct formulation,
        // because now a sub-function can required arguments that are var_args
        // in the super-function. So we special-case this.

      // don't advance if we have variable arguments
      if (!thisIsVarArgs) {
        thisParam = thisParam.getNext();
      }
      if (!thatIsVarArgs) {
        thatParam = thatParam.getNext();
      }

      // both var_args indicates the end
      if (thisIsVarArgs && thatIsVarArgs) {
        thisParam = null;
        thatParam = null;
      }
    }

    // "that" can't be a supertype, because it's missing a required arguement.

    return true;
  }

// relevant test
// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadExtendsWithNullable
  public void testBadExtendsWithNullable() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n * @extends {Object?} */",
        "Bad type annotation. expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(OBJECT_TYPE, jsdoc.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadImplementsWithNullable
  public void testBadImplementsWithNullable() throws Exception {
  JSDocInfo jsdoc = parse("@implements {Disposable?}\n * @constructor */",
      "Bad type annotation. expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(registry.createNamedType("Disposable", null, -1, -1),
        jsdoc.getImplementedInterfaces().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor1
  public void testBadTypeDefInterfaceAndConstructor1() throws Exception {
    JSDocInfo jsdoc = parse("@interface\n@constructor*/",
        "Bad type annotation. cannot be both an interface and a constructor");
    assertTrue(jsdoc.isInterface());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor2
  public void testBadTypeDefInterfaceAndConstructor2() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n@interface*/",
        "Bad type annotation. cannot be both an interface and a constructor");
    assertTrue(jsdoc.isConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationParameter
  public void testDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMultilineDocumentationParameter
  public void testMultilineDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description"
                + "\n* on multiple \n* lines.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description on multiple lines.",
                 jsdoc.getDescriptionForParameter("number42"));

  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter
  public void testDocumentationMultipleParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description.*/"
                , true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));

    assertTrue(jsdoc.hasDescriptionForParameter("number87"));
    assertEquals("This is another description.",
                 jsdoc.getDescriptionForParameter("number87"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter2
  public void testDocumentationMultipleParameter2() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {number} delta = 0 results in a redraw\n" +
                "  != 0 ..... */", true);
    assertTrue(jsdoc.hasDescriptionForParameter("delta"));
    assertEquals("= 0 results in a redraw != 0 .....",
                 jsdoc.getDescriptionForParameter("delta"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthors
  public void testAuthors() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description."
                + "\n* @author a@google.com (A Person)"
                + "\n* @author b@google.com (B Person)"
                + "\n* @author c@google.com (C Person)*/"
                , true);

    Collection<String> authors = jsdoc.getAuthors();

    assertTrue(authors != null);
    assertTrue(authors.size() == 3);

    assertContains(authors, "a@google.com (A Person)");
    assertContains(authors, "b@google.com (B Person)");
    assertContains(authors, "c@google.com (C Person)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress1
  public void testSuppress1() throws Exception {
    JSDocInfo info = parse("@suppress {x} */");
    assertEquals(Sets.newHashSet("x"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress2
  public void testSuppress2() throws Exception {
    JSDocInfo info = parse("@suppress {x|y|x|z} */");
    assertEquals(Sets.newHashSet("x", "y", "z"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress1
  public void testBadSuppress1() throws Exception {
    parse("@suppress {} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress2
  public void testBadSuppress2() throws Exception {
    parse("@suppress {x|} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress3
  public void testBadSuppress3() throws Exception {
    parse("@suppress {|x} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress4
  public void testBadSuppress4() throws Exception {
    parse("@suppress {x|y */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress5
  public void testBadSuppress5() throws Exception {
    parse("@suppress {x,y} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress6
  public void testBadSuppress6() throws Exception {
    parse("@suppress {x} \n * @suppress {y} */", "duplicate @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress7
  public void testBadSuppress7() throws Exception {
    parse("@suppress {impossible} */",
          "unknown @suppress parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies1
  public void testModifies1() throws Exception {
    JSDocInfo info = parse("@modifies {this} */");
    assertEquals(Sets.newHashSet("this"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies2
  public void testModifies2() throws Exception {
    JSDocInfo info = parse("@modifies {arguments} */");
    assertEquals(Sets.newHashSet("arguments"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies3
  public void testModifies3() throws Exception {
    JSDocInfo info = parse("@modifies {this|arguments} */");
    assertEquals(Sets.newHashSet("this", "arguments"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies4
  public void testModifies4() throws Exception {
    JSDocInfo info = parse("@param {*} x\n * @modifies {x} */");
    assertEquals(Sets.newHashSet("x"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies5
  public void testModifies5() throws Exception {
    JSDocInfo info = parse(
        "@param {*} x\n"
        + " * @param {*} y\n"
        + " * @modifies {x} */");
    assertEquals(Sets.newHashSet("x"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies6
  public void testModifies6() throws Exception {
    JSDocInfo info = parse(
        "@param {*} x\n"
        + " * @param {*} y\n"
        + " * @modifies {x|y} */");
    assertEquals(Sets.newHashSet("x", "y"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies1
  public void testBadModifies1() throws Exception {
    parse("@modifies {} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies2
  public void testBadModifies2() throws Exception {
    parse("@modifies {this|} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies3
  public void testBadModifies3() throws Exception {
    parse("@modifies {|this} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies4
  public void testBadModifies4() throws Exception {
    parse("@modifies {this|arguments */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies5
  public void testBadModifies5() throws Exception {
    parse("@modifies {this,arguments} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies6
  public void testBadModifies6() throws Exception {
    parse("@modifies {this} \n * @modifies {this} */",
        "conflicting @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies7
  public void testBadModifies7() throws Exception {
    parse("@modifies {impossible} */",
          "unknown @modifies parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies8
  public void testBadModifies8() throws Exception {
    parse("@modifies {this}\n"
        + "@nosideeffects */", "conflicting @nosideeffects tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies9
  public void testBadModifies9() throws Exception {
    parse("@nosideeffects\n"
        + "@modifies {this} */", "conflicting @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewSingleLine
  public void testFileOverviewSingleLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Hi mom! */");
    assertEquals("Hi mom!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewMultiLine
  public void testFileOverviewMultiLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Pie is \n * good! */");
    assertEquals("Pie is\n good!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewDuplicate
  public void testFileOverviewDuplicate() throws Exception {
    JSDocInfo jsdoc = parseFileOverview(
        "@fileoverview Pie \n * @fileoverview Cake */",
        "extra @fileoverview tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testReferences
  public void testReferences() throws Exception {
    JSDocInfo jsdoc
        = parse("@see A cool place!"
                + "\n* @see The world."
                + "\n* @see SomeClass#SomeMember"
                + "\n* @see A boring test case*/"
                , true);

    Collection<String> references = jsdoc.getReferences();

    assertTrue(references != null);
    assertTrue(references.size() == 4);

    assertContains(references, "A cool place!");
    assertContains(references, "The world.");
    assertContains(references, "SomeClass#SomeMember");
    assertContains(references, "A boring test case");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTags
  public void testSingleTags() throws Exception {
    JSDocInfo jsdoc
        = parse("@version Some old version"
                + "\n* @deprecated In favor of the new one!"
                + "\n* @return {SomeType} The most important object :-)*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTagsReordered
  public void testSingleTagsReordered() throws Exception {
    JSDocInfo jsdoc
        = parse("@deprecated In favor of the new one!"
                + "\n * @return {SomeType} The most important object :-)"
                + "\n * @version Some old version*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionDuplication
  public void testVersionDuplication() throws Exception {
    parse("* @version Some old version"
          + "\n* @version Another version*/", true,
          "conflicting @version tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionMissing
  public void testVersionMissing() throws Exception {
    parse("* @version */", true,
          "@version tag missing version information");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthorMissing
  public void testAuthorMissing() throws Exception {
    parse("* @author */", true,
          "@author tag missing author");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSeeMissing
  public void testSeeMissing() throws Exception {
    parse("* @see */", true,
          "@see tag missing description");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSourceName
  public void testSourceName() throws Exception {
    JSDocInfo jsdoc = parse("@deprecated */", true);
    assertEquals("testcode", jsdoc.getAssociatedNode().getSourceFileName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment
  public void testParseBlockComment() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment2
  public void testParseBlockComment2() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is *** multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is *** multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment3
  public void testParseBlockComment3() throws Exception {
    JSDocInfo jsdoc = parse("\n "
                            + "* hello world \n"
                            + "* @author abc@google.com */", true);

    assertEquals("hello world", jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers1
  public void testParseWithMarkers1() throws Exception {
    JSDocInfo jsdoc = parse("@author abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 0, 0),
        "abc@google.com", 7, 0, 21);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers2
  public void testParseWithMarkers2() throws Exception {
    JSDocInfo jsdoc = parse("@param {Foo} somename abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "abc@google.com", 21, 0, 37);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers3
  public void testParseWithMarkers3() throws Exception {
    JSDocInfo jsdoc =
        parse("@return {Foo} some long \n * multiline" +
              " \n * description */", true);

    JSDocInfo.Marker returnDoc =
        assertAnnotationMarker(jsdoc, "return", 0, 0);
    assertDocumentationInMarker(returnDoc,
        "some long multiline description", 13, 2, 15);
    assertEquals(8, returnDoc.getType().getPositionOnStartLine());
    assertEquals(12, returnDoc.getType().getPositionOnEndLine());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers4
  public void testParseWithMarkers4() throws Exception {
    JSDocInfo jsdoc =
        parse("@author foobar \n * @param {Foo} somename abc@google.com */",
              true);

    assertAnnotationMarker(jsdoc, "author", 0, 0);
    assertAnnotationMarker(jsdoc, "param", 1, 3);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers5
  public void testParseWithMarkers5() throws Exception {
    JSDocInfo jsdoc =
        parse("@return some long \n * multiline" +
              " \n * description */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "return", 0, 0),
        "some long multiline description", 8, 2, 15);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers6
  public void testParseWithMarkers6() throws Exception {
    JSDocInfo jsdoc =
        parse("@param x some long \n * multiline" +
              " \n * description */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "some long multiline description", 8, 2, 15);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames1
  public void testParseWithMarkerNames1() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription */", true);

    assertNameInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "name", 0, 18);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames2
  public void testParseWithMarkerNames2() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription \n" +
                            "* @param {AnotherType} anothername des */", true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 0, 0, 0),
            "name", 0, 18),
        "SomeType", 0, 7, 0, 16, true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 1, 2, 1),
            "anothername", 1, 23),
        "AnotherType", 1, 9, 1, 21, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames3
  public void testParseWithMarkerNames3() throws Exception {
    JSDocInfo jsdoc = parse(
        "@param {Some.Long.Type.\n *  Name} name somedescription */", true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 0, 0, 0),
            "name", 1, 10),
        "Some.Long.Type.Name", 0, 7, 1, 8, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithoutMarkerName
  public void testParseWithoutMarkerName() throws Exception {
    JSDocInfo jsdoc = parse("@author helloworld*/", true);
    assertNull(assertAnnotationMarker(jsdoc, "author", 0, 0).getName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType
  public void testParseWithMarkerType() throws Exception {
    JSDocInfo jsdoc = parse("@extends {FooBar}*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 0, 9, 0, 16, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType2
  public void testParseWithMarkerType2() throws Exception {
    JSDocInfo jsdoc = parse("@extends FooBar*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 0, 9, 0, 15, false);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict1
  public void testTypeTagConflict1() throws Exception {
    parse("@constructor \n * @constructor */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict2
  public void testTypeTagConflict2() throws Exception {
    parse("@interface \n * @interface */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict3
  public void testTypeTagConflict3() throws Exception {
    parse("@constructor \n * @interface */",
        "Bad type annotation. cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict4
  public void testTypeTagConflict4() throws Exception {
    parse("@interface \n * @constructor */",
        "Bad type annotation. cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict5
  public void testTypeTagConflict5() throws Exception {
    parse("@interface \n * @type {string} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict6
  public void testTypeTagConflict6() throws Exception {
    parse("@typedef {string} \n * @type {string} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict7
  public void testTypeTagConflict7() throws Exception {
    parse("@typedef {string} \n * @constructor */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict8
  public void testTypeTagConflict8() throws Exception {
    parse("@typedef {string} \n * @return {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict9
  public void testTypeTagConflict9() throws Exception {
    parse("@enum {string} \n * @return {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict10
  public void testTypeTagConflict10() throws Exception {
    parse("@this {Object} \n * @enum {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict11
  public void testTypeTagConflict11() throws Exception {
    parse("@param {Object} x \n * @type {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict12
  public void testTypeTagConflict12() throws Exception {
    parse("@typedef {boolean} \n * @param {Object} x */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict13
  public void testTypeTagConflict13() throws Exception {
    parse("@typedef {boolean} \n * @extends {Object} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict14
  public void testTypeTagConflict14() throws Exception {
    parse("@return x \n * @return y */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateTypeNameMissing
  public void testParserWithTemplateTypeNameMissing() {
    parse("@template */",
        "Bad type annotation. @template tag missing type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateDuplicated
  public void testParserWithTemplateDuplicated() {
    parse("@template T\n@template V */",
        "Bad type annotation. @template tag at most once");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedNewAnnotations
  public void testWhitelistedNewAnnotations() {
    parse("@foobar */",
        "illegal use of unknown JSDoc tag \"foobar\"; ignoring it");
    extraAnnotations.add("foobar");
    parse("@foobar */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedConflictingAnnotation
  public void testWhitelistedConflictingAnnotation() {
    extraAnnotations.add("param");
    JSDocInfo info = parse("@param {number} index */");
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNonIdentifierAnnotation
  public void testNonIdentifierAnnotation() {
    
    
    extraAnnotations.add("123");
    parse("@123 */", "illegal use of unknown JSDoc tag \"\"; ignoring it");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax1
  public void testUnsupportedJsDocSyntax1() {
    JSDocInfo info =
        parse("@param {string} [accessLevel=\"author\"] The user level */",
            true);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("accessLevel"));
    assertEquals("The user level",
        info.getDescriptionForParameter("accessLevel"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax2
  public void testUnsupportedJsDocSyntax2() {
    JSDocInfo info =
        parse("@param userInfo The user info. \n" +
              " * @param userInfo.name The name of the user */", true);
    assertEquals(1, info.getParameterCount());
    assertEquals("The user info.",
        info.getDescriptionForParameter("userInfo"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedAnnotations
  public void testWhitelistedAnnotations() {
    parse(
      "* @addon \n" +
      "* @augments \n" +
      "* @base \n" +
      "* @borrows \n" +
      "* @bug \n" +
      "* @class \n" +
      "* @config \n" +
      "* @constructs \n" +
      "* @default \n" +
      "* @description \n" +
      "* @event \n" +
      "* @example \n" +
      "* @exception \n" +
      "* @exec \n" +
      "* @externs \n" +
      "* @field \n" +
      "* @function \n" +
      "* @id \n" +
      "* @ignore \n" +
      "* @inner \n" +
      "* @lends {string} \n" +
      "* @link \n" +
      "* @member \n" +
      "* @memberOf \n" +
      "* @modName \n" +
      "* @mods \n" +
      "* @name \n" +
      "* @namespace \n" +
      "* @nocompile \n" +
      "* @property \n" +
      "* @requires \n" +
      "* @since \n" +
      "* @static \n" +
      "* @supported */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testGetOriginalCommentString
  public void testGetOriginalCommentString() throws Exception {
    String comment = "* @desc This is a comment */";
    JSDocInfo info = parse(comment);
    assertNull(info.getOriginalCommentString());
    info = parse(comment, true );
    assertEquals(comment, info.getOriginalCommentString());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign1
  public void testLinenoCharnoAssign1() throws Exception {
    Node assign = parse("a = b").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(1, assign.getLineno());
    assertEquals(0, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign2
  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(2, assign.getLineno());
    assertEquals(1, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoCall
  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp1
  public void testLinenoCharnoGetProp1() throws Exception {
    Node getprop = parse("\n foo.bar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(2, name.getLineno());
    assertEquals(5, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp2
  public void testLinenoCharnoGetProp2() throws Exception {
    Node getprop = parse("\n foo.\nbar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(3, name.getLineno());
    assertEquals(0, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem1
  public void testLinenoCharnoGetelem1() throws Exception {
    Node call = parse("\n foo[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem2
  public void testLinenoCharnoGetelem2() throws Exception {
    Node call = parse("\n   \n foo()[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem3
  public void testLinenoCharnoGetelem3() throws Exception {
    Node call = parse("\n   \n (8 + kl)[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(2, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoForComparison
  public void testLinenoCharnoForComparison() throws Exception {
    Node lt =
      parse("for (; i < j;){}").getFirstChild().getFirstChild().getNext();

    assertEquals(Token.LT, lt.getType());
    assertEquals(1, lt.getLineno());
    assertEquals(7, lt.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoHook
  public void testLinenoCharnoHook() throws Exception {
    Node n = parse("\n a ? 9 : 0").getFirstChild().getFirstChild();

    assertEquals(Token.HOOK, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(1, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoArrayLiteral
  public void testLinenoCharnoArrayLiteral() throws Exception {
    Node n = parse("\n  [8, 9]").getFirstChild().getFirstChild();

    assertEquals(Token.ARRAYLIT, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(2, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(3, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(6, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoObjectLiteral
  public void testLinenoCharnoObjectLiteral() throws Exception {
    Node n = parse("\n\n var a = {a:0\n,b :1};")
        .getFirstChild().getFirstChild().getFirstChild();

    assertEquals(Token.OBJECTLIT, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(9, n.getCharno());

    Node key = n.getFirstChild();

    assertEquals(Token.STRING, key.getType());
    assertEquals(3, key.getLineno());
    assertEquals(10, key.getCharno());

    Node value = key.getFirstChild();

    assertEquals(Token.NUMBER, value.getType());
    assertEquals(3, value.getLineno());
    assertEquals(12, value.getCharno());

    key = key.getNext();

    assertEquals(Token.STRING, key.getType());
    assertEquals(4, key.getLineno());
    assertEquals(1, key.getCharno());

    value = key.getFirstChild();

    assertEquals(Token.NUMBER, value.getType());
    assertEquals(4, value.getLineno());
    assertEquals(4, value.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAdd
  public void testLinenoCharnoAdd() throws Exception {
    testLinenoCharnoBinop("+");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoSub
  public void testLinenoCharnoSub() throws Exception {
    testLinenoCharnoBinop("-");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMul
  public void testLinenoCharnoMul() throws Exception {
    testLinenoCharnoBinop("*");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoDiv
  public void testLinenoCharnoDiv() throws Exception {
    testLinenoCharnoBinop("/");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMod
  public void testLinenoCharnoMod() throws Exception {
    testLinenoCharnoBinop("%");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoShift
  public void testLinenoCharnoShift() throws Exception {
    testLinenoCharnoBinop("<<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryAnd
  public void testLinenoCharnoBinaryAnd() throws Exception {
    testLinenoCharnoBinop("&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAnd
  public void testLinenoCharnoAnd() throws Exception {
    testLinenoCharnoBinop("&&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryOr
  public void testLinenoCharnoBinaryOr() throws Exception {
    testLinenoCharnoBinop("|");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoOr
  public void testLinenoCharnoOr() throws Exception {
    testLinenoCharnoBinop("||");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLt
  public void testLinenoCharnoLt() throws Exception {
    testLinenoCharnoBinop("<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLe
  public void testLinenoCharnoLe() throws Exception {
    testLinenoCharnoBinop("<=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGt
  public void testLinenoCharnoGt() throws Exception {
    testLinenoCharnoBinop(">");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGe
  public void testLinenoCharnoGe() throws Exception {
    testLinenoCharnoBinop(">=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment1
  public void testJSDocAttachment1() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment2
  public void testJSDocAttachment2() {
    Node varNode = parse("var a,b;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode1 = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode1.getType());
    assertNull(nameNode1.getJSDocInfo());

    
    Node nameNode2 = nameNode1.getNext();
    assertEquals(Token.NAME, nameNode2.getType());
    assertNull(nameNode2.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment3
  public void testJSDocAttachment3() {
    Node assignNode = parse(
        "goog.FOO = 5;").getFirstChild().getFirstChild();

    
    assertEquals(Token.ASSIGN, assignNode.getType());
    JSDocInfo info = assignNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment4
  public void testJSDocAttachment4() {
    Node varNode = parse(
        "var a, b = 5;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNull(a.getJSDocInfo());

    
    Node b = a.getNext();
    JSDocInfo info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment5
  public void testJSDocAttachment5() {
    Node varNode = parse(
        "var a, b = 5;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNotNull(a.getJSDocInfo());
    JSDocInfo info = a.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node b = a.getNext();
    info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment6
  public void testJSDocAttachment6() throws Exception {
    Node functionNode = parse(
        "var a = 5;" +
        "function f(index){}")
        .getFirstChild().getNext();

    assertEquals(Token.FUNCTION, functionNode.getType());
    JSDocInfo info = functionNode.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.hasParameter("index"));
    assertTrue(info.hasReturnType());
    assertTypeEquals(UNKNOWN_TYPE, info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment7
  public void testJSDocAttachment7() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment8
  public void testJSDocAttachment8() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment9
  public void testJSDocAttachment9() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment10
  public void testJSDocAttachment10() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment11
  public void testJSDocAttachment11() {
    Node varNode =
       parse("var a;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);

    assertTypeEquals(createRecordTypeBuilder().
                     addProperty("x", NUMBER_TYPE, null).
                     addProperty("y", STRING_TYPE, null).
                     addProperty("z", UNKNOWN_TYPE, null).
                     build(),
                     info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment12
  public void testJSDocAttachment12() {
    Node varNode =
       parse("var a = { b: c};")
        .getFirstChild();
    Node objectLitNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLitNode.getType());
    assertNotNull(objectLitNode.getFirstChild().getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment13
  public void testJSDocAttachment13() {
    Node varNode = parse(" var a;").getFirstChild();
    assertNotNull(varNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment14
  public void testJSDocAttachment14() {
    Node varNode = parse(" var a;").getFirstChild();
    assertNull(varNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment15
  public void testJSDocAttachment15() {
    Node varNode = parse(" var a;").getFirstChild();
    assertNull(varNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment16
  public void testJSDocAttachment16() {
    Node exprCall =
        parse(" x(); function f() {};").getFirstChild();
    assertEquals(Token.EXPR_RESULT, exprCall.getType());
    assertNull(exprCall.getNext().getJSDocInfo());
    assertNotNull(exprCall.getFirstChild().getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing1
  public void testIncorrectJSDocDoesNotAlterJSParsing1() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing2
  public void testIncorrectJSDocDoesNotAlterJSParsing2() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing3
  public void testIncorrectJSDocDoesNotAlterJSParsing3() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing4
  public void testIncorrectJSDocDoesNotAlterJSParsing4() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing5
  public void testIncorrectJSDocDoesNotAlterJSParsing5() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing6
  public void testIncorrectJSDocDoesNotAlterJSParsing6() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            "Bad type annotation. expected closing }",
            "Bad type annotation. expecting a variable name in a @param tag"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing7
  public void testIncorrectJSDocDoesNotAlterJSParsing7() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@see tag missing description"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing8
  public void testIncorrectJSDocDoesNotAlterJSParsing8() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@author tag missing author"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing9
  public void testIncorrectJSDocDoesNotAlterJSParsing9() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
              "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "illegal use of unknown JSDoc tag \"someillegaltag\";"
              + " ignoring it"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnescapedSlashInRegexpCharClass
  public void testUnescapedSlashInRegexpCharClass() throws Exception {
    
    parse("var foo = /[/]/;");
    parse("var foo = /[hi there/]/;");
    parse("var foo = /[/yo dude]/;");
    parse("var foo = /\\/[@#$/watashi/wa/suteevu/desu]/;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testParse
  public void testParse() {
    Node a = Node.newString(Token.NAME, "a");
    a.addChildToFront(Node.newString(Token.NAME, "b"));
    List<ParserResult> testCases = ImmutableList.of(
        new ParserResult(
            "3;",
            createScript(new Node(Token.EXPR_RESULT, Node.newNumber(3.0)))),
        new ParserResult(
            "var a = b;",
             createScript(new Node(Token.VAR, a))),
        new ParserResult(
            "\"hell\\\no\\ world\\\n\\\n!\"",
             createScript(new Node(Token.EXPR_RESULT,
             Node.newString(Token.STRING, "hello world!")))));

    for (ParserResult testCase : testCases) {
      assertNodeEquality(testCase.node, parse(testCase.code));
    }
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning1
  public void testTrailingCommaWarning1() {
    parse("var a = ['foo', 'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning2
  public void testTrailingCommaWarning2() {
    parse("var a = ['foo',,'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning3
  public void testTrailingCommaWarning3() {
    parse("var a = ['foo', 'bar',];", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = ['foo', 'bar',];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning4
  public void testTrailingCommaWarning4() {
    parse("var a = [,];", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = [,];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning5
  public void testTrailingCommaWarning5() {
    parse("var a = {'foo': 'bar'};");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning6
  public void testTrailingCommaWarning6() {
    parse("var a = {'foo': 'bar',};", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = {'foo': 'bar',};");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning7
  public void testTrailingCommaWarning7() {
    parseError("var a = {,};", BAD_PROPERTY_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning1
  public void testSuspiciousBlockCommentWarning1() {
    parse(" var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning2
  public void testSuspiciousBlockCommentWarning2() {
    parse(" var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testCatchClauseForbidden
  public void testCatchClauseForbidden() {
    parseError("try { } catch (e if true) {}",
        "Catch clauses are not supported");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testConstForbidden
  public void testConstForbidden() {
    parseError("const x = 3;", "Unsupported syntax: CONST");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden
  public void testDestructuringAssignForbidden() {
    parseError("var [x, y] = foo();", "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden2
  public void testDestructuringAssignForbidden2() {
    parseError("var {x, y} = foo();", "missing : after property id");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden3
  public void testDestructuringAssignForbidden3() {
    parseError("var {x: x, y: y} = foo();",
        "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden4
  public void testDestructuringAssignForbidden4() {
    parseError("[x, y] = foo();",
        "destructuring assignment forbidden",
        "invalid assignment target");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLetForbidden
  public void testLetForbidden() {
    parseError("function f() { let (x = 3) { alert(x); }; }",
        "missing ; before statement", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testYieldForbidden
  public void testYieldForbidden() {
    parseError("function f() { yield 3; }", "missing ; before statement");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testBracelessFunctionForbidden
  public void testBracelessFunctionForbidden() {
    parseError("var sq = function(x) x * x;",
        "missing { before function body");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGeneratorsForbidden
  public void testGeneratorsForbidden() {
    parseError("var i = (x for (x in obj));",
        "missing ) in parenthetical");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden1
  public void testGettersForbidden1() {
    parseError("var x = {get foo() { return 3; }};",
        "getters are not supported in Internet Explorer");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden2
  public void testGettersForbidden2() {
    parseError("var x = {get foo bar() { return 3; }};",
        "invalid property id");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden3
  public void testGettersForbidden3() {
    parseError("var x = {a getter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden4
  public void testGettersForbidden4() {
    parseError("var x = {\"a\" getter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden5
  public void testGettersForbidden5() {
    parseError("var x = {a: 2, get foo() { return 3; }};",
        "getters are not supported in Internet Explorer");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSettersForbidden
  public void testSettersForbidden() {
    parseError("var x = {set foo() { return 3; }};",
        "setters are not supported in Internet Explorer");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSettersForbidden2
  public void testSettersForbidden2() {
    parseError("var x = {a setter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc1
  public void testFileOverviewJSDoc1() {
    Node n = parse(" function Foo() {}");
    assertEquals(Token.FUNCTION, n.getFirstChild().getType());
    assertTrue(n.getJSDocInfo() != null);
    assertNull(n.getFirstChild().getJSDocInfo());
    assertEquals("Hi mom!",
        n.getJSDocInfo().getFileOverview());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDocDoesNotHoseParsing
  public void testFileOverviewJSDocDoesNotHoseParsing() {
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc2
  public void testFileOverviewJSDoc2() {
    Node n = parse(" " +
        " function Foo() {}");
    assertTrue(n.getJSDocInfo() != null);
    assertEquals("Hi mom!", n.getJSDocInfo().getFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo() != null);
    assertFalse(n.getFirstChild().getJSDocInfo().hasFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo().isConstructor());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testObjectLiteralDoc1
  public void testObjectLiteralDoc1() {
    Node n = parse("var x = { 1: 2};");

    Node objectLit = n.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLit.getType());

    Node number = objectLit.getFirstChild();
    assertEquals(Token.STRING, number.getType());
    assertNotNull(number.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDuplicatedParam
  public void testDuplicatedParam() {
    parse("function foo(x, x) {}", "Duplicate parameter name \"x\".");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGetter
  public void testGetter() {
    mode = LanguageMode.ECMASCRIPT3;
    parseError("var x = {get 1(){}};",
        "getters are not supported in Internet Explorer");
    parseError("var x = {get 'a'(){}};",
        "getters are not supported in Internet Explorer");
    parseError("var x = {get a(){}};",
        "getters are not supported in Internet Explorer");
    mode = LanguageMode.ECMASCRIPT5;
    parse("var x = {get 1(){}};");
    parse("var x = {get 'a'(){}};");
    parse("var x = {get a(){}};");
    parseError("var x = {get a(b){}};", "getters may not have parameters");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSetter
  public void testSetter() {
    mode = LanguageMode.ECMASCRIPT3;
    parseError("var x = {set 1(x){}};",
        "setters are not supported in Internet Explorer");
    parseError("var x = {set 'a'(x){}};",
        "setters are not supported in Internet Explorer");
    parseError("var x = {set a(x){}};",
        "setters are not supported in Internet Explorer");
    mode = LanguageMode.ECMASCRIPT5;
    parse("var x = {set 1(x){}};");
    parse("var x = {set 'a'(x){}};");
    parse("var x = {set a(x){}};");
    parseError("var x = {set a(){}};",
        "setters must have exactly one parameter");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLamestWarningEver
  public void testLamestWarningEver() {
    
    parse("var x =  (y);");
    parse("var x =  (y);");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnfinishedComment
  public void testUnfinishedComment() {
    parseError(" var x;");
    Node var = n.getFirstChild();
    assertNotNull(var.getJSDocInfo());
    assertEquals("This is a variable.",
        var.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnnamedFunctionStatement
  public void testUnnamedFunctionStatement() {
    
    parseError("function() {};", "unnamed function statement");
    parseError("if (true) { function() {}; }", "unnamed function statement");
    parse("function f() {};");
    
    parse("(function f() {});");
    parse("(function () {});");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testReservedKeywords
  public void testReservedKeywords() {
    boolean isIdeMode = false;

    mode = LanguageMode.ECMASCRIPT3;

    parseError("var boolean;", "missing variable name");
    parseError("function boolean() {};",
        "missing ( before function parameters.");
    parseError("boolean = 1;", "identifier is a reserved word");
    parseError("class = 1;", "identifier is a reserved word");
    parseError("public = 2;", "identifier is a reserved word");

    mode = LanguageMode.ECMASCRIPT5;

    parse("var boolean;");
    parse("function boolean() {};");
    parse("boolean = 1;");
    parseError("class = 1;", "identifier is a reserved word");
    parse("public = 2;");

    mode = LanguageMode.ECMASCRIPT5_STRICT;

    parse("var boolean;");
    parse("function boolean() {};");
    parse("boolean = 1;");
    parseError("class = 1;", "identifier is a reserved word");
    parseError("public = 2;", "identifier is a reserved word");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testKeywordsAsProperties
  public void testKeywordsAsProperties() {
    boolean isIdeMode = false;

    mode = LanguageMode.ECMASCRIPT3;

    parseError("var x = {function: 1};", "invalid property id");
    parseError("x.function;", "missing name after . operator");
    parseError("var x = {get x(){} };",
        "getters are not supported in Internet Explorer");
    parseError("var x = {get function(){} };", "invalid property id");
    parseError("var x = {get 'function'(){} };",
        "getters are not supported in Internet Explorer");
    parseError("var x = {get 1(){} };",
        "getters are not supported in Internet Explorer");
    parseError("var x = {set function(a){} };", "invalid property id");
    parseError("var x = {set 'function'(a){} };",
        "setters are not supported in Internet Explorer");
    parseError("var x = {set 1(a){} };",
        "setters are not supported in Internet Explorer");
    parseError("var x = {class: 1};", "invalid property id");
    parseError("x.class;", "missing name after . operator");
    parse("var x = {let: 1};");
    parse("x.let;");
    parse("var x = {yield: 1};");
    parse("x.yield;");

    mode = LanguageMode.ECMASCRIPT5;

    parse("var x = {function: 1};");
    parse("x.function;");
    parse("var x = {get function(){} };");
    parse("var x = {get 'function'(){} };");
    parse("var x = {get 1(){} };");
    parse("var x = {set function(a){} };");
    parse("var x = {set 'function'(a){} };");
    parse("var x = {set 1(a){} };");
    parse("var x = {class: 1};");
    parse("x.class;");
    parse("var x = {let: 1};");
    parse("x.let;");
    parse("var x = {yield: 1};");
    parse("x.yield;");

    mode = LanguageMode.ECMASCRIPT5_STRICT;

    parse("var x = {function: 1};");
    parse("x.function;");
    parse("var x = {get function(){} };");
    parse("var x = {get 'function'(){} };");
    parse("var x = {get 1(){} };");
    parse("var x = {set function(a){} };");
    parse("var x = {set 'function'(a){} };");
    parse("var x = {set 1(a){} };");
    parse("var x = {class: 1};");
    parse("x.class;");
    parse("var x = {let: 1};");
    parse("x.let;");
    parse("var x = {yield: 1};");
    parse("x.yield;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGetPropFunctionName
  public void testGetPropFunctionName() {
    parseError("function a.b() {}",
        "missing ( before function parameters.");
    parseError("var x = function a.b() {}",
        "missing ( before function parameters.");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGetPropFunctionNameIdeMode
  public void testGetPropFunctionNameIdeMode() {
    
    
    isIdeMode = true;
    parseError("function a.b() {}",
        "missing ( before function parameters.",
        "missing formal parameter",
        "missing ) after formal parameters",
        "missing { before function body",
        "syntax error",
        "missing ; before statement",
        "Unsupported syntax: ERROR",
        "Unsupported syntax: ERROR");
    parseError("var x = function a.b() {}",
        "missing ( before function parameters.",
        "missing formal parameter",
        "missing ) after formal parameters",
        "missing { before function body",
        "syntax error",
        "missing ; before statement",
        "missing ; before statement",
        "Unsupported syntax: ERROR",
        "Unsupported syntax: ERROR");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIdeModePartialTree
  public void testIdeModePartialTree() {
    Node partialTree = parseError("function Foo() {} f.",
        "missing name after . operator");
    assertNull(partialTree);

    isIdeMode = true;
    partialTree = parseError("function Foo() {} f.",
        "missing name after . operator");
    assertNotNull(partialTree);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testForEach
  public void testForEach() {
    parseError(
        "function f(stamp, status) {\n" +
        "  for each ( var curTiming in this.timeLog.timings ) {\n" +
        "    if ( curTiming.callId == stamp ) {\n" +
        "      curTiming.flag = status;\n" +
        "      break;\n" +
        "    }\n" +
        "  }\n" +
        "};",
        "unsupported language extension: for each");
  }

// com.google.javascript.rhino.JSDocInfoTest::testVisibilityOrdinal
  public void testVisibilityOrdinal() {
    assertEquals(0, PRIVATE.ordinal());
    assertEquals(1, PROTECTED.ordinal());
    assertEquals(2, PUBLIC.ordinal());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetType
  public void testSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertTypeEquals(STRING_TYPE, resolve(info.getType()));
    assertNull(info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypeAndVisibility
  public void testSetTypeAndVisibility() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));
    info.setVisibility(PROTECTED);

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertTypeEquals(STRING_TYPE, resolve(info.getType()));
    assertEquals(PROTECTED, info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnType
  public void testSetReturnType() {
    JSDocInfo info = new JSDocInfo();
    info.setReturnType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnTypeAndBaseType
  public void testSetReturnTypeAndBaseType() {
    JSDocInfo info = new JSDocInfo();
    info.setBaseType(
        new JSTypeExpression(
            new Node(Token.BANG, Node.newString("Number")), ""));
    info.setReturnType(fromString("string"));

    assertTypeEquals(NUMBER_OBJECT_TYPE,
        resolve(info.getBaseType()));
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetEnumParameterType
  public void testSetEnumParameterType() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertTypeEquals(STRING_TYPE,
        resolve(info.getEnumParameterType()));
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType
  public void testMultipleSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("number"));

    try {
      info.setReturnType(fromString("boolean"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertTypeEquals(NUMBER_TYPE, resolve(info.getType()));
    assertNull(info.getReturnType());
    assertNull(info.getEnumParameterType());
    assertNull(info.getTypedefType());
    assertTrue(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType2
  public void testMultipleSetType2() {
    JSDocInfo info = new JSDocInfo();

    info.setReturnType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getReturnType()));
    assertNull(info.getEnumParameterType());
    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertFalse(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType3
  public void testMultipleSetType3() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setReturnType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertNull(info.getReturnType());
    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getEnumParameterType()));
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypedefType
  public void testSetTypedefType() {
    JSDocInfo info = new JSDocInfo();
    info.setTypedefType(fromString("boolean"));

    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getTypedefType()));
    assertTrue(info.hasTypedefType());
    assertFalse(info.hasType());
    assertFalse(info.hasEnumParameterType());
    assertFalse(info.hasReturnType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetConstant
  public void testSetConstant() {
    JSDocInfo info = new JSDocInfo();
    info.setConstant(true);

    assertFalse(info.hasType());
    assertTrue(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetConstructor
  public void testSetConstructor() {
    JSDocInfo info = new JSDocInfo();
    info.setConstructor(true);

    assertFalse(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetDefine
  public void testSetDefine() {
    JSDocInfo info = new JSDocInfo();
    info.setDefine(true);

    assertTrue(info.isConstant());
    assertFalse(info.isConstructor());
    assertTrue(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetHidden
  public void testSetHidden() {
    JSDocInfo info = new JSDocInfo();
    info.setHidden(true);

    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertTrue(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetShouldPreserveTry
  public void testSetShouldPreserveTry() {
    JSDocInfo info = new JSDocInfo();
    info.setShouldPreserveTry(true);

    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetNoTypeCheck
  public void testSetNoTypeCheck() {
    JSDocInfo info = new JSDocInfo();
    info.setNoCheck(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isNoAlias());
    assertFalse(info.isOverride());
    assertTrue(info.isNoTypeCheck());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetOverride
  public void testSetOverride() {
    JSDocInfo info = new JSDocInfo();
    info.setOverride(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isNoAlias());
    assertTrue(info.isOverride());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetExport
  public void testSetExport() {
    JSDocInfo info = new JSDocInfo();
    info.setExport(true);

    assertTrue(info.isExport());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetNoAlias
  public void testSetNoAlias() {
    JSDocInfo info = new JSDocInfo();
    info.setNoAlias(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isOverride());
    assertTrue(info.isNoAlias());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetDeprecated
  public void testSetDeprecated() {
    JSDocInfo info = new JSDocInfo();
    info.setDeprecated(true);

    assertFalse(info.isNoAlias());
    assertFalse(info.isOverride());
    assertTrue(info.isDeprecated());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetFlags1
  public void testMultipleSetFlags1() {
    JSDocInfo info = new JSDocInfo();
    info.setConstant(true);
    info.setConstructor(true);
    info.setHidden(true);
    info.setShouldPreserveTry(true);

    assertFalse(info.hasType());
    assertTrue(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertTrue(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setHidden(false);

    assertTrue(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setConstant(false);
    info.setConstructor(false);

    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setConstructor(true);

    assertFalse(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetFileOverviewWithDocumentationOff
  public void testSetFileOverviewWithDocumentationOff() {
    JSDocInfo info = new JSDocInfo();
    info.documentFileOverview("hi bob");
    assertNull(info.getFileOverview());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetFileOverviewWithDocumentationOn
  public void testSetFileOverviewWithDocumentationOn() {
    JSDocInfo info = new JSDocInfo(true);
    info.documentFileOverview("hi bob");
    assertEquals("hi bob", info.getFileOverview());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetSuppressions
  public void testSetSuppressions() {
    JSDocInfo info = new JSDocInfo(true);
    info.setSuppressions(Sets.newHashSet("sam", "bob"));
    assertEquals(Sets.newHashSet("bob", "sam"), info.getSuppressions());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetModifies
  public void testSetModifies() {
    JSDocInfo info = new JSDocInfo(true);
    info.setModifies(Sets.newHashSet("this"));
    assertEquals(Sets.newHashSet("this"), info.getModifies());

    info = new JSDocInfo(true);
    info.setModifies(Sets.newHashSet("arguments"));
    assertEquals(Sets.newHashSet("arguments"), info.getModifies());
  }

// com.google.javascript.rhino.NodeTest::testMergeExtractNormal
  public void testMergeExtractNormal() throws Exception {
    testMergeExtract(5, 6);
    testMergeExtract(456, 3423);
    testMergeExtract(0, 0);
  }

// com.google.javascript.rhino.NodeTest::testMergeExtractErroneous
  public void testMergeExtractErroneous() throws Exception {
    assertEquals(-1, Node.mergeLineCharNo(-5, 90));
    assertEquals(-1, Node.mergeLineCharNo(0, -1));
    assertEquals(-1, Node.extractLineno(-1));
    assertEquals(-1, Node.extractCharno(-1));
  }

// com.google.javascript.rhino.NodeTest::testMergeOverflowGraciously
  public void testMergeOverflowGraciously() throws Exception {
    int linecharno = Node.mergeLineCharNo(89, 4096);
    assertEquals(89, Node.extractLineno(linecharno));
    assertEquals(4095, Node.extractCharno(linecharno));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplSame
  public void testCheckTreeEqualsImplSame() {
    Node node1 = new Node(1, new Node(2));
    Node node2 = new Node(1, new Node(2));
    assertEquals(null, node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentType
  public void testCheckTreeEqualsImplDifferentType() {
    Node node1 = new Node(1, new Node(2));
    Node node2 = new Node(2, new Node(2));
    assertEquals(new NodeMismatch(node1, node2),
        node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentChildCount
  public void testCheckTreeEqualsImplDifferentChildCount() {
    Node node1 = new Node(1, new Node(2));
    Node node2 = new Node(1);
    assertEquals(new NodeMismatch(node1, node2),
        node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentChild
  public void testCheckTreeEqualsImplDifferentChild() {
    Node child1 = new Node(1);
    Node child2 = new Node(2);
    Node node1 = new Node(1, child1);
    Node node2 = new Node(1, child2);
    assertEquals(new NodeMismatch(child1, child2),
        node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsSame
  public void testCheckTreeEqualsSame() {
    Node node1 = new Node(1);
    assertEquals(null, node1.checkTreeEquals(node1));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsStringDifferent
  public void testCheckTreeEqualsStringDifferent() {
    Node node1 = new Node(Token.ADD);
    Node node2 = new Node(Token.SUB);
    assertNotNull(node1.checkTreeEquals(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsBooleanSame
  public void testCheckTreeEqualsBooleanSame() {
    Node node1 = new Node(1);
    assertEquals(true, node1.isEquivalentTo(node1));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsBooleanDifferent
  public void testCheckTreeEqualsBooleanDifferent() {
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    assertEquals(false, node1.isEquivalentTo(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsSlashVDifferent
  public void testCheckTreeEqualsSlashVDifferent() {
    Node node1 = Node.newString("\u000B");
    node1.putBooleanProp(Node.SLASH_V, true);
    Node node2 = Node.newString("\u000B");
    assertEquals(false, node1.isEquivalentTo(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentIncProp
  public void testCheckTreeEqualsImplDifferentIncProp() {
    Node node1 = new Node(Token.INC);
    node1.putIntProp(Node.INCRDECR_PROP, 1);
    Node node2 = new Node(Token.INC);
    assertNotNull(node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsSame
  public void testCheckTreeTypeAwareEqualsSame() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    node2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertTrue(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsSameNull
  public void testCheckTreeTypeAwareEqualsSameNull() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    Node node2 = Node.newString(Token.NAME, "f");
    assertTrue(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsDifferent
  public void testCheckTreeTypeAwareEqualsDifferent() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    node2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertFalse(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsDifferentNull
  public void testCheckTreeTypeAwareEqualsDifferentNull() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    assertFalse(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testVarArgs1
  public void testVarArgs1() {
    assertFalse(new Node(1).isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testVarArgs2
  public void testVarArgs2() {
    Node n = new Node(1);
    n.setVarArgs(false);
    assertFalse(n.isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testVarArgs3
  public void testVarArgs3() {
    Node n = new Node(1);
    n.setVarArgs(true);
    assertTrue(n.isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testFileLevelJSDocAppender
  public void testFileLevelJSDocAppender() {
    Node n = new Node(1);
    Node.FileLevelJsDocBuilder builder = n.getJsDocBuilderForNode();
    builder.append("foo");
    builder.append("bar");
    assertEquals("foobar", n.getJSDocInfo().getLicense());
  }

// com.google.javascript.rhino.NodeTest::testCloneAnnontations
  public void testCloneAnnontations() {
    Node n = getVarRef("a");
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    Node nodeClone = n.cloneNode();
    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

// com.google.javascript.rhino.NodeTest::testSharedProps1
  public void testSharedProps1() {
    Node n = getVarRef("A");
    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 5);
    Node m = new Node(Token.TRUE);
    m.clonePropsFrom(n);
    assertEquals(m.getPropListHeadForTesting(), n.getPropListHeadForTesting());
    assertEquals(5, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(5, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
  }

// com.google.javascript.rhino.NodeTest::testSharedProps2
  public void testSharedProps2() {
    Node n = getVarRef("A");
    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 5);
    Node m = new Node(Token.TRUE);
    m.clonePropsFrom(n);

    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 6);
    assertEquals(6, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(5, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertFalse(
        m.getPropListHeadForTesting() == n.getPropListHeadForTesting());

    m.putIntProp(Node.SIDE_EFFECT_FLAGS, 7);
    assertEquals(6, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(7, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
  }

// com.google.javascript.rhino.NodeTest::testSharedProps3
  public void testSharedProps3() {
    Node n = getVarRef("A");
    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 2);
    n.putIntProp(Node.INCRDECR_PROP, 3);
    Node m = new Node(Token.TRUE);
    m.clonePropsFrom(n);

    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 4);
    assertEquals(4, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(2, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
  }

// com.google.javascript.rhino.NodeTest::testBooleanProp
  public void testBooleanProp() {
    Node n = getVarRef("a");

    n.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    assertNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    assertNotNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    n.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    assertNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

// com.google.javascript.rhino.NodeTest::testCloneAnnontations2
  public void testCloneAnnontations2() {
    Node n = getVarRef("a");
    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    n.putBooleanProp(Node.IS_DISPATCHER, true);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(n.getBooleanProp(Node.IS_DISPATCHER));

    Node nodeClone = n.cloneNode();
    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(nodeClone.getBooleanProp(Node.IS_DISPATCHER));

    n.putBooleanProp(Node.IS_DISPATCHER, false);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_DISPATCHER));

    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(nodeClone.getBooleanProp(Node.IS_DISPATCHER));
  }

// com.google.javascript.rhino.NodeTest::testGetIndexOfChild
  public void testGetIndexOfChild() {
    Node assign = getAssignExpr("b","c");
    assertEquals(2, assign.getChildCount());

    Node firstChild = assign.getFirstChild();
    Node secondChild = firstChild.getNext();
    assertNotNull(secondChild);

    assertEquals(0, assign.getIndexOfChild(firstChild));
    assertEquals(1, assign.getIndexOfChild(secondChild));
    assertEquals(-1, assign.getIndexOfChild(assign));
  }

// com.google.javascript.rhino.NodeTest::testCopyInformationFrom
  public void testCopyInformationFrom() {
    Node assign = getAssignExpr("b","c");
    assign.setSourceEncodedPosition(99);
    assign.setSourceFileForTesting("foo.js");

    Node lhs = assign.getFirstChild();
    lhs.copyInformationFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());

    assign.setSourceEncodedPosition(101);
    assign.setSourceFileForTesting("bar.js");
    lhs.copyInformationFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());
  }

// com.google.javascript.rhino.NodeTest::testUseSourceInfoIfMissingFrom
  public void testUseSourceInfoIfMissingFrom() {
    Node assign = getAssignExpr("b","c");
    assign.setSourceEncodedPosition(99);
    assign.setSourceFileForTesting("foo.js");

    Node lhs = assign.getFirstChild();
    lhs.useSourceInfoIfMissingFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());

    assign.setSourceEncodedPosition(101);
    assign.setSourceFileForTesting("bar.js");
    lhs.useSourceInfoIfMissingFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());
  }

// com.google.javascript.rhino.NodeTest::testUseSourceInfoFrom
  public void testUseSourceInfoFrom() {
    Node assign = getAssignExpr("b","c");
    assign.setSourceEncodedPosition(99);
    assign.setSourceFileForTesting("foo.js");

    Node lhs = assign.getFirstChild();
    lhs.useSourceInfoFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());

    assign.setSourceEncodedPosition(101);
    assign.setSourceFileForTesting("bar.js");
    lhs.useSourceInfoFrom(assign);
    assertEquals(101, lhs.getSourcePosition());
    assertEquals("bar.js", lhs.getSourceFileName());
  }

// com.google.javascript.rhino.jstype.EnumElementTypeTest::testSubtypeRelation
  public void testSubtypeRelation() throws Exception {
    EnumElementType typeA = registry.createEnumType(
        "typeA", null, NUMBER_TYPE).getElementsType();
    EnumElementType typeB = registry.createEnumType(
        "typeB", null, NUMBER_TYPE).getElementsType();

    assertFalse(typeA.isSubtype(typeB));
    assertFalse(typeB.isSubtype(typeA));

    assertFalse(NUMBER_TYPE.isSubtype(typeB));
    assertFalse(NUMBER_TYPE.isSubtype(typeA));

    assertTrue(typeA.isSubtype(NUMBER_TYPE));
    assertTrue(typeB.isSubtype(NUMBER_TYPE));
  }

// com.google.javascript.rhino.jstype.EnumElementTypeTest::testMeet
  public void testMeet() throws Exception {
    EnumElementType typeA = registry.createEnumType(
        "typeA", null, createUnionType(NUMBER_TYPE, STRING_TYPE))
        .getElementsType();

    JSType stringsOfA = typeA.getGreatestSubtype(STRING_TYPE);
    assertFalse(stringsOfA.isEmptyType());
    assertEquals("typeA.<string>", stringsOfA.toString());
    assertTrue(stringsOfA.isSubtype(typeA));

    JSType numbersOfA = NUMBER_TYPE.getGreatestSubtype(typeA);
    assertFalse(numbersOfA.isEmptyType());
    assertEquals("typeA.<number>", numbersOfA.toString());
    assertTrue(numbersOfA.isSubtype(typeA));
  }

// com.google.javascript.rhino.jstype.FunctionParamBuilderTest::testBuild
  public void testBuild() throws Exception {
    FunctionParamBuilder builder = new FunctionParamBuilder(registry);
    assertTrue(builder.addRequiredParams(NUMBER_TYPE));
    assertTrue(builder.addOptionalParams(BOOLEAN_TYPE));
    assertTrue(builder.addVarArgs(STRING_TYPE));

    Node params = builder.build();
    assertTypeEquals(NUMBER_TYPE, params.getFirstChild().getJSType());
    assertTypeEquals(registry.createOptionalType(BOOLEAN_TYPE),
        params.getFirstChild().getNext().getJSType());
    assertTypeEquals(registry.createOptionalType(STRING_TYPE),
        params.getLastChild().getJSType());

    assertTrue(params.getFirstChild().getNext().isOptionalArg());
    assertTrue(params.getLastChild().isVarArgs());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testDefaultReturnType
  public void testDefaultReturnType() {
    FunctionType f = new FunctionBuilder(registry).build();
    assertEquals(UNKNOWN_TYPE, f.getReturnType());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfOfReturnTypes
  public void testSupAndInfOfReturnTypes() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withInferredReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "function (): (number|string)", retString, retNumber);
    assertGreatestSubtype(
        "function (): None", retString, retNumber);

    assertTrue(retString.isReturnTypeInferred());
    assertFalse(retNumber.isReturnTypeInferred());
    assertTrue(
        ((FunctionType) retString.getLeastSupertype(retNumber))
        .isReturnTypeInferred());
    assertTrue(
        ((FunctionType) retString.getGreatestSubtype(retString))
        .isReturnTypeInferred());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfOfReturnTypesWithDifferentParams
  public void testSupAndInfOfReturnTypesWithDifferentParams() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withInferredReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "Function", retString, retNumber);
    assertGreatestSubtype(
        "function (...[*]): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfWithDifferentParams
  public void testSupAndInfWithDifferentParams() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(STRING_TYPE))
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "Function", retString, retNumber);
    assertGreatestSubtype(
        "function (...[*]): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfWithDifferentThisTypes
  public void testSupAndInfWithDifferentThisTypes() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(OBJECT_TYPE)
        .withReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(DATE_TYPE)
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "function (this:Object): (number|string)", retString, retNumber);
    assertGreatestSubtype(
        "function (this:Date): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfWithDifferentThisTypes2
  public void testSupAndInfWithDifferentThisTypes2() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(ARRAY_TYPE)
        .withReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(DATE_TYPE)
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "function (this:Object): (number|string)", retString, retNumber);
    assertGreatestSubtype(
        "function (this:NoObject): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfOfReturnTypesWithNumOfParams
  public void testSupAndInfOfReturnTypesWithNumOfParams() {
    FunctionType twoNumbers = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE, NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();
    FunctionType oneNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();

    assertLeastSupertype(
        "function (number, number): boolean", twoNumbers, oneNumber);
    assertGreatestSubtype(
        "function (number): boolean", twoNumbers, oneNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSubtypeWithInterfaceThisType
  public void testSubtypeWithInterfaceThisType() {
    FunctionType iface = registry.createInterfaceType("I", null);
    FunctionType ifaceReturnBoolean = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(iface.getInstanceType())
        .withReturnType(BOOLEAN_TYPE).build();
    FunctionType objReturnBoolean = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(OBJECT_TYPE)
        .withReturnType(BOOLEAN_TYPE).build();
    assertTrue(objReturnBoolean.canAssignTo(ifaceReturnBoolean));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testOrdinaryFunctionPrototype
  public void testOrdinaryFunctionPrototype() {
    FunctionType oneNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();
    assertEquals(ImmutableSet.<String>of(), oneNumber.getOwnPropertyNames());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testCtorWithPrototypeSet
  public void testCtorWithPrototypeSet() {
    FunctionType ctor = registry.createConstructorType(
        "Foo", null, null, null);
    assertFalse(ctor.getInstanceType().isUnknownType());

    Node node = new Node(Token.OBJECTLIT);
    ctor.defineDeclaredProperty("prototype", UNKNOWN_TYPE, node);
    assertTrue(ctor.getInstanceType().isUnknownType());

    assertEquals(ImmutableSet.<String>of("prototype"),
        ctor.getOwnPropertyNames());
    assertTrue(ctor.isPropertyTypeInferred("prototype"));
    assertTrue(ctor.getPropertyType("prototype").isUnknownType());

    
    assertNull(ctor.getPropertyNode("prototype"));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testEmptyFunctionTypes
  public void testEmptyFunctionTypes() {
    assertTrue(LEAST_FUNCTION_TYPE.isEmptyType());
    assertFalse(GREATEST_FUNCTION_TYPE.isEmptyType());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testInterfacePrototypeChain1
  public void testInterfacePrototypeChain1() {
    FunctionType iface = registry.createInterfaceType("I", null);
    assertTypeEquals(
        iface.getPrototype(),
        iface.getInstanceType().getImplicitPrototype());
    assertTypeEquals(
        OBJECT_TYPE,
        iface.getPrototype().getImplicitPrototype());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testInterfacePrototypeChain2
  public void testInterfacePrototypeChain2() {
    FunctionType iface = registry.createInterfaceType("I", null);
    iface.getPrototype().defineDeclaredProperty(
        "numberProp", NUMBER_TYPE, null);

    FunctionType subIface = registry.createInterfaceType("SubI", null);
    subIface.setExtendedInterfaces(
        Lists.<ObjectType>newArrayList(iface.getInstanceType()));
    assertTypeEquals(
        subIface.getPrototype(),
        subIface.getInstanceType().getImplicitPrototype());
    assertTypeEquals(
        OBJECT_TYPE,
        subIface.getPrototype().getImplicitPrototype());

    ObjectType subIfaceInst = subIface.getInstanceType();
    assertTrue(subIfaceInst.hasProperty("numberProp"));
    assertTrue(subIfaceInst.isPropertyTypeDeclared("numberProp"));
    assertFalse(subIfaceInst.isPropertyTypeInferred("numberProp"));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testIsEquivalentTo
  public void testIsEquivalentTo() {
    FunctionType type = new FunctionBuilder(registry).build();
    assertFalse(type.isEquivalentTo(null));
    assertTrue(type.isEquivalentTo(type));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testRecursiveFunction
  public void testRecursiveFunction() {
    ProxyObjectType loop = new ProxyObjectType(registry, NUMBER_TYPE);
    FunctionType fn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(loop))
        .withReturnType(loop).build();

    loop.setReferencedType(fn);
    assertEquals("function (Function): Function", fn.toString());

    Asserts.assertEquivalenceOperations(fn, loop);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testBindSignature
  public void testBindSignature() {
    FunctionType fn = new FunctionBuilder(registry)
        .withTypeOfThis(DATE_TYPE)
        .withParamsNode(registry.createParameters(STRING_TYPE, NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();

    assertEquals(
        "function ((Date|null|undefined), string=, number=):" +
        " function (...[?]): boolean",
        fn.getPropertyType("bind").toString());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetBuiltInType
  public void testGetBuiltInType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    assertTypeEquals(typeRegistry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        typeRegistry.getType("boolean"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetDeclaredType
  public void testGetDeclaredType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "Foo";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));

    
    JSTypeRegistry typeRegistry2 = new JSTypeRegistry(null);
    assertEquals(null, typeRegistry2.getType(name));
    assertTypeEquals(type, typeRegistry.getType(name));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetDeclaredTypeInNamespace
  public void testGetDeclaredTypeInNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));
    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testPropertyOnManyTypes
  public void testPropertyOnManyTypes() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);

    JSType type = null;

    
    
    
    for (int i = 0; i < 100; i++) {
      type = typeRegistry.createObjectType("type: " + i, null, null);
      typeRegistry.registerPropertyOnType("foo", type);
    }

    assertFalse(typeRegistry.getGreatestSubtypeWithProperty(type, "foo").isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testTypeAsNamespace
  public void testTypeAsNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);

    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));

    type = typeRegistry.createAnonymousObjectType();
    name = "a.b.Foo.Bar";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));

    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
    assertTrue(typeRegistry.hasNamespace("a.b.Foo"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGenerationIncrementing1
  public void testGenerationIncrementing1() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    final JSTypeRegistry typeRegistry = new JSTypeRegistry(reporter);

    StaticScope<JSType> scope = new AbstractStaticScope<JSType>() {
          @Override
          public StaticSlot<JSType> getSlot(final String name) {
            return new SimpleSlot(
                name,
                typeRegistry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
                false);
          }
        };

    ObjectType namedType =
        (ObjectType) typeRegistry.getType(scope, "Foo", null, 0, 0);
    ObjectType subNamed =
        typeRegistry.createObjectType(typeRegistry.createObjectType(namedType));

    
    typeRegistry.setLastGeneration(false);
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    
    
    typeRegistry.declareType("Foo", typeRegistry.createAnonymousObjectType());
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    assertNull("Unexpected errors: " + reporter.errors(),
        reporter.errors());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    
    typeRegistry.incrementGeneration();
    typeRegistry.setLastGeneration(true);
    typeRegistry.resolveTypesInScope(scope);
    assertFalse(subNamed.isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGenerationIncrementing2
  public void testGenerationIncrementing2() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    final JSTypeRegistry typeRegistry = new JSTypeRegistry(reporter);

    StaticScope<JSType> scope = new AbstractStaticScope<JSType>() {
          @Override
          public StaticSlot<JSType> getSlot(final String name) {
            return new SimpleSlot(
                name,
                typeRegistry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
                false);
          }
        };

    ObjectType namedType =
        (ObjectType) typeRegistry.getType(scope, "Foo", null, 0, 0);
    FunctionType functionType = typeRegistry.createFunctionType(namedType);

    
    typeRegistry.setLastGeneration(false);
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(functionType.getReturnType().isUnknownType());
    functionType.resolve(reporter, scope);
    assertTrue(functionType.getReturnType().isUnknownType());

    
    
    typeRegistry.declareType("Foo", typeRegistry.createAnonymousObjectType());
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(functionType.getReturnType().isUnknownType());

    assertNull("Unexpected errors: " + reporter.errors(),
        reporter.errors());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    
    typeRegistry.incrementGeneration();
    typeRegistry.setLastGeneration(true);
    typeRegistry.resolveTypesInScope(scope);
    assertFalse(functionType.getReturnType().isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testTypeResolutionModes
  public void testTypeResolutionModes() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();

    JSTypeRegistry lazyExprRegistry = new JSTypeRegistry(reporter);
    lazyExprRegistry.setResolveMode(ResolveMode.LAZY_EXPRESSIONS);

    JSTypeRegistry lazyNameRegistry = new JSTypeRegistry(reporter);
    lazyNameRegistry.setResolveMode(ResolveMode.LAZY_NAMES);

    JSTypeRegistry immediateRegistry = new JSTypeRegistry(reporter);
    immediateRegistry.setResolveMode(ResolveMode.IMMEDIATE);

    Node expr = new Node(Token.QMARK, Node.newString("foo"));
    StaticScope<JSType> empty = MapBasedScope.emptyScope();

    JSType type = lazyExprRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertTrue(type instanceof UnresolvedTypeExpression);
    assertTrue(type.isUnknownType());
    assertEquals("?", type.toString());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    type = lazyNameRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertTrue(type instanceof UnionType);
    assertTrue(type.isUnknownType());
    assertEquals("(foo|null)", type.toString());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    type = immediateRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertTrue(type instanceof UnknownType);
    assertEquals("Expected warnings", 1, reporter.warnings().size());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testForceResolve
  public void testForceResolve() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();

    JSTypeRegistry lazyExprRegistry = new JSTypeRegistry(reporter);
    lazyExprRegistry.setResolveMode(ResolveMode.LAZY_EXPRESSIONS);

    Node expr = new Node(Token.QMARK, Node.newString("foo"));
    StaticScope<JSType> empty = MapBasedScope.emptyScope();

    JSType type = lazyExprRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertFalse(type.isResolved());
    assertTrue(type.forceResolve(reporter, empty).isResolved());
    assertEquals("Expected warnings", 1, reporter.warnings().size());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testAllTypeResolvesImmediately
  public void testAllTypeResolvesImmediately() {
    JSTypeRegistry lazyExprRegistry = new JSTypeRegistry(
        new SimpleErrorReporter());
    lazyExprRegistry.setResolveMode(ResolveMode.LAZY_EXPRESSIONS);

    Node expr = new Node(Token.STAR);
    JSType type = lazyExprRegistry.createFromTypeNodes(
        expr, "source.js", MapBasedScope.emptyScope());
    assertTrue(type instanceof AllType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testUniversalConstructorType
  public void testUniversalConstructorType() throws Exception {
    
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNoObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNoType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isArrayType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isBooleanValueType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isDateType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isEnumElementType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNamedType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumber());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumberObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumberValueType());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isObject());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isFunctionPrototypeType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isRegexpType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isString());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isStringObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isStringValueType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isEnumType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isUnionType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isAllType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isVoidType());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isConstructor());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isInstanceType());

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NO_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(functionType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NO_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ALL_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(DATE_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(functionType));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(URI_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(STRING_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(TYPE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(VOID_TYPE));

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(functionType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue( U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullable());

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.isObject());

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesInt32Context());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesNumberContext());
    assertTrue(U2U_CONSTRUCTOR_TYPE.matchesObjectContext());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesStringContext());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesUint32Context());

    
    assertEquals("Function",
        U2U_CONSTRUCTOR_TYPE.toString());
    assertTrue(U2U_CONSTRUCTOR_TYPE.hasDisplayName());
    assertEquals("Function", U2U_CONSTRUCTOR_TYPE.getDisplayName());

    
    assertTypeEquals(UNKNOWN_TYPE,
        U2U_CONSTRUCTOR_TYPE.getPropertyType("anyProperty"));

    assertTrue(U2U_CONSTRUCTOR_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(U2U_CONSTRUCTOR_TYPE);

    assertTrue(U2U_CONSTRUCTOR_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoObjectType
  public void testNoObjectType() throws Exception {
    
    assertTrue(NO_OBJECT_TYPE.isNoObjectType());
    assertFalse(NO_OBJECT_TYPE.isNoType());
    assertFalse(NO_OBJECT_TYPE.isArrayType());
    assertFalse(NO_OBJECT_TYPE.isBooleanValueType());
    assertFalse(NO_OBJECT_TYPE.isDateType());
    assertFalse(NO_OBJECT_TYPE.isEnumElementType());
    assertFalse(NO_OBJECT_TYPE.isNullType());
    assertFalse(NO_OBJECT_TYPE.isNamedType());
    assertFalse(NO_OBJECT_TYPE.isNullType());
    assertTrue(NO_OBJECT_TYPE.isNumber());
    assertFalse(NO_OBJECT_TYPE.isNumberObjectType());
    assertFalse(NO_OBJECT_TYPE.isNumberValueType());
    assertTrue(NO_OBJECT_TYPE.isObject());
    assertFalse(NO_OBJECT_TYPE.isFunctionPrototypeType());
    assertFalse(NO_OBJECT_TYPE.isRegexpType());
    assertTrue(NO_OBJECT_TYPE.isString());
    assertFalse(NO_OBJECT_TYPE.isStringObjectType());
    assertFalse(NO_OBJECT_TYPE.isStringValueType());
    assertFalse(NO_OBJECT_TYPE.isEnumType());
    assertFalse(NO_OBJECT_TYPE.isUnionType());
    assertFalse(NO_OBJECT_TYPE.isAllType());
    assertFalse(NO_OBJECT_TYPE.isVoidType());
    assertTrue(NO_OBJECT_TYPE.isConstructor());
    assertFalse(NO_OBJECT_TYPE.isInstanceType());

    
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(functionType));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(recordType));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(VOID_TYPE));

    
    assertCannotTestForEqualityWith(NO_OBJECT_TYPE, NO_TYPE);
    assertCannotTestForEqualityWith(NO_OBJECT_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, EVAL_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, recordType);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, NULL_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, NUMBER_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, URI_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, RANGE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, REFERENCE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, SYNTAX_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, TYPE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, VOID_TYPE);

    
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(NO_OBJECT_TYPE.isNullable());

    
    assertTrue(NO_OBJECT_TYPE.isObject());

    
    assertTrue(NO_OBJECT_TYPE.matchesInt32Context());
    assertTrue(NO_OBJECT_TYPE.matchesNumberContext());
    assertTrue(NO_OBJECT_TYPE.matchesObjectContext());
    assertTrue(NO_OBJECT_TYPE.matchesStringContext());
    assertTrue(NO_OBJECT_TYPE.matchesUint32Context());

    
    assertEquals("NoObject", NO_OBJECT_TYPE.toString());
    assertFalse(NO_OBJECT_TYPE.hasDisplayName());
    assertEquals(null, NO_OBJECT_TYPE.getDisplayName());

    
    assertTypeEquals(NO_TYPE,
        NO_OBJECT_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_OBJECT_TYPE);

    assertFalse(NO_OBJECT_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoType
  public void testNoType() throws Exception {
    
    assertFalse(NO_TYPE.isNoObjectType());
    assertTrue(NO_TYPE.isNoType());
    assertFalse(NO_TYPE.isArrayType());
    assertFalse(NO_TYPE.isBooleanValueType());
    assertFalse(NO_TYPE.isDateType());
    assertFalse(NO_TYPE.isEnumElementType());
    assertFalse(NO_TYPE.isNullType());
    assertFalse(NO_TYPE.isNamedType());
    assertFalse(NO_TYPE.isNullType());
    assertTrue(NO_TYPE.isNumber());
    assertFalse(NO_TYPE.isNumberObjectType());
    assertFalse(NO_TYPE.isNumberValueType());
    assertTrue(NO_TYPE.isObject());
    assertFalse(NO_TYPE.isFunctionPrototypeType());
    assertFalse(NO_TYPE.isRegexpType());
    assertTrue(NO_TYPE.isString());
    assertFalse(NO_TYPE.isStringObjectType());
    assertFalse(NO_TYPE.isStringValueType());
    assertFalse(NO_TYPE.isEnumType());
    assertFalse(NO_TYPE.isUnionType());
    assertFalse(NO_TYPE.isAllType());
    assertFalse(NO_TYPE.isVoidType());
    assertTrue(NO_TYPE.isConstructor());
    assertFalse(NO_TYPE.isInstanceType());

    
    assertTrue(NO_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NO_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(functionType));
    assertTrue(NO_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(NO_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ALL_TYPE));
    assertTrue(NO_TYPE.canAssignTo(VOID_TYPE));

    
    assertCannotTestForEqualityWith(NO_TYPE, NO_TYPE);
    assertCannotTestForEqualityWith(NO_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, EVAL_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, functionType);
    assertCanTestForEqualityWith(NO_TYPE, NULL_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, NUMBER_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, URI_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, RANGE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, REFERENCE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, SYNTAX_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, TYPE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, VOID_TYPE);

    
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertTrue(NO_TYPE.isNullable());

    
    assertTrue(NO_TYPE.isObject());

    
    assertTrue(NO_TYPE.matchesInt32Context());
    assertTrue(NO_TYPE.matchesNumberContext());
    assertTrue(NO_TYPE.matchesObjectContext());
    assertTrue(NO_TYPE.matchesStringContext());
    assertTrue(NO_TYPE.matchesUint32Context());

    
    assertEquals("None", NO_TYPE.toString());
    assertEquals(null, NO_TYPE.getDisplayName());
    assertFalse(NO_TYPE.hasDisplayName());

    
    assertTypeEquals(NO_TYPE,
        NO_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_TYPE);

    assertFalse(NO_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoResolvedType
  public void testNoResolvedType() throws Exception {
    
    assertFalse(NO_RESOLVED_TYPE.isNoObjectType());
    assertFalse(NO_RESOLVED_TYPE.isNoType());
    assertTrue(NO_RESOLVED_TYPE.isNoResolvedType());
    assertFalse(NO_RESOLVED_TYPE.isArrayType());
    assertFalse(NO_RESOLVED_TYPE.isBooleanValueType());
    assertFalse(NO_RESOLVED_TYPE.isDateType());
    assertFalse(NO_RESOLVED_TYPE.isEnumElementType());
    assertFalse(NO_RESOLVED_TYPE.isNullType());
    assertFalse(NO_RESOLVED_TYPE.isNamedType());
    assertTrue(NO_RESOLVED_TYPE.isNumber());
    assertFalse(NO_RESOLVED_TYPE.isNumberObjectType());
    assertFalse(NO_RESOLVED_TYPE.isNumberValueType());
    assertTrue(NO_RESOLVED_TYPE.isObject());
    assertFalse(NO_RESOLVED_TYPE.isFunctionPrototypeType());
    assertFalse(NO_RESOLVED_TYPE.isRegexpType());
    assertTrue(NO_RESOLVED_TYPE.isString());
    assertFalse(NO_RESOLVED_TYPE.isStringObjectType());
    assertFalse(NO_RESOLVED_TYPE.isStringValueType());
    assertFalse(NO_RESOLVED_TYPE.isEnumType());
    assertFalse(NO_RESOLVED_TYPE.isUnionType());
    assertFalse(NO_RESOLVED_TYPE.isAllType());
    assertFalse(NO_RESOLVED_TYPE.isVoidType());
    assertTrue(NO_RESOLVED_TYPE.isConstructor());
    assertFalse(NO_RESOLVED_TYPE.isInstanceType());

    
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NO_RESOLVED_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(functionType));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(ALL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(VOID_TYPE));

    
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NO_RESOLVED_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, EVAL_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, functionType);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NULL_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NUMBER_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, URI_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, RANGE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, REFERENCE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, SYNTAX_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, TYPE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, VOID_TYPE);

    
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NO_RESOLVED_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertTrue(NO_RESOLVED_TYPE.isNullable());

    
    assertTrue(NO_RESOLVED_TYPE.isObject());

    
    assertTrue(NO_RESOLVED_TYPE.matchesInt32Context());
    assertTrue(NO_RESOLVED_TYPE.matchesNumberContext());
    assertTrue(NO_RESOLVED_TYPE.matchesObjectContext());
    assertTrue(NO_RESOLVED_TYPE.matchesStringContext());
    assertTrue(NO_RESOLVED_TYPE.matchesUint32Context());

    
    assertEquals("NoResolvedType", NO_RESOLVED_TYPE.toString());
    assertEquals(null, NO_RESOLVED_TYPE.getDisplayName());
    assertFalse(NO_RESOLVED_TYPE.hasDisplayName());

    
    assertTypeEquals(CHECKED_UNKNOWN_TYPE,
        NO_RESOLVED_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_RESOLVED_TYPE);

    assertTrue(forwardDeclaredNamedType.isEmptyType());
    assertTrue(forwardDeclaredNamedType.isNoResolvedType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testArrayType
  public void testArrayType() throws Exception {
    
    assertTrue(ARRAY_TYPE.isArrayType());
    assertFalse(ARRAY_TYPE.isBooleanValueType());
    assertFalse(ARRAY_TYPE.isDateType());
    assertFalse(ARRAY_TYPE.isEnumElementType());
    assertFalse(ARRAY_TYPE.isNamedType());
    assertFalse(ARRAY_TYPE.isNullType());
    assertFalse(ARRAY_TYPE.isNumber());
    assertFalse(ARRAY_TYPE.isNumberObjectType());
    assertFalse(ARRAY_TYPE.isNumberValueType());
    assertTrue(ARRAY_TYPE.isObject());
    assertFalse(ARRAY_TYPE.isFunctionPrototypeType());
    assertTrue(ARRAY_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(ARRAY_TYPE.isRegexpType());
    assertFalse(ARRAY_TYPE.isString());
    assertFalse(ARRAY_TYPE.isStringObjectType());
    assertFalse(ARRAY_TYPE.isStringValueType());
    assertFalse(ARRAY_TYPE.isEnumType());
    assertFalse(ARRAY_TYPE.isUnionType());
    assertFalse(ARRAY_TYPE.isAllType());
    assertFalse(ARRAY_TYPE.isVoidType());
    assertFalse(ARRAY_TYPE.isConstructor());
    assertTrue(ARRAY_TYPE.isInstanceType());

    
    assertFalse(ARRAY_TYPE.canAssignTo(NO_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(functionType));
    assertFalse(ARRAY_TYPE.canAssignTo(recordType));
    assertFalse(ARRAY_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(ARRAY_TYPE.canAssignTo(namedGoogBar));
    assertFalse(ARRAY_TYPE.canAssignTo(REGEXP_TYPE));

    
    assertFalse(ARRAY_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(ARRAY_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, functionType);
    assertCanTestForEqualityWith(ARRAY_TYPE, recordType);
    assertCannotTestForEqualityWith(ARRAY_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, REGEXP_TYPE);

    
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(ARRAY_TYPE.isNullable());
    assertTrue(createUnionType(ARRAY_TYPE, NULL_TYPE).isNullable());

    
    assertTrue(ARRAY_TYPE.isObject());

    
    assertTypeEquals(ALL_TYPE,
        ARRAY_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(STRING_OBJECT_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(ARRAY_TYPE, functionType),
        ARRAY_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE, ARRAY_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(17, ARRAY_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(18, ARRAY_TYPE.getPropertiesCount());
    assertReturnTypeEquals(ARRAY_TYPE,
        ARRAY_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("concat"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("join"));
    assertReturnTypeEquals(UNKNOWN_TYPE, ARRAY_TYPE.getPropertyType("pop"));
    assertReturnTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("push"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("reverse"));
    assertReturnTypeEquals(UNKNOWN_TYPE, ARRAY_TYPE.getPropertyType("shift"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("slice"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("sort"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("splice"));
    assertReturnTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("unshift"));
    assertTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("length"));

    
    assertPropertyTypeDeclared(ARRAY_TYPE, "pop");

    
    assertFalse(ARRAY_TYPE.matchesInt32Context());
    assertFalse(ARRAY_TYPE.matchesNumberContext());
    assertTrue(ARRAY_TYPE.matchesObjectContext());
    assertTrue(ARRAY_TYPE.matchesStringContext());
    assertFalse(ARRAY_TYPE.matchesUint32Context());

    
    assertEquals("Array", ARRAY_TYPE.toString());
    assertTrue(ARRAY_TYPE.hasDisplayName());
    assertEquals("Array", ARRAY_TYPE.getDisplayName());

    assertTrue(ARRAY_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(ARRAY_TYPE);

    assertFalse(ARRAY_TYPE.isNominalConstructor());
    assertTrue(ARRAY_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testUnknownType
  public void testUnknownType() throws Exception {
    
    assertFalse(UNKNOWN_TYPE.isArrayType());
    assertFalse(UNKNOWN_TYPE.isBooleanObjectType());
    assertFalse(UNKNOWN_TYPE.isBooleanValueType());
    assertFalse(UNKNOWN_TYPE.isDateType());
    assertFalse(UNKNOWN_TYPE.isEnumElementType());
    assertFalse(UNKNOWN_TYPE.isNamedType());
    assertFalse(UNKNOWN_TYPE.isNullType());
    assertFalse(UNKNOWN_TYPE.isNumberObjectType());
    assertFalse(UNKNOWN_TYPE.isNumberValueType());
    assertTrue(UNKNOWN_TYPE.isObject());
    assertFalse(UNKNOWN_TYPE.isFunctionPrototypeType());
    assertFalse(UNKNOWN_TYPE.isRegexpType());
    assertFalse(UNKNOWN_TYPE.isStringObjectType());
    assertFalse(UNKNOWN_TYPE.isStringValueType());
    assertFalse(UNKNOWN_TYPE.isEnumType());
    assertFalse(UNKNOWN_TYPE.isUnionType());
    assertTrue(UNKNOWN_TYPE.isUnknownType());
    assertFalse(UNKNOWN_TYPE.isVoidType());
    assertFalse(UNKNOWN_TYPE.isConstructor());
    assertFalse(UNKNOWN_TYPE.isInstanceType());

    
    assertNull(UNKNOWN_TYPE.autoboxesTo());

    
    assertTrue(UNKNOWN_TYPE.canAssignTo(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(functionType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(recordType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(namedGoogBar));
    assertTrue(UNKNOWN_TYPE.canAssignTo(unresolvedNamedType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(UNKNOWN_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(UNKNOWN_TYPE, UNKNOWN_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, functionType);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, recordType);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, BOOLEAN_TYPE);

    
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(recordType));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));

    
    assertTrue(UNKNOWN_TYPE.isNullable());

    
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(UNKNOWN_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(STRING_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(UNKNOWN_TYPE.matchesInt32Context());
    assertTrue(UNKNOWN_TYPE.matchesNumberContext());
    assertTrue(UNKNOWN_TYPE.matchesObjectContext());
    assertTrue(UNKNOWN_TYPE.matchesStringContext());
    assertTrue(UNKNOWN_TYPE.matchesUint32Context());

    
    assertPropertyTypeUnknown(UNKNOWN_TYPE, "XXX");

    
    assertEquals("?", UNKNOWN_TYPE.toString());
    assertTrue(UNKNOWN_TYPE.hasDisplayName());
    assertEquals("Unknown", UNKNOWN_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(UNKNOWN_TYPE);
    assertFalse(UNKNOWN_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testAllType
  public void testAllType() throws Exception {
    
    assertFalse(ALL_TYPE.isArrayType());
    assertFalse(ALL_TYPE.isBooleanValueType());
    assertFalse(ALL_TYPE.isDateType());
    assertFalse(ALL_TYPE.isEnumElementType());
    assertFalse(ALL_TYPE.isNamedType());
    assertFalse(ALL_TYPE.isNullType());
    assertFalse(ALL_TYPE.isNumber());
    assertFalse(ALL_TYPE.isNumberObjectType());
    assertFalse(ALL_TYPE.isNumberValueType());
    assertFalse(ALL_TYPE.isObject());
    assertFalse(ALL_TYPE.isFunctionPrototypeType());
    assertFalse(ALL_TYPE.isRegexpType());
    assertFalse(ALL_TYPE.isString());
    assertFalse(ALL_TYPE.isStringObjectType());
    assertFalse(ALL_TYPE.isStringValueType());
    assertFalse(ALL_TYPE.isEnumType());
    assertFalse(ALL_TYPE.isUnionType());
    assertTrue(ALL_TYPE.isAllType());
    assertFalse(ALL_TYPE.isVoidType());
    assertFalse(ALL_TYPE.isConstructor());
    assertFalse(ALL_TYPE.isInstanceType());

    
    assertFalse(ALL_TYPE.canAssignTo(NO_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(functionType));
    assertFalse(ALL_TYPE.canAssignTo(recordType));
    assertFalse(ALL_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(ALL_TYPE.canAssignTo(namedGoogBar));
    assertFalse(ALL_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(VOID_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(ALL_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(ALL_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, functionType);
    assertCanTestForEqualityWith(ALL_TYPE, recordType);
    assertCanTestForEqualityWith(ALL_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, REGEXP_TYPE);

    
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(recordType));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(ALL_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(UNKNOWN_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertFalse(ALL_TYPE.matchesInt32Context());
    assertFalse(ALL_TYPE.matchesNumberContext());
    assertTrue(ALL_TYPE.matchesObjectContext());
    assertTrue(ALL_TYPE.matchesStringContext());
    assertFalse(ALL_TYPE.matchesUint32Context());

    
    assertEquals("*", ALL_TYPE.toString());

    assertTrue(ALL_TYPE.hasDisplayName());
    assertEquals("<Any Type>", ALL_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(ALL_TYPE);
    assertFalse(ALL_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testTheObjectType
  public void testTheObjectType() throws Exception {
    
    assertTypeEquals(OBJECT_PROTOTYPE, OBJECT_TYPE.getImplicitPrototype());

    
    assertFalse(OBJECT_TYPE.isNoObjectType());
    assertFalse(OBJECT_TYPE.isNoType());
    assertFalse(OBJECT_TYPE.isArrayType());
    assertFalse(OBJECT_TYPE.isBooleanValueType());
    assertFalse(OBJECT_TYPE.isDateType());
    assertFalse(OBJECT_TYPE.isEnumElementType());
    assertFalse(OBJECT_TYPE.isNullType());
    assertFalse(OBJECT_TYPE.isNamedType());
    assertFalse(OBJECT_TYPE.isNullType());
    assertFalse(OBJECT_TYPE.isNumber());
    assertFalse(OBJECT_TYPE.isNumberObjectType());
    assertFalse(OBJECT_TYPE.isNumberValueType());
    assertTrue(OBJECT_TYPE.isObject());
    assertFalse(OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(OBJECT_TYPE.isRegexpType());
    assertFalse(OBJECT_TYPE.isString());
    assertFalse(OBJECT_TYPE.isStringObjectType());
    assertFalse(OBJECT_TYPE.isStringValueType());
    assertFalse(OBJECT_TYPE.isEnumType());
    assertFalse(OBJECT_TYPE.isUnionType());
    assertFalse(OBJECT_TYPE.isAllType());
    assertFalse(OBJECT_TYPE.isVoidType());
    assertFalse(OBJECT_TYPE.isConstructor());
    assertTrue(OBJECT_TYPE.isInstanceType());

    
    assertFalse(OBJECT_TYPE.canAssignTo(NO_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(functionType));
    assertFalse(OBJECT_TYPE.canAssignTo(recordType));
    assertFalse(OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(namedGoogBar));
    assertTrue(OBJECT_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(OBJECT_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(OBJECT_TYPE, recordType);
    assertCannotTestForEqualityWith(OBJECT_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, UNKNOWN_TYPE);

    
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(OBJECT_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        OBJECT_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(OBJECT_TYPE, NUMBER_TYPE),
        OBJECT_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(7, OBJECT_TYPE.getPropertiesCount());
    assertReturnTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        OBJECT_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        OBJECT_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(UNKNOWN_TYPE,
        OBJECT_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("hasOwnProperty"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("isPrototypeOf"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("propertyIsEnumerable"));

    
    assertFalse(OBJECT_TYPE.matchesInt32Context());
    assertFalse(OBJECT_TYPE.matchesNumberContext());
    assertTrue(OBJECT_TYPE.matchesObjectContext());
    assertTrue(OBJECT_TYPE.matchesStringContext());
    assertFalse(OBJECT_TYPE.matchesUint32Context());

    
    assertTypeEquals(OBJECT_PROTOTYPE, OBJECT_TYPE.getImplicitPrototype());

    
    assertEquals("Object", OBJECT_TYPE.toString());

    assertTrue(OBJECT_TYPE.isNativeObjectType());
    assertTrue(OBJECT_TYPE.getImplicitPrototype().isNativeObjectType());

    Asserts.assertResolvesToSame(OBJECT_TYPE);
    assertFalse(OBJECT_TYPE.isNominalConstructor());
    assertTrue(OBJECT_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNumberObjectType
  public void testNumberObjectType() throws Exception {
    
    assertFalse(NUMBER_OBJECT_TYPE.isArrayType());
    assertFalse(NUMBER_OBJECT_TYPE.isBooleanObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isBooleanValueType());
    assertFalse(NUMBER_OBJECT_TYPE.isDateType());
    assertFalse(NUMBER_OBJECT_TYPE.isEnumElementType());
    assertFalse(NUMBER_OBJECT_TYPE.isNamedType());
    assertFalse(NUMBER_OBJECT_TYPE.isNullType());
    assertTrue(NUMBER_OBJECT_TYPE.isNumber());
    assertTrue(NUMBER_OBJECT_TYPE.isNumberObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isNumberValueType());
    assertTrue(NUMBER_OBJECT_TYPE.isObject());
    assertFalse(NUMBER_OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(
        NUMBER_OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(NUMBER_OBJECT_TYPE.isRegexpType());
    assertFalse(NUMBER_OBJECT_TYPE.isString());
    assertFalse(NUMBER_OBJECT_TYPE.isStringObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isStringValueType());
    assertFalse(NUMBER_OBJECT_TYPE.isEnumType());
    assertFalse(NUMBER_OBJECT_TYPE.isUnionType());
    assertFalse(NUMBER_OBJECT_TYPE.isAllType());
    assertFalse(NUMBER_OBJECT_TYPE.isVoidType());
    assertFalse(NUMBER_OBJECT_TYPE.isConstructor());
    assertTrue(NUMBER_OBJECT_TYPE.isInstanceType());

    
    assertTypeEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autoboxesTo());

    
    assertTypeEquals(NUMBER_TYPE, NUMBER_OBJECT_TYPE.unboxesTo());

    
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(functionType));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(namedGoogBar));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(
            createUnionType(NUMBER_OBJECT_TYPE, NULL_TYPE)));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(
            createUnionType(NUMBER_TYPE, NULL_TYPE)));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_OBJECT_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, elementsType);
    assertCannotTestForEqualityWith(NUMBER_OBJECT_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, ARRAY_TYPE);

    
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(NUMBER_OBJECT_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        NUMBER_OBJECT_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, STRING_OBJECT_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, NUMBER_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, functionType),
        NUMBER_OBJECT_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE,
        NUMBER_OBJECT_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, DATE_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, REGEXP_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NUMBER_OBJECT_TYPE.matchesInt32Context());
    assertTrue(NUMBER_OBJECT_TYPE.matchesNumberContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesObjectContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesStringContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesUint32Context());

    
    assertEquals("Number", NUMBER_OBJECT_TYPE.toString());
    assertTrue(NUMBER_OBJECT_TYPE.hasDisplayName());
    assertEquals("Number", NUMBER_OBJECT_TYPE.getDisplayName());

    assertTrue(NUMBER_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(NUMBER_OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNumberValueType
  public void testNumberValueType() throws Exception {
    
    assertFalse(NUMBER_TYPE.isArrayType());
    assertFalse(NUMBER_TYPE.isBooleanObjectType());
    assertFalse(NUMBER_TYPE.isBooleanValueType());
    assertFalse(NUMBER_TYPE.isDateType());
    assertFalse(NUMBER_TYPE.isEnumElementType());
    assertFalse(NUMBER_TYPE.isNamedType());
    assertFalse(NUMBER_TYPE.isNullType());
    assertTrue(NUMBER_TYPE.isNumber());
    assertFalse(NUMBER_TYPE.isNumberObjectType());
    assertTrue(NUMBER_TYPE.isNumberValueType());
    assertFalse(NUMBER_TYPE.isFunctionPrototypeType());
    assertFalse(NUMBER_TYPE.isRegexpType());
    assertFalse(NUMBER_TYPE.isString());
    assertFalse(NUMBER_TYPE.isStringObjectType());
    assertFalse(NUMBER_TYPE.isStringValueType());
    assertFalse(NUMBER_TYPE.isEnumType());
    assertFalse(NUMBER_TYPE.isUnionType());
    assertFalse(NUMBER_TYPE.isAllType());
    assertFalse(NUMBER_TYPE.isVoidType());
    assertFalse(NUMBER_TYPE.isConstructor());
    assertFalse(NUMBER_TYPE.isInstanceType());

    
    assertTypeEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autoboxesTo());

    
    assertTrue(NUMBER_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NUMBER_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(functionType));
    assertFalse(NUMBER_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NUMBER_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(NUMBER_TYPE.canAssignTo(namedGoogBar));
    assertTrue(NUMBER_TYPE.canAssignTo(
            createUnionType(NUMBER_TYPE, NULL_TYPE)));
    assertTrue(NUMBER_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(NUMBER_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, STRING_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NUMBER_TYPE, functionType);
    assertCannotTestForEqualityWith(NUMBER_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, UNKNOWN_TYPE);

    
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        NUMBER_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, STRING_OBJECT_TYPE),
        NUMBER_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(NUMBER_TYPE,
        NUMBER_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, functionType),
        NUMBER_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(createUnionType(NUMBER_TYPE, OBJECT_TYPE),
        NUMBER_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, DATE_TYPE),
        NUMBER_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, REGEXP_TYPE),
        NUMBER_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NUMBER_TYPE.matchesInt32Context());
    assertTrue(NUMBER_TYPE.matchesNumberContext());
    assertTrue(NUMBER_TYPE.matchesObjectContext());
    assertTrue(NUMBER_TYPE.matchesStringContext());
    assertTrue(NUMBER_TYPE.matchesUint32Context());

    
    assertEquals("number", NUMBER_TYPE.toString());
    assertTrue(NUMBER_TYPE.hasDisplayName());
    assertEquals("number", NUMBER_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(NUMBER_TYPE);
    assertFalse(NUMBER_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNullType
  public void testNullType() throws Exception {

    
    assertFalse(NULL_TYPE.isArrayType());
    assertFalse(NULL_TYPE.isBooleanValueType());
    assertFalse(NULL_TYPE.isDateType());
    assertFalse(NULL_TYPE.isEnumElementType());
    assertFalse(NULL_TYPE.isNamedType());
    assertTrue(NULL_TYPE.isNullType());
    assertFalse(NULL_TYPE.isNumber());
    assertFalse(NULL_TYPE.isNumberObjectType());
    assertFalse(NULL_TYPE.isNumberValueType());
    assertFalse(NULL_TYPE.isFunctionPrototypeType());
    assertFalse(NULL_TYPE.isRegexpType());
    assertFalse(NULL_TYPE.isString());
    assertFalse(NULL_TYPE.isStringObjectType());
    assertFalse(NULL_TYPE.isStringValueType());
    assertFalse(NULL_TYPE.isEnumType());
    assertFalse(NULL_TYPE.isUnionType());
    assertFalse(NULL_TYPE.isAllType());
    assertFalse(NULL_TYPE.isVoidType());
    assertFalse(NULL_TYPE.isConstructor());
    assertFalse(NULL_TYPE.isInstanceType());

    
    assertNull(NULL_TYPE.autoboxesTo());

    
    assertFalse(NULL_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(functionType));
    assertFalse(NULL_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(UNKNOWN_TYPE));

    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NO_OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NO_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NULL_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(ALL_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(STRING_OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NUMBER_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(functionType)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(DATE_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(REGEXP_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(ARRAY_TYPE)));

    
    assertFalse(NULL_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(NULL_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NULL_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NULL_TYPE, ALL_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, ARRAY_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, BOOLEAN_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, DATE_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, EVAL_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, functionType);
    assertCannotTestForEqualityWith(NULL_TYPE, NULL_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, NUMBER_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, NUMBER_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, URI_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, RANGE_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, REFERENCE_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, REGEXP_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, STRING_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, STRING_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, SYNTAX_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, TYPE_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, VOID_TYPE);

    
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NULL_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NULL_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(
            createNullableType(STRING_OBJECT_TYPE)));

    
    assertTypeEquals(NULL_TYPE, NULL_TYPE.getLeastSupertype(NULL_TYPE));
    assertTypeEquals(ALL_TYPE, NULL_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createNullableType(STRING_OBJECT_TYPE),
        NULL_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createNullableType(NUMBER_TYPE),
        NULL_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createNullableType(functionType),
        NULL_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(createNullableType(OBJECT_TYPE),
        NULL_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createNullableType(DATE_TYPE),
        NULL_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createNullableType(REGEXP_TYPE),
        NULL_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NULL_TYPE.matchesInt32Context());
    assertTrue(NULL_TYPE.matchesNumberContext());
    assertFalse(NULL_TYPE.matchesObjectContext());
    assertTrue(NULL_TYPE.matchesStringContext());
    assertTrue(NULL_TYPE.matchesUint32Context());

    
    assertFalse(NULL_TYPE.matchesObjectContext());

    
    assertEquals("null", NULL_TYPE.toString());
    assertTrue(NULL_TYPE.hasDisplayName());
    assertEquals("null", NULL_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(NULL_TYPE);

    
    assertTrue(
        NULL_TYPE.isSubtype(
            createUnionType(forwardDeclaredNamedType, NULL_TYPE)));
    assertTypeEquals(NULL_TYPE,
        NULL_TYPE.getGreatestSubtype(
            createUnionType(forwardDeclaredNamedType, NULL_TYPE)));
    assertFalse(NULL_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testDateType
  public void testDateType() throws Exception {
    
    assertFalse(DATE_TYPE.isArrayType());
    assertFalse(DATE_TYPE.isBooleanValueType());
    assertTrue(DATE_TYPE.isDateType());
    assertFalse(DATE_TYPE.isEnumElementType());
    assertFalse(DATE_TYPE.isNamedType());
    assertFalse(DATE_TYPE.isNullType());
    assertFalse(DATE_TYPE.isNumberValueType());
    assertFalse(DATE_TYPE.isFunctionPrototypeType());
    assertTrue(DATE_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(DATE_TYPE.isRegexpType());
    assertFalse(DATE_TYPE.isStringValueType());
    assertFalse(DATE_TYPE.isEnumType());
    assertFalse(DATE_TYPE.isUnionType());
    assertFalse(DATE_TYPE.isAllType());
    assertFalse(DATE_TYPE.isVoidType());
    assertFalse(DATE_TYPE.isConstructor());
    assertTrue(DATE_TYPE.isInstanceType());

    
    assertNull(DATE_TYPE.autoboxesTo());

    
    assertFalse(DATE_TYPE.canAssignTo(NO_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(DATE_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(functionType));
    assertFalse(DATE_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(VOID_TYPE));

    
    assertFalse(DATE_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(DATE_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, functionType);
    assertCannotTestForEqualityWith(DATE_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, ARRAY_TYPE);

    
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(DATE_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(DATE_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(DATE_TYPE.isNullable());
    assertTrue(createNullableType(DATE_TYPE).isNullable());

    
    assertTypeEquals(ALL_TYPE,
        DATE_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, STRING_OBJECT_TYPE),
        DATE_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, NUMBER_TYPE),
        DATE_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, functionType),
        DATE_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE, DATE_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(DATE_TYPE, DATE_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, REGEXP_TYPE),
        DATE_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(46, DATE_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(46, DATE_TYPE.getPropertiesCount());
    assertReturnTypeEquals(DATE_TYPE, DATE_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toDateString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toTimeString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleDateString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleTimeString"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getTime"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMonth"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getDate"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCDate"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getDay"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getUTCDay"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getTimezoneOffset"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setTime"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCHours"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setDate"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCDate"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCFullYear"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toUTCString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toGMTString"));

    
    assertTrue(DATE_TYPE.matchesInt32Context());
    assertTrue(DATE_TYPE.matchesNumberContext());
    assertTrue(DATE_TYPE.matchesObjectContext());
    assertTrue(DATE_TYPE.matchesStringContext());
    assertTrue(DATE_TYPE.matchesUint32Context());

    
    assertEquals("Date", DATE_TYPE.toString());
    assertTrue(DATE_TYPE.hasDisplayName());
    assertEquals("Date", DATE_TYPE.getDisplayName());

    assertTrue(DATE_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(DATE_TYPE);
    assertFalse(DATE_TYPE.isNominalConstructor());
    assertTrue(DATE_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRegExpType
  public void testRegExpType() throws Exception {
    
    assertFalse(REGEXP_TYPE.isNoType());
    assertFalse(REGEXP_TYPE.isNoObjectType());
    assertFalse(REGEXP_TYPE.isArrayType());
    assertFalse(REGEXP_TYPE.isBooleanValueType());
    assertFalse(REGEXP_TYPE.isDateType());
    assertFalse(REGEXP_TYPE.isEnumElementType());
    assertFalse(REGEXP_TYPE.isNamedType());
    assertFalse(REGEXP_TYPE.isNullType());
    assertFalse(REGEXP_TYPE.isNumberValueType());
    assertFalse(REGEXP_TYPE.isFunctionPrototypeType());
    assertTrue(REGEXP_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertTrue(REGEXP_TYPE.isRegexpType());
    assertFalse(REGEXP_TYPE.isStringValueType());
    assertFalse(REGEXP_TYPE.isEnumType());
    assertFalse(REGEXP_TYPE.isUnionType());
    assertFalse(REGEXP_TYPE.isAllType());
    assertFalse(REGEXP_TYPE.isVoidType());

    
    assertNull(REGEXP_TYPE.autoboxesTo());

    
    assertFalse(REGEXP_TYPE.canAssignTo(NO_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(ARRAY_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(functionType));
    assertFalse(REGEXP_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(REGEXP_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(REGEXP_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, functionType);
    assertCannotTestForEqualityWith(REGEXP_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, ARRAY_TYPE);

    
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(REGEXP_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(REGEXP_TYPE.isNullable());
    assertTrue(createNullableType(REGEXP_TYPE).isNullable());

    
    assertTypeEquals(ALL_TYPE,
        REGEXP_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, STRING_OBJECT_TYPE),
        REGEXP_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, NUMBER_TYPE),
        REGEXP_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, functionType),
        REGEXP_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE, REGEXP_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, REGEXP_TYPE),
        REGEXP_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(REGEXP_TYPE,
        REGEXP_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(9, REGEXP_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(14, REGEXP_TYPE.getPropertiesCount());
    assertReturnTypeEquals(REGEXP_TYPE,
        REGEXP_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(createNullableType(ARRAY_TYPE),
        REGEXP_TYPE.getPropertyType("exec"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        REGEXP_TYPE.getPropertyType("test"));
    assertReturnTypeEquals(STRING_TYPE,
        REGEXP_TYPE.getPropertyType("toString"));
    assertTypeEquals(STRING_TYPE, REGEXP_TYPE.getPropertyType("source"));
    assertTypeEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("global"));
    assertTypeEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("ignoreCase"));
    assertTypeEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("multiline"));
    assertTypeEquals(NUMBER_TYPE, REGEXP_TYPE.getPropertyType("lastIndex"));

    
    assertFalse(REGEXP_TYPE.matchesInt32Context());
    assertFalse(REGEXP_TYPE.matchesNumberContext());
    assertTrue(REGEXP_TYPE.matchesObjectContext());
    assertTrue(REGEXP_TYPE.matchesStringContext());
    assertFalse(REGEXP_TYPE.matchesUint32Context());

    
    assertEquals("RegExp", REGEXP_TYPE.toString());
    assertTrue(REGEXP_TYPE.hasDisplayName());
    assertEquals("RegExp", REGEXP_TYPE.getDisplayName());

    assertTrue(REGEXP_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(REGEXP_TYPE);
    assertFalse(REGEXP_TYPE.isNominalConstructor());
    assertTrue(REGEXP_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringObjectType
  public void testStringObjectType() throws Exception {
    
    assertFalse(STRING_OBJECT_TYPE.isArrayType());
    assertFalse(STRING_OBJECT_TYPE.isBooleanObjectType());
    assertFalse(STRING_OBJECT_TYPE.isBooleanValueType());
    assertFalse(STRING_OBJECT_TYPE.isDateType());
    assertFalse(STRING_OBJECT_TYPE.isEnumElementType());
    assertFalse(STRING_OBJECT_TYPE.isNamedType());
    assertFalse(STRING_OBJECT_TYPE.isNullType());
    assertFalse(STRING_OBJECT_TYPE.isNumber());
    assertFalse(STRING_OBJECT_TYPE.isNumberObjectType());
    assertFalse(STRING_OBJECT_TYPE.isNumberValueType());
    assertFalse(STRING_OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(
        STRING_OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(STRING_OBJECT_TYPE.isRegexpType());
    assertTrue(STRING_OBJECT_TYPE.isString());
    assertTrue(STRING_OBJECT_TYPE.isStringObjectType());
    assertFalse(STRING_OBJECT_TYPE.isStringValueType());
    assertFalse(STRING_OBJECT_TYPE.isEnumType());
    assertFalse(STRING_OBJECT_TYPE.isUnionType());
    assertFalse(STRING_OBJECT_TYPE.isAllType());
    assertFalse(STRING_OBJECT_TYPE.isVoidType());
    assertFalse(STRING_OBJECT_TYPE.isConstructor());
    assertTrue(STRING_OBJECT_TYPE.isInstanceType());

    
    assertTypeEquals(STRING_OBJECT_TYPE, STRING_TYPE.autoboxesTo());

    
    assertTypeEquals(STRING_TYPE, STRING_OBJECT_TYPE.unboxesTo());

    
    assertTrue(STRING_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(ARRAY_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(STRING_TYPE));

    
    assertFalse(STRING_OBJECT_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, UNKNOWN_TYPE);

    
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertEquals(23, STRING_OBJECT_TYPE.getImplicitPrototype().
        getPropertiesCount());
    assertEquals(24, STRING_OBJECT_TYPE.getPropertiesCount());

    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("charAt"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("charCodeAt"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("concat"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("indexOf"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("lastIndexOf"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("localeCompare"));
    assertReturnTypeEquals(createNullableType(ARRAY_TYPE),
        STRING_OBJECT_TYPE.getPropertyType("match"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("replace"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("search"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("slice"));
    assertReturnTypeEquals(ARRAY_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("split"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("substring"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toLowerCase"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toLocaleLowerCase"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toUpperCase"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toLocaleUpperCase"));
    assertTypeEquals(NUMBER_TYPE, STRING_OBJECT_TYPE.getPropertyType("length"));

    
    assertTrue(STRING_OBJECT_TYPE.matchesInt32Context());
    assertTrue(STRING_OBJECT_TYPE.matchesNumberContext());
    assertTrue(STRING_OBJECT_TYPE.matchesObjectContext());
    assertTrue(STRING_OBJECT_TYPE.matchesStringContext());
    assertTrue(STRING_OBJECT_TYPE.matchesUint32Context());

    
    assertFalse(STRING_OBJECT_TYPE.isNullable());
    assertTrue(createNullableType(STRING_OBJECT_TYPE).isNullable());

    assertTrue(STRING_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(STRING_OBJECT_TYPE);

    assertTrue(STRING_OBJECT_TYPE.hasDisplayName());
    assertEquals("String", STRING_OBJECT_TYPE.getDisplayName());
    assertFalse(STRING_OBJECT_TYPE.isNominalConstructor());
    assertTrue(STRING_OBJECT_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringValueType
  public void testStringValueType() throws Exception {
    
    assertFalse(STRING_TYPE.isArrayType());
    assertFalse(STRING_TYPE.isBooleanObjectType());
    assertFalse(STRING_TYPE.isBooleanValueType());
    assertFalse(STRING_TYPE.isDateType());
    assertFalse(STRING_TYPE.isEnumElementType());
    assertFalse(STRING_TYPE.isNamedType());
    assertFalse(STRING_TYPE.isNullType());
    assertFalse(STRING_TYPE.isNumber());
    assertFalse(STRING_TYPE.isNumberObjectType());
    assertFalse(STRING_TYPE.isNumberValueType());
    assertFalse(STRING_TYPE.isFunctionPrototypeType());
    assertFalse(STRING_TYPE.isRegexpType());
    assertTrue(STRING_TYPE.isString());
    assertFalse(STRING_TYPE.isStringObjectType());
    assertTrue(STRING_TYPE.isStringValueType());
    assertFalse(STRING_TYPE.isEnumType());
    assertFalse(STRING_TYPE.isUnionType());
    assertFalse(STRING_TYPE.isAllType());
    assertFalse(STRING_TYPE.isVoidType());
    assertFalse(STRING_TYPE.isConstructor());
    assertFalse(STRING_TYPE.isInstanceType());

    
    assertTypeEquals(STRING_OBJECT_TYPE, STRING_TYPE.autoboxesTo());

    
    assertTypeEquals(STRING_TYPE, STRING_OBJECT_TYPE.unboxesTo());

    
    assertTrue(STRING_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(STRING_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(STRING_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(STRING_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(STRING_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, STRING_OBJECT_TYPE);
    assertCannotTestForEqualityWith(STRING_TYPE, functionType);
    assertCanTestForEqualityWith(STRING_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, UNKNOWN_TYPE);

    
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(STRING_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertTrue(STRING_TYPE.matchesInt32Context());
    assertTrue(STRING_TYPE.matchesNumberContext());
    assertTrue(STRING_TYPE.matchesObjectContext());
    assertTrue(STRING_TYPE.matchesStringContext());
    assertTrue(STRING_TYPE.matchesUint32Context());

    
    assertFalse(STRING_TYPE.isNullable());
    assertTrue(createNullableType(STRING_TYPE).isNullable());

    
    assertEquals("string", STRING_TYPE.toString());
    assertTrue(STRING_TYPE.hasDisplayName());
    assertEquals("string", STRING_TYPE.getDisplayName());

    
    assertTypeEquals(NUMBER_TYPE, STRING_TYPE.findPropertyType("length"));
    assertEquals(null, STRING_TYPE.findPropertyType("unknownProperty"));

    Asserts.assertResolvesToSame(STRING_TYPE);
    assertFalse(STRING_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordType
  public void testRecordType() throws Exception {
    
    assertTrue(recordType.isObject());
    assertFalse(recordType.isFunctionPrototypeType());

    
    assertTrue(recordType.canAssignTo(ALL_TYPE));
    assertFalse(recordType.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(recordType.canAssignTo(NUMBER_TYPE));
    assertFalse(recordType.canAssignTo(DATE_TYPE));
    assertFalse(recordType.canAssignTo(REGEXP_TYPE));
    assertTrue(recordType.canAssignTo(UNKNOWN_TYPE));
    assertTrue(recordType.canAssignTo(OBJECT_TYPE));
    assertFalse(recordType.canAssignTo(U2U_CONSTRUCTOR_TYPE));

    
    assertNull(recordType.autoboxesTo());

    
    assertFalse(recordType.canBeCalled());

    
    assertCanTestForEqualityWith(recordType, ALL_TYPE);
    assertCanTestForEqualityWith(recordType, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(recordType, recordType);
    assertCanTestForEqualityWith(recordType, functionType);
    assertCanTestForEqualityWith(recordType, OBJECT_TYPE);
    assertCanTestForEqualityWith(recordType, NUMBER_TYPE);
    assertCanTestForEqualityWith(recordType, DATE_TYPE);
    assertCanTestForEqualityWith(recordType, REGEXP_TYPE);

    
    assertTrue(recordType.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(recordType.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(recordType));
    assertFalse(recordType.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(recordType.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(recordType.matchesInt32Context());
    assertFalse(recordType.matchesNumberContext());
    assertTrue(recordType.matchesObjectContext());
    assertFalse(recordType.matchesStringContext());
    assertFalse(recordType.matchesUint32Context());

    Asserts.assertResolvesToSame(recordType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionInstanceType
  public void testFunctionInstanceType() throws Exception {
    FunctionType functionInst = FUNCTION_INSTANCE_TYPE;

    
    assertTrue(functionInst.isObject());
    assertFalse(functionInst.isFunctionPrototypeType());
    assertTrue(functionInst.getImplicitPrototype()
        .isFunctionPrototypeType());

    
    assertTrue(functionInst.canAssignTo(ALL_TYPE));
    assertFalse(functionInst.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(functionInst.canAssignTo(NUMBER_TYPE));
    assertFalse(functionInst.canAssignTo(DATE_TYPE));
    assertFalse(functionInst.canAssignTo(REGEXP_TYPE));
    assertTrue(functionInst.canAssignTo(UNKNOWN_TYPE));
    assertTrue(functionInst.canAssignTo(U2U_CONSTRUCTOR_TYPE));

    
    assertNull(functionInst.autoboxesTo());

    
    assertTrue(functionInst.canBeCalled());

    
    assertCanTestForEqualityWith(functionInst, ALL_TYPE);
    assertCanTestForEqualityWith(functionInst, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(functionInst, functionInst);
    assertCanTestForEqualityWith(functionInst, OBJECT_TYPE);
    assertCannotTestForEqualityWith(functionInst, NUMBER_TYPE);
    assertCanTestForEqualityWith(functionInst, DATE_TYPE);
    assertCanTestForEqualityWith(functionInst, REGEXP_TYPE);

    
    assertTrue(functionInst.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(functionInst.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(functionInst));
    assertFalse(functionInst.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(functionInst.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(functionInst.matchesInt32Context());
    assertFalse(functionInst.matchesNumberContext());
    assertTrue(functionInst.matchesObjectContext());
    assertFalse(functionInst.matchesStringContext());
    assertFalse(functionInst.matchesUint32Context());

    
    assertTrue(functionInst.hasProperty("prototype"));
    assertPropertyTypeInferred(functionInst, "prototype");

    
    assertTypeEquals(FUNCTION_FUNCTION_TYPE, functionInst.getConstructor());
    assertTypeEquals(FUNCTION_PROTOTYPE, functionInst.getImplicitPrototype());
    assertTypeEquals(functionInst, FUNCTION_FUNCTION_TYPE.getInstanceType());

    Asserts.assertResolvesToSame(functionInst);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionType
  public void testFunctionType() throws Exception {
    
    assertTrue(functionType.isObject());
    assertFalse(functionType.isFunctionPrototypeType());
    assertTrue(functionType.getImplicitPrototype().getImplicitPrototype()
        .isFunctionPrototypeType());

    
    assertTrue(functionType.canAssignTo(ALL_TYPE));
    assertFalse(functionType.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(functionType.canAssignTo(NUMBER_TYPE));
    assertFalse(functionType.canAssignTo(DATE_TYPE));
    assertFalse(functionType.canAssignTo(REGEXP_TYPE));
    assertTrue(functionType.canAssignTo(UNKNOWN_TYPE));
    assertTrue(functionType.canAssignTo(U2U_CONSTRUCTOR_TYPE));

    
    assertNull(functionType.autoboxesTo());

    
    assertTrue(functionType.canBeCalled());

    
    assertCanTestForEqualityWith(functionType, ALL_TYPE);
    assertCanTestForEqualityWith(functionType, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(functionType, functionType);
    assertCanTestForEqualityWith(functionType, OBJECT_TYPE);
    assertCannotTestForEqualityWith(functionType, NUMBER_TYPE);
    assertCanTestForEqualityWith(functionType, DATE_TYPE);
    assertCanTestForEqualityWith(functionType, REGEXP_TYPE);

    
    assertTrue(functionType.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(functionType.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(functionType));
    assertFalse(functionType.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(functionType.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(functionType.matchesInt32Context());
    assertFalse(functionType.matchesNumberContext());
    assertTrue(functionType.matchesObjectContext());
    assertFalse(functionType.matchesStringContext());
    assertFalse(functionType.matchesUint32Context());

    
    assertTrue(functionType.hasProperty("prototype"));
    assertPropertyTypeInferred(functionType, "prototype");

    Asserts.assertResolvesToSame(functionType);

    assertEquals("aFunctionName", new FunctionBuilder(registry).
        withName("aFunctionName").build().getDisplayName());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeSubtyping
  public void testRecordTypeSubtyping() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);
    JSType subRecordType = builder.build();

    assertTrue(subRecordType.isSubtype(recordType));
    assertFalse(recordType.isSubtype(subRecordType));

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", OBJECT_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    JSType differentRecordType = builder.build();

    assertFalse(differentRecordType.isSubtype(recordType));
    assertFalse(recordType.isSubtype(differentRecordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeSubtypingWithInferredProperties
  public void testRecordTypeSubtypingWithInferredProperties() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", googSubBarInst, null);
    JSType record = builder.build();

    ObjectType subtypeProp = registry.createAnonymousObjectType();
    subtypeProp.defineInferredProperty("a", googSubSubBarInst, null);
    assertTrue(subtypeProp.isSubtype(record));
    assertFalse(record.isSubtype(subtypeProp));

    ObjectType supertypeProp = registry.createAnonymousObjectType();
    supertypeProp.defineInferredProperty("a", googBarInst, null);
    assertFalse(supertypeProp.isSubtype(record));
    assertFalse(record.isSubtype(supertypeProp));

    ObjectType declaredSubtypeProp = registry.createAnonymousObjectType();
    declaredSubtypeProp.defineDeclaredProperty("a", googSubSubBarInst,
        null);
    assertFalse(declaredSubtypeProp.isSubtype(record));
    assertFalse(record.isSubtype(declaredSubtypeProp));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType1
  public void testRecordTypeLeastSuperType1() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);
    JSType subRecordType = builder.build();

    JSType leastSupertype = recordType.getLeastSupertype(subRecordType);
    assertTypeEquals(leastSupertype, recordType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType2
  public void testRecordTypeLeastSuperType2() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("e", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);
    JSType otherRecordType = builder.build();

    assertTypeEquals(
        registry.createUnionType(recordType, otherRecordType),
        recordType.getLeastSupertype(otherRecordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType3
  public void testRecordTypeLeastSuperType3() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE, null);
    builder.addProperty("e", STRING_TYPE, null);
    builder.addProperty("f", STRING_TYPE, null);
    JSType otherRecordType = builder.build();

    assertTypeEquals(
        registry.createUnionType(recordType, otherRecordType),
        recordType.getLeastSupertype(otherRecordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType4
  public void testRecordTypeLeastSuperType4() {
    JSType leastSupertype = recordType.getLeastSupertype(OBJECT_TYPE);
    assertTypeEquals(leastSupertype, OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType1
  public void testRecordTypeGreatestSubType1() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE, null);
    builder.addProperty("e", STRING_TYPE, null);
    builder.addProperty("f", STRING_TYPE, null);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE, null);
    builder.addProperty("e", STRING_TYPE, null);
    builder.addProperty("f", STRING_TYPE, null);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);

    assertTypeEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType2
  public void testRecordTypeGreatestSubType2() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);

    assertTypeEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType3
  public void testRecordTypeGreatestSubType3() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);

    assertTypeEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType4
  public void testRecordTypeGreatestSubType4() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);
    assertTypeEquals(subtype, NO_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType5
  public void testRecordTypeGreatestSubType5() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);

    JSType recordType = builder.build();

    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("a", STRING_TYPE, null);
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType6
  public void testRecordTypeGreatestSubType6() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("x", UNKNOWN_TYPE, null);

    JSType recordType = builder.build();

    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("x", STRING_TYPE, null);
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType7
  public void testRecordTypeGreatestSubType7() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("x", NUMBER_TYPE, null);

    JSType recordType = builder.build();

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("x", STRING_TYPE, null);
    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType8
  public void testRecordTypeGreatestSubType8() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("xyz", UNKNOWN_TYPE, null);

    JSType recordType = builder.build();

    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    googBar.defineDeclaredProperty("xyz", STRING_TYPE, null);

    assertTypeEquals(googBar,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTypeEquals(googBar,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));

    ObjectType googBarInst = googBar.getInstanceType();
    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(googBarInst));
    assertTypeEquals(NO_OBJECT_TYPE,
                 googBarInst.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testApplyOfDateMethod
  public void testApplyOfDateMethod() {
    JSType applyType = dateMethod.getPropertyType("apply");
    assertTrue("apply should be a function",
        applyType instanceof FunctionType);

    FunctionType applyFn = (FunctionType) applyType;
    assertTypeEquals("apply should have the same return type as its function",
        NUMBER_TYPE, applyFn.getReturnType());

    Node params = applyFn.getParametersNode();
    assertEquals("apply takes two args",
        2, params.getChildCount());
    assertTypeEquals("apply's first arg is the @this type",
        registry.createOptionalNullableType(DATE_TYPE),
        params.getFirstChild().getJSType());
    assertTypeEquals("apply's second arg is an Array",
        registry.createOptionalNullableType(OBJECT_TYPE),
        params.getLastChild().getJSType());
    assertTrue("apply's args must be optional",
        params.getFirstChild().isOptionalArg());
    assertTrue("apply's args must be optional",
        params.getLastChild().isOptionalArg());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCallOfDateMethod
  public void testCallOfDateMethod() {
    JSType callType = dateMethod.getPropertyType("call");
    assertTrue("call should be a function",
        callType instanceof FunctionType);

    FunctionType callFn = (FunctionType) callType;
    assertTypeEquals("call should have the same return type as its function",
        NUMBER_TYPE, callFn.getReturnType());

    Node params = callFn.getParametersNode();
    assertEquals("call takes one argument in this case",
        1, params.getChildCount());
    assertTypeEquals("call's first arg is the @this type",
        registry.createOptionalNullableType(DATE_TYPE),
        params.getFirstChild().getJSType());
    assertTrue("call's args must be optional",
        params.getFirstChild().isOptionalArg());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionTypeRepresentation
  public void testFunctionTypeRepresentation() {
    assertEquals("function (number, string): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, false, NUMBER_TYPE,
            STRING_TYPE).toString());

    assertEquals("function (new:Array, ...[*]): Array",
        ARRAY_FUNCTION_TYPE.toString());

    assertEquals("function (new:Boolean, *): boolean",
        BOOLEAN_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (new:Number, *): number",
        NUMBER_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (new:String, *): string",
        STRING_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (...[number]): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, true, NUMBER_TYPE)
        .toString());

    assertEquals("function (number, ...[string]): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, true, NUMBER_TYPE,
            STRING_TYPE).toString());

    assertEquals("function (this:Date, number): (boolean|number|string)",
        new FunctionBuilder(registry)
            .withParamsNode(registry.createParameters(NUMBER_TYPE))
            .withReturnType(NUMBER_STRING_BOOLEAN)
            .withTypeOfThis(DATE_TYPE)
            .build().toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionTypeRelationships
  public void testFunctionTypeRelationships() {
    FunctionType dateMethodEmpty = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(DATE_TYPE).build();
    FunctionType dateMethodWithParam = new FunctionBuilder(registry)
        .withParamsNode(registry.createOptionalParameters(NUMBER_TYPE))
        .withTypeOfThis(DATE_TYPE).build();
    FunctionType dateMethodWithReturn = new FunctionBuilder(registry)
        .withReturnType(NUMBER_TYPE)
        .withTypeOfThis(DATE_TYPE).build();
    FunctionType stringMethodEmpty = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(STRING_OBJECT_TYPE).build();
    FunctionType stringMethodWithParam = new FunctionBuilder(registry)
        .withParamsNode(registry.createOptionalParameters(NUMBER_TYPE))
        .withTypeOfThis(STRING_OBJECT_TYPE).build();
    FunctionType stringMethodWithReturn = new FunctionBuilder(registry)
        .withReturnType(NUMBER_TYPE)
        .withTypeOfThis(STRING_OBJECT_TYPE).build();

    
    assertFalse(stringMethodEmpty.isSubtype(dateMethodEmpty));

    
    List<FunctionType> allFunctions = Lists.newArrayList(
        dateMethodEmpty, dateMethodWithParam, dateMethodWithReturn,
        stringMethodEmpty, stringMethodWithParam, stringMethodWithReturn);
    for (int i = 0; i < allFunctions.size(); i++) {
      for (int j = 0; j < allFunctions.size(); j++) {
        FunctionType typeA = allFunctions.get(i);
        FunctionType typeB = allFunctions.get(j);
        assertEquals(String.format("equals(%s, %s)", typeA, typeB),
            i == j, typeA.isEquivalentTo(typeB));

        
        
        assertEquals(String.format("isSubtype(%s, %s)", typeA, typeB),
            typeA.getTypeOfThis().isEquivalentTo(typeB.getTypeOfThis()),
            typeA.isSubtype(typeB));

        if (i == j) {
          assertTypeEquals(typeA, typeA.getLeastSupertype(typeB));
          assertTypeEquals(typeA, typeA.getGreatestSubtype(typeB));
        } else {
          assertTypeEquals(String.format("sup(%s, %s)", typeA, typeB),
              U2U_CONSTRUCTOR_TYPE, typeA.getLeastSupertype(typeB));
          assertTypeEquals(String.format("inf(%s, %s)", typeA, typeB),
              LEAST_FUNCTION_TYPE, typeA.getGreatestSubtype(typeB));
        }
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testProxiedFunctionTypeRelationships
  public void testProxiedFunctionTypeRelationships() {
    FunctionType dateMethodEmpty = new FunctionBuilder(registry)
      .withParamsNode(registry.createParameters())
      .withTypeOfThis(DATE_TYPE).build().toMaybeFunctionType();
    FunctionType dateMethodWithParam = new FunctionBuilder(registry)
      .withParamsNode(registry.createParameters(NUMBER_TYPE))
      .withTypeOfThis(DATE_TYPE).build().toMaybeFunctionType();
    ProxyObjectType proxyDateMethodEmpty =
        new ProxyObjectType(registry, dateMethodEmpty);
    ProxyObjectType proxyDateMethodWithParam =
        new ProxyObjectType(registry, dateMethodWithParam);

    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
        proxyDateMethodEmpty.getLeastSupertype(proxyDateMethodWithParam));
    assertTypeEquals(LEAST_FUNCTION_TYPE,
        proxyDateMethodEmpty.getGreatestSubtype(proxyDateMethodWithParam));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionSubTypeRelationships
  public void testFunctionSubTypeRelationships() {
    FunctionType googBarMethod = new FunctionBuilder(registry)
        .withTypeOfThis(googBar).build();
    FunctionType googBarParamFn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(googBar)).build();
    FunctionType googBarReturnFn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withReturnType(googBar).build();
    FunctionType googSubBarMethod = new FunctionBuilder(registry)
        .withTypeOfThis(googSubBar).build();
    FunctionType googSubBarParamFn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(googSubBar)).build();
    FunctionType googSubBarReturnFn = new FunctionBuilder(registry)
        .withReturnType(googSubBar).build();

    assertTrue(googBarMethod.isSubtype(googSubBarMethod));
    assertTrue(googBarReturnFn.isSubtype(googSubBarReturnFn));

    List<FunctionType> allFunctions = Lists.newArrayList(
        googBarMethod, googBarParamFn, googBarReturnFn,
        googSubBarMethod, googSubBarParamFn, googSubBarReturnFn);
    for (int i = 0; i < allFunctions.size(); i++) {
      for (int j = 0; j < allFunctions.size(); j++) {
        FunctionType typeA = allFunctions.get(i);
        FunctionType typeB = allFunctions.get(j);
        assertEquals(String.format("equals(%s, %s)", typeA, typeB),
            i == j, typeA.isEquivalentTo(typeB));

        
        
        if (i == j) {
          assertTypeEquals(typeA, typeA.getLeastSupertype(typeB));
          assertTypeEquals(typeA, typeA.getGreatestSubtype(typeB));
        } else {
          assertTypeEquals(String.format("sup(%s, %s)", typeA, typeB),
              U2U_CONSTRUCTOR_TYPE, typeA.getLeastSupertype(typeB));
          assertTypeEquals(String.format("inf(%s, %s)", typeA, typeB),
              LEAST_FUNCTION_TYPE, typeA.getGreatestSubtype(typeB));
        }
      }
    }
  }

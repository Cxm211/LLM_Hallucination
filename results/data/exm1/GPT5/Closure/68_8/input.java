// buggy code
  boolean parse() {
    int lineno;
    int charno;

    // JSTypes are represented as Rhino AST nodes, and then resolved later.
    JSTypeExpression type;

    state = State.SEARCHING_ANNOTATION;
    skipEOLs();

    JsDocToken token = next();

    List<ExtendedTypeInfo> extendedTypes = Lists.newArrayList();

    // Always record that we have a comment.
    if (jsdocBuilder.shouldParseDocumentation()) {
      ExtractionInfo blockInfo = extractBlockComment(token);
      token = blockInfo.token;
      if (!blockInfo.string.isEmpty()) {
        jsdocBuilder.recordBlockDescription(blockInfo.string);
      }
    } else {
      if (token != JsDocToken.ANNOTATION &&
          token != JsDocToken.EOC) {
        // Mark that there was a description, but don't bother marking
        // what it was.
        jsdocBuilder.recordBlockDescription("");
      }
    }

    // Parse the actual JsDoc.
    retry: for (;;) {
      switch (token) {
        case ANNOTATION:
          if (state == State.SEARCHING_ANNOTATION) {
            state = State.SEARCHING_NEWLINE;
            lineno = stream.getLineno();
            charno = stream.getCharno();

            String annotationName = stream.getString();
            Annotation annotation = annotationNames.get(annotationName);
            if (annotation == null) {
              parser.addParserWarning("msg.bad.jsdoc.tag", annotationName,
                  stream.getLineno(), stream.getCharno());
            } else {
              // Mark the beginning of the annotation.
              jsdocBuilder.markAnnotation(annotationName, lineno, charno);

              switch (annotation) {
                case AUTHOR:
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo authorInfo = extractSingleLineBlock();
                    String author = authorInfo.string;

                    if (author.length() == 0) {
                      parser.addParserWarning("msg.jsdoc.authormissing",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      jsdocBuilder.addAuthor(author);
                    }
                    token = authorInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case CONSTANT:
                  if (!jsdocBuilder.recordConstancy()) {
                    parser.addParserWarning("msg.jsdoc.const",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case CONSTRUCTOR:
                  if (!jsdocBuilder.recordConstructor()) {
                    if (jsdocBuilder.isInterfaceRecorded()) {
                      parser.addTypeWarning("msg.jsdoc.interface.constructor",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      parser.addTypeWarning("msg.jsdoc.incompat.type",
                          stream.getLineno(), stream.getCharno());
                    }
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case DEPRECATED:
                  if (!jsdocBuilder.recordDeprecated()) {
                    parser.addParserWarning("msg.jsdoc.deprecated",
                        stream.getLineno(), stream.getCharno());
                  }

                  // Find the reason/description, if any.
                  ExtractionInfo reasonInfo =
                      extractMultilineTextualBlock(token);

                  String reason = reasonInfo.string;

                  if (reason.length() > 0) {
                    jsdocBuilder.recordDeprecationReason(reason);
                  }

                  token = reasonInfo.token;
                  continue retry;

                case INTERFACE:
                  if (!jsdocBuilder.recordInterface()) {
                    if (jsdocBuilder.isConstructorRecorded()) {
                      parser.addTypeWarning("msg.jsdoc.interface.constructor",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      parser.addTypeWarning("msg.jsdoc.incompat.type",
                          stream.getLineno(), stream.getCharno());
                    }
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case DESC:
                  if (jsdocBuilder.isDescriptionRecorded()) {
                    parser.addParserWarning("msg.jsdoc.desc.extra",
                        stream.getLineno(), stream.getCharno());
                    token = eatTokensUntilEOL();
                    continue retry;
                  } else {
                    ExtractionInfo descriptionInfo =
                        extractMultilineTextualBlock(token);

                    String description = descriptionInfo.string;

                    jsdocBuilder.recordDescription(description);
                    token = descriptionInfo.token;
                    continue retry;
                  }

                case FILE_OVERVIEW:
                  String fileOverview = "";
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo fileOverviewInfo =
                        extractMultilineTextualBlock(token,
                            WhitespaceOption.TRIM);

                    fileOverview = fileOverviewInfo.string;

                    token = fileOverviewInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }

                  if (!jsdocBuilder.recordFileOverview(fileOverview) ||
                      fileOverviewJSDocInfo != null) {
                    parser.addParserWarning("msg.jsdoc.fileoverview.extra",
                        stream.getLineno(), stream.getCharno());
                  }
                  continue retry;

                case LICENSE:
                case PRESERVE:
                  ExtractionInfo preserveInfo =
                      extractMultilineTextualBlock(token,
                                                   WhitespaceOption.PRESERVE);

                  String preserve = preserveInfo.string;

                  if (preserve.length() > 0) {
                    if (fileLevelJsDocBuilder != null) {
                      fileLevelJsDocBuilder.append(preserve);
                    }
                  }

                  token = preserveInfo.token;
                  continue retry;

                case ENUM:
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();

                  type = null;
                  if (token != JsDocToken.EOL && token != JsDocToken.EOC) {
                    type = createJSTypeExpression(
                        parseAndRecordTypeNode(token));
                  }

                  if (type == null) {
                    type = createJSTypeExpression(newStringNode("number"));
                  }
                  if (!jsdocBuilder.recordEnumParameterType(type)) {
                    parser.addTypeWarning(
                        "msg.jsdoc.incompat.type", lineno, charno);
                  }
                  token = eatTokensUntilEOL(token);
                  continue retry;

                case EXPORT:
                  if (!jsdocBuilder.recordExport()) {
                    parser.addParserWarning("msg.jsdoc.export",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case EXTERNS:
                  if (!jsdocBuilder.recordExterns()) {
                    parser.addParserWarning("msg.jsdoc.externs",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case JAVA_DISPATCH:
                  if (!jsdocBuilder.recordJavaDispatch()) {
                    parser.addParserWarning("msg.jsdoc.javadispatch",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case EXTENDS:
                case IMPLEMENTS:
                  skipEOLs();
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();
                  boolean matchingRc = false;

                  if (token == JsDocToken.LC) {
                    token = next();
                    matchingRc = true;
                  }

                  if (token == JsDocToken.STRING) {
                    Node typeNode = parseAndRecordTypeNameNode(
                        token, lineno, charno, matchingRc);

                    lineno = stream.getLineno();
                    charno = stream.getCharno();

                    typeNode = wrapNode(Token.BANG, typeNode);
                    if (typeNode != null && !matchingRc) {
                      typeNode.putBooleanProp(Node.BRACELESS_TYPE, true);
                    }
                    type = createJSTypeExpression(typeNode);

                    if (annotation == Annotation.EXTENDS) {
                      // record the extended type, check later
                      extendedTypes.add(new ExtendedTypeInfo(
                          type, stream.getLineno(), stream.getCharno()));
                    } else {
                      Preconditions.checkState(
                          annotation == Annotation.IMPLEMENTS);
                      if (!jsdocBuilder.recordImplementedInterface(type)) {
                        parser.addTypeWarning("msg.jsdoc.implements.duplicate",
                            lineno, charno);
                      }
                    }
                    token = next();
                    if (matchingRc) {
                      if (token != JsDocToken.RC) {
                        parser.addTypeWarning("msg.jsdoc.missing.rc",
                            stream.getLineno(), stream.getCharno());
                      }
                    } else if (token != JsDocToken.EOL &&
                        token != JsDocToken.EOF && token != JsDocToken.EOC) {
                      parser.addTypeWarning("msg.end.annotation.expected",
                          stream.getLineno(), stream.getCharno());
                    }
                  } else {
                    parser.addTypeWarning("msg.no.type.name", lineno, charno);
                  }
                  token = eatTokensUntilEOL(token);
                  continue retry;

                case HIDDEN:
                  if (!jsdocBuilder.recordHiddenness()) {
                    parser.addParserWarning("msg.jsdoc.hidden",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case LENDS:
                  skipEOLs();

                  matchingRc = false;
                  if (match(JsDocToken.LC)) {
                    token = next();
                    matchingRc = true;
                  }

                  if (match(JsDocToken.STRING)) {
                    token = next();
                    if (!jsdocBuilder.recordLends(stream.getString())) {
                      parser.addTypeWarning("msg.jsdoc.lends.incompatible",
                          stream.getLineno(), stream.getCharno());
                    }
                  } else {
                    parser.addTypeWarning("msg.jsdoc.lends.missing",
                        stream.getLineno(), stream.getCharno());
                  }

                  if (matchingRc && !match(JsDocToken.RC)) {
                    parser.addTypeWarning("msg.jsdoc.missing.rc",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case MEANING:
                  ExtractionInfo meaningInfo =
                      extractMultilineTextualBlock(token);
                  String meaning = meaningInfo.string;
                  token = meaningInfo.token;
                  if (!jsdocBuilder.recordMeaning(meaning)) {
                    parser.addParserWarning("msg.jsdoc.meaning.extra",
                        stream.getLineno(), stream.getCharno());
                  }
                  continue retry;

                case NO_ALIAS:
                  if (!jsdocBuilder.recordNoAlias()) {
                    parser.addParserWarning("msg.jsdoc.noalias",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_COMPILE:
                  if (!jsdocBuilder.recordNoCompile()) {
                    parser.addParserWarning("msg.jsdoc.nocompile",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_TYPE_CHECK:
                  if (!jsdocBuilder.recordNoTypeCheck()) {
                    parser.addParserWarning("msg.jsdoc.nocheck",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NOT_IMPLEMENTED:
                  token = eatTokensUntilEOL();
                  continue retry;

                case INHERIT_DOC:
                case OVERRIDE:
                  if (!jsdocBuilder.recordOverride()) {
                    parser.addTypeWarning("msg.jsdoc.override",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case THROWS:
                  skipEOLs();
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();
                  type = null;

                  if (token == JsDocToken.LC) {
                    type = createJSTypeExpression(
                        parseAndRecordTypeNode(token));

                    if (type == null) {
                      // parsing error reported during recursive descent
                      // recovering parsing
                      token = eatTokensUntilEOL();
                      continue retry;
                    }
                  }

                  // *Update* the token to that after the type annotation.
                  token = current();

                  // Save the throw type.
                  jsdocBuilder.recordThrowType(type);

                  // Find the throw's description (if applicable).
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo descriptionInfo =
                        extractMultilineTextualBlock(token);

                    String description = descriptionInfo.string;

                    if (description.length() > 0) {
                      jsdocBuilder.recordThrowDescription(type, description);
                    }

                    token = descriptionInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case PARAM:
                  skipEOLs();
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();
                  type = null;

                  if (token == JsDocToken.LC) {
                    type = createJSTypeExpression(
                        parseAndRecordParamTypeNode(token));

                    if (type == null) {
                      // parsing error reported during recursive descent
                      // recovering parsing
                      token = eatTokensUntilEOL();
                      continue retry;
                    }
                    skipEOLs();
                    token = next();
                    lineno = stream.getLineno();
                    charno = stream.getCharno();
                  }

                  String name = null;
                  boolean isBracketedParam = JsDocToken.LB == token;
                  if (isBracketedParam) {
                    token = next();
                  }

                  if (JsDocToken.STRING != token) {
                    parser.addTypeWarning("msg.missing.variable.name",
                        lineno, charno);
                  } else {
                    name = stream.getString();

                    if (isBracketedParam) {
                      token = next();

                      // Throw out JsDocToolkit's "default" parameter
                      // annotation.  It makes no sense under our type
                      // system.
                      if (JsDocToken.EQUALS == token) {
                        token = next();
                        if (JsDocToken.STRING == token) {
                          token = next();
                        }
                      }

                      if (JsDocToken.RB != token) {
                        reportTypeSyntaxWarning("msg.jsdoc.missing.rb");
                      } else if (type != null) {
                        // Make the type expression optional, if it isn't
                        // already.
                        type = JSTypeExpression.makeOptionalArg(type);
                      }
                    }

                    // If the param name has a DOT in it, just throw it out
                    // quietly. We do not handle the JsDocToolkit method
                    // for handling properties of params.
                    if (name.indexOf('.') > -1) {
                      name = null;
                    } else if (!jsdocBuilder.recordParameter(name, type)) {
                      if (jsdocBuilder.hasParameter(name)) {
                        parser.addTypeWarning("msg.dup.variable.name", name,
                            lineno, charno);
                      } else {
                        parser.addTypeWarning("msg.jsdoc.incompat.type", name,
                            lineno, charno);
                      }
                    }
                  }

                  if (name == null) {
                    token = eatTokensUntilEOL(token);
                    continue retry;
                  }

                  jsdocBuilder.markName(name, lineno, charno);

                  // Find the parameter's description (if applicable).
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo paramDescriptionInfo =
                        extractMultilineTextualBlock(token);

                    String paramDescription = paramDescriptionInfo.string;

                    if (paramDescription.length() > 0) {
                      jsdocBuilder.recordParameterDescription(name,
                          paramDescription);
                    }

                    token = paramDescriptionInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case PRESERVE_TRY:
                  if (!jsdocBuilder.recordPreserveTry()) {
                    parser.addParserWarning("msg.jsdoc.preservertry",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case PRIVATE:
                  if (!jsdocBuilder.recordVisibility(Visibility.PRIVATE)) {
                    parser.addParserWarning("msg.jsdoc.visibility.private",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case PROTECTED:
                  if (!jsdocBuilder.recordVisibility(Visibility.PROTECTED)) {
                    parser.addParserWarning("msg.jsdoc.visibility.protected",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case PUBLIC:
                  if (!jsdocBuilder.recordVisibility(Visibility.PUBLIC)) {
                    parser.addParserWarning("msg.jsdoc.visibility.public",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_SHADOW:
                  if (!jsdocBuilder.recordNoShadow()) {
                    parser.addParserWarning("msg.jsdoc.noshadow",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_SIDE_EFFECTS:
                  if (!jsdocBuilder.recordNoSideEffects()) {
                    parser.addParserWarning("msg.jsdoc.nosideeffects",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case MODIFIES:
                  token = parseModifiesTag(next());
                  continue retry;

                case IMPLICIT_CAST:
                  if (!jsdocBuilder.recordImplicitCast()) {
                    parser.addTypeWarning("msg.jsdoc.implicitcast",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case SEE:
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo referenceInfo = extractSingleLineBlock();
                    String reference = referenceInfo.string;

                    if (reference.length() == 0) {
                      parser.addParserWarning("msg.jsdoc.seemissing",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      jsdocBuilder.addReference(reference);
                    }

                    token = referenceInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case SUPPRESS:
                  token = parseSuppressTag(next());
                  continue retry;

                case TEMPLATE:
                  ExtractionInfo templateInfo = extractSingleLineBlock();
                  String templateTypeName = templateInfo.string;

                  if (templateTypeName.length() == 0) {
                    parser.addTypeWarning("msg.jsdoc.templatemissing",
                          stream.getLineno(), stream.getCharno());
                  } else if (!jsdocBuilder.recordTemplateTypeName(
                      templateTypeName)) {
                    parser.addTypeWarning("msg.jsdoc.template.at.most.once",
                        stream.getLineno(), stream.getCharno());
                  }

                  token = templateInfo.token;
                  continue retry;

                case VERSION:
                  ExtractionInfo versionInfo = extractSingleLineBlock();
                  String version = versionInfo.string;

                  if (version.length() == 0) {
                    parser.addParserWarning("msg.jsdoc.versionmissing",
                          stream.getLineno(), stream.getCharno());
                  } else {
                    if (!jsdocBuilder.recordVersion(version)) {
                       parser.addParserWarning("msg.jsdoc.extraversion",
                          stream.getLineno(), stream.getCharno());
                    }
                  }

                  token = versionInfo.token;
                  continue retry;

                case DEFINE:
                case RETURN:
                case THIS:
                case TYPE:
                case TYPEDEF:
                  lineno = stream.getLineno();
                  charno = stream.getCharno();

                  Node typeNode = null;
                  if (!lookAheadForTypeAnnotation() &&
                      annotation == Annotation.RETURN) {
                    // If RETURN doesn't have a type annotation, record
                    // it as the unknown type.
                    typeNode = newNode(Token.QMARK);
                  } else {
                    skipEOLs();
                    token = next();
                    typeNode = parseAndRecordTypeNode(token, lineno, charno);
                  }

                  if (annotation == Annotation.THIS) {
                    typeNode = wrapNode(Token.BANG, typeNode);
                    if (typeNode != null && token != JsDocToken.LC) {
                      typeNode.putBooleanProp(Node.BRACELESS_TYPE, true);
                    }
                  }
                  type = createJSTypeExpression(typeNode);

                  if (type == null) {
                    // error reported during recursive descent
                    // recovering parsing
                  } else {
                    switch (annotation) {
                      case DEFINE:
                        if (!jsdocBuilder.recordDefineType(type)) {
                          parser.addParserWarning("msg.jsdoc.define",
                              lineno, charno);
                        }
                        break;

                      case RETURN:
                        if (!jsdocBuilder.recordReturnType(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                          break;
                        }

                        // Find the return's description (if applicable).
                        if (jsdocBuilder.shouldParseDocumentation()) {
                          ExtractionInfo returnDescriptionInfo =
                              extractMultilineTextualBlock(token);

                          String returnDescription =
                              returnDescriptionInfo.string;

                          if (returnDescription.length() > 0) {
                            jsdocBuilder.recordReturnDescription(
                                returnDescription);
                          }

                          token = returnDescriptionInfo.token;
                        } else {
                          token = eatTokensUntilEOL(token);
                        }
                        continue retry;

                      case THIS:
                        if (!jsdocBuilder.recordThisType(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                        }
                        break;

                      case TYPE:
                        if (!jsdocBuilder.recordType(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                        }
                        break;

                      case TYPEDEF:
                        if (!jsdocBuilder.recordTypedef(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                        }
                        break;
                    }

                  token = eatTokensUntilEOL();
                  }
                  continue retry;
              }
            }
          }
          break;

        case EOC:
          if (hasParsedFileOverviewDocInfo()) {
            fileOverviewJSDocInfo = retrieveAndResetParsedJSDocInfo();
          }
          checkExtendedTypes(extendedTypes);
          return true;

        case EOF:
          // discard any accumulated information
          jsdocBuilder.build(null);
          parser.addParserWarning("msg.unexpected.eof",
              stream.getLineno(), stream.getCharno());
          checkExtendedTypes(extendedTypes);
          return false;

        case EOL:
          if (state == State.SEARCHING_NEWLINE) {
            state = State.SEARCHING_ANNOTATION;
          }
          token = next();
          continue retry;

        default:
          if (token == JsDocToken.STAR && state == State.SEARCHING_ANNOTATION) {
            token = next();
            continue retry;
          } else {
            state = State.SEARCHING_NEWLINE;
            token = eatTokensUntilEOL();
            continue retry;
          }
      }

      // next token
      token = next();
    }
  }

  private Node parseBasicTypeExpression(JsDocToken token) {
    if (token == JsDocToken.STAR) {
      return newNode(Token.STAR);
    } else if (token == JsDocToken.LB) {
      skipEOLs();
      return parseArrayType(next());
    } else if (token == JsDocToken.LC) {
      skipEOLs();
      return parseRecordType(next());
    } else if (token == JsDocToken.LP) {
      skipEOLs();
      return parseUnionType(next());
    } else if (token == JsDocToken.STRING) {
      String string = stream.getString();
      if ("function".equals(string)) {
        skipEOLs();
        return parseFunctionType(next());
      } else if ("null".equals(string) || "undefined".equals(string)) {
        return newStringNode(string);
      } else {
        return parseTypeName(token);
      }
    }

    return reportGenericTypeSyntaxWarning();
  }

  private Node parseFunctionType(JsDocToken token) {
    // NOTE(nicksantos): We're not implementing generics at the moment, so
    // just throw out TypeParameters.
    if (token != JsDocToken.LP) {
      return reportTypeSyntaxWarning("msg.jsdoc.missing.lp");
    }

    Node functionType = newNode(Token.FUNCTION);
    Node parameters = null;
    skipEOLs();
    if (!match(JsDocToken.RP)) {
      token = next();

      boolean hasParams = true;
      if (token == JsDocToken.STRING) {
        String tokenStr = stream.getString();
        boolean isThis = "this".equals(tokenStr);
        boolean isNew = "new".equals(tokenStr);
        if (isThis || isNew) {
          if (match(JsDocToken.COLON)) {
            next();
            skipEOLs();
            Node contextType = wrapNode(
                isThis ? Token.THIS : Token.NEW,
                parseTypeName(next()));
            if (contextType == null) {
              return null;
            }

            functionType.addChildToFront(contextType);
          } else {
            return reportTypeSyntaxWarning("msg.jsdoc.missing.colon");
          }

          if (match(JsDocToken.COMMA)) {
            next();
            skipEOLs();
            token = next();
          } else {
            hasParams = false;
          }
        }
      }

      if (hasParams) {
        parameters = parseParametersType(token);
        if (parameters == null) {
          return null;
        }
      }
    }

    if (parameters != null) {
      functionType.addChildToBack(parameters);
    }

    skipEOLs();
    if (!match(JsDocToken.RP)) {
      return reportTypeSyntaxWarning("msg.jsdoc.missing.rp");
    }

    skipEOLs();
    Node resultType = parseResultType(next());
    if (resultType == null) {
      return null;
    } else {
      functionType.addChildToBack(resultType);
    }
    return functionType;
  }

// relevant test
// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateRequire
  public void testLateRequire() {
    additionalEndCode = "goog.require('a.A');";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides
  public void testReorderedProvides() {
    additionalCode = "a.B = {};";  
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides2
  public void testReorderedProvides2() {
    additionalEndCode = "a.B = {};";
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder1
  public void testProvideOrder1() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b.c;" +
         "a.b = function(x,y) {};",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b.c;" +
         "a.b = function(x,y) {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder2
  public void testProvideOrder2() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b = function(x,y) {};" +
         "a.b.c;",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3a
  public void testProvideOrder3a() {
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3b
  public void testProvideOrder3b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4a
  public void testProvideOrder4a() {
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4b
  public void testProvideOrder4b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidProvide
  public void testInvalidProvide() {
    test("goog.provide('a.class');", null, INVALID_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase1
  public void testInvalidBase1() {
    test("goog.base(this, 'method');", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase2
  public void testInvalidBase2() {
    test("function Foo() {}" +
         "Foo.method = function() {" +
         "  goog.base(this, 'method');" +
         "};", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase3
  public void testInvalidBase3() {
    test(String.format(METHOD_FORMAT, "goog.base();"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase4
  public void testInvalidBase4() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'bar');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase5
  public void testInvalidBase5() {
    test(String.format(METHOD_FORMAT, "goog.base('foo', 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase6
  public void testInvalidBase6() {
    test(String.format(METHOD_FORMAT, "goog.base.call(null, this, 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase7
  public void testInvalidBase7() {
    test("function Foo() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase8
  public void testInvalidBase8() {
    test("var Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase9
  public void testInvalidBase9() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase1
  public void testValidBase1() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method');"),
         String.format(METHOD_FORMAT, "Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase2
  public void testValidBase2() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method', 1, 2);"),
         String.format(METHOD_FORMAT,
             "Foo.superClass_.method.call(this, 1, 2)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase3
  public void testValidBase3() {
    test(String.format(METHOD_FORMAT, "return goog.base(this, 'method');"),
         String.format(METHOD_FORMAT,
             "return Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase4
  public void testValidBase4() {
    test("function Foo() { goog.base(this, 1, 2); }" + FOO_INHERITS,
         "function Foo() { BaseFoo.call(this, 1, 2); } " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase5
  public void testValidBase5() {
    test("var Foo = function() { goog.base(this, 1); };" + FOO_INHERITS,
         "var Foo = function() { BaseFoo.call(this, 1); }; " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase6
  public void testValidBase6() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);",
         "var goog = {}; goog.Foo = function() { goog.BaseFoo.call(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitAndExplicitProvide
  public void testImplicitAndExplicitProvide() {
    test("var goog = {}; " +
         "goog.provide('goog.foo.bar'); goog.provide('goog.foo');",
         "var goog = {}; goog.foo = {}; goog.foo.bar = {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules
  public void testImplicitProvideInIndependentModules() {
    test(
        createModuleStar(
            "",
            "goog.provide('apps.A');",
            "goog.provide('apps.B');"),
        new String[] {
            "var apps = {};",
            "apps.A = {};",
            "apps.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules2
  public void testImplicitProvideInIndependentModules2() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.A');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.A = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules3
  public void testImplicitProvideInIndependentModules3() {
    test(
        createModuleStar(
            "var goog = {};",
            "goog.provide('goog.foo.A');",
            "goog.provide('goog.foo.B');"),
        new String[] {
            "var goog = {}; goog.foo = {};",
            "goog.foo.A = {};",
            "goog.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules1
  public void testProvideInIndependentModules1() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2
  public void testProvideInIndependentModules2() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2b
  public void testProvideInIndependentModules2b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = function() {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = function() {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3
  public void testProvideInIndependentModules3() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); goog.require('apps.foo');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.B = {};",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3b
  public void testProvideInIndependentModules3b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); apps.foo = function() {}; " +
            "goog.require('apps.foo');"),
        new String[] {
            "var apps = {};",
            "apps.foo.B = {};",
            "apps.foo = function() {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules4
  public void testProvideInIndependentModules4() {
    
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.bar.B');",
            "goog.provide('apps.foo.bar.C');"),
        new String[] {
            "var apps = {};apps.foo = {};apps.foo.bar = {}",
            "apps.foo.bar.B = {};",
            "apps.foo.bar.C = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfBaseGoog
  public void testRequireOfBaseGoog() {
    test("goog.require('goog');",
         "", MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSourcePositionPreservation
  public void testSourcePositionPreservation() {
    test("goog.provide('foo.bar.baz');",
         "var foo = {};" +
         "foo.bar = {};" +
         "foo.bar.baz = {};");

    Node root = getLastCompiler().getRoot();

    Node fooDecl = findQualifiedNameNode("foo", root);
    Node fooBarDecl = findQualifiedNameNode("foo.bar", root);
    Node fooBarBazDecl = findQualifiedNameNode("foo.bar.baz", root);

    assertEquals(1, fooDecl.getLineno());
    assertEquals(14, fooDecl.getCharno());

    assertEquals(1, fooBarDecl.getLineno());
    assertEquals(18, fooBarDecl.getCharno());

    assertEquals(1, fooBarBazDecl.getLineno());
    assertEquals(22, fooBarBazDecl.getCharno());
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef
  public void testNoStubForProvidedTypedef() {
    test("goog.provide('x');  var x;", "var x;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef2
  public void testNoStubForProvidedTypedef2() {
    test("goog.provide('x.y');  x.y;",
         "var x = {}; x.y;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef4
  public void testNoStubForProvidedTypedef4() {
    test("goog.provide('x.y.z');  x.y.z;",
         "var x = {}; x.y = {}; x.y.z;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine1
  public void testBasicDefine1() {
    test(" var DEF = true", "var DEF=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine2
  public void testBasicDefine2() {
    test(" var DEF = 'a'", "var DEF=\"a\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine3
  public void testBasicDefine3() {
    test(" var DEF = 0", "var DEF=0");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineBadType
  public void testDefineBadType() {
    test(" var DEF = {}",
        null, ProcessDefines.INVALID_DEFINE_TYPE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue1
  public void testDefineWithBadValue1() {
    test(" var DEF = new Boolean(true);", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue2
  public void testDefineWithBadValue2() {
    test(" var DEF = 'x' + y;", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithDependentValue
  public void testDefineWithDependentValue() {
    test(" var BASE = false;\n" +
         " var DEF = !BASE;",
         "var BASE=false;var DEF=!BASE");
    test("var a = {};\n" +
         " a.BASE = false;\n" +
         " a.DEF = !a.BASE;",
         "var a={};a.BASE=false;a.DEF=!a.BASE");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithInvalidDependentValue
  public void testDefineWithInvalidDependentValue() {
    test("var BASE = false;\n" +
         " var DEF = !BASE;",
         null,
          ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding1
  public void testOverriding1() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    overrides.put("DEF_OVERRIDE_TO_FALSE", new Node(Token.FALSE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = false;" +
        " var DEF_OVERRIDE_TO_FALSE = true",
        "var DEF_OVERRIDE_TO_TRUE=true;var DEF_OVERRIDE_TO_FALSE=false");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding2
  public void testOverriding2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    String normalConst = "var DEF_OVERRIDE_TO_FALSE=true;";
    testWithPrefix(
        normalConst,
        " var DEF_OVERRIDE_TO_TRUE = false",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding3
  public void testOverriding3() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = true;",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString0
  public void testOverridingString0() {
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"x\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString1
  public void testOverridingString1() {
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"x\" + \"y\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString2
  public void testOverridingString2() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString3
  public void testOverridingString3() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testMisspelledOverride
  public void testMisspelledOverride() {
    overrides.put("DEF_BAD_OVERIDE", new Node(Token.TRUE));
    test(" var DEF_BAD_OVERRIDE = true",
        "var DEF_BAD_OVERRIDE=true", null,
        ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testCompiledIsKnownDefine
  public void testCompiledIsKnownDefine() {
    overrides.put("COMPILED", new Node(Token.TRUE));
    testSame("");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign1
  public void testSimpleReassign1() {
    test(" var DEF = false; DEF = true;",
        "var DEF=true;true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign2
  public void testSimpleReassign2() {
    test(" var DEF=false;DEF=true;DEF=3",
        "var DEF=3;true;3");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(1, def.getRefs().size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign3
  public void testSimpleReassign3() {
    test(" var DEF = false;var x;x = DEF = true;",
        "var DEF=true;var x;x=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDuplicateVar
  public void testDuplicateVar() {
    test(" var DEF = false; var DEF = true;",
         null, VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration1
  public void testAssignBeforeDeclaration1() {
    test("DEF=false;var b=false,DEF=true,c=false",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration2
  public void testAssignBeforeDeclaration2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        "DEF_OVERRIDE_TO_TRUE = 3;" +
        " var DEF_OVERRIDE_TO_TRUE = false;",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testEmptyDeclaration
  public void testEmptyDeclaration() {
    test(" var DEF;",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterCall
  public void testReassignAfterCall() {
    test("var DEF=true;externMethod();DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRef
  public void testReassignAfterRef() {
    test("var DEF=true;var x = DEF;DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignWithExpr
  public void testReassignWithExpr() {
    test("var DEF=true;var x;DEF=x=false",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterNonGlobalRef
  public void testReassignAfterNonGlobalRef() {
    test(
        "var DEF=true;" +
        "var x=function(){var y=DEF}; DEF=false",
        "var DEF=false;var x=function(){var y=DEF};false");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(2, def.getRefs().size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRefInConditional
  public void testReassignAfterRefInConditional() {
    test(
        "var DEF=true;" +
        "if (false) {var x=DEF} DEF=false;",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignInNonGlobalScope
  public void testAssignInNonGlobalScope() {
    test("var DEF=true;function foo() {DEF=false};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDeclareInNonGlobalScope
  public void testDeclareInNonGlobalScope() {
    test("function foo() {var DEF=true;};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineAssignmentInLoop
  public void testDefineAssignmentInLoop() {
    test("var DEF=true;var x=0;while (x) {DEF=false;}",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testWithNoDefines
  public void testWithNoDefines() {
    testSame("var DEF=true;var x={};x.foo={}");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine1
  public void testNamespacedDefine1() {
    test("var a = {};  a.B = false; a.B = true;",
         "var a = {}; a.B = true; true;");

    Name aDotB = namespace.getNameIndex().get("a.B");
    assertEquals(1, aDotB.getRefs().size());
    assertEquals(1, aDotB.globalSets);
    assertNotNull(aDotB.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2a
  public void testNamespacedDefine2a() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2b
  public void testNamespacedDefine2b() {
    
    
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {  B : false };",
         "var a = {B : false};",
         null, ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2c
  public void testNamespacedDefine2c() {
    
    
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {  get B() { return false } };",
      "var a = {get B() { return false } };",
      null, ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine3
  public void testNamespacedDefine3() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};", "var a = {};", null,
         ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine4
  public void testNamespacedDefine4() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverrideAfterAlias
  public void testOverrideAfterAlias() {
    test("var x; var DEF=true; x=DEF; DEF=false;",
         null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303
  public void testIssue303() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "(new F()).setLocation('http://www.google.com/');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303b
  public void testIssue303b() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "function x() {" +
        "  (new F()).setLocation('http://www.google.com/');" +
        "} window['x'] = x;",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new1
  public void testAnnotationInExterns_new1() throws Exception {
    checkMarkedCalls("externSENone()",
        ImmutableList.<String>of("externSENone"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new2
  public void testAnnotationInExterns_new2() throws Exception {
    checkMarkedCalls("externSEThis()",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new3
  public void testAnnotationInExterns_new3() throws Exception {
    checkMarkedCalls("new externObjSEThis()",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new4
  public void testAnnotationInExterns_new4() throws Exception {
    
    

    checkMarkedCalls("new externObjSEThis().externObjSEThisMethod('')",
        ImmutableList.<String>of(
           "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new5
  public void testAnnotationInExterns_new5() throws Exception {
    checkMarkedCalls(
        "function f() { new externObjSEThis() };" +
        "f();",
        ImmutableList.<String>of("externObjSEThis", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new6
  public void testAnnotationInExterns_new6() throws Exception {
    
    
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod('') " +
        "};" +
        "f();",
         ImmutableList.<String>of(
             "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new7
  public void testAnnotationInExterns_new7() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  var x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new8
  public void testAnnotationInExterns_new8() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(new externObjSEThis());",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new9
  public void testAnnotationInExterns_new9() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(g);",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new10
  public void testAnnotationInExterns_new10() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod2('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns1
  public void testAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externSef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns2
  public void testAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externSef2()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns3
  public void testAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externNsef1()", ImmutableList.of("externNsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns4
  public void testAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externNsef2()", ImmutableList.of("externNsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns5
  public void testAnnotationInExterns5() throws Exception {
    checkMarkedCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns1
  public void testNamespaceAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externObj.sef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns2
  public void testNamespaceAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns3
  public void testNamespaceAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns4
  public void testNamespaceAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externObj.partialFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns5
  public void testNamespaceAnnotationInExterns5() throws Exception {
    
    
    
    String templateSrc = "var o = {}; o.<fnName> = function(){}; o.<fnName>()";

    
    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "notPartialFn"),
                     ImmutableList.of("o.notPartialFn"));

    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "partialFn"),
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns6
  public void testNamespaceAnnotationInExterns6() throws Exception {
    checkMarkedCalls("externObj.partialSharedFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns1
  public void testConstructorAnnotationInExterns1() throws Exception {
    checkMarkedCalls("new externSefConstructor()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns2
  public void testConstructorAnnotationInExterns2() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.sefFnOfSefObj()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns3
  public void testConstructorAnnotationInExterns3() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.nsefFnOfSefObj()",
                     ImmutableList.of("a.nsefFnOfSefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns4
  public void testConstructorAnnotationInExterns4() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.externShared()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns5
  public void testConstructorAnnotationInExterns5() throws Exception {
    checkMarkedCalls("new externNsefConstructor()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns6
  public void testConstructorAnnotationInExterns6() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.sefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns7
  public void testConstructorAnnotationInExterns7() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.nsefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor",
                                      "a.nsefFnOfNsefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns8
  public void testConstructorAnnotationInExterns8() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName1
  public void testSharedFunctionName1() throws Exception {
    checkMarkedCalls("var a; " +
                     "if (true) {" +
                     "  a = new externNsefConstructor()" +
                     "} else {" +
                     "  a = new externSefConstructor()" +
                     "}" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName2
  public void testSharedFunctionName2() throws Exception {
    
    
    boolean broken = true;
    if (broken) {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2"));
    } else {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2",
                                        "a.externShared"));
    }
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1
  public void testAnnotationInExternStubs1() throws Exception {
    checkMarkedCalls("o.propWithStubBefore('a');",
        ImmutableList.<String>of("o.propWithStubBefore"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1b
  public void testAnnotationInExternStubs1b() throws Exception {
    checkMarkedCalls("o.propWithStubBeforeWithJSDoc('a');",
        ImmutableList.<String>of("o.propWithStubBeforeWithJSDoc"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2
  public void testAnnotationInExternStubs2() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2b
  public void testAnnotationInExternStubs2b() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs3
  public void testAnnotationInExternStubs3() throws Exception {
    checkMarkedCalls("propWithAnnotatedStubAfter('a');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs4
  public void testAnnotationInExternStubs4() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs5
  public void testAnnotationInExternStubs5() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNoSideEffectsSimple
  public void testNoSideEffectsSimple() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");

    checkMarkedCalls(
        prefix + "" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; return a" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "return externObj" + suffix, expected);
    checkMarkedCalls(
        "function g(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, expected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testResultLocalitySimple
  public void testResultLocalitySimple() throws Exception {
    String prefix = "var g; function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");
    List<String> notExpected = ImmutableList.of();

    
    checkLocalityOfMarkedCalls(
        prefix + "" + suffix, expected);
    
    checkLocalityOfMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return 2" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; return a" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return {foo : 1}.foo" + suffix,
        notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix,
        notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return externObj" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        "function inner(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, notExpected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testExternCalls
  public void testExternCalls() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";

    checkMarkedCalls(prefix + "externNsef1()" + suffix,
                     ImmutableList.of("externNsef1", "f"));
    checkMarkedCalls(prefix + "externObj.nsef1()" + suffix,
                     ImmutableList.of("externObj.nsef1", "f"));

    checkMarkedCalls(prefix + "externSef1()" + suffix,
                     ImmutableList.<String>of());
    checkMarkedCalls(prefix + "externObj.sef1()" + suffix,
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testApply
  public void testApply() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.apply()",
                     ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCall
  public void testCall() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.call()",
                     ImmutableList.<String>of("f.call"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference1
  public void testInference1() throws Exception {
    checkMarkedCalls("function f() {return g()}" +
                     "function g() {return 42}" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference2
  public void testInference2() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "function f() {g()}" +
                     "function g() {a=2}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference3
  public void testInference3() throws Exception {
    checkMarkedCalls("var f = function() {return g()};" +
                     "var g = function() {return 42};" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference4
  public void testInference4() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var f = function() {g()};" +
                     "var g = function() {a=2};" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference5
  public void testInference5() throws Exception {
    checkMarkedCalls("var goog = {};" +
                     "goog.f = function() {return goog.g()};" +
                     "goog.g = function() {return 42};" +
                     "goog.f()",
                     ImmutableList.of("goog.g", "goog.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference6
  public void testInference6() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var goog = {};" +
                     "goog.f = function() {goog.g()};" +
                     "goog.g = function() {a=2};" +
                     "goog.f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects1
  public void testLocalizedSideEffects1() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; return function() {x.foo++};" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects2
  public void testLocalizedSideEffects2() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; (function() {x.foo++})();" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects3
  public void testLocalizedSideEffects3() throws Exception {
    
    
    checkMarkedCalls("var g = {foo:1}; function f() {var x = g; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects4
  public void testLocalizedSideEffects4() throws Exception {
    
    
    checkMarkedCalls("function f() {var x = []; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects5
  public void testLocalizedSideEffects5() throws Exception {
    
    
    checkMarkedCalls("var g = [];function f() {var x = g; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects6
  public void testLocalizedSideEffects6() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {}; x.foo = 1; return x;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects7
  public void testLocalizedSideEffects7() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = []; a[1] = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects8
  public void testLocalizedSideEffects8() throws Exception {
    
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects9
  public void testLocalizedSideEffects9() throws Exception {
    
    
    
    checkMarkedCalls(" function A() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects10
  public void testLocalizedSideEffects10() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "A.prototype.g = function() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.g(); return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects11
  public void testLocalizedSideEffects11() throws Exception {
    
    checkMarkedCalls(
        " function A() {}" +
        "A.prototype.update = function() { this.x = 1; };" +
        " function B() { " +
        "  this.a_ = new A();" +
        "}" +
        "B.prototype.updateA = function() {" +
        "  var b = this.a_;" +
        "  b.update();" +
        "};" +
        "var x = new B();" +
        "x.updateA();",
        ImmutableList.of("A", "B"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators1
  public void testUnaryOperators1() throws Exception {
    checkMarkedCalls("function f() {var x = 1; x++}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators2
  public void testUnaryOperators2() throws Exception {
    checkMarkedCalls("var x = 1;" +
                     "function f() {x++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators3
  public void testUnaryOperators3() throws Exception {
    checkMarkedCalls("function f() {var x = {foo : 0}; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators4
  public void testUnaryOperators4() throws Exception {
    checkMarkedCalls("var x = {foo : 0};" +
                     "function f() {x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators5
  public void testUnaryOperators5() throws Exception {
    checkMarkedCalls("function f(x) {x.foo++}" +
                     "f({foo : 0})",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    checkMarkedCalls("var x = {};" +
                     "function f() {delete x}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    checkMarkedCalls("function f() {var x = {}; delete x}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator1
  public void testOrOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator2
  public void testOrOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator3
  public void testOrOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperators4
  public void testOrOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator1
  public void testAndOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator2
  public void testAndOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator3
  public void testAndOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperators4
  public void testAndOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator1
  public void testHookOperator1() throws Exception {
    checkMarkedCalls("var f = true ? externNsef1 : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator2
  public void testHookOperator2() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator3
  public void testHookOperator3() throws Exception {
    checkMarkedCalls("var f = true ? externNsef2 : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperators4
  public void testHookOperators4() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow1
  public void testThrow1() throws Exception {
    checkMarkedCalls("function f(){throw Error()};\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow2
  public void testThrow2() throws Exception {
    checkMarkedCalls("function A(){throw Error()};\n" +
                     "function f(){return new A()}\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAssignmentOverride
  public void testAssignmentOverride() throws Exception {
    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var a = new A;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A", "a.foo"));

    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var x = 1\n" +
                     "function f(){x = 10}\n" +
                     "var a = new A;\n" +
                     "a.foo = f;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance1
  public void testInheritance1() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){var data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source,
                     ImmutableList.of("this.foo", "goog.inherits",
                                      "I", "i.foo", "i.bar",
                                      "A", "a.foo", "a.bar"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance2
  public void testInheritance2() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source, ImmutableList.of("goog.inherits", "I", "A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallBeforeDefinition
  public void testCallBeforeDefinition() throws Exception {
    checkMarkedCalls("f(); function f(){}",
                     ImmutableList.of("f"));

    checkMarkedCalls("var a = {}; a.f(); a.f = function (){}",
                     ImmutableList.of("a.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis1
  public void testConstructorThatModifiesThis1() throws Exception {
    String source = "function A(){this.foo = 1}\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis2
  public void testConstructorThatModifiesThis2() throws Exception {
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis3
  public void testConstructorThatModifiesThis3() throws Exception {

    
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.bar()};\n" +
        "A.prototype.bar = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis4
  public void testConstructorThatModifiesThis4() throws Exception {

    
    String source = "function A(){foo.call(this)}\n" +
        "function foo(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal1
  public void testConstructorThatModifiesGlobal1() throws Exception {
    String source = "var b = 0;" +
        "function A(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal2
  public void testConstructorThatModifiesGlobal2() throws Exception {
    String source = "var b = 0;" +
        "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionThatModifiesThis
  public void testCallFunctionThatModifiesThis() throws Exception {
    String source = "function A(){}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f(){var a = new A; return a}\n" +
        "function g(){var a = new A; a.foo(); return a}\n" +
        "f(); g()";

    checkMarkedCalls(source, ImmutableList.<String>of("A", "A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrG
  public void testCallFunctionFOrG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f || g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHook
  public void testCallFunctionFOrGViaHook() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionForGorH
  public void testCallFunctionForGorH() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){}\n" +
        "function i(){ (false ? f : (g || h))() }\n" +
        "i()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : (g || h))", "i"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGWithSideEffects
  public void testCallFunctionFOrGWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "function i(){ (g || f)() }\n" +
        "function j(){ (f || f)() }\n" +
        "function k(){ (g || g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g || g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHookWithSideEffects
  public void testCallFunctionFOrGViaHookWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "function i(){ (false ? g : f)() }\n" +
        "function j(){ (false ? f : f)() }\n" +
        "function k(){ (false ? g : g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g : g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallRegExpWithSideEffects
  public void testCallRegExpWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function k(){(/a/).exec('')}\n" +
        "k()";

    regExpHaveSideEffects = true;
    checkMarkedCalls(source, ImmutableList.<String>of());
    regExpHaveSideEffects = false;
    checkMarkedCalls(source, ImmutableList.<String>of(
        "REGEXP STRING exec", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction1
  public void testAnonymousFunction1() throws Exception {
    String source = "(function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "FUNCTION"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction2
  public void testAnonymousFunction2() throws Exception {
    String source = "(Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction3
  public void testAnonymousFunction3() throws Exception {
    String source = "var a = (Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction4
  public void testAnonymousFunction4() throws Exception {
    String source = "var a = (Error || function (){});" +
                    "a();";

    
    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatch
  public void testRemoveTryCatch() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "var b;var a=1");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}",
         "var d;var c;var a=1;var b=2");
    test("try{var a=1;var b=2}catch(ex){}",
         "var a=1;var b=2");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryFinally
  public void testRemoveTryFinally() {
    test("try{var a=1;}finally{var c=3;}",
         "var a=1;var c=3");
    test("try{var a=1;var b=2}finally{var e=5;var f=6;}",
         "var a=1;var b=2;var e=5;var f=6");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatchFinally
  public void testRemoveTryCatchFinally() {
    test("try{var a=1;}catch(ex){var b=2;}finally{var c=3;}",
         "var b;var a=1;var c=3");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}finally{var e=5;" +
         "var f=6;}",
         "var d;var c;var a=1;var b=2;var e=5;var f=6");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveTryBlockContainingReturnStatement
  public void testPreserveTryBlockContainingReturnStatement() {
    testSame("function f(){var a;try{a=1;return}finally{a=2}}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveAnnotatedTryBlock
  public void testPreserveAnnotatedTryBlock() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "try{var a=1}catch(ex){var b=2}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryFinally
  public void testIfTryFinally() {
    test("if(x)try{y}finally{z}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryCatch
  public void testIfTryCatch() {
    test("if(x)try{y;z}catch(e){}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties
  public void testAnalyzeUnusedPrototypeProperties() {
    
    test(" \n" +
        "function e(){} \n" +
        "e.prototype.a = function(){};" +
        "e.prototype.b = function(){};" +
        "var x = new e; x.a()",

        "function e(){}" +
        " e.prototype.a = function(){};" +
        "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties2
  public void testAnalyzeUnusedPrototypeProperties2() {
    
    
    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties3
  public void testAnalyzeUnusedPrototypeProperties3() {
    
    
    test(" \n" +
        "function e(){} \n" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           
           "var x = new e; x.a()");

    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAliasing
  public void testAliasing() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testStatement
  public void testStatement() {
    test(" \n" +
         "" +
        "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveUnusedVars
  public void testRemoveUnusedVars() {
    
    test("var a;var b=3;var c=function(){};var x=A();var y; var z;" +
         "function A(){B()} function B(){C(b)} function C(){} " +
         "function X(){Y()} function Y(z){Z(x)} function Z(){y} " +
         "P=function(){A()}; " +
         "try{0}catch(e){a}",

         "var a;var b=3;A();function A(){B()}" +
         "function B(){C(b)}" +
         "function C(){}" +
         "P=function(){A()}" +
         ";try{0}catch(e){a}");

    
    test("var i=0;var j=0;if(i>0){var k=1;}",
         "var i=0;if(i>0);");

    
    test("for (var i in booyah) {" +
         "  if (i > 0) x += ', ';" +
         "  var arg = 'foo';" +
         "  if (arg.length > 40) {" +
         "    var unused = 'bar';" +   
         "    arg = arg.substr(0, 40) + '...';" +
         "  }" +
         "  x += arg;" +
         "}",

         "for(var i in booyah){if(i>0)x+=\", \";" +
         "var arg=\"foo\";if(arg.length>40)arg=arg.substr(0,40)+\"...\";" +
         "x+=arg}");

    
    test("function A(){}" +
         "if(0){function B(){}}win.setTimeout(function(){A()})",
         "function A(){}" +
         "if(0);win.setTimeout(function(){A()})");

    
    test("function A(){A()}function B(){B()}B()",
         "function B(){B()}B()");

    
    test("var x,y=2,z=3;A(x);B(z);var a,b,c=4;C()",
         "var x,z=3;A(x);B(z);C()");

    
    test("for(var i=0,j=0;i<10;){}" +
         "for(var x=0,y=0;;y++){}" +
         "for(var a,b;;){a}" +
         "for(var c,d;;);" +
         "for(var item in items){}",

         "for(var i=0;i<10;);" +
         "for(var y=0;;y++);" +
         "for(var a;;)a;" +
         "for(;;);" +
         "for(var item in items);");

    
    test("var a,b,c,d;var e=[b,c];var x=e[3];var f=[d];print(f[0])",
         "var d;var f=[d];print(f[0])");

    
    test("var x;function A(){var x;B()}function B(){print(x)}A()",
         "var x;function A(){B()}function B(){print(x)}A()");

    
    test("function A(){var x;return function(){print(x)}}A()",
         "function A(){var x;return function(){print(x)}}A()");

    
    test("function A(){}function B(){" +
         "var c,d,e,f,g,h;" +
         "function C(){print(c)}" +
         "var handler=function(){print(d)};" +
         "var handler2=function(){handler()};" +
         "e=function(){print(e)};" +
         "if(1){function G(){print(g)}}" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()",

         "function B(){" +
         "var f,h;" +
         "if(1);" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()");

    
    test("var a,b=1; function _A1() {this.foo(a)}",
         "var a;function _A1(){this.foo(a)}");

    
    test("undefinedVar = 1", "undefinedVar=1");

    
    test("var a,b=foo(),c=i++,d;var e=boo();var f;print(d);",
         "foo(); i++; var d; boo(); print(d)");

    test("var a,b=foo()", "foo()");
    test("var b=foo(),a", "foo()");
    test("var a,b=foo(a)", "var a; foo(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemoval
  public void testFunctionArgRemoval() {
    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b(1,2)");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");
    testSame("var b=function(e,f,c,d){return c+d};b(1,2)");

    
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(e,c,f,d){return c+d};b(1,2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemovalFromCallSites
  public void testFunctionArgRemovalFromCallSites() {
    this.modifyCallSites = true;

    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b()");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,f,c,d){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b()");

    
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionsDeadButEscaped
  public void testFunctionsDeadButEscaped() {
    testSame("function b(a) { a = 1; print(arguments[0]) }; b(6)");
    testSame("function b(a) { a = 1; arguments=1; }; b(6)");
    testSame("function b(a) { var c = 2; a = c; print(arguments[0]) }; b(6)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testVarInControlStructure
  public void testVarInControlStructure() {
    test("if (true) var b = 3;", "if(true);");
    test("if (true) var b = 3; else var c = 5;", "if(true);else;");
    test("while (true) var b = 3;", "while(true);");
    test("for (;;) var b = 3;", "for(;;);");
    test("do var b = 3; while(true)", "do;while(true)");
    test("with (true) var b = 3;", "with(true);");
    test("f: var b = 3;","f:{}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRValueHoisting
  public void testRValueHoisting() {
    test("var x = foo();", "foo()");
    test("var x = {a: foo()};", "({a:foo()})");

    test("var x=function y(){}", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testModule
  public void testModule() {
    test(createModules(
             "var unreferenced=1; function x() { foo(); }" +
             "function uncalled() { var x; return 2; }",
             "var a,b; function foo() { this.foo(a); } x()"),
         new String[] {
           "function x(){foo()}",
           "var a;function foo(){this.foo(a)}x()"
         });
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRecursiveFunction1
  public void testRecursiveFunction1() {
    testSame("(function x(){return x()})()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    test("var x = 3; (function x() { return x(); })();",
         "(function x$$1(){return x$$1()})()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionWithName1
  public void testFunctionWithName1() {
    test("var x=function f(){};x()",
         "var x=function(){};x()");

    preserveFunctionExpressionNames = true;
    testSame("var x=function f(){};x()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionWithName2
  public void testFunctionWithName2() {
    test("foo(function bar(){})",
         "foo(function(){})");

    preserveFunctionExpressionNames = true;
    testSame("foo(function bar(){})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal1
  public void testRemoveGlobal1() {
    removeGlobal = false;
    testSame("var x=1");
    test("var y=function(x){var z;}", "var y=function(){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal2
  public void testRemoveGlobal2() {
    removeGlobal = false;
    testSame("var x=1");
    test("function y(x){var z;}", "function y(){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal3
  public void testRemoveGlobal3() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}y()}",
         "function x(){function y(){}y()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal4
  public void testRemoveGlobal4() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}}",
         "function x(){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue168a
  public void testIssue168a() {
    test("function _a(){" +
         "  (function(x){ _b(); })(1);" +
         "}" +
         "function _b(){" +
         "  _a();" +
         "}",
         "function _a(){(function(){_b()})(1)}" +
         "function _b(){_a()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue168b
  public void testIssue168b() {
    removeGlobal = false;
    test("function a(){" +
         "  (function(x){ b(); })(1);" +
         "}" +
         "function b(){" +
         "  a();" +
         "}",
         "function a(){(function(){b()})(1)}" +
         "function b(){a()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign1
  public void testUnusedAssign1() {
    test("var x = 3; x = 5;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign2
  public void testUnusedAssign2() {
    test("function f(a) { a = 3; } this.x = f;",
         "function f(){} this.x=f");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign3
  public void testUnusedAssign3() {
    
    
    test("try { throw ''; } catch (e) { e = 3; }",
        "try{throw\"\";}catch(e){e=3}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign4
  public void testUnusedAssign4() {
    test("function f(a, b) { this.foo(b); a = 3; } this.x = f;",
        "function f(a,b){this.foo(b);}this.x=f");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign5
  public void testUnusedAssign5() {
    test("var z = function f() { f = 3; }; z();",
         "var z=function(){};z()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign5b
  public void testUnusedAssign5b() {
    test("var z = function f() { f = alert(); }; z();",
         "var z=function(){alert()};z()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign6
  public void testUnusedAssign6() {
    test("var z; z = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign6b
  public void testUnusedAssign6b() {
    test("var z; z = alert();", "alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign7
  public void testUnusedAssign7() {
    
    test("var a = 3; for (var i in {}) { i = a; }",
         
         "var a = 3; var i; for (i in {}) {i = a;}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign8
  public void testUnusedAssign8() {
    
    test("var a = 3; for (var i in {}) { i = a; } alert(a);",
         
         "var a = 3; var i; for (i in {}) {i = a} alert(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign1
  public void testUnusedPropAssign1() {
    test("var x = {}; x.foo = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign1b
  public void testUnusedPropAssign1b() {
    test("var x = {}; x.foo = alert();", "alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign2
  public void testUnusedPropAssign2() {
    test("var x = {}; x['foo'] = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign2b
  public void testUnusedPropAssign2b() {
    test("var x = {}; x[alert()] = alert();", "alert(),alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign3
  public void testUnusedPropAssign3() {
    test("var x = {}; x['foo'] = {}; x['bar'] = 3", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign3b
  public void testUnusedPropAssign3b() {
    test("var x = {}; x[alert()] = alert(); x[alert() + alert()] = alert()",
         "alert(),alert();(alert() + alert()),alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign4
  public void testUnusedPropAssign4() {
    test("var x = {foo: 3}; x['foo'] = 5;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign5
  public void testUnusedPropAssign5() {
    test("var x = {foo: bar()}; x['foo'] = 5;",
         "var x={foo:bar()};x[\"foo\"]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign6
  public void testUnusedPropAssign6() {
    test("var x = function() {}; x.prototype.bar = function() {};", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7
  public void testUnusedPropAssign7() {
    test("var x = {}; x[x.foo] = x.bar;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7b
  public void testUnusedPropAssign7b() {
    testSame("var x = {}; x[x.foo] = alert(x.bar);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7c
  public void testUnusedPropAssign7c() {
    test("var x = {}; x[alert(x.foo)] = x.bar;",
         "var x={};x[alert(x.foo)]=x.bar");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign1
  public void testUsedPropAssign1() {
    test("function f(x) { x.bar = 3; } f({});",
         "function f(x){x.bar=3}f({})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign2
  public void testUsedPropAssign2() {
    test("try { throw z; } catch (e) { e.bar = 3; }",
         "try{throw z;}catch(e){e.bar=3}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign3
  public void testUsedPropAssign3() {
    
    test("var x = {}; x.foo = 3; x = bar();",
         "var x={};x.foo=3;x=bar()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign4
  public void testUsedPropAssign4() {
    test("var y = foo(); var x = {}; x.foo = 3; y[x.foo] = 5;",
         "var y=foo();var x={};x.foo=3;y[x.foo]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign5
  public void testUsedPropAssign5() {
    test("var y = foo(); var x = 3; y[x] = 5;",
         "var y=foo();var x=3;y[x]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign6
  public void testUsedPropAssign6() {
    test("var x = newNodeInDom(doc); x.innerHTML = 'new text';",
         "var x=newNodeInDom(doc);x.innerHTML=\"new text\"");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign7
  public void testUsedPropAssign7() {
    testSame("var x = {}; for (x in alert()) { x.foo = 3; }");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign8
  public void testUsedPropAssign8() {
    testSame("for (var x in alert()) { x.foo = 3; }");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign9
  public void testUsedPropAssign9() {
    testSame(
        "var x = {}; x.foo = newNodeInDom(doc); x.foo.innerHTML = 'new test';");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1
  public void testDependencies1() {
    test("var a = 3; var b = function() { alert(a); };", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1b
  public void testDependencies1b() {
    test("var a = 3; var b = alert(function() { alert(a); });",
         "var a=3;alert(function(){alert(a)})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1c
  public void testDependencies1c() {
    test("var a = 3; var _b = function() { alert(a); };",
         "var a=3;var _b=function(){alert(a)}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2
  public void testDependencies2() {
    test("var a = 3; var b = 3; b = function() { alert(a); };", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2b
  public void testDependencies2b() {
    test("var a = 3; var b = 3; b = alert(function() { alert(a); });",
         "var a=3;alert(function(){alert(a)})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2c
  public void testDependencies2c() {
    testSame("var a=3;var _b=3;_b=function(){alert(a)}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testGlobalVarReferencesLocalVar
  public void testGlobalVarReferencesLocalVar() {
    testSame("var a=3;function f(){var b=4;a=b}alert(a + f())");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testLocalVarReferencesGlobalVar1
  public void testLocalVarReferencesGlobalVar1() {
    testSame("var a=3;function f(b, c){b=a; alert(b + c);} f();");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testLocalVarReferencesGlobalVar2
  public void testLocalVarReferencesGlobalVar2() {
    test("var a=3;function f(b, c){b=a; alert(c);} f();",
         "function f(b, c) { alert(c); } f();");
    this.modifyCallSites = true;
    test("var a=3;function f(b, c){b=a; alert(c);} f();",
         "function f(c) { alert(c); } f();");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var b = null; var a = (b = 3); alert(a);",
         "var a = 3; alert(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign2
  public void testNestedAssign2() {
    test("var a = 1; var b = 2; var c = (b = a); alert(c);",
         "var a = 1; var c = a; alert(c);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign3
  public void testNestedAssign3() {
    test("var b = 0; var z; z = z = b = 1; alert(b);",
         "var b = 0; b = 1; alert(b);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testCallSiteInteraction
  public void testCallSiteInteraction() {
    this.modifyCallSites = true;

    testSame("var b=function(){return};b()");
    testSame("var b=function(c){return c};b(1)");
    test("var b=function(c){};b.call(null, x)",
         "var b=function(){};b.call(null)");
    test("var b=function(c){};b.apply(null, x)",
         "var b=function(){};b.apply(null, x)");

    test("var b=function(c){return};b(1)",
         "var b=function(){return};b()");
    test("var b=function(c){return};b(1,2)",
         "var b=function(){return};b()");
    test("var b=function(c){return};b(1,2);b(3,4)",
         "var b=function(){return};b();b()");

    
    
    test("var b=function(c,d){return d};b(1,2);b(3,4);b.length",
         "var b=function(c,d){return d};b(0,2);b(0,4);b.length");

    test("var b=function(c){return};b(1,2);b(3,new x())",
         "var b=function(){return};b();b(new x())");

    test("var b=function(c){return};b(1,2);b(new x(),4)",
         "var b=function(){return};b();b(new x())");

    test("var b=function(c,d){return d};b(1,2);b(new x(),4)",
         "var b=function(c,d){return d};b(0,2);b(new x(),4)");
    test("var b=function(c,d,e){return d};b(1,2,3);b(new x(),4,new x())",
         "var b=function(c,d){return d};b(0,2);b(new x(),4,new x())");

    
    test("var b=function(c,d){b(1,2);return d};b(3,4);b(5,6)",
         "var b=function(d){b(2);return d};b(4);b(6)");

    testSame("var b=function(c){return arguments};b(1,2);b(3,4)");

    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b()");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");

    
    test("var b=function(e,f,c,d){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b()");
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(2)");

    
    
    test("var b=function(c,d){};var b=function(e,f){};b(1,2)",
         "var b=function(){};var b=function(){};b(1,2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testCallSiteInteraction_contructors
  public void testCallSiteInteraction_contructors() {
    this.modifyCallSites = true;
    
    
    test("var Ctor1=function(a,b){return a};" +
        "var Ctor2=function(a,b){Ctor1.call(this,a,b)};" +
        "goog$inherits(Ctor2, Ctor1);" +
        "new Ctor2(1,2)",
        "var Ctor1=function(a){return a};" +
        "var Ctor2=function(a){Ctor1.call(this,a)};" +
        "goog$inherits(Ctor2, Ctor1);" +
        "new Ctor2(1)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemovalCausingInconsistency
  public void testFunctionArgRemovalCausingInconsistency() {
    this.modifyCallSites = true;
    
    
    
    test("var a=function(x,y){};" +
        "var b=function(z){};" +
        "a(new b, b)",
        "var a=function(){};" +
        "var b=function(){};" +
        "a(new b)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveUnusedVarsPossibleNpeCase
  public void testRemoveUnusedVarsPossibleNpeCase() {
    this.modifyCallSites = true;
    test("var a = [];" +
        "var register = function(callback) {a[0] = callback};" +
        "register(function(transformer) {});" +
        "register(function(transformer) {});",
        "var register=function(){};register();register()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeJSCompiler_renameProperty
  public void testDoNotOptimizeJSCompiler_renameProperty() {
    this.modifyCallSites = true;

    
    test("function JSCompiler_renameProperty(a) {};" +
         "JSCompiler_renameProperty('a');",
         "function JSCompiler_renameProperty() {};" +
         "JSCompiler_renameProperty('a');");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeJSCompiler_ObjectPropertyString
  public void testDoNotOptimizeJSCompiler_ObjectPropertyString() {
    this.modifyCallSites = true;
    test("function JSCompiler_ObjectPropertyString(a, b) {};" +
         "JSCompiler_ObjectPropertyString(window,'b');",
         "function JSCompiler_ObjectPropertyString() {};" +
         "JSCompiler_ObjectPropertyString(window,'b');");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeSetters
  public void testDoNotOptimizeSetters() {
    testSame("({set s(a) {}})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass1
  public void testRemoveInheritedClass1() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new a",
        "function a(){} new a");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass2
  public void testRemoveInheritedClass2() {
    test("function goog$inherits(){}" +
        "function goog$mixin(){}" +
        "function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "goog$inherits(b,a);" +
        "goog$mixin(c.prototype,b.prototype);",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass3
  public void testRemoveInheritedClass3() {
    testSame("function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass4
  public void testRemoveInheritedClass4() {
    testSame("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a);" +
        "function c(){}" +
        "goog$inherits(c,b); new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass5
  public void testRemoveInheritedClass5() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a);" +
        "function c(){}" +
        "goog$inherits(c,b); new b",
        "function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass6
  public void testRemoveInheritedClass6() {
    test("function goog$mixin(){}" +
        "function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "function d(){}" +
        "goog$mixin(b.prototype,a.prototype);" +
        "goog$mixin(c.prototype,a.prototype); new c;" +
        "goog$mixin(d.prototype,a.prototype)",
        "function goog$mixin(){}" +
        "function a(){}" +
        "function c(){}" +
        "goog$mixin(c.prototype,a.prototype); new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass7
  public void testRemoveInheritedClass7() {
    test("function goog$mixin(){}" +
        "function a(){alert(goog$mixin(a, a))}" +
        "function b(){}" +
        "goog$mixin(b.prototype,a.prototype); new a",
        "function goog$mixin(){}" +
        "function a(){alert(goog$mixin(a, a))} new a");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass8
  public void testRemoveInheritedClass8() {
    test("function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "b.inherits(a);c.mixin(b.prototype)",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass9
  public void testRemoveInheritedClass9() {
    testSame("function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "b.inherits(a);c.mixin(b.prototype);new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass10
  public void testRemoveInheritedClass10() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new a;" +
        "var c = a; var d = a.g; new b",
        "function goog$inherits(){}" +
        "function a(){} function b(){} goog$inherits(b,a); new a; new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass11
  public void testRemoveInheritedClass11() {
    testSame("function goog$inherits(){}" +
        "function goog$mixin(a,b){goog$inherits(a,b)}" +
        "function a(){}" +
        "function b(){}" +
        "goog$mixin(b.prototype,a.prototype);new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass12
  public void testRemoveInheritedClass12() {
    testSame("function goog$inherits(){}" +
        "function a(){}" +
        "var b = {};" +
        "goog$inherits(b.foo, a)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testReflectedMethods
  public void testReflectedMethods() {
    this.modifyCallSites = true;
    testSame(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;");
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
         "var a = function a(b, c) {" +
         "  a(b, c);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var walk = function walk(a, b) {" +
         "  walk(a, b);" +
         "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions2
  public void testRecursiveFunctions2() {
    preserveFunctionExpressionNames = true;

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

// com.google.javascript.jscomp.RenameVarsTest::testBleedingRecursiveFunctions1
  public void testBleedingRecursiveFunctions1() {
    
    
    
    test("var x = function a(x) { return x ? 1 : a(1); };" +
         "var y = function b(x) { return x ? 2 : b(2); };",
         "var c = function b(a) { return a ? 1 : b(1); };" +
         "var e = function d(a) { return a ? 2 : d(2); };");
  }

// com.google.javascript.jscomp.RenameVarsTest::testBleedingRecursiveFunctions2
  public void testBleedingRecursiveFunctions2() {
    test("function f() {" +
         "  var x = function a(x) { return x ? 1 : a(1); };" +
         "  var y = function b(x) { return x ? 2 : b(2); };" +
         "}",
         "function d() {" +
         "  var e = function b(a) { return a ? 1 : b(1); };" +
         "  var f = function a(c) { return c ? 2 : a(2); };" +
         "}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testBleedingRecursiveFunctions3
  public void testBleedingRecursiveFunctions3() {
    test("function f() {" +
         "  var x = function a(x) { return x ? 1 : a(1); };" +
         "  var y = function b(x) { return x ? 2 : b(2); };" +
         "  var z = function c(x) { return x ? y : c(2); };" +
         "}",
         "function f() {" +
         "  var g = function c(a) { return a ? 1 : c(1); };" +
         "  var d = function a(b) { return b ? 2 : a(2); };" +
         "  var h = function b(e) { return e ? d : b(2); };" +
         "}");
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

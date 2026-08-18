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
// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef1a
  public void testObjLitDef1a() throws Exception {
    testTypes(
        "var x = { a:12 };\n" +
        "x.a = 'a';",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef1b
  public void testObjLitDef1b() throws Exception {
    testTypes(
        "function f(){" +
          "var x = { a:12 };\n" +
          "x.a = 'a';" +
        "};\n" +
        "f();",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef2a
  public void testObjLitDef2a() throws Exception {
    testTypes(
        "var x = { set a(b){} };\n" +
        "x.a = 'a';",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef2b
  public void testObjLitDef2b() throws Exception {
    testTypes(
        "function f(){" +
          "var x = { set a(b){} };\n" +
          "x.a = 'a';" +
        "};\n" +
        "f();",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef3a
  public void testObjLitDef3a() throws Exception {
    testTypes(
        " var y;\n" +
        "var x = { get a(){} };\n" +
        "y = x.a;",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef3b
  public void testObjLitDef3b() throws Exception {
    testTypes(
      " var y;\n" +
        "function f(){" +
          "var x = { get a(){} };\n" +
          "y = x.a;" +
        "};\n" +
        "f();",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef4
  public void testObjLitDef4() throws Exception {
    testTypes(
        "var x = {" +
          " a:12 };\n",
          "assignment to property a of {a: function (): number}\n" +
          "found   : number\n" +
          "required: function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef5
  public void testObjLitDef5() throws Exception {
    testTypes(
        "var x = {};\n" +
        " x.a = 12;\n",
        "assignment to property a of x\n" +
        "found   : number\n" +
        "required: function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction1
  public void testInstanceOfReduction1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return x; } else { return new T(); }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction2
  public void testInstanceOfReduction2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return ''; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInferredPropagation
  public void testPropertyInferredPropagation() throws Exception {
    testTypes("function f() { return {}; }\n" +
         "function g() { var x = f(); if (x.p) x.a = 'a'; else x.a = 'b'; }\n" +
         "function h() { var x = f(); x.a = false; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference1
  public void testPropertyInference1() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference2
  public void testPropertyInference2() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = null; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference3
  public void testPropertyInference3() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : (boolean|number)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference4
  public void testPropertyInference4() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.x_ = 3;" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference5
  public void testPropertyInference5() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference6
  public void testPropertyInference6() throws Exception {
    testTypes(
        " function F() { }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference7
  public void testPropertyInference7() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference8
  public void testPropertyInference8() throws Exception {
    testTypes(
        " function F() { " +
        "   this.x_ = 'x';" +
        "}" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "assignment to property x_ of F\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoPersistentTypeInferenceForObjectProperties
  public void testNoPersistentTypeInferenceForObjectProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoPersistentTypeInferenceForFunctionProperties
  public void testNoPersistentTypeInferenceForFunctionProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope1
  public void testObjectPropertyTypeInferredInLocalScope1() throws Exception {
    testTypes("\n" +
        "function f(o) { o.x = 1; return o.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope2
  public void testObjectPropertyTypeInferredInLocalScope2() throws Exception {
    testTypes("" +
        "function f(o, x) { o.x = 'a';\nif (x) {o.x = x;}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope3
  public void testObjectPropertyTypeInferredInLocalScope3() throws Exception {
    testTypes("" +
        "function f(o, x) { if (x) {o.x = x;} else {o.x = 'a';}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x = 0;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3()
      throws Exception {
    testTypes(" var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4()
      throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyUsedBeforeDefinition1
  public void testPropertyUsedBeforeDefinition1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "" +
        "T.prototype.f = function() { return this.g(); };\n" +
        " T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyUsedBeforeDefinition2
  public void testPropertyUsedBeforeDefinition2() throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() {};\n" +
        "" +
        "n.T.prototype.f = function() { return this.g(); };\n" +
        " n.T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd1
  public void testAdd1() throws Exception {
    testTypes("function foo(){var a = 'abc'+foo();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd2
  public void testAdd2() throws Exception {
    testTypes("function foo(){var a = foo()+4;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd3
  public void testAdd3() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd4
  public void testAdd4() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd5
  public void testAdd5() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd6
  public void testAdd6() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd7
  public void testAdd7() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd8
  public void testAdd8() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd9
  public void testAdd9() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd10
  public void testAdd10() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd11
  public void testAdd11() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd12
  public void testAdd12() throws Exception {
    testTypes(" function a() { return 5; }" +
        " var b = 5;" +
        " var c = a() + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd13
  public void testAdd13() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd14
  public void testAdd14() throws Exception {
    testTypes(" var a = null;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd15
  public void testAdd15() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd16
  public void testAdd16() throws Exception {
    testTypes(" var a = undefined;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd17
  public void testAdd17() throws Exception {
    testTypes(" var a = 5;" +
        " var b = undefined;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd18
  public void testAdd18() throws Exception {
    testTypes("function f() {};" +
        " var a = 'a';" +
        " var c = a + f();",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd19
  public void testAdd19() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd20
  public void testAdd20() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd21
  public void testAdd21() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison1
  public void testNumericComparison1() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison2
  public void testNumericComparison2() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : Object\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison3
  public void testNumericComparison3() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison4
  public void testNumericComparison4() throws Exception {
    testTypes(" " +
              "function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison5
  public void testNumericComparison5() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : *\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison6
  public void testNumericComparison6() throws Exception {
    testTypes(" function foo() { if (3 >= foo()) return; }",
        "right side of numeric comparison\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison1
  public void testStringComparison1() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison2
  public void testStringComparison2() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison3
  public void testStringComparison3() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison4
  public void testStringComparison4() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison5
  public void testStringComparison5() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison6
  public void testStringComparison6() throws Exception {
    testTypes(" " +
        "function foo() { if ('a' >= foo()) return; }",
        "right side of comparison\n" +
        "found   : undefined\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison1
  public void testValueOfComparison1() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        " function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison2
  public void testValueOfComparison2() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison3
  public void testValueOfComparison3() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.toString = function() { return 'o'; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGenericRelationalExpression
  public void testGenericRelationalExpression() throws Exception {
    testTypes(" " +
                  "function f(a,b) {return a < b;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof1
  public void testInstanceof1() throws Exception {
    testTypes("function foo(){" +
        "if (bar instanceof 3)return;}",
        "instanceof requires an object\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof2
  public void testInstanceof2() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}",
        "deterministic instanceof yields false\n" +
        "found   : undefined\n" +
        "required: NoObject");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof3
  public void testInstanceof3() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof4
  public void testInstanceof4() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof5
  public void testInstanceof5() throws Exception {
    
    testTypes(" function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof6
  public void testInstanceof6() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction3
  public void testInstanceOfReduction3() throws Exception {
    testTypes(
        "\n" +
        "var f = function(x, y) {\n" +
        "  return x instanceof y;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping1
  public void testScoping1() throws Exception {
    testTypes(
        "function foo(a){" +
        "  function bar(a){" +
        "    if (a instanceof Array)return;" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping2
  public void testScoping2() throws Exception {
    testTypes(
        " var a;" +
        "function Foo() {" +
        "   var a;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping3
  public void testScoping3() throws Exception {
    testTypes("\n\nvar b;\nvar b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:3 with type (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping4
  public void testScoping4() throws Exception {
    testTypes("var b; if (true) var b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:1 with type (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping5
  public void testScoping5() throws Exception {
    
    
    testTypes("if (true) var b; var b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping6
  public void testScoping6() throws Exception {
    
    
    testTypes("if (true) var b; if (true) var b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping7
  public void testScoping7() throws Exception {
    testTypes("function A() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of A\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping8
  public void testScoping8() throws Exception {
    testTypes("function A() {}" +
        "function B() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping9
  public void testScoping9() throws Exception {
    testTypes("function B() {" +
        "  this.a = null;" +
        "}" +
        "function A() {}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping10
  public void testScoping10() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = function b(){};");

    
    assertTrue(p.scope.isDeclared("a", false));
    assertFalse(p.scope.isDeclared("b", false));

    
    assertEquals("function (): undefined",
        p.scope.getVar("a").getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping11
  public void testScoping11() throws Exception {
    
    
    testTypes(
        "var a = function b(){ return b };",
        "inconsistent return type\n" +
        "found   : function (): number\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping12
  public void testScoping12() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.bar = 3;" +
        " function g(f) {" +
        "  " +
        "  function h() {" +
        "    return f.bar;" +
        "  }" +
        "}",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments1
  public void testFunctionArguments1() throws Exception {
    testFunctionType(
        "" +
        "function f(a) {}",
        "function (number): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments2
  public void testFunctionArguments2() throws Exception {
    testFunctionType(
        "" +
        "function f(opt_a) {}",
        "function ((number|undefined)): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments3
  public void testFunctionArguments3() throws Exception {
    testFunctionType(
        "" +
        "function f(a,b) {}",
        "function (?, number): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments4
  public void testFunctionArguments4() throws Exception {
    testFunctionType(
        "" +
        "function f(a,opt_a) {}",
        "function (?, (number|undefined)): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments5
  public void testFunctionArguments5() throws Exception {
    testTypes(
        "function a(opt_a,a) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments6
  public void testFunctionArguments6() throws Exception {
    testTypes(
        "function a(var_args,a) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments7
  public void testFunctionArguments7() throws Exception {
    testTypes(
        "" +
        "function a(a,opt_a,var_args) {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments8
  public void testFunctionArguments8() throws Exception {
    testTypes(
        "function a(a,opt_a,var_args,b) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments9
  public void testFunctionArguments9() throws Exception {
    
    testTypes(
        "function a(a,opt_a,var_args,b,c) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments10
  public void testFunctionArguments10() throws Exception {
    
    testTypes(
        "function a(a,opt_a,b,c) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments11
  public void testFunctionArguments11() throws Exception {
    testTypes(
        "function a(a,opt_a,b,c,var_args,d) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments12
  public void testFunctionArguments12() throws Exception {
    testTypes("function bar(baz){}",
        "parameter foo does not appear in bar's parameter list");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments13
  public void testFunctionArguments13() throws Exception {
    
    testTypes(
        " function u() { return true; }" +
        "" +
        "function f(b) { if (u()) { b = null; } return b; }",
        "assignment\n" +
        "found   : null\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments14
  public void testFunctionArguments14() throws Exception {
    testTypes(
        " function f(x, opt_y, var_args) {}" +
        "f('3'); f('3', 2); f('3', 2, true); f('3', 2, true, false);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments15
  public void testFunctionArguments15() throws Exception {
    testTypes(
        "" +
        "function g(f) { f(1, 2); }",
        "Function f: called with 2 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments16
  public void testFunctionArguments16() throws Exception {
    testTypes(
        "" +
        "function g(var_args) {} g(1, true);",
        "actual parameter 2 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: (number|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrintFunctionName1
  public void testPrintFunctionName1() throws Exception {
    
    testTypes(
        "var goog = {}; goog.run = function(f) {};" +
        "goog.run();",
        "Function goog.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrintFunctionName2
  public void testPrintFunctionName2() throws Exception {
    testTypes(
        " var Foo = function() {}; " +
        "Foo.prototype.run = function(f) {};" +
        "(new Foo).run();",
        "Function Foo.prototype.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference1
  public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference2
  public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference3
  public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference4
  public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference5
  public void testFunctionInference5() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference6
  public void testFunctionInference6() throws Exception {
    testFunctionType(
        "function f(opt_a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference7
  public void testFunctionInference7() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (this:Date, ?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference8
  public void testFunctionInference8() throws Exception {
    testFunctionType(
        "function f() {}",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference9
  public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference10
  public void testFunctionInference10() throws Exception {
    testFunctionType(
        "" +
        "var f = function(a,b) {};",
        "function (this:Date, ?, boolean): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference11
  public void testFunctionInference11() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference12
  public void testFunctionInference12() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference13
  public void testFunctionInference13() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(f){};",
        "eatFoo",
        "function (goog.Foo): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference14
  public void testFunctionInference14() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(){ return new goog.Foo; };",
        "eatFoo",
        "function (): goog.Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference15
  public void testFunctionInference15() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "f.prototype.foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference16
  public void testFunctionInference16() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "(new f).foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference17
  public void testFunctionInference17() throws Exception {
    testFunctionType(
        " function f() {}" +
        "function abstractMethod() {}" +
        " f.prototype.foo = abstractMethod;",
        "(new f).foo",
        "function (this:f, number): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference18
  public void testFunctionInference18() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.eatWithDate;",
        "goog.eatWithDate",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference19
  public void testFunctionInference19() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (string): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference20
  public void testFunctionInference20() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction1
  public void testInnerFunction1() throws Exception {
    testTypes(
        "function f() {" +
        "  var x = 3;\n" +
        " function g() { x = null; }" +
        " return x;" +
        "}",
        "assignment\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction2
  public void testInnerFunction2() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = null;\n" +
        " function g() { x = 3; }" +
        " g();" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : (null|number)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction3
  public void testInnerFunction3() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = 3;\n" +
        " \n" +
        " function g() { x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction4
  public void testInnerFunction4() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = '3';\n" +
        " \n" +
        " function g() { x = 3; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction5
  public void testInnerFunction5() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = 3;\n" +
        " " +
        " function g() { var x = 3;x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction6
  public void testInnerFunction6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction7
  public void testInnerFunction7() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " " +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction8
  public void testInnerFunction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " function x() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction9
  public void testInnerFunction9() throws Exception {
    testTypes(
        "function f() {" +
        " var x = 3;\n" +
        " function g() { x = null; };\n" +
        " function h() { return x == null; }" +
        " return h();" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling1
  public void testAbstractMethodHandling1() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        "abstractFn(1);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling2
  public void testAbstractMethodHandling2() throws Exception {
    testTypes(
        "var abstractFn = function() {};" +
        "abstractFn(1);",
        "Function abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling3
  public void testAbstractMethodHandling3() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        "goog.abstractFn(1);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling4
  public void testAbstractMethodHandling4() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.abstractFn = function() {};" +
        "goog.abstractFn(1);",
        "Function goog.abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling5
  public void testAbstractMethodHandling5() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        " var f = abstractFn;" +
        "f('x');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling6
  public void testAbstractMethodHandling6() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        " goog.f = abstractFn;" +
        "goog.f('x');",
        "actual parameter 1 of goog.f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference1
  public void testMethodInference1() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function() { return 3; };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference2
  public void testMethodInference2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.F = function() {};" +
        " goog.F.prototype.foo = " +
        "    function() { return 3; };" +
        " " +
        "goog.G = function() {};" +
        " goog.G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference3
  public void testMethodInference3() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference4
  public void testMethodInference4() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(y) { return y; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference5
  public void testMethodInference5() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 'x'; };" +
        " " +
        "function G() {}" +
        " G.prototype.num = 3;" +
        " " +
        "G.prototype.foo = function(y) { return this.num + y; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference6
  public void testMethodInference6() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function(x) { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { };" +
        "(new G()).foo(1);",
        "Function G.prototype.foo: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference7
  public void testMethodInference7() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function(x, y) { };" +
        "(new G()).foo();",
        "Function G.prototype.foo: called with 0 argument(s). " +
        "Function requires at least 2 argument(s) " +
        "and no more than 2 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference8
  public void testMethodInference8() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(a, opt_b, var_args) { };" +
        "(new G()).foo();",
        "Function G.prototype.foo: called with 0 argument(s). " +
        "Function requires at least 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference9
  public void testMethodInference9() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(a, var_args, opt_b) { };",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticMethodDeclaration1
  public void testStaticMethodDeclaration1() throws Exception {
    testTypes(
        " function F() { F.foo(true); }" +
        " F.foo = function(x) {};",
        "actual parameter 1 of F.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticMethodDeclaration2
  public void testStaticMethodDeclaration2() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        " goog.foo = function(x) {};",
        "actual parameter 1 of goog.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticMethodDeclaration3
  public void testStaticMethodDeclaration3() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        "goog.foo = function() {};",
        "Function goog.foo: called with 1 argument(s). Function requires " +
        "at least 0 argument(s) and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl1
  public void testDuplicateStaticMethodDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (number): undefined, " +
        "original definition at [testcode]:1 " +
        "with type function (number): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl2
  public void testDuplicateStaticMethodDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " " +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl3
  public void testDuplicateStaticMethodDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl4
  public void testDuplicateStaticMethodDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl5
  public void testDuplicateStaticMethodDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (?): undefined, " +
        "original definition at [testcode]:1 with type " +
        "function (?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl1
  public void testDuplicateStaticPropertyDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl2
  public void testDuplicateStaticPropertyDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl3
  public void testDuplicateStaticPropertyDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl4
  public void testDuplicateStaticPropertyDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl5
  public void testDuplicateStaticPropertyDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl6
  public void testDuplicateStaticPropertyDecl6() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = 'y';" +
        " goog.foo = 'x';");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl7
  public void testDuplicateStaticPropertyDecl7() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl8
  public void testDuplicateStaticPropertyDecl8() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " function EventCopy() {}" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl9
  public void testDuplicateStaticPropertyDecl9() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function EventCopy() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateLocalVarDecl
  public void testDuplicateLocalVarDecl() throws Exception {
    testClosureTypesMultipleWarnings(
        "\n" +
        "function f(x) {  var x = ''; }",
        Lists.newArrayList(
            "variable x redefined with type string, original definition" +
            " at  [testcode] :2 with type number",
            "initializing variable\n" +
            "found   : string\n" +
            "required: number"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration1
  public void testStubFunctionDeclaration1() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.prototype.foo;",
        "(new f).foo",
        "function (this:f, number, string): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration2
  public void testStubFunctionDeclaration2() throws Exception {
    testExternFunctionType(
        
        " function f() {};" +
        " f.subclass;",
        "f.subclass",
        "function (new:f.subclass): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration3
  public void testStubFunctionDeclaration3() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.foo;",
        "f.foo",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration4
  public void testStubFunctionDeclaration4() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        "function (this:f): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration5
  public void testStubFunctionDeclaration5() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration6
  public void testStubFunctionDeclaration6() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo;",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration7
  public void testStubFunctionDeclaration7() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo = function() {};",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration8
  public void testStubFunctionDeclaration8() throws Exception {
    testFunctionType(
        " var f = function() {}; ",
        "f",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration9
  public void testStubFunctionDeclaration9() throws Exception {
    testFunctionType(
        " var f; ",
        "f",
        "function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration10
  public void testStubFunctionDeclaration10() throws Exception {
    testFunctionType(
        " var f = function(x) {};",
        "f",
        "function (number): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNestedFunctionInference1
  public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        " function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeRedefinition
  public void testTypeRedefinition() throws Exception {
    testTypes("a={}; a.A = {ZOR:'b'};"
        + " a.A = function() {}",
        "variable a.A redefined with type function (new:a.A): undefined, " +
        "original definition at [testcode]:1 with type enum{a.A}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn1
  public void testIn1() throws Exception {
    testTypes("'foo' in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn2
  public void testIn2() throws Exception {
    testTypes("3 in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn3
  public void testIn3() throws Exception {
    testTypes("undefined in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn4
  public void testIn4() throws Exception {
    testTypes("Date in Object",
        "left side of 'in'\n" +
        "found   : function (new:Date, ?, ?, ?, ?, ?, ?, ?): string\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn5
  public void testIn5() throws Exception {
    testTypes("'x' in null",
        "'in' requires an object\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn6
  public void testIn6() throws Exception {
    testTypes(
        "" +
        "function g(x) {}" +
        "g(1 in {});",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn7
  public void testIn7() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  return g(x.foo) in {};" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn1
  public void testForIn1() throws Exception {
    testTypes(
        " function f(x) {}" +
        "for (var k in {}) {" +
        "  f(k);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn2
  public void testForIn2() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var E = {FOO: 'bar'};" +
        " var obj = {};" +
        "var k = null;" +
        "for (k in obj) {" +
        "  f(k);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : E.<string>\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn3
  public void testForIn3() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var obj = {};" +
        "for (var k in obj) {" +
        "  f(obj[k]);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn4
  public void testForIn4() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var E = {FOO: 'bar'};" +
        " var obj = {};" +
        "for (var k in obj) {" +
        "  f(obj[k]);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (Array|null)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn5
  public void testForIn5() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var E = function(){};" +
        " var obj = {};" +
        "for (var k in obj) {" +
        "  f(k);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison2
  public void testComparison2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "if (a!==b) {}",
        "condition always evaluates to the same value\n" +
        "left : number\n" +
        "right: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison3
  public void testComparison3() throws Exception {
    
    testTypes("var a;" +
        "var b = a == null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison4
  public void testComparison4() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a == b");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison5
  public void testComparison5() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison6
  public void testComparison6() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a != b",
        "condition always evaluates to false\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison7
  public void testComparison7() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison8
  public void testComparison8() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null || a[1] == undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison9
  public void testComparison9() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison10
  public void testComparison10() throws Exception {
    testTypes(" var a = [];" +
        "a[0] === null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison11
  public void testComparison11() throws Exception {
    testTypes(
        "(function(){}) == 'x'",
        "condition always evaluates to false\n" +
        "left : function (): undefined\n" +
        "right: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison12
  public void testComparison12() throws Exception {
    testTypes(
        "(function(){}) == 3",
        "condition always evaluates to false\n" +
        "left : function (): undefined\n" +
        "right: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison13
  public void testComparison13() throws Exception {
    testTypes(
        "(function(){}) == false",
        "condition always evaluates to false\n" +
        "left : function (): undefined\n" +
        "right: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    testTypes(
        "var x = {};" +
        " function f() { return delete x['a']; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    testTypes(
        "var obj = {};" +
        " function f(x) { return obj; }" +
        " function g(x) {" +
        "  if (x) { delete f(x)['a']; }" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnumStaticMethod1
  public void testEnumStaticMethod1() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "Foo.method(true);",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnumStaticMethod2
  public void testEnumStaticMethod2() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "function f() { Foo.method(true); }",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum1
  public void testEnum1() throws Exception {
    testTypes("var a={BB:1,CC:2};\n" +
        "var d;d=a.BB;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum2
  public void testEnum2() throws Exception {
    testTypes("var a={b:1}",
        "enum key b must be a syntactic constant");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum3
  public void testEnum3() throws Exception {
    testTypes("var a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum4
  public void testEnum4() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum5
  public void testEnum5() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: (String|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum6
  public void testEnum6() throws Exception {
    testTypes("var a={BB:1,CC:2};\nvar d;d=a.BB;",
        "assignment\n" +
        "found   : a.<number>\n" +
        "required: Array");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum7
  public void testEnum7() throws Exception {
    testTypes("var a={AA:1,BB:2,CC:3};" +
        "var b=a.D;",
        "element D does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum8
  public void testEnum8() throws Exception {
    testTypes("var a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum9
  public void testEnum9() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum10
  public void testEnum10() throws Exception {
    testTypes(
        "" +
        "goog.K = { A : 3 };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum11
  public void testEnum11() throws Exception {
    testTypes(
        "" +
        "goog.K = { 502 : 3 };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum12
  public void testEnum12() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum13
  public void testEnum13() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;",
        "incompatible enum element types\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum14
  public void testEnum14() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.FOO;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum15
  public void testEnum15() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.BAR;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum16
  public void testEnum16() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum17
  public void testEnum17() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum18
  public void testEnum18() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum19
  public void testEnum19() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: E.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum20
  public void testEnum20() throws Exception {
    testTypes(" var E = {A: 1, B: 2}; var x = []; x[E.A] = 0;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum21
  public void testEnum21() throws Exception {
    Node n = parseAndTypeCheck(
        " var E = {A : 'a', B : 'b'};\n" +
        " function f(x) { return x; }");
    Node nodeX = n.getLastChild().getLastChild().getLastChild().getLastChild();
    JSType typeE = nodeX.getJSType();
    assertFalse(typeE.isObject());
    assertFalse(typeE.isNullable());
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum22
  public void testEnum22() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum23
  public void testEnum23() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum24
  public void testEnum24() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<(Object|null)>\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum25
  public void testEnum25() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum26
  public void testEnum26() throws Exception {
    testTypes("var a = {};  a.B = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum27
  public void testEnum27() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A == x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum28
  public void testEnum28() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A.B == x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum29
  public void testEnum29() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum30
  public void testEnum30() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum31
  public void testEnum31() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: A.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum32
  public void testEnum32() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum34
  public void testEnum34() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f(x) { return x == A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum35
  public void testEnum35() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return a.b.C; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum36
  public void testEnum36() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return 1; }",
              "inconsistent return type\n" +
              "found   : number\n" +
              "required: a.b.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum37
  public void testEnum37() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.a = {};" +
        " var b = goog.a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum38
  public void testEnum38() throws Exception {
    testTypes(
        " var MyEnum = {};" +
        " function f(x) {}",
        "Parse error. Cycle detected in inheritance chain " +
        "of type MyEnum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum39
  public void testEnum39() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum40
  public void testEnum40() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum41
  public void testEnum41() throws Exception {
    testTypes(
        " var MyEnum = { FOO: 1};" +
        "" +
        "function f() { return MyEnum.FOO; }",
        "inconsistent return type\n" +
        "found   : MyEnum.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum1
  public void testAliasedEnum1() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum2
  public void testAliasedEnum2() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum3
  public void testAliasedEnum3() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum4
  public void testAliasedEnum4() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum5
  public void testAliasedEnum5() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : YourEnum.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse1
  public void testBackwardsEnumUse1() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse2
  public void testBackwardsEnumUse2() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};",
        "inconsistent return type\n" +
        "found   : MyEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse3
  public void testBackwardsEnumUse3() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse4
  public void testBackwardsEnumUse4() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "inconsistent return type\n" +
        "found   : YourEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse5
  public void testBackwardsEnumUse5() throws Exception {
    testTypes(
        " function f() { return MyEnum.BAR; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse1
  public void testBackwardsTypedefUse1() throws Exception {
    testTypes(
        " function f() {}" +
        " var MyTypedef;",
        "@this type of a function must be an object\n" +
        "Actual type: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse2
  public void testBackwardsTypedefUse2() throws Exception {
    testTypes(
        " function f() {}" +
        " var MyTypedef;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse3
  public void testBackwardsTypedefUse3() throws Exception {
    testTypes(
        " function f() {}" +
        " var MyTypedef;",
        "@this type of a function must be an object\n" +
        "Actual type: (Date|null|string)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse4
  public void testBackwardsTypedefUse4() throws Exception {
    testTypes(
        " function f() { return null; }" +
        " var MyTypedef;",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse6
  public void testBackwardsTypedefUse6() throws Exception {
    testTypes(
        " function f() { return null; }" +
        "var goog = {};" +
        " goog.MyTypedef;",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse7
  public void testBackwardsTypedefUse7() throws Exception {
    testTypes(
        " function f() { return null; }" +
        "var goog = {};" +
        " goog.MyTypedef;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse8
  public void testBackwardsTypedefUse8() throws Exception {
    
    
    testTypes(
        " function g(x) {}" +
        " function f() { g(this); }" +
        "var goog = {};" +
        " goog.MyTypedef;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse9
  public void testBackwardsTypedefUse9() throws Exception {
    testTypes(
        " function g(x) {}" +
        " function f() { g(this); }" +
        "var goog = {};" +
        " goog.MyTypedef;",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : Error\n" +
        "required: Array");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsConstructor1
  public void testBackwardsConstructor1() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var Foo = function(x) {};",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsConstructor2
  public void testBackwardsConstructor2() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var YourFoo = function(x) {};" +
        "" +
        "var Foo = YourFoo;",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMinimalConstructorAnnotation
  public void testMinimalConstructorAnnotation() throws Exception {
    testTypes("function Foo(){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends1
  public void testGoodExtends1() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends2
  public void testGoodExtends2() throws Exception {
    testTypes("function derived() {}\n" +
        "function base() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends3
  public void testGoodExtends3() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends4
  public void testGoodExtends4() throws Exception {
    
    
    
    Node n = parseAndTypeCheck(
        "var goog = {};\n" +
        "goog.Base = function(){};\n" +
        "goog.Derived = function(){};\n");
    Node subTypeName = n.getLastChild().getLastChild().getFirstChild();
    assertEquals("goog.Derived", subTypeName.getQualifiedName());

    FunctionType subCtorType =
        (FunctionType) subTypeName.getNext().getJSType();
    assertEquals("goog.Derived", subCtorType.getInstanceType().toString());

    JSType superType = subCtorType.getPrototype().getImplicitPrototype();
    assertEquals("goog.Base", superType.toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends5
  public void testGoodExtends5() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends6
  public void testGoodExtends6() throws Exception {
    testFunctionType(
        CLOSURE_DEFS +
        "function base() {}\n" +
        " " +
        "  base.prototype.foo = function() { return 1; };\n" +
        "function derived() {}\n" +
        "goog.inherits(derived, base);",
        "derived.superClass_.foo",
        "function (this:base): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends7
  public void testGoodExtends7() throws Exception {
    testFunctionType(
        "Function.prototype.inherits = function(x) {};" +
        "function base() {}\n" +
        "function derived() {}\n" +
        "derived.inherits(base);",
        "(new derived).constructor",
        "function (new:derived): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends8
  public void testGoodExtends8() throws Exception {
    testTypes(" function Sub() {}" +
        " function f() { return (new Sub()).foo; }" +
        " function Base() {}" +
        " Base.prototype.foo = true;",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends9
  public void testGoodExtends9() throws Exception {
    testTypes(
        " function Super() {}" +
        "Super.prototype.foo = function() {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends10
  public void testGoodExtends10() throws Exception {
    testTypes(
        " function Super() {}" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " function foo() { return new Sub(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends11
  public void testGoodExtends11() throws Exception {
    testTypes(
        " function Super() {}" +
        " Super.prototype.foo = function(x) {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        "(new Sub()).foo(0);",
        "actual parameter 1 of Super.prototype.foo " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends12
  public void testGoodExtends12() throws Exception {
    testTypes(
        " function Sub() {}" +
        " function Sub2() {}" +
        " function Super() {}" +
        " function foo(x) {}" +
        "foo(new Sub2());");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends13
  public void testGoodExtends13() throws Exception {
    testTypes(
        " function C() {}" +
        " function E() {}" +
        " function D() {}" +
        " function B() {}" +
        " function A() {}" +
        " function f(x) {} f(new E());",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : E\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends1
  public void testBadExtends1() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n",
        "Bad type annotation. Unknown type not_base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends2
  public void testBadExtends2() throws Exception {
    testTypes("function base() {\n" +
        "\n" +
        "this.baseMember = new Number(4);\n" +
        "}\n" +
        "function derived() {}\n" +
        "\n" +
        "function foo(x){ }\n" +
        "var y;\n" +
        "foo(y.baseMember);\n",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends3
  public void testBadExtends3() throws Exception {
    testTypes("function base() {}",
        "@extends used without @constructor or @interface for base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends4
  public void testBadExtends4() throws Exception {
    
    
    testTypes(
        " function Sub() {}" +
        " function Sub2() {}" +
        " function foo(x) {}" +
        "foo(new Sub2());",
        "Bad type annotation. Unknown type bad");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLateExtends
  public void testLateExtends() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function Foo() {}\n" +
        "Foo.prototype.foo = function() {};\n" +
        "function Bar() {}\n" +
        "goog.inherits(Foo, Bar);\n",
        "Missing @extends tag on type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatch
  public void testSuperclassMatch() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatchWithMixin
  public void testSuperclassMatchWithMixin() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Baz = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.mixin = function(y){};" +
        "Bar.inherits(Foo);\n" +
        "Bar.mixin(Baz);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch1
  public void testSuperclassMismatch1() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch2
  public void testSuperclassMismatch2() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function(){};\n" +
        " var Bar = function(){};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass1
  public void testSuperClassDefinedAfterSubClass1() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " function Base() {}" +
        " " +
        "function foo(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass2
  public void testSuperClassDefinedAfterSubClass2() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " " +
        "function foo(x) { return x; }" +
        " function Base() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment1
  public void testDirectPrototypeAssignment1() throws Exception {
    testTypes(
        " function Base() {}" +
        "Base.prototype.foo = 3;" +
        " function A() {}" +
        "A.prototype = new Base();" +
        " function foo() { return (new A).foo; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment2
  public void testDirectPrototypeAssignment2() throws Exception {
    
    
    testTypes(
        " function Base() {}" +
        " function A() {}" +
        "A.prototype = new Base();" +
        "A.prototype.foo = 3;" +
        " function foo() { return (new Base).foo; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment3
  public void testDirectPrototypeAssignment3() throws Exception {
    
    
    testTypes(
        " var MainWidgetCreator = function() {};" +
        "" +
        "function createMainWidget(ctor) {" +
        "   function tempCtor() {};" +
        "  tempCtor.prototype = ctor.prototype;" +
        "  MainWidgetCreator.superClass_ = ctor.prototype;" +
        "  MainWidgetCreator.prototype = new tempCtor();" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements1
  public void testGoodImplements1() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements2
  public void testGoodImplements2() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements3
  public void testGoodImplements3() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements4
  public void testGoodImplements4() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.abstractMethod = function() {};" +
        "\n" +
        "goog.Disposable = goog.abstractMethod;" +
        "goog.Disposable.prototype.dispose = goog.abstractMethod;" +
        "" +
        "goog.SubDisposable = function() {};" +
        " " +
        "goog.SubDisposable.prototype.dispose = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements1
  public void testBadImplements1() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}",
        "Bad type annotation. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements2
  public void testBadImplements2() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}",
        "@implements used without @constructor or @interface for f");
  }

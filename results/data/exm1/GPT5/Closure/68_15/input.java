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
    assertEquals(2, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign2
  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(2, assign.getLineno());
    assertEquals(12, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoCall
  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(4, call.getCharno());
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
    assertEquals(9, lt.getCharno());
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

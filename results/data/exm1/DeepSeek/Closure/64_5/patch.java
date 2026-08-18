public void toSource(final CodeBuilder cb,
                       final int inputSeqNum,
                       final Node root) {
    runInCompilerThread(new Callable<Void>() {
      public Void call() throws Exception {
        if (options.printInputDelimiter) {
          if (root.getType() == Token.SCRIPT) {
            if ((cb.getLength() > 0) && !cb.endsWith("\n")) {
              cb.append("\n");
            }
            String delimiter = options.inputDelimiter;
            String sourceName = (String)root.getProp(Node.SOURCENAME_PROP);
            if (sourceName != null && !sourceName.isEmpty()) {
              delimiter = delimiter.replaceAll("%name%", sourceName)
                .replaceAll("%num%", String.valueOf(inputSeqNum));
            } else {
              delimiter = delimiter.replaceAll("%num%", String.valueOf(inputSeqNum));
            }
            cb.append(delimiter).append("\n");
          }
        }
        if (root.getJSDocInfo() != null &&
            root.getJSDocInfo().getLicense() != null) {
          cb.append("/*\n")
            .append(root.getJSDocInfo().getLicense())
            .append("*/\n");
        }
        if (options.sourceMapOutputPath != null) {
          sourceMap.setStartingPosition(
              cb.getLineIndex(), cb.getColumnIndex());
        }
        String code = toSource(root, sourceMap);
        if (!code.isEmpty()) {
          cb.append(code);
          int length = code.length();
          char lastChar = code.charAt(length - 1);
          char secondLastChar = length >= 2 ?
              code.charAt(length - 2) : '\0';
          boolean hasSemiColon = lastChar == ';' ||
              (lastChar == '\n' && secondLastChar == ';');
          if (!hasSemiColon) {
            cb.append(";");
          }
        }
        return null;
      }
    });
  }
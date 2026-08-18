public void appendTo(Appendable out, String name) throws IOException {
    int maxLine = findLastLine();

    out.append("/** Begin line maps. **/{ \"file\" : ");
    out.append(escapeString(name));
    out.append(", \"count\": ");
    out.append(String.valueOf(maxLine + 1));
    out.append(" }\n");
    (new LineMapper(out)).appendLineMappings();

    out.append("/** Begin file information. **/\n");

    for (int i = 0; i <= maxLine; ++i) {
      out.append("[]\n");
    }

    out.append("/** Begin mapping definitions. **/\n");

    for (Mapping mapping : mappings) {
      mapping.appendTo(out);
      out.append("\n");
    }
  }
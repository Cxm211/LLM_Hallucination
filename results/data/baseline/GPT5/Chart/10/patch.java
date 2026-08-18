public String generateToolTipFragment(String toolTipText) {
        if (toolTipText == null) {
            toolTipText = "";
        }
        StringBuilder escaped = new StringBuilder(toolTipText.length() + 16);
        for (int i = 0; i < toolTipText.length(); i++) {
            char c = toolTipText.charAt(i);
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                default:
                    escaped.append(c);
            }
        }
        return " title=\"" + escaped.toString() + "\" alt=\"\"";
    }
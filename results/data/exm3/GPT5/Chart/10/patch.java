public String generateToolTipFragment(String toolTipText) {
        String s = (toolTipText == null) ? "" : toolTipText;
        StringBuilder sb = new StringBuilder(s.length() + 20);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return " title=\"" + sb.toString() + "\" alt=\"\"";
    }
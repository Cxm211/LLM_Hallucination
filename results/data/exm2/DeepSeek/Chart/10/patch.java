    public String generateToolTipFragment(String toolTipText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toolTipText.length(); i++) {
            char c = toolTipText.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '\"':
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
        String escaped = sb.toString();
        return " title=\"" + escaped + "\" alt=\"\"";
    }
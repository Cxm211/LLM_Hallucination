String consumeToEnd() {
        if (pos >= input.length())
            return "";
        String data = input.substring(pos);
        // normalize CR and CRLF to LF
        data = data.replace("\r\n", "\n").replace('\r', '\n');
        pos = input.length();
        return data;
    }
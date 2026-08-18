public static String random(int count, int start, int end, boolean letters, boolean numbers,
                                char[] chars, Random random) {
    if (count == 0) {
        return "";
    } else if (count < 0) {
        throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
    }

    if (chars != null) {
        char[] buffer = new char[count];
        for (int i = 0; i < count; i++) {
            buffer[i] = chars[random.nextInt(chars.length)];
        }
        return new String(buffer);
    }

    if (start == 0 && end == 0) {
        if (!letters && !numbers) {
            end = Integer.MAX_VALUE;
        } else {
            end = 'z' + 1;
            start = ' ';                
        }
    }

    char[] buffer = new char[count];
    int gap = end - start;
    int pos = 0;

    while (pos < count) {
        char ch = (char) (random.nextInt(gap) + start);
        boolean accept = (letters && Character.isLetter(ch))
                || (numbers && Character.isDigit(ch))
                || (!letters && !numbers);
        if (!accept) continue;

        if (ch >= 55296 && ch <= 56191) {
            if (pos + 1 >= count) continue;
            buffer[pos++] = ch;
            buffer[pos++] = (char) (56320 + random.nextInt(128));
        } else if (ch >= 56192 && ch <= 56319) {
            continue;
        } else if (ch >= 56320 && ch <= 57343) {
            if (pos + 1 >= count) continue;
            buffer[pos++] = (char) (55296 + random.nextInt(128));
            buffer[pos++] = ch;
        } else {
            buffer[pos++] = ch;
        }
    }
    return new String(buffer);
}
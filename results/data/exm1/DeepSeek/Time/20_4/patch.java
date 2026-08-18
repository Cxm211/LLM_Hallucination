public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String matchedId = null;
    int maxLen = 0;
    for (String id : ALL_IDS) {
        if (str.startsWith(id) && id.length() > maxLen) {
            matchedId = id;
            maxLen = id.length();
        }
    }
    if (matchedId != null) {
        bucket.setZone(DateTimeZone.forID(matchedId));
        return position + maxLen;
    }
    return ~position;
}
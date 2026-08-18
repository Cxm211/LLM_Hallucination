public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String bestMatch = null;
    int bestLen = -1;
    for (String id : ALL_IDS) {
        if (str.startsWith(id) && id.length() > bestLen) {
            bestMatch = id;
            bestLen = id.length();
        }
    }
    if (bestMatch != null) {
        bucket.setZone(DateTimeZone.forID(bestMatch));
        return position + bestLen;
    }
    return ~position;
}

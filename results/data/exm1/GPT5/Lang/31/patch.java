// buggy code
	public static boolean containsAny(CharSequence cs, char[] searchChars) {
		if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
			return false;
		}
		int csLength = cs.length();
		int searchLength = searchChars.length;
		for (int i = 0; i < csLength; i++) {
			char ch = cs.charAt(i);
			for (int j = 0; j < searchLength; j++) {
				if (searchChars[j] == ch) {
					// handle supplementary characters: ensure surrogate pairs match when applicable
					if (Character.isHighSurrogate(ch)) {
						if (j + 1 < searchLength) {
							if (i + 1 < csLength && Character.isLowSurrogate(searchChars[j + 1])
									&& searchChars[j + 1] == cs.charAt(i + 1)) {
								return true;
							}
							// otherwise continue searching; the pair does not match here
						} else {
							// search contains only the high surrogate; consider it a match
							return true;
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

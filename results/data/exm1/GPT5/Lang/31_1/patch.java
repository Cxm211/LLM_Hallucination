public static boolean containsAny(CharSequence cs, char[] searchChars) {
		if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
			return false;
		}
		int csLength = cs.length();
		int searchLength = searchChars.length;
		for (int i = 0; i < csLength; i++) {
			char ch = cs.charAt(i);
			boolean chIsHighSurrogate = Character.isHighSurrogate(ch);
			if (chIsHighSurrogate && i + 1 < csLength) {
				char ch2 = cs.charAt(i + 1);
				for (int j = 0; j < searchLength - 1; j++) {
					if (Character.isHighSurrogate(searchChars[j]) && searchChars[j] == ch && searchChars[j + 1] == ch2) {
						return true;
					}
				}
				// skip the low surrogate in next iteration
			}
			for (int j = 0; j < searchLength; j++) {
				if (searchChars[j] == ch) {
					return true;
				}
			}
			if (chIsHighSurrogate) {
				i++; // skip low surrogate already considered
			}
		}
		return false;
	}
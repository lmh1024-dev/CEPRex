package dk.brics.automaton;

import java.util.ArrayList;
import java.util.List;

public class ExtendedRegex {

	public static RegExp getSimplifiedRegexp(String s) {
		String eString = extendRegex(s);
		return new RegExp(eString);
	}

	/**
	 * 将字符串中的 Unicode 转义 (\\uXXXX) 和 Hex 转义 (\xHH) 转换为实际字符
	 */
	public static String decodeUnicode(String s) {
		// 检查是否包含转义符起始标志
		if (s == null || (s.indexOf("\\u") == -1 && s.indexOf("\\x") == -1)) {
			return s;
		}

		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			// 检查是否是转义符 \
			if (c == '\\' && i + 1 < s.length()) {
				char next = s.charAt(i + 1);

				// 1. 处理 \\uXXXX (4位)
				if (next == 'u') {
					if (i + 5 < s.length()) {
						String hex = s.substring(i + 2, i + 6);
						try {
							int code = Integer.parseInt(hex, 16);
							sb.append((char) code);
							i += 5; // 跳过 \\uXXXX
							continue;
						} catch (NumberFormatException e) {
							// ignore
						}
					}
				}
				// 2. [新增] 处理 \xHH (2位)
				else if (next == 'x') {
					if (i + 3 < s.length()) {
						String hex = s.substring(i + 2, i + 4);
						try {
							int code = Integer.parseInt(hex, 16);
							sb.append((char) code);
							i += 3; // 跳过 \xHH
							continue;
						} catch (NumberFormatException e) {
							// ignore
						}
					}
				}
			}

			// 非转义或无效转义，直接追加
			sb.append(c);
		}
		return sb.toString();
	}

	public static String extendRegex(String str) {
		// 1. 先进行 Unicode 转义处理
		String s = decodeUnicode(str);
		s = s.replace("(?:", "(");
		StringBuilder out = new StringBuilder();
		int i = 0;
		while (i < s.length()) {
			char c = s.charAt(i);

			if (c == '[') {
				// -------------------------------------------------
				// 处理字符类 [...] (内部的 < > 不需要转义)
				// -------------------------------------------------
				int j = i + 1;
				boolean neg = false;
				if (j < s.length() && s.charAt(j) == '^') {
					neg = true;
					j++;
				}

				StringBuilder cls = new StringBuilder();
				while (j < s.length() && s.charAt(j) != ']') {
					cls.append(s.charAt(j));
					j++;
				}

				if (j >= s.length()) { // 未闭合
					out.append(c);
					i++;
					continue;
				}

				String expanded = expandCharClass(cls.toString());

				if (expanded.startsWith("(")) {
					out.append(expanded);
				} else {
					out.append("[");
					if (neg) out.append("^");
					out.append(expanded);
					out.append("]");
				}

				i = j + 1;
			} else {
				// -------------------------------------------------
				// 处理字符类外部的内容
				// -------------------------------------------------
				if (c == '\\' && i + 1 < s.length()) {
					// 如果已经是转义字符 (例如 \d, \w, 或者用户自己写的 \<), 保持原样或扩展
					char n = s.charAt(i + 1);
					switch (n) {
						case 'd': out.append("[0-9]"); break;
						case 'D': out.append("[^0-9]"); break;
						case 'w': out.append("[a-zA-Z0-9_]"); break;
						case 'W': out.append("[^a-zA-Z0-9_]"); break;
						case 's': out.append("[ \t\n\r\f]"); break;
						case 'S': out.append("[^ \t\n\r\f]"); break;
						default:
							// 对于其他转义 (包括用户可能已经手动转义的 \<)，直接保留
							out.append(c).append(n);
							break;
					}
					i += 2;
				}// B. [核心修改] 自动检测并修复 "|)" 写法
				// 如果遇到 "|" 且下一个字符是 ")"，说明是空分支 (A|B|)，替换为 (A|B)?
				else if (c == '|' && i + 1 < s.length() && s.charAt(i + 1) == ')') {
					out.append(")?");
					i += 2; // 跳过原本的 | 和 )
				}
				else {
					// ---------------------------------------------
					// [新功能] : 自动转义 < 和 >
					// ---------------------------------------------
					if (c == '<' || c == '>' || c == '"') {
						out.append('\\').append(c);
					} else {
						out.append(c);
					}
					i++;
				}
			}
		}
		return out.toString();
	}

	private static String expandCharClass(String cls) {
		List<String> tokens = new ArrayList<>();
		boolean hasComplex = false;

		for (int i = 0; i < cls.length(); i++) {
			char c = cls.charAt(i);

			if (c == '\\' && i + 1 < cls.length()) {
				char n = cls.charAt(i + 1);
				if (n == 'd') { tokens.add("0-9"); i++; }
				else if (n == 'D') { tokens.add("[^0-9]"); hasComplex = true; i++; }
				else if (n == 'w') { tokens.add("a-zA-Z0-9_"); i++; }
				else if (n == 'W') { tokens.add("[^a-zA-Z0-9_]"); hasComplex = true; i++; }
				else if (n == 's') { tokens.add(" \t\n\r\f"); i++; }
				else if (n == 'S') { tokens.add("[^ \t\n\r\f]"); hasComplex = true; i++; }
				else if (n == 'u') {
					if (i + 5 < cls.length()) {
						tokens.add(cls.substring(i, i + 6));
						i += 5;
					} else {
						tokens.add("\\u");
						i++;
					}
				}
				else {
					tokens.add("\\" + n);
					i++;
				}
			} else {
				tokens.add(String.valueOf(c));
			}
		}

		if (!hasComplex) {
			StringBuilder sb = new StringBuilder();
			for (String t : tokens) sb.append(t);
			return sb.toString();
		}

		StringBuilder out = new StringBuilder();
		out.append("(");
		StringBuilder simpleGroup = new StringBuilder();
		boolean first = true;

		for (String t : tokens) {
			if (t.startsWith("[^")) {
				if (simpleGroup.length() > 0) {
					if (!first) out.append("|");
					out.append("[").append(simpleGroup).append("]");
					simpleGroup.setLength(0);
					first = false;
				}
				if (!first) out.append("|");
				out.append(t);
				first = false;
			} else {
				simpleGroup.append(t);
			}
		}

		if (simpleGroup.length() > 0) {
			if (!first) out.append("|");
			out.append("[").append(simpleGroup).append("]");
		}

		out.append(")");
		return out.toString();
	}
}
package com.lmh.coverage.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTokenMapper {

    public static void main(String[] args) {
        // 你的原始正则表达式
        String originalRegex = "(3[0-1]|2[0-9]|1[0-9]|0[1-9])[\\s{1}|\\/|-](Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)[\\s{1}|\\/|-]\\d{4}";
        String regex = "(3[0-1]|2[0-9]|1[0-9]|0[1-9])[\\s{1}|\\/|-](Jan|JAN|Feb|FEB|Mar|MAR|Apr|APR|May|MAY|Jun|JUN|Jul|JUL|Aug|AUG|Sep|SEP|Oct|OCT|Nov|NOV|Dec|DEC)[\\s{1}|\\/|-]\\d{4}";
        // 用于存储：占位符 -> 原始单词
        // 使用 LinkedHashMap 保证顺序
        Map<Character, String> tokenMap = new LinkedHashMap<>();

        // 1. 提取并替换
        String modifiedRegex = tokenize(originalRegex, tokenMap);

        // 2. 输出结果
        System.out.println("=== 修改后的正则表达式 ===");
        System.out.println(modifiedRegex);

        System.out.println("\n=== Map 映射表 ===");
        tokenMap.forEach((key, value) -> {
            System.out.printf("%-10s : %s%n", key, value);
        });

        // 3. 还原演示 (验证是否能改回去)
        String restored = restore(modifiedRegex, tokenMap);
        System.out.println("\n=== 还原后的正则表达式 ===");
        System.out.println(restored);
        System.out.println("还原是否成功: " + originalRegex.equals(restored));
        System.out.println(transformWithExistingMap(regex,tokenMap));
    }

    /**
     * 将正则中的确定性单词替换为占位符，并存入 Map
     */
    public static String tokenize(String regex, Map<Character, String> tokenMap) {
        // 匹配逻辑：前面不是反斜杠的 连续2个及以上字母
        Pattern p = Pattern.compile("\\b[a-zA-Z]{2,}\\b(?![^\\[]*\\])");
        Matcher m = p.matcher(regex);

        StringBuilder sb = new StringBuilder();
        int counter = 0;
        char c = '\u2f00';
        while (m.find()) {
            String word = m.group();

            // 检查这个单词是否已经存在于 Map 中（避免同一个 Jan 生成多个不同的 Token）
            Character token = findKeyByValue(tokenMap, word);

            if (token == null) {
                // 生成新的特殊符号，例如 @[W0]@
                token = (char)(c + (counter++));
                tokenMap.put(token, word);
            }

            // 将当前匹配到的部分替换为占位符
            // 注意：appendReplacement 会处理转义，这里 token 不含特殊正则字符，安全
            m.appendReplacement(sb, Matcher.quoteReplacement(token.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 将带有占位符的正则还原
     */
    public static String restore(String modifiedRegex, Map<Character, String> tokenMap) {
        String result = modifiedRegex;
        for (Map.Entry<Character, String> entry : tokenMap.entrySet()) {
            // 使用 replace 将占位符换回原单词
            result = result.replace(entry.getKey().toString(), entry.getValue());
        }
        return result;
    }

    // 辅助方法：通过 Value 找 Key
    private static Character findKeyByValue(Map<Character, String> map, String value) {
        for (Map.Entry<Character, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 根据已有的 tokenMap，将待转换的正则表达式中的单词替换为对应的 Token
     *
     * @param inputRegex  待转换的正则表达式字符串
     * @param tokenMap    已有的映射表 (Key: @[W0]@, Value: Jan)
     * @return            转换后的字符串
     */
    public static String transformWithExistingMap(String inputRegex, Map<Character, String> tokenMap) {
        if (inputRegex == null || tokenMap == null || tokenMap.isEmpty()) {
            return inputRegex;
        }

        // 第一步：为了方便查找，先将 Map 的 Key 和 Value 对调
        // 生成一个 Word -> Token 的临时 Map
        Map<String, String> wordToTokenMap = new HashMap<>();
        for (Map.Entry<Character, String> entry : tokenMap.entrySet()) {
            wordToTokenMap.put(entry.getValue(), entry.getKey().toString());
        }

        // 第二步：使用正则表达式识别出输入字符串中的所有“确定性单词”
        // 逻辑保持一致：前面不是反斜杠的连续3个及以上字母
        Pattern p = Pattern.compile("(?<!\\\\)[a-zA-Z0-9]{3,}");
        Matcher m = p.matcher(inputRegex);

        StringBuilder sb = new StringBuilder();

        // 第三步：遍历匹配到的单词，如果在 Map 中存在，则替换；不存在则保留原样
        while (m.find()) {
            String foundWord = m.group();

            if (wordToTokenMap.containsKey(foundWord)) {
                // 如果 Map 里有这个单词，换成对应的 Token
                String token = wordToTokenMap.get(foundWord);
                m.appendReplacement(sb, Matcher.quoteReplacement(token));
            } else {
                // 如果 Map 里没有（比如示例中的 Monday），保留原词
                m.appendReplacement(sb, Matcher.quoteReplacement(foundWord));
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }

}
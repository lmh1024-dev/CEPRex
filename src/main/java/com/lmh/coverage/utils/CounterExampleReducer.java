package com.lmh.coverage.utils;


import java.util.*;
import java.util.stream.Collectors;

public class CounterExampleReducer {

    /**
     * 通用约简入口
     * @param rawCounterExamples 原始的反例集合 (乱七八糟的一堆)
     * @param goldenSample 一个标准的、正确的正例字符串 (用于计算编辑距离，评估反例的价值)
     * 如果不提供 (null)，则仅根据长度进行优选。
     * @return 约简后的精简集合
     */
    public static List<String> reduce(Collection<String> rawCounterExamples, String goldenSample) {
        if (rawCounterExamples == null || rawCounterExamples.isEmpty()) {
            return Collections.emptyList();
        }

        // 用于存储每种"错误模式"的最佳代表
        // Key: 抽象骨架 (Pattern), Value: 最佳反例
        Map<String, String> bestRepresentatives = new HashMap<>();

        for (String raw : rawCounterExamples) {
            if (raw == null) continue;

            // 1. 提取骨架 (Fingerprint)
            String skeleton = generateSkeleton(raw);

            // 2. 优胜劣汰
            if (!bestRepresentatives.containsKey(skeleton)) {
                // 如果是新模式，直接收录
                bestRepresentatives.put(skeleton, raw);
            } else {
                // 如果是已知模式，比较当前反例和已有的反例谁更好
                String existing = bestRepresentatives.get(skeleton);
                if (isBetter(raw, existing, goldenSample)) {
                    bestRepresentatives.put(skeleton, raw);
                }
            }
        }

        // 3. 排序输出 (按长度排序，短的在前，方便阅读)
        return bestRepresentatives.values().stream()
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());
    }

    /**
     * 生成抽象骨架
     * 将具体的数字变成 'd'，字母变成 'w'，保留标点符号。
     * 这样可以将同类错误归并。
     */
    private static String generateSkeleton(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                // 压缩连续的数字：如果前一个已经是 d，就不再追加 d
                // 比如 000 和 0 都变成 d，避免 000 变成 ddd 导致模式无法合并
                if (sb.length() == 0 || sb.charAt(sb.length() - 1) != 'd') {
                    sb.append('d');
                }
            } else if (Character.isLetter(c)) {
                if (sb.length() == 0 || sb.charAt(sb.length() - 1) != 'w') {
                    sb.append('w');
                }
            } else if (Character.isWhitespace(c)) {
                sb.append('s'); // 空白符
            } else {
                sb.append(c); // 标点符号保留原样，因为它们通常是正则结构的关键
            }
        }
        return sb.toString();
    }

    /**
     * 判断 candidate 是否比 incumbent (现任) 更好
     */
    private static boolean isBetter(String candidate, String incumbent, String goldenSample) {
        // 策略 1: 如果提供了标准正例，优先选编辑距离更小的 (Near-Miss)
        if (goldenSample != null) {
            int dist1 = levenshteinDistance(candidate, goldenSample);
            int dist2 = levenshteinDistance(incumbent, goldenSample);
            if (dist1 != dist2) {
                return dist1 < dist2;
            }
        }

        // 策略 2: 如果距离一样 (或者没提供正例)，选更短的
        if (candidate.length() != incumbent.length()) {
            return candidate.length() < incumbent.length();
        }

        // 策略 3: 如果长度也一样，选字典序小的 (为了确定性)
        return candidate.compareTo(incumbent) < 0;
    }

    /**
     * 计算编辑距离 (Levenshtein Distance)
     * 简单的动态规划实现
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[s1.length()][s2.length()];
    }

    // --- 测试 Main 方法 ---
    public static void main(String[] args) {
        // 你的原始乱序数据
        String rawInput = "$00, $0,0, $0.00.0, $0$,, $0.00.., $0$., $0.00.,, $0$0, $0.,$, $$$$, $$,,, $$,0, $0,$, $0$$, $0,,, $0,., $000$$, $000$0, $0.$$, $0.,,, $0.,., $0.,0, $0.$,, $0.$., $000$,, $0.00.$, $0.$0, $000$., $0.00,0, $0.00,., $0.00$$, $0.00,,, $0..$, $0.00$0, $0.00$., $0.00$,, $000,0000, $$.., $$.$,$, $$0.,, $000.,, $000.., $0.0$$, $0.0,,, $000.0, $000.$, $0.0,$, $0.0$., $000,000., $0..,, $0.0$0, $0..., $0..0, $0.0,., ,$, $0.0,0, $000,000$, $0.00,$, $0.0$,, ,,, ,., ,0, $000,00.0, $00,$, $000,00.., $$00, $000,000,,0, $,$, $00$0, $0000,, $000,$$, $0000., $0.0.,, $,,, $000,000,,$, $00000, $000,0.0, $0.0.., $,., $000,0.., $,0, $0000$, $000,$., $000,$,, $0.0.$, $000,000,,,, $000,$0, $000,000,,., $000,00.,, $000,0.$, $00$$, $00,,, ε, $$.$0, .$, $00,., $000,00.$, $0.0.0, $000,0.,, $00,0, $00$,, .,, $00$., .., $000,00$0, .0, $000,00$., $0.0000, $000,00$,, $0.000., $000,00,0, $00.$, $000,00,., $000,00$$, $000,00,,, $000,.$, $.$, $000,.,, $000,.0, $.,, $000,.., $000,0,0, $.., $000,0,., $.0, $000,0$$, $000,0,,, $000,0$0, $000,0$., $000,0$,, $0.000$, $00.,, 0$, $00.., $000,00,$, $00.0, $0.000,, 0,, 0., $000,0,$";

        List<String> rawList = Arrays.asList(rawInput.split(", "));

        Set<String> cleanedList = rawList.stream()
                .map(s -> s.replaceAll("([^a-zA-Z0-9])\\1+", "$1")) // 对每个元素执行清洗
                .collect(Collectors.toSet()); // 收集结果

        // 设置一个"黄金正例"，这对于筛选"近邻反例"非常有用
        // 正则: \$[0-9]{1,3}(,[0-9]{3})*(\.[0-9]{2})?
        String goldenSample = "$000.00";

        System.out.println("原始数量: " + rawList.size());
        System.out.println("原始: " + rawList);

        System.out.println("第一次清洗数量: " + cleanedList.size());
        System.out.println("第一次清洗: " + cleanedList);

        List<String> reduced = reduce(cleanedList, goldenSample);

        System.out.println("约简后数量: " + reduced.size());
        System.out.println("约简结果: " + reduced);

        List<String> finalList = AdvancedReducer.refineByFailurePosition(reduced,goldenSample);

        System.out.println("第二次约简后数量: " + finalList.size());
        System.out.println("约简结果: " + finalList);
    }
}
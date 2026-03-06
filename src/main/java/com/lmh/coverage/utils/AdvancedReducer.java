package com.lmh.coverage.utils;

import java.util.*;

public class AdvancedReducer {

    /**
     * 二次约简：基于首次失败位置 (FFP)
     * @param reducedExamples 上一步约简后的列表
     * @param goldenSample 黄金正例 (必须提供)
     * @return 极简结果集
     */
    public static List<String> refineByFailurePosition(List<String> reducedExamples, String goldenSample) {
        if (goldenSample == null) return reducedExamples;

        // Key: 出错的索引位置 (Index)
        // Value: 该位置出错的最佳反例
        Map<Integer, String> failureMap = new TreeMap<>(); // TreeMap 保证按索引顺序输出
        List<String> validOrComplex = new ArrayList<>(); // 存那些完全匹配或超长的复杂反例

        for (String example : reducedExamples) {
            // 找到该反例相对于黄金正例的"第一个分叉点"
            int diffIndex = findFirstDifference(example, goldenSample);

            if (diffIndex == -1) {
                // 如果完全一样（或者是黄金正例的前缀/超集），暂时归入特殊类
                validOrComplex.add(example);
            } else {
                // 竞争逻辑：在同一个位置出错的反例，保留最好的那个
                if (!failureMap.containsKey(diffIndex)) {
                    failureMap.put(diffIndex, example);
                } else {
                    String currentBest = failureMap.get(diffIndex);
                    if (isSimpler(example, currentBest)) {
                        failureMap.put(diffIndex, example);
                    }
                }
            }
        }

        // 合并结果
        List<String> finalResult = new ArrayList<>();
        finalResult.addAll(failureMap.values());

        // 可选：保留少量的复杂反例（防止过度清洗漏掉深层逻辑），比如保留最长的1-2个
        if (!validOrComplex.isEmpty()) {
            validOrComplex.sort(Comparator.comparingInt(String::length));
            // 只保留最短的（可能是空串或完全匹配）和最长的（可能是缓冲区溢出攻击）
            finalResult.add(validOrComplex.get(0));
            if (validOrComplex.size() > 1) {
                finalResult.add(validOrComplex.get(validOrComplex.size() - 1));
            }
        }

        return finalResult;
    }

    /**
     * 寻找两个字符串第一个不同的字符索引
     */
    private static int findFirstDifference(String s1, String s2) {
        int len = Math.min(s1.length(), s2.length());
        for (int i = 0; i < len; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        // 如果前缀完全相同，但长度不同，则差异点在短字符串的末尾
        if (s1.length() != s2.length()) {
            return len;
        }
        return -1; // 完全相同
    }

    /**
     * 判断 s1 是否比 s2 "更简单"
     * 简单 = 长度更短 > 标点符号更少
     */
    private static boolean isSimpler(String s1, String s2) {
        // 1. 优先选短的
        if (s1.length() != s2.length()) {
            return s1.length() < s2.length();
        }
        // 2. 如果长度一样，选标点符号少的 (通常标点多的看起来像乱码)
        return countPunctuation(s1) < countPunctuation(s2);
    }

    private static long countPunctuation(String s) {
        return s.chars().filter(c -> !Character.isLetterOrDigit(c)).count();
    }
}
package com.lmh.coverage.utils;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class GoldenGenerator {

    /**
     * 直接从正则表达式生成"黄金正例"
     * @param regex 原始正则表达式
     * @return 结构最完整的标准字符串
     */
    public static String generateGolden(String regex) {
        if (regex == null || regex.isEmpty()) return "";

        String enrichedRegex = enrichRegex(regex);

        Automaton automaton = new RegExp(enrichedRegex).toAutomaton();

        return automaton.getShortestExample(true);
    }

    /**
     * 正则强化逻辑
     * 策略：
     * ? -> (空) : 变成必选 (0或1 -> 1)
     * * -> (空) : 变成必选 (0或多 -> 1)
     * {0, -> {1, : 将下界0提升为1
     */
    private static String enrichRegex(String regex) {
        // 1. 处理 {0, m} -> {1, m}
        // 这里的正则替换需要小心，只替换数字为0的情况
        String stage1 = regex.replaceAll("\\{0,", "{1,");

        // 2. 处理 * (0或多) -> + (1或多)
        // 注意：要避开转义的 \*
        // 使用 lookbehind 确保前面不是 \
        String stage2 = stage1.replaceAll("(?<!\\\\)\\*", "+");

        // 3. 处理 ? (0或1) -> {1} (即移除?，使其变成默认的1次)
        // 注意：要避开转义的 \? 以及非捕获组 (?:...) 中的 ?
        // 这是一个简单的启发式处理，对于极复杂的嵌套可能有边缘情况，但在大多数业务正则中有效

        // 逻辑：如果 ? 前面是 ) 或 ] 或 普通字符，将其移除
        // 暂时简单粗暴地将所有非转义 ? 替换为 {1} (或者直接移除符号)
        // 为了安全，我们只处理作为量词的 ? (通常跟在 ), ], } 或 字符 后面)
        String stage3 = stage2.replaceAll("(?<=[\\w)\\]}])(?<!\\\\)\\?", "");

        return stage3;
    }

    // --- 测试 ---
    public static void main(String[] args) {
        // 你的原始正则：金额格式
        // 原始含义：
        // [0-9]{1,3}   : 1到3位数字
        // (,[0-9]{3})* : 0组或多组逗号分隔 (可选) -> 最短路径通常会跳过这个
        // (\.[0-9]{2})?: 0组或1组小数 (可选)     -> 最短路径通常会跳过这个
        String regex = "(n|m)([a-z]{3,4})?;(n|m)([a-z]{3,4})?;(n|m)([a-z]{3,4})?";

        System.out.println("原始正则: " + regex);

        // 1. 普通生成 (看看 dk.brics 原生效果)
        Automaton rawAuto = new RegExp(regex).toAutomaton();
        System.out.println("普通最短生成: " + rawAuto.getShortestExample(true));
        // 预期输出: $0 (因为它偷懒跳过了所有可选部分)

        System.out.println("-----------------------------");

        // 2. 黄金生成 (使用强化策略)
        String golden = generateGolden(regex);
        System.out.println("黄金正例生成: " + golden);
        // 预期输出: $0,000.00 (结构完整！)
    }
}
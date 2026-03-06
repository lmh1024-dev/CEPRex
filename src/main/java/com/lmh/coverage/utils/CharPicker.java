package com.lmh.coverage.utils;

public class CharPicker {

    // 定义可见字符范围（ASCII 32空格 ~ 126波浪号）
    private static final char VISIBLE_MIN = 32;
    private static final char VISIBLE_MAX = 126;

    private static final char VISIBLE_0 = 48;
    private static final char VISIBLE_9 = 57;

    private static final char VISIBLE_a = 97;
    private static final char VISIBLE_z = 122;

    private static final char VISIBLE_A = 65;
    private static final char VISIBLE_Z = 90;
    /**
     * 从给定的字符区间 [rangeMin, rangeMax] 中优选一个字符。
     * 策略：如果区间内包含可见字符，优先返回可见字符；否则返回 rangeMin。
     */
    public static char[] pickBestChar(char rangeMin, char rangeMax, char range) {
        // 1. 计算当前区间与可见字符区间的“交集”
        // 交集的下界是两者的最大值
        char min = VISIBLE_MIN,max = VISIBLE_MAX;

        switch (range){
            case 'a': min = VISIBLE_a;max = VISIBLE_z;break;
            case 'A': min = VISIBLE_A;max = VISIBLE_Z;break;
            case '0': min = VISIBLE_0;max = VISIBLE_9;break;
            default:break;
        }
        int overlapStart = Math.max(rangeMin, min);
        // 交集的上界是两者的最小值
        int overlapEnd = Math.min(rangeMax, max);

        // 2. 判断交集是否有效 (Start <= End 说明有重叠)
        if (overlapStart <= overlapEnd) {
            // 命中可见字符！直接返回交集的第一个字符
            // (也可以在这里改为 return (char) (overlapStart + random...))
            return new char[]{(char) overlapStart,(char) overlapEnd};
        }

        // 3. 如果没有交集（例如区间全是不可见字符，或全是中文/特殊符号）
        // 则不得不返回原始的最小值
        return new char[]{rangeMin,rangeMax};
    }

    // --- 测试用例 ---
    public static void main(String[] args) {
        test('\u0000', '\u002F'); // 你的痛点：0之前的字符
        test('\u0000', 'z');           // 普通字母
        test('\u0000', '\u0005'); // 纯不可见字符
        test('0', '9');           // 数字
        test('\u0000', '\uffff');
    }

    private static void test(char min, char max) {
        char[] ranges = new char[]{'0', 'a', 'A', 'V'};
        for(char range: ranges){
            char[] result = pickBestChar(min, max, range);
            System.out.printf("range : %s 区间 [\\u%04x - \\u%04x] -> 优选结果: '%s' (\\u%04x) '%s' (\\u%04x)%n",
                    range, (int)min, (int)max, result[0], (int)result[0],result[1],(int)result[1]);
        }

    }
}
package com.lmh.coverage.utils;

import com.lmh.coverage.entity.Path;
import dk.brics.automaton.Transition;

import java.util.*;
import java.util.stream.Collectors;

public class EPCReducer {

    /**
     * 核心方法：降低 EPC 规模
     * 策略：
     * 1. 去重（去除 toString 完全一样的路径）
     * 2. 贪心算法（保留覆盖能力最强且不冗余的路径）
     */
    public static LinkedList<Path> reduce(LinkedList<Path> originalEPC) {
        if (originalEPC == null || originalEPC.isEmpty()) {
            return new LinkedList<>();
        }


        // 第1步：简单的字符串去重 (解决你截图中 Path 8 和 Path 9 重复的问题)
        // 使用 LinkedHashMap 保持插入顺序，Key 为路径的字符串表示
        Map<String, Path> uniqueMap = new LinkedHashMap<>();
        for (Path p : originalEPC) {
            uniqueMap.putIfAbsent(p.toString(), p);
        }
        List<Path> uniquePaths = new ArrayList<>(uniqueMap.values());

        // 如果去重后依然很多，启用贪心算法剔除被包含的子路径
        return greedyReduce(uniquePaths);
    }

    private static LinkedList<Path> greedyReduce(List<Path> candidates) {
        // 1. 统计全集：找出所有路径总共覆盖了哪些 Edge Pairs
        // 使用 String 代表一个唯一的对边（Pair ID）
        Set<String> allCoveredPairs = new HashSet<>();
        // 记录每条路径覆盖了哪些 Pair
        Map<Path, Set<String>> pathCoverageMap = new HashMap<>();

        for (Path path : candidates) {
            Set<String> pairs = getEdgePairs(path);
            if (!pairs.isEmpty()) {
                pathCoverageMap.put(path, pairs);
                allCoveredPairs.addAll(pairs);
            }
        }

        // 如果没有产生任何对边（路径长度都很短），直接返回去重后的结果
        if (allCoveredPairs.isEmpty()) {
            return new LinkedList<>(candidates);
        }

        // 2. 贪心选择循环
        LinkedList<Path> reducedEPC = new LinkedList<>();
        Set<String> alreadyCovered = new HashSet<>();

        // 只要还有未覆盖的对边，就继续循环
        while (alreadyCovered.size() < allCoveredPairs.size()) {
            Path bestPath = null;
            int maxNewCoverage = -1;

            // 遍历所有候选路径，找一个"性价比"最高的
            // (即：能覆盖最多"当前尚未覆盖的对边"的路径)
            for (Path path : candidates) {
                Set<String> pairs = pathCoverageMap.get(path);
                if (pairs == null) continue;

                // 计算增量贡献
                int newContribution = 0;
                for (String pair : pairs) {
                    if (!alreadyCovered.contains(pair)) {
                        newContribution++;
                    }
                }

                if (newContribution > maxNewCoverage) {
                    maxNewCoverage = newContribution;
                    bestPath = path;
                }
            }

            // 如果找不到能提供新贡献的路径了（或者所有路径都被选完了），跳出
            if (bestPath == null || maxNewCoverage == 0) {
                break;
            }

            // 选中最佳路径
            reducedEPC.add(bestPath);
            alreadyCovered.addAll(pathCoverageMap.get(bestPath));

            // 优化：将被选中的路径从候选中移除（虽然不移除逻辑也没错，但移除能加速）
            candidates.remove(bestPath);
        }

        return reducedEPC;
    }

    /**
     * 辅助方法：提取一条路径中所有的对边 (Edge Pairs)
     * 一个对边由两个连续的 Transition 组成
     */
    private static Set<String> getEdgePairs(Path path) {
        Set<String> pairs = new HashSet<>();
        LinkedList<Transition> transitions = path.getTransitions();

        if (transitions.size() < 2) {
            return pairs; // 长度小于2没有对边
        }

        int currentState = path.getInitial();

        // 遍历 transitions，取 i 和 i+1 组成一对
        for (int i = 0; i < transitions.size() - 1; i++) {
            Transition t1 = transitions.get(i);
            Transition t2 = transitions.get(i + 1);

            // 生成 t1 的唯一签名
            String t1Sig = getTransitionSignature(currentState, t1);

            // t1 的目标是 t2 的源
            int midState = t1.getDest().getNumber();

            // 生成 t2 的唯一签名
            String t2Sig = getTransitionSignature(midState, t2);

            // 组合成对边签名: T1 -> T2
            pairs.add(t1Sig + "||" + t2Sig);

            // 更新当前状态为下一条边的源状态
            currentState = midState;
        }
        return pairs;
    }

    /**
     * 生成 Transition 的唯一字符串签名
     * 格式: SourceID-[Min-Max]->DestID
     */
    private static String getTransitionSignature(int sourceState, Transition t) {
        StringBuilder sb = new StringBuilder();
        sb.append(sourceState).append("-[").append(t.getMin());
        if (t.getMin() != t.getMax()) {
            sb.append("-").append(t.getMax());
        }
        sb.append("]->").append(t.getDest().getNumber());
        return sb.toString();
    }
}
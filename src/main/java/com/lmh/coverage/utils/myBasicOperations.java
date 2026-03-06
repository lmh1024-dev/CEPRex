package com.lmh.coverage.utils;

import com.lmh.coverage.edgePairCoverage;
import com.lmh.coverage.entity.Edge;
import com.lmh.coverage.entity.Path;
import com.lmh.coverage.entity.transitionDefine;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lmh.coverage.utils.CharPicker.pickBestChar;
import static com.lmh.coverage.utils.GoldenGenerator.generateGolden;
import static com.lmh.coverage.utils.RegexTokenMapper.*;
import static com.lmh.coverage.utils.RegexTokenMapper.restore;

public class myBasicOperations {
    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
    public static Automaton myComplement(Automaton automaton, Automaton attemptAutomaton){
        //定义死状态
        State dead = new State();
        Set<State> attemptAutomatonStates = attemptAutomaton.getStates();
        Set<State> states = automaton.getStates();
        Iterator<State> stateIterator = states.iterator();

        Set<transitionDefine> transitions = new HashSet<>();
        //获取边的转换
        while (stateIterator.hasNext()){
            State nextState = stateIterator.next();
            for (Transition transition : nextState.getTransitions()) {
                transitionDefine transitionDefine = new transitionDefine(transition);
                transitions.add(transitionDefine);
            }
        }
        //System.out.println(transitions);
        Set<transitionDefine> unionDefine = transitionDefine.getUnionDefine(transitions);
        //System.out.println(unionDefine);

        stateIterator = attemptAutomatonStates.iterator();
        while (stateIterator.hasNext()){
            State nextState = stateIterator.next();
            //Set<Transition> deadTransitions = new HashSet<>();
            for (transitionDefine next : unionDefine) {

                char minChar = '\u0000';
                char maxChar = '\u0000';
                if (next.getMax() == next.getMin()) {
                    if (!nextState.haveStep(next.getMin())) {
                        minChar = maxChar = next.getMin();
                    }
                } else {
                    for (int i = 0; next.getMin() + i <= next.getMax(); i++) {
                        if (!nextState.haveStep((char) (next.getMin() + i))) {
                            if (minChar == '\u0000') {
                                minChar = (char) (next.getMin() + i);
                            } else {
                                maxChar = (char) (next.getMin() + i);
                            }
                        }
                    }
                }
                if (minChar != '\u0000' && maxChar != '\u0000') {
                    Transition deadTransition = new Transition(minChar, maxChar, dead);
                    nextState.addTransition(deadTransition);
                }else if(minChar != '\u0000'){
                    Transition deadTransition = new Transition(minChar, dead);
                    nextState.addTransition(deadTransition);
                }

            }
        }

        for (State p : attemptAutomaton.getStates())
            p.setAccept(!p.isAccept());

        for (transitionDefine next : unionDefine) {
            dead.addTransition(new Transition(next.getMin(), next.getMax(), dead));
        }

        Automaton.setStateNumbers(attemptAutomaton.getStates());

        return attemptAutomaton;
    }

    public static List<Integer> dijkstraToSource(int begin, int end, int[][] adjMatrix) {

        int numNodes = adjMatrix.length;

        // 记录每个节点到 begin 节点的最短距离
        int[] distances = new int[numNodes];
        // 初始化距离为无穷大
        for (int i = 0; i < numNodes; i++) {
            distances[i] = Integer.MAX_VALUE;
        }
        distances[begin] = 0;

        // 记录已确定最短距离的节点
        boolean[] visited = new boolean[numNodes];

        Map<Integer, List<Integer>> paths = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            paths.put(i, new ArrayList<>());
        }

        for (int i = 0; i < numNodes; i++) {
            int minNode = -1;
            int minDistance = Integer.MAX_VALUE;

            for (int j = 0; j < numNodes; j++) {
                if (!visited[j] && distances[j] < minDistance) {
                    minNode = j;
                    minDistance = distances[j];
                }
            }

            if (minNode == -1) {
                break;
            }

            visited[minNode] = true;

            for (int j = 0; j < numNodes; j++) {
                if (adjMatrix[minNode][j] > 0) {
                    int newDistance = distances[minNode] + adjMatrix[minNode][j];
                    if (newDistance < distances[j]) {
                        distances[j] = newDistance;
                        paths.get(j).clear();
                        paths.get(j).addAll(paths.get(minNode));
                        paths.get(j).add(minNode);
                    }
                }
            }
        }

        return paths.get(end);
    }

    public static List<Integer> dijkstraToAccept(int[][] adjMatrix, boolean[] isAcceptState, int source) {
        int numNodes = adjMatrix.length;

        // 记录每个节点到源节点的最短距离
        int[] distances = new int[numNodes];
        // 初始化距离为无穷大
        for (int i = 0; i < numNodes; i++) {
            distances[i] = Integer.MAX_VALUE;
        }
        distances[source] = 0;

        // 记录已确定最短距离的节点
        boolean[] visited = new boolean[numNodes];

        Map<Integer, List<Integer>> paths = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            paths.put(i, new ArrayList<>());
        }

        for (int i = 0; i < numNodes; i++) {
            int minNode = -1;
            int minDistance = Integer.MAX_VALUE;

            for (int j = 0; j < numNodes; j++) {
                if (!visited[j] && distances[j] < minDistance) {
                    minNode = j;
                    minDistance = distances[j];
                }
            }

            if (minNode == -1) {
                break;
            }

            visited[minNode] = true;

            for (int j = 0; j < numNodes; j++) {
                if (adjMatrix[minNode][j] > 0) {
                    int newDistance = distances[minNode] + adjMatrix[minNode][j];
                    if (newDistance < distances[j]) {
                        distances[j] = newDistance;
                        paths.get(j).clear();
                        paths.get(j).addAll(paths.get(minNode));
                        paths.get(j).add(minNode);
                    }
                }
            }
        }

        if (isAcceptState[source]) {
            return List.of(source);
        }

        for (int i = 0; i < numNodes; i++) {
            if (isAcceptState[i] && distances[i]!= Integer.MAX_VALUE) {
                List<Integer> path = paths.get(i);
                path.add(i);
                return path;
            }
        }

        return new ArrayList<>();
    }

    public static int[][] automatonToG(Automaton automaton) {
        Set<State> states = automaton.getStates();
        int size = states.size();
        int[][] G = new int[size][size];
        Iterator<State> stateIterator = states.iterator();
        while (stateIterator.hasNext()){
            State state = stateIterator.next();
            int number = state.getNumber();
            Set<Transition> transitions = state.getTransitions();
            for(Transition transition:transitions){
                G[number][transition.getDest().getNumber()] = 1;
            }
        }
        return G;
    }

    public static boolean[] getAcceptState(Automaton automaton){
        Set<State> states = automaton.getStates();
        int size = states.size();
        boolean[] isAcceptState = new boolean[size];
        Iterator<State> stateIterator = states.iterator();
        while (stateIterator.hasNext()){
            State state = stateIterator.next();
            if(state.isAccept())
                isAcceptState[state.getNumber()] = true;
        }
        return isAcceptState;
    }

    public static HashSet<String> boardProcess(LinkedList<String> generatedStrings) {
        HashSet<String> stringsSet = new HashSet<>();
        int flag = 0;
        while (flag <= 1){
            for(String s:generatedStrings){
                if(s.equals("ε")){
                    stringsSet.add("");
                    continue;
                }
                StringBuilder stringBuilder = getStringBuilder(s, flag);
                stringsSet.add(String.valueOf(stringBuilder.reverse()));
            }
            flag++;
        }

        return stringsSet;
    }

    public static LinkedList<String> generateStrings(LinkedList<Path> coverList) {
        LinkedList<String> strings = new LinkedList<>();
        for(Path p:coverList){
            LinkedList<Transition> transitions = p.getTransitions();
            if(transitions.isEmpty()){
                //strings.add("ε");
                continue;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (Transition t:transitions){
                char c = t.getMin();


                // 如果是控制字符或不可见字符，转换为 \\uXXXX 格式
                if (c < 32 || (c >= 127 && c <= 160) || Character.isISOControl(c)|| !Character.isDefined(c)) {

                    stringBuilder.append(String.format("u%04x", (int) c));
                }else {
                    stringBuilder.append(c);
                }


                stringBuilder.append("~");
                //appendCharString(t.getMax(), stringBuilder);
                c = t.getMax();

                // 如果是控制字符或不可见字符，转换为 \\uXXXX 格式
                if (c < 32 || (c >= 127 && c <= 160) || Character.isISOControl(c)|| !Character.isDefined(c)) {

                    stringBuilder.append(String.format("u%04x", (int) c));
                } else {
                    stringBuilder.append(c);
                }

            }
            strings.add(stringBuilder.toString());
        }
        return strings;
    }

    private static boolean isInvalid(char c) {
        return c < 32 || (c >= 127 && c <= 160) || Character.isISOControl(c) || !Character.isDefined(c);
    }

    public static void bigAlphabetProcess(LinkedList<Path> coverList, char range){

        // 1. 创建一个临时列表存放需要新增的新路径，避免在遍历时修改原列表导致死循环或异常
        List<Path> originalPathsToAdd = new ArrayList<>();

        for (Path p : coverList) {
            LinkedList<Transition> transitions = p.getTransitions();
            if (transitions == null || transitions.isEmpty()) {
                continue;
            }

            boolean hasChanged = false;
            Path backupPath = null; // 用于延迟克隆

            ListIterator<Transition> it = transitions.listIterator();
            while (it.hasNext()) {
                Transition t = it.next();
                char min = t.getMin();
                char max = t.getMax();

                // 2. 检查是否需要处理
                if (isInvalid(min) || isInvalid(max)) {
                    char[] result = pickBestChar(min, max, range);

                    // 3. 只有当字符真的发生改变时，才进行处理
                    if (result[0] != min || result[1] != max) {
                        // 4. 延迟克隆：只有确定要修改当前路径时，才克隆备份原路径
                        if (!hasChanged) {
                            backupPath = p.deepClone();
                            hasChanged = true;
                        }
                        // 直接替换当前 Transition (O(1) 操作)
                        it.set(new Transition(result[0], result[1], t.getDest()));
                    }
                }
            }

            // 5. 如果路径被修改了，将备份的原始路径存入临时列表
            if (hasChanged && backupPath != null) {
                originalPathsToAdd.add(backupPath);
            }
        }

        // 6. 最后一次性把原始路径加回列表（这些路径不会再被本次循环处理，避免死循环）
        coverList.addAll(originalPathsToAdd);

    }

    public static void removeInvalid(LinkedList<Path> coverList){

        ListIterator<Path> pathListIterator = coverList.listIterator();
        while (pathListIterator.hasNext()) {
            Path next = pathListIterator.next();
            LinkedList<Transition> transitions = next.getTransitions();
            if (transitions == null || transitions.isEmpty()) {
                continue;
            }

            for (Transition t : transitions) {
                char min = t.getMin();
                char max = t.getMax();

                // 2. 检查是否需要处理
                if (isInvalid(min) || isInvalid(max)) {
                    pathListIterator.remove();
                    break;
                }
            }

        }


    }

    public static HashSet<String> processPath(LinkedList<String> generatedStrings) {
        HashSet<String> set = new HashSet<>();

        for (String input : generatedStrings) {
            // 1. 按照 ~ 物理分割
            // input: A~Z\u0000~\uffff -> parts: ["A", "Z\u0000", "\uffff"]
            String[] parts = input.split("~");

            StringBuilder minResult = new StringBuilder();
            StringBuilder maxResult = new StringBuilder();

            if(parts.length == 0)
                continue;
            // 第一个元素必然是第一个 min
            minResult.append(extractUnit(parts[0], true));

            // 中间的元素既包含前一个的 max，也包含后一个的 min
            for (int i = 1; i < parts.length - 1; i++) {
                String mid = parts[i];
                // mid 的开头是前一个范围的 max
                maxResult.append(extractUnit(mid, true));
                // mid 的剩余部分是后一个范围的 min
                minResult.append(extractUnit(mid, false));
            }

            // 最后一个元素必然是最后一个 max
            maxResult.append(extractUnit(parts[parts.length - 1], false));

            set.add(minResult.toString());
            //set.add(maxResult.toString());
        }
        return set;
    }


    public static HashSet<String> expandToMinMax(LinkedList<Path> EPC) {
        HashSet<String> resultSet = new HashSet<>();

        if(EPC == null)
            return resultSet;

        for (Path p : EPC) {
            if (p == null) {
                continue;
            }

            StringBuilder minBuilder = new StringBuilder();
            StringBuilder maxBuilder = new StringBuilder();

            LinkedList<Transition> transitions = p.getTransitions();

            for (Transition t: transitions){
                minBuilder.append(t.getMin());
            }

            resultSet.add(minBuilder.toString());
            // 如果你也想要 max 的组合，请取消下面的注释
            // resultSet.add(maxBuilder.toString());
        }

        return resultSet;
    }

    /**
     * 辅助方法：将字符串中的 Unicode 转义序列 (如 \u00e0) 转换为实际字符 (à)
     */
    private static String decodeUnicode(String raw) {
        if (raw == null || !raw.contains("\\u")) {
            return raw;
        }

        Matcher matcher = UNICODE_PATTERN.matcher(raw);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            try {
                // 1. 取出 \\u 后面的 4 位 16 进制数
                String hex = matcher.group(1);

                // 2. 解析为 int 并强转为 char
                int charCode = Integer.parseInt(hex, 16);
                String replacement = String.valueOf((char) charCode);

                // 3. [关键修复] 使用 quoteReplacement 防止 $ 和 \ 导致报错
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));

            } catch (NumberFormatException e) {
                // 如果解析失败（理论上正则限制了格式不会失败，但为了稳健），保持原样
                // 注意：appendReplacement 必须成对出现，这里如果跳过会有问题
                // 简单策略是：如果解析失败就不替换，但这需要复杂的逻辑
                // 鉴于正则已经限制了 [0-9a-fA-F]，这里基本不会抛异常
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 辅助方法：从片段中提取单元
     * isHead 为 true 提取开头的单元，为 false 提取结尾的单元
     */
    private static String extractUnit(String s, boolean isHead) {
        if (isHead) {
            // 如果开头是 uXXXX
            if (s.startsWith("u") && s.length() >= 5) return s.substring(0, 5);
            // 否则就是单个字符
            if (!s.isEmpty())
                return s.substring(0, 1);
            else
                return "";
        } else {
            // 如果结尾是 uXXXX (形如 ...u1234)
            if ( s.length() >= 5 && s.substring(s.length()-5).startsWith("u")) {
                return s.substring(s.length() - 5);
            }
            // 否则就是最后一个字符
            if (!s.isEmpty())
                return s.substring(s.length() - 1);
            else
                return "";
        }
    }

    public static void extendEPC(LinkedList<Path> EPC, Automaton automaton) {
        int[][] G = automatonToG(automaton);
        //获取isAcceptState，是否是接受状态
        boolean[] isAcceptState = getAcceptState(automaton);
        LinkedList<Integer> removeIndexes = new LinkedList<>();
        Iterator<Path> iterator = EPC.iterator();
        while(iterator.hasNext()){
            Path path = iterator.next();
            int j = EPC.indexOf(path);
            LinkedList<Transition> transitions = path.getTransitions();
            if(transitions.isEmpty())
                continue;
            int initial = automaton.getInitialState().getNumber();
            int end = transitions.getLast().getDest().getNumber();
            if(path.getInitial() != initial){
                //路径不从初始状态开始，要延申到初始状态
                Path newPath = new Path(initial);
                List<Integer> shortestPath = dijkstraToSource(initial, path.getInitial(), G);
                shortestPath.add(path.getInitial());
                for (int i = 1; i < shortestPath.size(); i++) {
                    State firstState = automaton.getStateByNumber(shortestPath.get(i - 1));
                    Transition transition = firstState.getTransitionByDest(shortestPath.get(i));
                    if(transition != null)
                        newPath.push(transition);
                    else
                        throw new RuntimeException("延申出现错误");
                }
                newPath.add(path);
                EPC.set(j, newPath);
            }
            if(!isAcceptState[end]){
                //路径不在接受状态结束，要延申到最近的接受状态
                Path newPath = EPC.get(j).deepClone();
                List<Integer> pathToAccept = dijkstraToAccept(G, isAcceptState, end);
                if(!pathToAccept.isEmpty()){
                    //此状态能够延申到接受状态
                    Integer lastNumber = pathToAccept.get(0);
                    pathToAccept.remove(0);
                    //StringBuilder s = new StringBuilder();
                    for(Integer dest:pathToAccept){
                        State lastState = automaton.getStateByNumber(lastNumber);
                        if(lastState == null) {
                            throw new RuntimeException("延申路径出现错误，找不到下一状态！");
                        }
                        else {
                            Transition transition = lastState.getTransitionByDest(dest);
                            if(transition != null)
                                newPath.push(transition);
                            else
                                throw new RuntimeException("延申出现错误");
                            lastNumber = dest;
                        }
                    }
                    EPC.set(j,newPath);
                }else{
                    //不能延申到接受状态，依次减少一条边，直到达到终态
                    LinkedList<Transition> newPathTransitions = newPath.getTransitions();
                    while (!isAcceptState[end]){
                        newPath.pop();
                        if(!newPathTransitions.isEmpty())
                            end = newPathTransitions.getLast().getDest().getNumber();
                        else
                            break;
                    }
                    if(!newPathTransitions.isEmpty() || isAcceptState[newPath.getInitial()])
                        EPC.set(j,newPath);
                    else
                        iterator.remove();
                }
            }

        }

    }

    public static void acceptExtendEPC(LinkedList<Path> EPC, Automaton automaton) {

        Set<Integer> acceptStatesNumber = new HashSet<>();
        Set<State> acceptStates = automaton.getAcceptStates();
        for (State acceptState : acceptStates)
            acceptStatesNumber.add(acceptState.getNumber());

        //已经覆盖的终态集合
        Set<Integer> acceptedStatesNumber = new HashSet<>();
        for(Path path:EPC){
            LinkedList<Transition> transitions = path.getTransitions();
            if(transitions.isEmpty())
                continue;
            State dest = transitions.getLast().getDest();
            acceptedStatesNumber.add(dest.getNumber());
        }
        /*if(acceptedStatesNumber.equals(acceptStatesNumber))
            return;*/
        LinkedList<Path> addList = new LinkedList<>();
        for(Path p:EPC){
            LinkedList<Transition> transitions = p.getTransitions();
            Path clone = p.deepClone();
            for(int j = transitions.size()-1;j>=0;j--){
                Transition t = transitions.get(j);
                int number = t.getDest().getNumber();
                if(/*!acceptedStatesNumber.contains(number) &&*/ acceptStatesNumber.contains(number)){
                    acceptedStatesNumber.add(number);
                    addList.add(clone.deepClone());
                }
                clone.pop();
            }
            if(clone.getTransitions().isEmpty() && !acceptedStatesNumber.contains(clone.getInitial()) && acceptStatesNumber.contains(clone.getInitial())){
                acceptedStatesNumber.add(clone.getInitial());
                addList.add(clone);
            }
        }
        EPC.addAll(addList);
    }

    public static Map<State, LinkedList<Edge>> generateEdgeMap(Automaton automaton){
        Map<State, LinkedList<Edge>> form = new HashMap<>();

        Set<State> states = automaton.getStates();

        for (State firstState : states) {
            LinkedList<Edge> edgePairs = new LinkedList<>();

            for (Transition transition : firstState.getTransitions()) {
                edgePairs.add(new Edge(transition,false));
            }

            form.put(firstState, edgePairs);
        }

        return form;
    }

    private static StringBuilder getStringBuilder(String s, int flag) {
        Stack<Character> stack = new Stack<>();
        char[] charArray = s.toCharArray();
        for(int i = 0;i<charArray.length;i++){
            if(charArray[i] == '~'){
                if(flag == 0){
                    //flag=0表示字符范围取最小
                    ++i;
                }else {
                    if(!stack.isEmpty()){
                        stack.pop();
                        stack.push(charArray[++i]);
                    }
                }
            }else
                stack.push(charArray[i]);
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (!stack.isEmpty()){
            stringBuilder.append(stack.pop());
        }
        return stringBuilder;
    }

    public static void OuntputNonMatchingString(String regex){


        Map<Character, String> tokenMap = new LinkedHashMap<>();

        String modifiedRegex = tokenize(regex, tokenMap);


        edgePairCoverage edgePairCoverage = new edgePairCoverage();

        RegExp regexp = new RegExp(modifiedRegex);
        Automaton automaton = regexp.toAutomaton();
        automaton.determinize();
        automaton.minimize();


        edgePairCoverage.setAutomaton(automaton);
        edgePairCoverage.setAttemptAutomaton(automaton);

        edgePairCoverage.setGoldenExample(generateGolden(regex));
        edgePairCoverage.generate();


        edgePairCoverage.setPositiveStrings(restore(edgePairCoverage.getPositiveStrings(), tokenMap));
        edgePairCoverage.setNegativeStrings(restore(edgePairCoverage.getNegativeStrings(), tokenMap));

        System.out.println(edgePairCoverage.getNegativeStrings());

    }
}

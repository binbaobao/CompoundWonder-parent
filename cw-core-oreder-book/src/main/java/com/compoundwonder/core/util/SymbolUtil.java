package com.compoundwonder.core.util;

import java.util.List;

public class SymbolUtil {


    private static final int BASE_OFFSET = 1000000;
    private static final String[] SYMBOL_CACHE = new String[2000001];
    private static final int[] SYMBOL_INDEX = new int[2000001];

    static {
        // 预热 1,000,000 到 2,000,000 之间的代码
        for (int i = 1000000; i <= 2000000; i++) {
            SYMBOL_CACHE[i] = formatToSixChars(i % 1000000);
        }
    }

    /**
     *
     * @param list
     */
    public void initAll(List<String> list) {
        for (String symbol : list) {
            int symbolToInt = SymbolUtil.fastSymbolToInt(symbol);
            SYMBOL_CACHE[symbolToInt] = symbol;
        }
    }

    public void index(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String symbol = list.get(i);
            int symbolToInt = SymbolUtil.fastSymbolToInt(symbol);
            SYMBOL_INDEX[symbolToInt] = i;
        }
    }

    public int getIndex(int symbolId) {
        return SYMBOL_INDEX[symbolId];
    }

    /**
     * 1. 核心方法：String -> int (InternalId)
     */
    public static int fastSymbolToInt(String symbol) {
        if (symbol == null || symbol.length() < 6) return -1;

        int result = (symbol.charAt(0) - '0') * 100000;
        result += (symbol.charAt(1) - '0') * 10000;
        result += (symbol.charAt(2) - '0') * 1000;
        result += (symbol.charAt(3) - '0') * 100;
        result += (symbol.charAt(4) - '0') * 10;
        result += (symbol.charAt(5) - '0');
        return result + BASE_OFFSET;
    }

    /**
     * 2. 路由方法：根据 InternalId 确定 Handler 索引
     * 逻辑：00/30 -> 深圳 (1, 3), 60/68 -> 上海 (0, 2)
     * 160-4,168-1,192-1 , 4+1+1=6
     * 100-3,130-3 ,3+3=6
     */
    public static int getHandlerIndex(int internalId) {
        if (internalId < 1000000) return -1;

        // 提取前两位前缀：1600519 -> 60
        int prefix = (internalId - BASE_OFFSET) / 10000;
        return switch (prefix) {
            case 60 -> 0; // 60xxxx -> Shard 2 (上证主板)  i/2500 0,1,2,3
            case 0 -> 1; // 00xxxx -> Shard 0 (深证主板) i/3333 0,1,2
            case 68 -> 2; // 68xxxx -> Shard 3 (科创板)    4  4
            case 30 -> 3; // 30xxxx -> Shard 1 (创业板)   i/3333+3 3,4,5
//            case 92 -> 4; // 68xxxx -> Shard 3 (北交所)    5  5
            default -> -1;
        };
    }

    /**
     * @param internalId 传入 1000062 这种格式
     * @param capacity   当前数组长度 (如 10000 或 5000)
     */
    public static int getIdxFromSymbolIdId(int internalId, int capacity) {
        // 1. 去掉偏移量，还原原始代码数字部分
        // 1000062 -> 62
        // 1300750 -> 300750
        int rawCode = internalId - BASE_OFFSET;

        // 2. 映射到数组下标
        // 如果 capacity 是 10000，取后 4 位: 62 % 10000 = 62, 300750 % 10000 = 750
        // 如果 capacity 是 5000，则: 750 % 5000 = 750
        return rawCode % capacity;
    }

    /**
     * 3. 辅助方法：int -> String
     */
    public static String intToSymbol(int internalId) {
        if (internalId < 1000000 || internalId >= SYMBOL_CACHE.length) return null;
        return SYMBOL_CACHE[internalId];
    }

    private static String formatToSixChars(int i) {
        char[] buf = new char[6];
        for (int j = 5; j >= 0; j--) {
            buf[j] = (char) ((i % 10) + '0');
            i /= 10;
        }
        return new String(buf);
    }

}

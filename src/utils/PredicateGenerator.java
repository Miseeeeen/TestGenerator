package utils;


public class PredicateGenerator {
    public static int counter = 0;

    /**
     * 随机生成general的布尔表达式
     * @return
     */
    public static String getPredicate() {
        String pr = expression();
        return pr + ";";
    }

    private static String expression() {
        if (getRandomInt(0, 1) == 0)
            return term() + "&" + term();
        else
            return term() + "|" + term();
    }

    private static String term() {
        if (getRandomInt(0, 1) == 0)
            return primary();
        else
            return "!" + primary();
    }

    private static String primary() {
        if (getRandomInt(0, 1) != 0)
            return "(" + expression() + ")";
        else
            return "A" + getRandomInt(0, 100);
    }

    private static int getRandomInt(int min, int max) {
        return (int) (min + Math.random() * (max - min + 1));
    }

}

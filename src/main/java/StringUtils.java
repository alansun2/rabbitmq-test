public class StringUtils {

    /**
     * 字符串是否为空
     *
     * @param string
     * @return
     */
    public static boolean isEmpty(String... string) {
        for (String str : string) {
            if (str == null || "".equals(str.trim()) || str.equals("null")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim()) || str.equals("null");
    }
}

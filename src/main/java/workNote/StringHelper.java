package workNote;

import com.fingard.constant.Format;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

public class StringHelper {
    public static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public static String valueOf(Object o) {
        return o == null ? "" : o.toString();
    }

    public static String defaultIfEmpty(String s, String def) {
        return isNullOrEmpty(s) ? def : s;
    }

    //判断是否为null或空
    public static boolean isNullOrEmpty(String p_str) {
        if (p_str != null && p_str.length() > 0) {
            return false;
        }
        return true;
    }

    //取空白字符开始位置
    public static int indexOfBlank(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (isBlank(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    //判断是否为空白字符
    public static boolean isBlank(char p_char) {
        if (p_char == ' ' || p_char == '\t' || p_char == '\r' || p_char == '\n') {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isBlankStr(String p_str) {
        if (p_str == null) {
            return true;
        }
        int blankCount = 0;
        for (int i = 0; i < p_str.length(); i++) {

            char p_char = p_str.charAt(i);

            byte bb = (byte) p_char;

            if (p_char == ' ' || p_char == '\t' || p_char == '\r' || p_char == '\n' || bb == -1) {
                blankCount++;
            } else {
                return false;
            }
        }
        if (blankCount == p_str.length()) {
            return true;
        }
        return false;
    }

    //非空
    public static boolean hasAnyChar(String p_str) {
        if (p_str != null && p_str.length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 除以100，返回两位小数
     */
    public static String amountDivide100(String p_cent) {
        BigDecimal tmpDec = new BigDecimal(p_cent);
        tmpDec = tmpDec.divide(new BigDecimal("100"));
        return Format.DecimalAmtFormat.format(tmpDec);
    }

    /**
     * 除以100，返回两位小数
     */
    public static String amtDiv100RetZeroIfNullOrEmpty(String p_cent) {
        if (StringHelper.isNullOrEmpty(p_cent)) {
            return "0.00";
        }
        BigDecimal tmpDec = new BigDecimal(p_cent);
        tmpDec = tmpDec.divide(new BigDecimal("100"));
        return Format.DecimalAmtFormat.format(tmpDec);
    }

    /**
     * 乘以100，返回整数
     */
    public static String amountMultiply100(String p_yuan) {
        BigDecimal tmpDecAmt = new BigDecimal(p_yuan);
        tmpDecAmt = tmpDecAmt.multiply(new BigDecimal("100"));
        return tmpDecAmt.toBigInteger().toString();
    }

    /**
     * 去掉最后一个目录分隔符
     */
    public static String trimEndFileSp(String p_path) {
        if (p_path != null && p_path.length() > 0) {
            if (p_path.endsWith("/")) {
                return p_path.substring(0, p_path.length() - 1);
            } else if (p_path.endsWith("\\")) {
                return p_path.substring(0, p_path.length() - 1);
            }
        }
        return p_path;
    }

    /**
     * 如果第一个字符是目录分隔符，则去掉
     */
    public static String trimStartFileSp(String p_path) {
        if (p_path != null && p_path.length() > 0) {
            if (p_path.startsWith("/")) {
                return p_path.substring(1, p_path.length());
            } else if (p_path.startsWith("\\")) {
                return p_path.substring(1, p_path.length());
            }
        }
        return p_path;
    }

    /**
     * 替换文件分隔符为本地环境的文件分隔符
     */
    public static String replaceFileSp(String p_path) {
        if (p_path != null && p_path.length() > 0) {
            p_path = p_path.replace("/", File.separator);
            p_path = p_path.replace("\\", File.separator);
        }
        return p_path;
    }

    public static String join(String p_separator, ArrayList<String> p_arr) {
        if (p_arr == null || p_arr.size() <= 0) {
            return "";
        }
        StringBuilder tmpSb = new StringBuilder();
        for (int i = 0; i < p_arr.size(); i++) {
            if (i > 0) {
                tmpSb.append(p_separator);
            }
            tmpSb.append(p_arr.get(i));
        }
        return new String(tmpSb);
    }

    public static String joinNotEmpty(String p_separator, String... p_values) {
        if (p_values == null) {
            return "";
        }
        StringBuilder tmpSb = new StringBuilder();
        for (int i = 0; i < p_values.length; i++) {
            if (!isNullOrEmpty(p_values[i])) {
                if (tmpSb.length() > 0) {
                    tmpSb.append(p_separator);
                }
                tmpSb.append(p_values[i]);
            }
        }
        return new String(tmpSb);
    }

    public static String getFirstNotEmpty(String... p_values) {
        if (p_values == null) {
            return "";
        }
        for (int i = 0; i < p_values.length; i++) {
            if (!isNullOrEmpty(p_values[i])) {
                return p_values[i];
            }
        }
        return "";
    }

    public static String getFirstNotEmptyTrim(String... p_values) {
        if (p_values == null) {
            return "";
        }
        for (int i = 0; i < p_values.length; i++) {
            if (!isNullOrEmpty(p_values[i])) {
                if (p_values[i].trim().length() > 0) {
                    return p_values[i];
                }
            }
        }
        return "";
    }

    /**
     * 转全角的函数(SBC case)
     * 全角空格为12288，半角空格为32
     * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     *
     * @return 返回全角字符
     */
    public static String convertToSBC(String input) {
        if (isNullOrEmpty(input)) {
            return "";
        }
        //半角转全角：
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) {
                c[i] = (char) 12288;
                continue;
            }
            if (c[i] < 127)
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    /**
     * 转半角的函数(DBC case)
     * 全角空格为12288，半角空格为32
     * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     *
     * @return 返回半角字符
     */
    public static String convertToDBC(String input) {
        if (isNullOrEmpty(input)) {
            return "";
        }
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    public static String padRightGBKByte(String p_oriStr, int p_maxLen, char p_padChar) throws UnsupportedEncodingException {
        return padByByte(p_oriStr, "gbk", p_maxLen, p_padChar, 'R');
    }

    /**
     * 填充字符
     *
     * @throws UnsupportedEncodingException
     */
    public static String padByByte(String p_oriStr, String p_charset, int p_maxLen, char p_padChar, char p_FillDirection) throws UnsupportedEncodingException {
        byte[] bytesOri = p_oriStr.getBytes(p_charset);
        StringBuilder retSb = new StringBuilder();
        if (p_FillDirection != 'L' && p_FillDirection != 'l') {
            retSb.append(p_oriStr);
        }
        int fillLen = p_maxLen - bytesOri.length;
        if (fillLen > 0) {
            for (int i = 0; i < fillLen; i++) {
                retSb.append(p_padChar);
            }
        }
        if (p_FillDirection == 'L' || p_FillDirection == 'l') {
            retSb.append(p_oriStr);
        }
        return retSb.toString();
    }

    public static String padLeftGBKByte(String p_oriStr, int p_maxLen, char p_padChar) throws UnsupportedEncodingException {
        return padByByte(p_oriStr, "gbk", p_maxLen, p_padChar, 'L');
    }

    public static String padLeftUTF8Byte(String p_oriStr, int p_maxLen, char p_padChar) throws UnsupportedEncodingException {
        return padByByte(p_oriStr, "utf-8", p_maxLen, p_padChar, 'L');
    }

    public static String padRightUTF8Byte(String p_oriStr, int p_maxLen, char p_padChar) throws UnsupportedEncodingException {
        return padByByte(p_oriStr, "utf-8", p_maxLen, p_padChar, 'R');
    }

    /**
     * 左补
     */
    public static String padLeft(String p_oriStr, int p_maxLen, char p_padChar) {
        StringBuilder retSb = new StringBuilder(p_maxLen);
        int fillLen = p_maxLen - p_oriStr.length();
        if (fillLen > 0) {
            for (int i = 0; i < fillLen; i++) {
                retSb.append(p_padChar);
            }
        } else if (fillLen < 0) {
            return p_oriStr;
        }
        retSb.append(p_oriStr);
        return retSb.toString();
    }

    /**
     * 右补
     */
    public static String padRight(String p_oriStr, int p_maxLen, char p_padChar) {
        StringBuilder retSb = new StringBuilder(p_maxLen);
        retSb.append(p_oriStr);
        int fillLen = p_maxLen - p_oriStr.length();
        if (fillLen > 0) {
            for (int i = 0; i < fillLen; i++) {
                retSb.append(p_padChar);
            }
        } else if (fillLen < 0) {
            return p_oriStr;
        }
        return retSb.toString();
    }

    public static String bytesToHexString(byte[] p_bytes) {
        if (p_bytes == null || p_bytes.length <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p_bytes.length; i++) {
            int each = p_bytes[i] & 0xFF;
            String eachHex = Integer.toHexString(each);
            if (eachHex.length() < 2) {
                sb.append(0);
            }
            sb.append(eachHex);
        }
        return sb.toString();
    }

    /**
     * 按GBK字节截取字符串，中文字不拆分
     */
    public static String subStringByGBKByte(String p_oriStr, int p_maxByteLen) throws UnsupportedEncodingException {
        return subStringByByte(p_oriStr, "gbk", p_maxByteLen);
    }

    /**
     * 按字节截取字符串，中文字不拆分
     *
     * @throws UnsupportedEncodingException
     */
    public static String subStringByByte(String p_oriStr, String p_charset, int p_maxByteLen) throws UnsupportedEncodingException {
        if (StringHelper.isNullOrEmpty(p_oriStr)) {
            return "";
        }
        byte[] bytesOri = p_oriStr.getBytes(p_charset);
        if (bytesOri.length <= p_maxByteLen) {
            return p_oriStr;
        }
        int tmpByteCount = 0;
        for (int i = 0; i < p_oriStr.length(); i++) {
            tmpByteCount += String.valueOf(p_oriStr.charAt(i)).getBytes(p_charset).length;
            if (tmpByteCount > p_maxByteLen) {
                if (i > 0) {
                    return p_oriStr.substring(0, i);
                } else {
                    return "";
                }
            }
        }
        return p_oriStr;
    }

    /**
     * 拆分出查询字符串，返回键值对，查询字符串如a=b&c=d
     * @throws UnsupportedEncodingException
     */
    public static HashMap<String, String> splitQryString(String pValue) throws UnsupportedEncodingException {
        return splitQryStringDecode(pValue, "", false, false);
    }
    
    public static HashMap<String, String> splitQryString(String pValue, boolean pKeyLowerCase) throws UnsupportedEncodingException {
    	return splitQryStringDecode(pValue, "", pKeyLowerCase, false);
    }

    /**拆分查询字符串，按&分隔，再按=分隔，提取key-value
     * @param pValue 值，待拆分的查询字符串
     * @param pCharset 字符集，url decode，
     * @param pKeyLowerCase 是否将key转小写
     * @param pDecode 是否url decode
     * */
    public static HashMap<String, String> splitQryStringDecode(String pValue, String pCharset, boolean pKeyLowerCase, boolean pDecode) throws UnsupportedEncodingException {
        HashMap<String, String> map = new HashMap<String, String>();
        if(StringHelper.isNullOrEmpty(pValue)){
        	return map;
        }
        String[] spValues = pValue.split("&");
        for (int i = 0; i < spValues.length; i++) {
            int idx = spValues[i].indexOf("=");
            if (idx > 0) {
                String kk = spValues[i].substring(0, idx);
                if (pKeyLowerCase) {
                    kk = kk.toLowerCase();
                }
                String vv = spValues[i].substring(idx + 1);
                if(pDecode){
                	vv = URLDecoder.decode(vv, pCharset);
                }
                map.put(kk, vv);
            } else {
                map.put(spValues[i], "");
            }
        }
        return map;
    }

    public static String letterToNum(String input) {
        String reg = "[a-zA-Z]";
        StringBuffer strBuf = new StringBuffer();
        if (null != input && !"".equals(input)) {
            for (char c : input.toCharArray()) {
                if (String.valueOf(c).matches(reg)) {
                    strBuf.append(c - 64);
                } else {
                    strBuf.append(c);
                }
            }
            return strBuf.toString();
        } else {
            return input;
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExt(String p_FileName) {
        int tmpIndexDot = p_FileName.lastIndexOf('.');
        if (tmpIndexDot >= 0) {
            if (tmpIndexDot < p_FileName.length() - 1) {
                return p_FileName.substring(tmpIndexDot + 1);
            }
        }
        return "";
    }

    /**
     * 提取文件名和扩展名
     */
    public static String[] splitFileNameAndExt(String p_FileName) {
        String[] retString = new String[2];
        String p_OutName = "";
        String p_OutExt = "";
        int tmpIndexDot = p_FileName.lastIndexOf('.');
        if (tmpIndexDot >= 0) {
            p_OutName = p_FileName.substring(0, tmpIndexDot);
            if (tmpIndexDot < p_FileName.length() - 1) {
                p_OutExt = p_FileName.substring(tmpIndexDot + 1);
            }
        } else if (tmpIndexDot < 0) {
            p_OutName = p_FileName;
        }
        int tmpIndexSlash1 = p_FileName.lastIndexOf('/');
        int tmpIndexSlash2 = p_FileName.lastIndexOf('\\');
        int tmpIndexSlash = tmpIndexSlash1 > tmpIndexSlash2 ? tmpIndexSlash1 : tmpIndexSlash2;
        if (tmpIndexSlash >= 0 && tmpIndexSlash < p_OutName.length() - 1) {
            p_OutName = p_OutName.substring(tmpIndexSlash + 1);
        } else if (tmpIndexSlash >= 0 && tmpIndexSlash == p_OutName.length() - 1) {
            p_OutName = "";
        }
        retString[0] = p_OutName;
        retString[1] = p_OutExt;
        return retString;
    }

    /**
     * 提取文件名，xxx.xxx
     */
    public static String getFileName(String p_FileName) {
        int tmpIndexSlash1 = p_FileName.lastIndexOf('/');
        int tmpIndexSlash2 = p_FileName.lastIndexOf('\\');
        int tmpIndexSlash = tmpIndexSlash1 > tmpIndexSlash2 ? tmpIndexSlash1 : tmpIndexSlash2;
        if (tmpIndexSlash >= 0 && tmpIndexSlash < p_FileName.length() - 1) {
            return p_FileName.substring(tmpIndexSlash + 1);
        }
        return p_FileName;
    }

    public static String getDirPathUsingSplitGiveUpLastOne(String p_String, int p_StepLen) {
        String[] tmpDirectories = splitByStepLen(p_String, p_StepLen);
        tmpDirectories[tmpDirectories.length - 1] = "";//放弃最后一个
        String retStr = join(File.separator, tmpDirectories);
        retStr = trimEnd(retStr, "\\");
        return retStr;
    }

    /**
     * 按固定长度拆分字符串，返回数组
     */
    public static String[] splitByStepLen(String p_String, int p_StepLen) {
        int tmpQuotient = p_String.length() / p_StepLen;
        int tmpRemainder = p_String.length() % p_StepLen;
        if (tmpRemainder > 0) {
            tmpQuotient++;
        }
        String[] tmpRetArray = new String[tmpQuotient];
        int tmpArrayIndex = 0;
        int tmpFromIndex = 0;
        int tmpToIndexOpen = 0;//截止索引，开区间
        while (tmpToIndexOpen < p_String.length()) {
            tmpToIndexOpen = tmpFromIndex + p_StepLen;
            if (tmpToIndexOpen > p_String.length()) {
                tmpToIndexOpen = p_String.length();
            }
            tmpRetArray[tmpArrayIndex] = p_String.substring(tmpFromIndex, tmpToIndexOpen);
            tmpArrayIndex++;
            tmpFromIndex = tmpToIndexOpen;
        }
        return tmpRetArray;
    }

    public static String join(String p_separator, String... p_values) {
        if (p_values == null) {
            return "";
        }
        StringBuilder tmpSb = new StringBuilder();
        for (int i = 0; i < p_values.length; i++) {
            if (i > 0) {
                tmpSb.append(p_separator);
            }
            tmpSb.append(p_values[i]);
        }
        return new String(tmpSb);
    }

    public static String join(String p_separator, int p_startIndex, String... p_values) {
        if (p_values == null) {
            return "";
        }
        StringBuilder tmpSb = new StringBuilder();
        for (int i = p_startIndex; i < p_values.length; i++) {
            if (i > p_startIndex) {
                tmpSb.append(p_separator);
            }
            tmpSb.append(p_values[i]);
        }
        return new String(tmpSb);
    }

    public static String trimEnd(String p_value, String p_endStr) {
        if (isNullOrEmpty(p_value) || isNullOrEmpty(p_endStr)) {
            return p_value;
        }
        int tmpIndex = p_value.length() - 1;
        int tmpChkLen = p_endStr.length();
        int tmpChkIndex = tmpChkLen - 1;
        while (tmpIndex >= tmpChkIndex) {
            int tmpChkCount = 0;
            for (int i = 0; i < tmpChkLen; i++) {

                //System.out.println(p_value.charAt(tmpIndex-i));
                //System.out.println(p_endStr.charAt(tmpChkIndex-i));

                if (p_value.charAt(tmpIndex - i) == p_endStr.charAt(tmpChkIndex - i)) {
                    tmpChkCount++;
                } else {
                    break;
                }
            }
            if (tmpChkCount == tmpChkLen) {
                tmpIndex = tmpIndex - tmpChkLen;
            } else {
                break;
            }
        }
        if (tmpIndex < p_value.length() - 1) {
            return p_value.substring(0, tmpIndex + 1);
        } else {
            return p_value;
        }
    }

    public static String trimStartAndEnd(String p_value, String p_trimStr) {
        String retValue = trimStart(p_value, p_trimStr);
        retValue = trimEnd(retValue, p_trimStr);
        return retValue;
    }

    public static String trimStart(String p_value, String p_startStr) {
        if (StringHelper.isNullOrEmpty(p_value) || StringHelper.isNullOrEmpty(p_startStr)) {
            return p_value;
        }
        String retValue = p_value;
        while (retValue.length() >= p_startStr.length()) {
            boolean isMatch = true;
            for (int i = 0; i < p_startStr.length(); i++) {
                if (retValue.charAt(i) != p_startStr.charAt(i)) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) {
                retValue = retValue.substring(p_startStr.length());
            } else {
                break;
            }
        }
        return retValue;
    }

    public static int convertStrToIntIgnoreEmptyOrNull(String p_value) {
        if (StringHelper.isNullOrEmpty(p_value)) {
            return 0;
        } else {
            return Integer.parseInt(p_value);
        }
    }

    public static boolean isNumber(String str) {
        //采用正则表达式的方式来判断一个字符串是否为数字，这种方式判断面比较全
        //可以判断正负、整数小数
        boolean isInt = Pattern.compile("^-?[1-9]\\d*$").matcher(str).find();
        boolean isDouble = Pattern.compile("^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)$").matcher(str).find();
        return isInt || isDouble;
    }

    //首字母转小写
    public static String toLowerCaseFirstOne(String s){
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }
    //首字母转大写
    public static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }


    /**
     * @author
     * Map参数格式化为 key按照Hash排序  key1=value1&key2=value2……
     *
     * @param tmpMap
     * @return
     */
    public static String mapToString(Map<String, Object> tmpMap, boolean isTrimEnd) {
        List<String> keys = new ArrayList<String>(tmpMap.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (!StringHelper.isNullOrEmpty(String.valueOf(tmpMap.get(key)))) {
                sb.append(key).append("=");
                sb.append(tmpMap.get(key));
                sb.append("&");
            }
        }
        if (isTrimEnd) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static HashSet<String> splitToHashSet(String pValue, String pSpStr){
    	HashSet<String> hash = new HashSet<String>();
    	if(StringHelper.hasAnyChar(pValue)){
    		String[] vs = pValue.split(pSpStr);
    		for(int i=0;i<vs.length;i++){
    			if(!hash.contains(vs[i])){
    				hash.add(vs[i]);
    			}
    		}
    	}
    	return hash;
    }

}

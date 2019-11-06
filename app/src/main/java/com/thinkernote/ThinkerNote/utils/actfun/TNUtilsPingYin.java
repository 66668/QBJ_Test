package com.thinkernote.ThinkerNote.utils.actfun;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;



public class TNUtilsPingYin {
	/**
	 * 将字符串中的中文转化为拼音,其他字符不变
	 * 
	 * @param inputString
	 * @return
	 */

	/** 
	 * (1).HanyuPinyinOutputFormat,定义汉语拼音的输出形式.
	 * 
	 * (2)setCaseType(HanyuPinyinCaseTy ag0)  定义汉语拼音的大小写
	 * HanyuPinyinCaseType:
	 * 1、LOWERCASE	小写
	 * 2、UPPERCASE	大写
	 *
	 * (3).HanyuPinyinToneType,定义音调的显示方式.如:
	 * WITH_TONE_MARK dǎ ,带音调
	 * WITH_TONE_NUMBER da3 ,带音调,用12345表示平上去入和轻声
	 * WITHOUT_TONE da ,不带音调
	 * 
	 * (4).HanyuPinyinVCharType,定义'ü' 的显示方式.如:
	 * WITH_U_AND_COLON u: ,u加两点表示,如律师表示为lu:shi
	 * WITH_V v ,用字母v表示,这个用搜狗输入法的人想必有些印象.
	 * WITH_U_UNICODE ü
	 * 
	 * (5).input[i]).matches("[\\u4E00-\\u9FA5]+"),这个用来判断是否为中文的.
	 * 
	 * (6).PinyinHelper.toHanyuPinyinStringArray(input[i], format),这个返回单字的拼音字符串数组.
	 * 如果音调类型为WITH_TONE_NUMBER的话,"张",将返回"zhang1","李",会返回"li4".
	 */
	
	public static String getPingYin(String inputString) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();
		String output = "";

		try {
			for (int i = 0; i < input.length; i++) {
				if (java.lang.Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
					output += temp[0];
				} else
					output += java.lang.Character.toString(input[i]);
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toLowerCase();
	}
}

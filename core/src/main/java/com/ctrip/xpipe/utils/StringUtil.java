package com.ctrip.xpipe.utils;

/**
 * @author wenchao.meng
 *
 * 2016年3月28日 下午6:46:26
 */
public class StringUtil {

	public static String join(String split, Object ...args){
		
		String []tmp = new String[args.length];
		int i=0;
		for(Object arg :args){
			if(arg != null){
				tmp[i++] = arg.toString();
			}else{
				tmp[i++] = null;
			}
		}
		return join(split, tmp);
	}

	
	public static String join(String split, String ...args){
		
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for(String arg : args){
			if(arg != null){
				
				if( i > 0 ){
					sb.append(split);
				}
				sb.append(arg);
				i++;
			}
		}
		return sb.toString();
	}
	
	public static boolean isEmpty(String str){
		return str == null || str.trim().length() == 0;
	}

}

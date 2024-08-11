package com.example.typeracer;

public class GetName {
	
	public static String get(String message) {
		String ans = "";
		
		int i = 7;
		
		while(message.charAt(i)!=' ') {
			ans+=message.charAt(i);
			i++;
		}
		
		return ans;
	}
	
	public static int getBestWPM(String s) {
		
		int i = s.length()-4;
		
		String temp = "";
		
		while(s.charAt(i)!=' ') {
			temp = s.charAt(i)+temp;
		}
		
		return Integer.parseInt(temp);
		
	}

}

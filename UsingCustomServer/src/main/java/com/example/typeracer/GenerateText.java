package com.example.typeracer;

import java.util.Random;

public class GenerateText {
	
	public static String get() {
		String[] texts = {
	            "The quick brown fox jumps over the lazy dog.",
	            "Java programming is fun and versatile.",
	            "Artificial Intelligence is transforming the world.",
	            "Space exploration opens new frontiers for humanity.",
	            "Music can influence our emotions and behaviors.",
	            "Healthy eating leads to a better lifestyle.",
	            "Technology is rapidly advancing every day.",
	            "Reading books can expand your knowledge and imagination.",
	            "Traveling allows you to experience new cultures.",
	            "Exercise is essential for maintaining good health.",
	            "The internet connects people from all over the globe.",
	            "Sustainable living helps protect our planet.",
	            "Art and creativity enrich our lives.",
	            "Learning new languages can be a rewarding challenge.",
	            "Innovation drives progress in many fields.",
	            "History teaches us valuable lessons.",
	            "Coding skills are highly sought after in today's job market.",
	            "Philosophy encourages deep thinking about life and existence.",
	            "Good communication is key to successful relationships.",
	            "Nature provides us with beauty and resources.",
	            "Education empowers individuals and communities.",
	            "Teamwork often leads to better results.",
	            "Critical thinking is an essential skill.",
	            "Climate change is a pressing global issue.",
	            "Volunteering can make a positive impact on society.",
	            "Financial literacy is important for managing personal finances.",
	            "Meditation can improve mental well-being.",
	            "Engineering solutions can solve complex problems.",
	            "Science helps us understand the world around us.",
	            "Human rights are fundamental to dignity and freedom."
	        };
		
		Random rand = new Random();
		
		int index = rand.nextInt(30);
		
		return texts[index];
	}

}

package greeter.impl;

import java.util.Random;

import greeter.api.GreeterInterface;

public class GreeterImplementation implements GreeterInterface {

	String[] greetings = new String[]{"Hello again", "Hi there", "Nice to meet you", "How are you" };
	
	@Override
	public String greet(String name) {
		Random r = new Random(System.currentTimeMillis());
		int index = r.nextInt(greetings.length);
		
		return greetings[index]+", "+name;
	}

}

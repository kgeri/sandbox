package org.ogreg;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HelloService {
	
	public String greetMe() {
		return "Bonjour";
	}
}

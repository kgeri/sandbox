package org.ogreg;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/hello")
public class HelloController {
	private final HelloService helloService;

	@Logged
	public HelloController(HelloService helloService) {
		this.helloService = helloService;
	}

	@GET
	@Logged
	public String hello() {
		return helloService.greetMe() + " World!";
	}
}

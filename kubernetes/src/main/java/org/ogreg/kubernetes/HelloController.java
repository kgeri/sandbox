package org.ogreg.kubernetes;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/hello")
public class HelloController {

	@Get
	public String index() {
		return "Yo.";
	}
}

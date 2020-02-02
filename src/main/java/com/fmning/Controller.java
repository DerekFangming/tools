package com.fmning;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
	
	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("haha1");
	}

}

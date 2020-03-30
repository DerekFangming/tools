package com.tools.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {
	
	@GetMapping("/test")
	public ResponseEntity<String> test() throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("npx.cmd heic-cli < D:/Github/IMG_3008.heic > D:/Github/4.jpg");
		pr.waitFor();
		return ResponseEntity.ok("haha1");
	}

}

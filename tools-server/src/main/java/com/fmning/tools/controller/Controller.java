package com.fmning.tools.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fmning.tools.domain.Image;
import com.fmning.tools.repository.PostRepo;
import com.fmning.tools.service.PostService;
import com.fmning.tools.service.QueryService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
//import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class Controller {

	private final PostService postService;
	private final PostRepo postRepo;
	private final QueryService queryService;

	@GetMapping("/whoami")
	@PreAuthorize("hasAuthority('USER')")
	public String whoami(String name) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		return "";
	}

	@GetMapping("/whoami1")
	@PreAuthorize("hasAuthority('SSO')")
	public String whoami1(String name) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		return "";
	}

	@GetMapping("/user/me")
	public Principal getPrincipal(@RequestHeader("Authorization") String token) {
		return () -> "user!";
	}
	
//	@GetMapping("/test")
//	public ResponseEntity<Dto> test() throws Exception {
//
//
//
//		//System.out.println(getUsernamePasswordHash("slpt.support", "b7gwZK41HBim7mJnMNst"));
//
//		System.out.println(encodeString("123"));
//		return ResponseEntity.ok(new Dto());
//	}

//	String getUsernamePasswordHash(String username, String password) throws Exception {
//		return encodeString(password + encodeString(username));
//	}

//	String encodeString(String string) throws Exception {
//		MessageDigest md = MessageDigest.getInstance("SHA-256");
//		md.update(string.getBytes("UTF-8"));
//		return new String(Hex.encode(md.digest()));
//	}

//	@GetMapping("/login")
//	public ResponseEntity login() {
//		return ResponseEntity.ok().build();
//	}

	@Data
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private class Dto {
		String string = "a";
		String hidden = null;
		int integer = 1;
		boolean bool = false;
	}
	@GetMapping("/api/fpga-input")
	public List<Long> fpgaInput() throws Exception {

		Resource resource = new ClassPathResource("snap.txt");

		File file = resource.getFile();
		List<String> allLines = Files.readAllLines(Paths.get(file.getPath()));
		return allLines.stream().map(Long::parseLong).collect(Collectors.toList());
	}

	@PostMapping("/webhook")
	public ResponseEntity create(@RequestHeader Map<String, String> headers, @RequestBody Map<String, Object> body) {
		System.out.println("========================");
		System.out.println(Arrays.toString(headers.entrySet().toArray()));
		System.out.println();
		System.out.println();
		System.out.println("========================");
		System.out.println(Arrays.toString(body.entrySet().toArray()));
		return ResponseEntity.ok().build();
	}


}

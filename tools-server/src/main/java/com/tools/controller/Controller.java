package com.tools.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tools.repository.PostRepo;
import com.tools.service.PostService;
import com.tools.service.QueryService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class Controller {

	private final PostService postService;
	private final PostRepo postRepo;
	private final QueryService queryService;

	@GetMapping("/whoami")
	@PreAuthorize("hasRole('USER')")
	public String whoami(String name) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		return "";
	}

	@GetMapping("/whoami1")
	@PreAuthorize("hasRole('SSO')")
	public String whoami1(String name) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		return "";
	}

	@GetMapping("/user/me")
	public Principal getPrincipal(@RequestHeader("Authorization") String token) {
		return () -> "user!";
	}
	
	@GetMapping("/test")
	public ResponseEntity<Dto> test() throws Exception {



		//System.out.println(getUsernamePasswordHash("slpt.support", "b7gwZK41HBim7mJnMNst"));

		System.out.println(encodeString("123"));
		return ResponseEntity.ok(new Dto());
	}

	String getUsernamePasswordHash(String username, String password) throws Exception {
		return encodeString(password + encodeString(username));
	}

	String encodeString(String string) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(string.getBytes("UTF-8"));
		return new String(Hex.encode(md.digest()));
	}

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

}

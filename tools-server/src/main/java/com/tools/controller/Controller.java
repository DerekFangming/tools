package com.tools.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tools.repository.PostRepo;
import com.tools.service.PostService;
import com.tools.service.QueryService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class Controller {

	private final PostService postService;
	private final PostRepo postRepo;
	private final QueryService queryService;
	
	@GetMapping("/test")
	public ResponseEntity<Dto> test() throws IOException, InterruptedException {

		System.out.println(111);
		return ResponseEntity.ok(new Dto());
	}

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

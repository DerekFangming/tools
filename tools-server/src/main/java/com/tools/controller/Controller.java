package com.tools.controller;

import com.tools.repository.PostRepo;
import com.tools.service.PostService;
import com.tools.service.QueryService;
import lombok.RequiredArgsConstructor;
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
	public ResponseEntity<String> test() throws IOException, InterruptedException {


//		Runtime rt = Runtime.getRuntime();
//		Process pr = rt.exec("npx.cmd heic-cli < D:/Github/IMG_3008.heic > D:/Github/4.jpg");
//		pr.waitFor();


//		Post post = Post.builder()
//				.title("test title")
//				.imgUrls("urls")
//				.attachment("attachment")
//				.htmlType(HtmlReaderType.JSOUP)
//				.html("html")
//				.exception("exception")
//				.created(Instant.now())
//				.viewed(Instant.now())
//				.rank(1)
//				.category(123)
//				.flagged(true)
//				.build();
//		postRepo.save(post);

		postService.loadPosts();

		return ResponseEntity.ok("res");
	}

}

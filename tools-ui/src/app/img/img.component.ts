import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Post } from '../model/post';

@Component({
  selector: 'app-img',
  templateUrl: './img.component.html',
  styleUrls: ['./img.component.css']
})
export class ImgComponent implements OnInit {

  posts: Post[];

  constructor(private http: HttpClient, private title: Title) {
    this.title.setTitle("Img");
    this.http.get<Post[]>(environment.urlPrefix + 'api/post').subscribe(posts => {
      this.posts = posts;
    });
  }

  ngOnInit() {
  }

}

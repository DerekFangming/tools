import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-heic',
  templateUrl: './heic.component.html',
  styleUrls: ['./heic.component.css']
})
export class HeicComponent implements OnInit {

  constructor(private title: Title, private http: HttpClient) {
    this.title.setTitle("Heic converter");
    this.http.get(environment.urlPrefix + 'ping').subscribe(_ => {});
  }

  ngOnInit() {
  }

}

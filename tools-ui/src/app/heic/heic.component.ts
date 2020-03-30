import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-heic',
  templateUrl: './heic.component.html',
  styleUrls: ['./heic.component.css']
})
export class HeicComponent implements OnInit {

  constructor(private title: Title) {
    this.title.setTitle("Heic converter");}

  ngOnInit() {
  }

}

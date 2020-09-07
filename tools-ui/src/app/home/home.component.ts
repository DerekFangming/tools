import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  constructor(private http: HttpClient) {
    this.http.get(environment.urlPrefix + 'ping').subscribe(a => {
      console.log('GOT RESULT!');
      console.log(a);
    }, error => {
      console.log('fffffffffffffffffffffffffff');
    });
  }

  ngOnInit() {
  }

}

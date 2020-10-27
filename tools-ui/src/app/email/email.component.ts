import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { Email } from '../model/email';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-email',
  templateUrl: './email.component.html',
  styleUrls: ['./email.component.css']
})
export class EmailComponent implements OnInit {

  currentPage = 0;
  totalPages = 0;
  emailPerPage = 15;

  loadingEmails = true;
  emailList: Email[];

  constructor(private http: HttpClient, private title: Title) {
    this.title.setTitle('Email');
    this.onPageIndexSelected(1);
  }

  ngOnInit() {
  }

  getCreatedTime(time: string) {
    return new Date(time).toLocaleString();
  }

  getType(input: string) {
    if (input == null) return '';

    return input
    .split("_")
    .reduce((res, word, i) =>
      `${res}${word.charAt(0).toUpperCase()}${word
        .substr(1)
        .toLowerCase()}`,
      ""
    );
  }

  onPageIndexSelected(newPage: number) {
    if(newPage != this.currentPage) {
      this.currentPage = newPage;
      this.loadingEmails = true;
      const httpOptions = {
        params: new HttpParams().set('page', (newPage - 1).toString()).set('size', this.emailPerPage.toString()),
        observe: 'response' as 'response'
      };
      this.http.get<Email[]>(environment.urlPrefix + 'api/email', httpOptions).subscribe(res => {
        this.emailList = res.body;
        this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count'))/ this.emailPerPage);
        this.loadingEmails = false;
      }, error => {
        this.loadingEmails = false;
        console.log(error.error);
      });
    }
  }

}

import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { Email } from '../model/email';
import { environment } from '../../environments/environment';
import { NgbModal, NgbModalOptions, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { UtilsService } from '../utils.service';

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
  selectedEmail: Email;

  modalRef: NgbModalRef;
  @ViewChild('emailDetailsModal', { static: true}) emailDetailsModal: TemplateRef<any>;
  ngbModalOptions: NgbModalOptions = {
    backdrop : 'static',
    keyboard : false,
    centered: true
  };

  constructor(private http: HttpClient, private title: Title, private modalService: NgbModal, private utils: UtilsService) {
    this.title.setTitle('Email');
  }

  ngOnInit() {
    this.onPageIndexSelected(1);
  }

  onEmailClicked(id: number) {
    this.selectedEmail = this.emailList.find(e => e.id == id);
    console.log(this.selectedEmail);
    this.modalRef = this.modalService.open(this.emailDetailsModal, this.ngbModalOptions);
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

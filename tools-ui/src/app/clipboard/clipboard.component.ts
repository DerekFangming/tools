import { Component, OnInit, Inject, TemplateRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Clipboard } from '../model/clipboard';
import { Title } from '@angular/platform-browser';
import { DOCUMENT } from '@angular/common';
import { NgbModalRef, NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap'

@Component({
  selector: 'app-clipboard',
  templateUrl: './clipboard.component.html',
  styleUrls: ['./clipboard.component.css']
})
export class ClipboardComponent implements OnInit {

  value = ''
  links = []
  modalRef: NgbModalRef
  @ViewChild('linksModal', { static: true}) linksModal: TemplateRef<any>
  ngbModalOptions: NgbModalOptions = {
    backdrop : 'static',
    keyboard : false,
    centered: true
  }

  constructor(private http: HttpClient, private title: Title, @Inject(DOCUMENT) private document: Document, private modalService: NgbModal) {
    this.title.setTitle("Clipboard");
    this.http.get<Clipboard>(environment.urlPrefix + 'api/clipboard').subscribe(cb => {
      this.value = cb.content;
    });
  }

  ngOnInit() { }

  onTextchanged() {
    let clipboard = new Clipboard(this.value);
    this.http.post<Clipboard>(environment.urlPrefix + 'api/clipboard', clipboard).subscribe(_ => {});
  }

  undo() {
    document.execCommand('undo');
  }

  redo() {
    document.execCommand('redo');
  }

  clear() {
    this.value = '';
  }

  copyAllToClipboard() {
    this.copyToClipboard(this.value)
  }

  copyToClipboard(value){
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = value;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

  showLinksModal() {
    this.links = []
    var re = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/gmi
    var m
    while (m = re.exec(this.value)) {
      this.links.push(m[1])
    }

    this.modalRef = this.modalService.open(this.linksModal, this.ngbModalOptions)
  }

  goToLink(url){
      window.open(url, "_blank");
  }

}

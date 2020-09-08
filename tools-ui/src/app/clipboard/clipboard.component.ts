import { Component, OnInit, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Clipboard } from '../model/clipboard';
import { Title } from '@angular/platform-browser';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-clipboard',
  templateUrl: './clipboard.component.html',
  styleUrls: ['./clipboard.component.css']
})
export class ClipboardComponent implements OnInit {

  value = '';

  constructor(private http: HttpClient, private title: Title, @Inject(DOCUMENT) private document: Document) {
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

  copyToClipboard(){
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.value;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

}

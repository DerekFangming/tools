import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgxEditorModel } from 'ngx-monaco-editor';
import { environment } from 'src/environments/environment';
import { Clipboard } from '../model/clipboard';

@Component({
  selector: 'app-clipboard',
  templateUrl: './clipboard.component.html',
  styleUrls: ['./clipboard.component.css']
})
export class ClipboardComponent implements OnInit {

  editor: any;

  editorOptions = {language: 'javascript'};

  model: NgxEditorModel = {
    value: '',
    language: 'text'
  };

  constructor(private http: HttpClient) {
    this.http.get<Clipboard>(environment.urlPrefix + 'api/clipboard').subscribe(cb => {
      if (this.editor == null) {
        this.model.value = cb.content;
      } else {
        this.editor.setValue(cb.content);
      }
    });
  }

  ngOnInit() { }
  
  onEditorInit(editor: any) {
    this.editor = editor;
    this.editor.getModel().onDidChangeContent(_ => {
      let clipboard = new Clipboard(this.editor.getValue());
      this.http.post<Clipboard>(environment.urlPrefix + 'api/clipboard', clipboard).subscribe(_ => {
      });
    });
  }

  undo() {
    this.editor.trigger('', 'undo', '');
  }

  redo() {
    this.editor.trigger('', 'redo', '');
  }

  clear() {
    this.editor.setValue('');
  }

  copyToClipboard(){
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.editor.getValue();
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

}

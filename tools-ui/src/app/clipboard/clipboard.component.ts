import { Component, OnInit, Inject } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Clipboard } from '../model/clipboard'
import { Title } from '@angular/platform-browser'
import { CommonModule, DOCUMENT } from '@angular/common'
import { environment } from '../../environments/environment'
import { FormsModule } from '@angular/forms'
import { RouterOutlet } from '@angular/router'

declare var $: any

@Component({
  selector: 'app-clipboard',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule],
  templateUrl: './clipboard.component.html',
  styleUrl: './clipboard.component.css'
})
export class ClipboardComponent implements OnInit {

  value = ''
  links: string[] = []

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
    document.execCommand('undo')
  }

  redo() {
    document.execCommand('redo')
  }

  clear() {
    this.value = ''
  }

  copyAllToClipboard() {
    this.copyToClipboard(this.value)
  }

  copyToClipboard(value: any){
    navigator.clipboard.writeText(value).then().catch(e => console.error(e))
  }

  showLinksModal() {
    this.links = []
    var re = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/gmi
    var m
    while (m = re.exec(this.value)) {
      this.links.push(m[1])
    }

    $("#linksModal").modal('show')
  }

  goToLink(url: any){
      window.open(url, "_blank");
  }

}

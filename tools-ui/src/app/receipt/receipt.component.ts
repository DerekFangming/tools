import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { RouterOutlet, RouterModule, Router, ActivatedRoute, NavigationStart, NavigationEnd } from '@angular/router';
import { NotificationsService } from 'angular2-notifications';
import { UtilsService } from '../utils.service';
import { Subscription } from 'rxjs';
import { environment } from '../../environments/environment';
import { Receipt } from '../model/receipt';
import { MarkdownModule, MarkdownService } from 'ngx-markdown';
import { AngularMarkdownEditorModule, EditorInstance, EditorOption } from 'angular-markdown-editor';

@Component({
  selector: 'app-receipt',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule, MarkdownModule, AngularMarkdownEditorModule],
  templateUrl: './receipt.component.html',
  styleUrl: './receipt.component.css'
})
export class ReceiptComponent implements OnDestroy, AfterViewInit {

  loading = false
  editing = false
  routerSubscription: Subscription | undefined
  category: string | null = null
  receiptId: string | null = null
  
  receiptList: Receipt[] = []
  receipt: Receipt = new Receipt()

  editorOptions: EditorOption = {
    autofocus: false,
    iconlibrary: 'fa',
    savable: false,
    height: '700',
    enableDropDataUri: true,
    dropZoneOptions: {
      dictDefaultMessage: 'Drop Here!',
      paramName: 'file',
      maxFilesize: 2, // MB
      addRemoveLinks: true,
      init: function () {
          this.on('success', function (file: any) {
              console.log('success > ' + file.name);
          });
      }
    },
    parser: (val) => this.parse(val),
    onChange: (e) => {
      
      let content = this.parseContent(e.getContent())
      if (content == null) {
        this.receipt.content = e.getContent()
      } else {
        this.receipt.content = content
        e.setContent(content)
      }
      // console.log('Changed')
      // this.receipt.content = e.getContent()

      // console.log(e.getContent())

      // e.setContent(e.getContent().replace('', ''))
    },
    onFocus: (e) => {
      let content = this.parseContent(e.getContent())
      if (content == null) {
        this.receipt.content = e.getContent()
      } else {
        this.receipt.content = content
        e.setContent(content)
      }
    }
  }

  constructor(private http: HttpClient, private title: Title, private notifierService: NotificationsService,
    public utils: UtilsService, private route: ActivatedRoute, private router: Router, private markdownService: MarkdownService) {
    this.title.setTitle('Receipts')

    this.routerSubscription = this.router.events.subscribe((val) => {
      if (val instanceof NavigationEnd) {
        this.loadData()
      }
    })
  }

  ngAfterViewInit() {
    this.loadData()
  }

  ngOnDestroy(): void {
    this.routerSubscription?.unsubscribe()
  }

  loadData() {
    this.category = this.route.snapshot.paramMap.get('category')
    this.receiptId = this.route.snapshot.paramMap.get('id')
    if (this.category) {
      let params = new HttpParams().set('category', this.category.replace('-', '_').toUpperCase())
      this.loading = true
      this.http.get<Receipt[]>(environment.urlPrefix + `api/receipts`, {params: params}).subscribe({
        next: (res: Receipt[]) => {
          this.loading = false
          this.receiptList = res
        },
        error: (error: any) => {
          this.loading = false
          this.notifierService.error('Error', 'Failed to list')
        }
      })

    } else if (this.receiptId) {
      this.loading = true
      this.http.get<Receipt>(environment.urlPrefix + `api/receipts/${this.receiptId}`).subscribe({
        next: (res: Receipt) => {
          this.loading = false
          this.receipt = res
          console.log(this.receipt.content)
        },
        error: (error: any) => {
          this.loading = false
          this.notifierService.error('Error', 'Failed to get')
        }
      })

      
    }
  }

  getCreatedTime(time: string | undefined) {
    return new Date(time ?? '').toLocaleString()
  }

  addReceipt() {
    this.receipt = new Receipt()
    this.category = null
    this.receiptId = null
    this.editing = true
  }

  parse(inputValue: string) {
    const markedOutput = this.markdownService.parse(inputValue.trim())
    setTimeout(() => {
      this.markdownService.highlight()
    })

    return markedOutput
  }

  parseContent(content: string) {
    if (/<img src="(.*?)" \/>/gm.test(content)) {
      console.log('Has image')
      return content.replace(/<img src="(.*?)" \/>/gm, '<IMAGE IS REPLACED HERE />');
    }
    
    return null
  }

}

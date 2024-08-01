import { Component, OnInit } from '@angular/core'
import { HttpClient, HttpParams } from '@angular/common/http'
import { Title } from '@angular/platform-browser'
import { Email } from '../model/email'
import { environment } from '../../environments/environment'
import { UtilsService } from '../utils.service'
import { CommonModule } from '@angular/common'
import { FormsModule } from '@angular/forms'
import { RouterOutlet } from '@angular/router'

declare var $: any

@Component({
  selector: 'app-email',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule],
  templateUrl: './email.component.html',
  styleUrl: './email.component.css'
})
export class EmailComponent implements OnInit {

  currentPage = 0
  totalPages = 0
  emailPerPage = 15

  loadingEmails = true
  emailList: Email[] = []
  selectedEmail: Email | undefined

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService) {
    this.title.setTitle('Email')
  }

  ngOnInit() {
    this.onPageIndexSelected(1)
  }

  onEmailClicked(id: number) {
    this.selectedEmail = this.emailList.find(e => e.id == id)
    $("#emailDetailsModal").modal('show')

  }

  onPageIndexSelected(newPage: number) {
    if(newPage != this.currentPage) {
      this.currentPage = newPage
      this.loadingEmails = true
      const httpOptions = {
        params: new HttpParams().set('page', (newPage - 1).toString()).set('size', this.emailPerPage.toString()),
        observe: 'response' as 'response'
      }
      this.http.get<Email[]>(environment.urlPrefix + 'api/email', httpOptions).subscribe({
        next: (res: any) => {
          this.emailList = res.body
          this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count'))/ this.emailPerPage)
          this.loadingEmails = false
        },
        error: (error: any) => {
          this.loadingEmails = false
          console.log(error)
        }
      })
    }
  }

}

import { CommonModule } from '@angular/common'
import { HttpClient } from '@angular/common/http'
import { AfterViewInit, Component } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { Title } from '@angular/platform-browser'
import { RouterOutlet, RouterModule, Router } from '@angular/router'
import { NotificationsService } from 'angular2-notifications'
import { UtilsService } from '../utils.service'

declare var $: any

@Component({
  selector: 'app-document',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule],
  templateUrl: './document.component.html',
  styleUrl: './document.component.css'
})
export class DocumentComponent implements AfterViewInit {
  
  tab = 'document'
  loading = false

  

  constructor(private http: HttpClient, private title: Title, private notifierService: NotificationsService,
    public utils: UtilsService, private router: Router) {
    this.title.setTitle('Document')
  }

  ngAfterViewInit() {
    if (this.router.url == '/document/manage') {
      this.showTab('manage')
    } else {
      this.showTab('document')
    }
  }

  showTab(newTab: string) {
    this.tab = newTab
  }

  showDocumentModal() {
    $("#accountModal").modal('show')
  }

}

import { CommonModule } from '@angular/common'
import { HttpClient } from '@angular/common/http'
import { AfterViewInit, Component } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { Title } from '@angular/platform-browser'
import { RouterOutlet, RouterModule, Router } from '@angular/router'
import { NotificationsService } from 'angular2-notifications'
import { UtilsService } from '../utils.service'
import { Document } from '../model/document'
import { NgbDatepickerModule } from '@ng-bootstrap/ng-bootstrap'
import { environment } from '../../environments/environment'

declare var $: any

@Component({
  selector: 'app-document',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule, NgbDatepickerModule],
  templateUrl: './document.component.html',
  styleUrl: './document.component.css'
})
export class DocumentComponent implements AfterViewInit {
  
  tab = 'document'
  loading = false

  expirationDate: any
  selectedDocument = new Document()
  documentList: Document[] = []

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
    if (newTab == 'document') {
      this.loading = true
      this.http.get<Document[]>(environment.urlPrefix + `api/documents`).subscribe({
        next: (res: Document[]) => {
          this.loading = false
          this.documentList = res
        },
        error: (error: any) => {
          this.loading = false
          this.notifierService.error('Error', 'Failed to list')
        }
      })
    }
  }

  showDocumentModal() {
    this.selectedDocument = new Document()
    $('#documentModal').modal('show')
  }

  onImagesSelected(event: any) {
    event.preventDefault();
    this.loadImages(event.target.files);
  }

  removeImage(image: string) {
    let index = this.selectedDocument.images?.indexOf(image)
    if (index !== -1) {
      this.selectedDocument.images?.splice(index!, 1)
    }
  }

  loadImages(files: any) {
    for (let file of files) {
      let fileName = file.name.toLowerCase()
      if (fileName.endsWith('jpg') || fileName.endsWith('jpeg') || fileName.endsWith('png') || fileName.endsWith('gif')) {
        var reader = new FileReader()
        reader.onload = (event) =>{
          var fileReader = event.target as FileReader
    
          this.selectedDocument.images?.push(fileReader.result!.toString())
        }
        reader.readAsDataURL(file);
      }
    }
  }

  saveDocument() {
    if (this.expirationDate != null) {
      let date = this.expirationDate.year + '-'
      date += this.expirationDate.month < 10 ? '0' + this.expirationDate.month : this.expirationDate.month
      date += '-'
      date += this.expirationDate.day < 10 ? '0' + this.expirationDate.day : this.expirationDate.day
      this.selectedDocument.expirationDate = date
    }

    if (!this.selectedDocument.name || this.selectedDocument.name!.trim() == '') {
      this.notifierService.error('Error', 'Document name is required')
      return
    }

    if (!this.selectedDocument.owner || this.selectedDocument.owner!.trim() == '') {
      this.notifierService.error('Error', 'Owner name is required')
      return
    }

    if (!this.selectedDocument.images || this.selectedDocument.images!.length == 0) {
      this.notifierService.error('Error', 'At least one image is required')
      return
    }

    this.loading = true
    this.http.post<any>(environment.urlPrefix + `api/documents`, this.selectedDocument).subscribe({
      next: (res: any) => {
        this.loading = false
        $('#documentModal').modal('hide')
      },
      error: (error: any) => {
        this.loading = false
        this.notifierService.error('Error', 'Failed to create')
      }
    })

  }

  getImageUrl(images: string[] | undefined) {
    if (images == undefined || images.length == 0) {
      return ''
    }
    return environment.urlPrefix + 'api/documents/images/' + images[0]
  }

}

import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core'
import { SpendingAccount } from '../model/spending-account'
import { NgbModal, NgbModalOptions, NgbModalRef } from '@ng-bootstrap/ng-bootstrap'
import { HttpClient } from '@angular/common/http'
import { Title } from '@angular/platform-browser'
import { environment } from '../../environments/environment'

@Component({
  selector: 'app-spending',
  templateUrl: './spending.component.html',
  styleUrls: ['./spending.component.css']
})
export class SpendingComponent implements OnInit {

  tab = 'reports'
  loading = false
  dragOver = false

  selectedAccount: SpendingAccount
  accountList: SpendingAccount[] = []

  modalRef: NgbModalRef
  @ViewChild('accountModal', { static: true}) accountModal: TemplateRef<any>
  @ViewChild('transactionUploadModal', { static: true}) transactionUploadModal: TemplateRef<any>

  constructor(private http: HttpClient, private title: Title, private modalService: NgbModal) {
    this.title.setTitle('Spending')
  }

  ngOnInit() {
    this.showTab('manage')
  }

  showTab(newTab: string) {
    this.tab = newTab
    if (newTab == 'manage') {
      this.loading = true
      this.http.get<SpendingAccount[]>(environment.urlPrefix + 'api/spending/accounts').subscribe(res => {
        this.accountList = res.sort((a, b) => a.owner.localeCompare(b.owner))
        this.loading = false
      }, error => {
        this.loading = false
        console.log(error.error)
      })
    }
  }

  showAccountModal(account: SpendingAccount) {
    this.selectedAccount = account == null ? new SpendingAccount() : account
    this.modalRef = this.modalService.open(this.accountModal, { backdrop: 'static', keyboard: false, centered: true })
  }

  saveAccount() {
    this.loading = true
    let promise = this.selectedAccount.id == null ?  this.http.post<SpendingAccount>(environment.urlPrefix + 'api/spending/accounts', this.selectedAccount)
      : this.http.put<SpendingAccount>(environment.urlPrefix + 'api/spending/accounts/' + this.selectedAccount.id, this.selectedAccount)

    promise.subscribe(res => {
      this.accountList = this.accountList.filter(a => a.id != res.id)
      this.accountList.push(res)
      this.loading = false
      this.modalRef.close()
    }, error => {
      this.loading = false
      console.log(error.error)
    })
  }

  showTransactionUploadModal(account: SpendingAccount) {
    this.selectedAccount = account
    this.modalRef = this.modalService.open(this.transactionUploadModal,  { backdrop: 'static', keyboard: false, centered: true, size: 'lg' })
  }

  uploadTransactions() {

  }

  onDragOver(event) {
    event.stopPropagation();
    event.preventDefault();
  }

  onDragEnter(event) {
    this.dragOver = true;
    event.preventDefault();
  }

  onDragLeave(event) {
    this.dragOver = false;
    event.preventDefault();
  }

  onFilesDropped(event) {
    this.dragOver = false;
    event.preventDefault();
    this.loadCSVs(event.dataTransfer.files)

  }

  onFilesSelected(event) {
    event.preventDefault();
    this.loadCSVs(event.target.files)
  }

  loadCSVs(files) {
    for (let file of files) {
      let fileName = file.name.toLowerCase()
      if (fileName.endsWith('csv')) {
        var reader = new FileReader()
        reader.onload = (event) =>{
          var fileReader = event.target as FileReader
    
          // let image = new Image({status: ImageStatus.New, data: fileReader.result.toString()});
          // this.imageList.push(image);

          // console.log(fileReader)
          // console.log(fileReader.result)

          let csv = atob(fileReader.result.toString().replace('data:application/vnd.ms-excel;base64,', ''))
          console.log(csv)

          let aa = this.csvToArray(csv)
          console.log(aa)
        }
        reader.readAsDataURL(file)
      }
    }
  }

  csvToArray(text: string) {
    let p = '', row = [''], ret = [row], i = 0, r = 0, s = !0, l
    for (l of text) {
        if ('"' === l) {
            if (s && l === p) row[i] += l
            s = !s
        } else if (',' === l && s) l = row[++i] = ''
        else if ('\n' === l && s) {
            if ('\r' === p) row[i] = row[i].slice(0, -1)
            row = ret[++r] = [l = '']; i = 0
        } else row[i] += l
        p = l
    }
    return ret
  }

}

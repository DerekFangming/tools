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

  selectedAccount: SpendingAccount
  accountList: SpendingAccount[] = []

  modalRef: NgbModalRef
  @ViewChild('accountModal', { static: true}) accountModal: TemplateRef<any>
  ngbModalOptions: NgbModalOptions = {
    backdrop : 'static',
    keyboard : false,
    centered: true
  }

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

  addAccount(account: SpendingAccount) {
    this.selectedAccount = account == null ? new SpendingAccount() : account
    this.modalRef = this.modalService.open(this.accountModal, this.ngbModalOptions)
  }

  saveAccount() {
    this.loading = true
    let promise = this.selectedAccount.id == null ?  this.http.post<SpendingAccount>(environment.urlPrefix + 'api/spending/accounts', this.selectedAccount)
      :this.http.put<SpendingAccount>(environment.urlPrefix + 'api/spending/accounts/' + this.selectedAccount.id, this.selectedAccount)

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

}

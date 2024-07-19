import { AfterViewInit, Component, Inject, OnInit, TemplateRef, ViewChild } from '@angular/core'
import { SpendingAccount } from '../model/spending-account'
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap'
import { HttpClient } from '@angular/common/http'
import { Title } from '@angular/platform-browser'
import { environment } from '../../environments/environment'
import { SpendingTransaction } from '../model/spending-transaction'
import { NotifierService } from 'angular-notifier'
import { DOCUMENT } from '@angular/common'
import { Chart } from 'chart.js'
import { UtilsService, transactionCategories } from '../utils.service'

enum Order { DATE_ASC='DATE_ASC', DATE_DESC='DATE_DESC', AMOUNT_ASC='AMOUNT_ASC', AMOUNT_DESC='AMOUNT_DESC' }

@Component({
  selector: 'app-spending',
  templateUrl: './spending.component.html',
  styleUrls: ['./spending.component.css']
})
export class SpendingComponent implements OnInit, AfterViewInit {

  tab = 'reports'
  transactionPage = 0
  transactionTotal = 0
  transactionOrder = Order.DATE_ASC
  transactionFilter = {keyword: null, category: null}
  filteredTransactions: SpendingTransaction[] = []
  loading = false
  dragOver = false
  hasDuplicatedTransactions = false
  transactionView = false

  monthlySpendingChart: any
  topSpendingChart: any

  selectedAccount: SpendingAccount
  accountList: SpendingAccount[] = []
  transactionFiles = new Map<string, string>()
  selectedTransaction: SpendingTransaction
  transactions: SpendingTransaction[] = []

  modalRef: NgbModalRef
  @ViewChild('accountModal', { static: true}) accountModal: TemplateRef<any>
  @ViewChild('transactionModal', { static: true}) transactionModal: TemplateRef<any>
  @ViewChild('transactionUploadModal', { static: true}) transactionUploadModal: TemplateRef<any>

  categories = []

  constructor(private http: HttpClient, private title: Title, private modalService: NgbModal, private notifierService: NotifierService,
    @Inject(DOCUMENT) private document, public utils: UtilsService) {
    this.title.setTitle('Spending')
    this.categories = transactionCategories
  }

  ngOnInit() { }

  ngAfterViewInit() {
    this.showTab('reports')
  }

  showTab(newTab: string) {
    this.tab = newTab
    if (newTab == 'reports') {
      this.loading = true
      this.transactions = []
      this.http.get<SpendingTransaction[]>(environment.urlPrefix + 'api/spending/transactions', {params:{
        from: new Date(new Date().getFullYear()-2, new Date().getMonth(), 1).toISOString().split('T')[0]
      }}).subscribe(res => {
        this.transactions = res.sort((a, b) => new Date(a.date) > new Date(b.date) ? 1 : -1)
        this.loading = false
        this.filterAndPageTransactions(0, Order.DATE_ASC)
        this.drawChart()
      }, error => {
        this.loading = false
        console.log(error.error)
      })

      this.http.get<SpendingAccount[]>(environment.urlPrefix + 'api/spending/accounts').subscribe(res => {
        this.accountList = res
      }, error => {
        console.log(error.error)
      })
    } else if (newTab == 'manage') {
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

  drawChart() {
    if (this.transactions.length == 0) return
    let currentMonth = this.transactions[0].date.substring(0,7), currentInx = 0
    let spendingByMerchant = new Map<string, any>()
    let monthlySpendingData = [{label: currentMonth}]
    this.transactions.forEach(t => {
      if (spendingByMerchant.has(t.name)) {
        spendingByMerchant.get(t.name).count ++
        spendingByMerchant.get(t.name).amount += parseFloat(t.amount)
      } else {
        spendingByMerchant.set(t.name, {label: t.name, count: 1, amount: parseFloat(t.amount)})
      }
      let month = t.date.substring(0,7)
      if (month != currentMonth) {
        currentMonth = month
        currentInx ++
        monthlySpendingData.push({label: currentMonth})
      }
      if (monthlySpendingData[currentInx].hasOwnProperty(t.category)) {
        monthlySpendingData[currentInx][t.category] += parseFloat(t.amount)
      } else {
        monthlySpendingData[currentInx][t.category] = parseFloat(t.amount)
      }
    })

    let monthlySpendingCanvas: any = document.getElementById('monthlySpending')
    this.monthlySpendingChart = new Chart(monthlySpendingCanvas.getContext('2d'), {
      type: 'bar',
      data: {
        labels: monthlySpendingData.map(d => d.label),
        datasets: [{
          data: monthlySpendingData.map(d => d.hasOwnProperty('Grocery') ? d['Grocery'].toFixed(2) : 0),
          backgroundColor: "#50c878", label: "Grocery",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Shopping') ? d['Shopping'].toFixed(2) : 0),
          backgroundColor: "#ff7373", label: "Shopping",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Subscription') ? d['Subscription'].toFixed(2) : 0),
          backgroundColor: "#36a2eb", label: "Subscription",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Transportation') ? d['Transportation'].toFixed(2) : 0),
          backgroundColor: "#ffa22e", label: "Transportation",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Government') ? d['Government'].toFixed(2) : 0),
          backgroundColor: "#c0c0c0", label: "Government",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Utility') ? d['Utility'].toFixed(2) : 0),
          backgroundColor: "#104624", label: "Utility",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Real Estate') ? d['Real Estate'].toFixed(2) : 0),
          backgroundColor: "#ff80ed", label: "Real Estate",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Restaurant') ? d['Restaurant'].toFixed(2) : 0),
          backgroundColor: "#ffd700", label: "Restaurant",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Entertainment') ? d['Entertainment'].toFixed(2) : 0),
          backgroundColor: "#cc0000", label: "Entertainment",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Healthcare') ? d['Healthcare'].toFixed(2) : 0),
          backgroundColor: "#444eff", label: "Healthcare",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Travel') ? d['Travel'].toFixed(2) : 0),
          backgroundColor: "#885640", label: "Travel",
        }]
      },
      options: {
        interaction: {
          intersect: false
        },
        maintainAspectRatio: false,
        scales: {
          xAxes: [{
            stacked: true
          }],
          yAxes: [{
            stacked: true
          }]
        },
        title: {
          display: true,
          text: 'Monthly Spending Chart'
        }
      }
    })

    let topSpendingData = Array.from( spendingByMerchant.values()).sort((a, b) => b.amount - a.amount).slice(60, 80)

    let topSpendingCanvas: any = document.getElementById('topSpending')
    this.topSpendingChart = new Chart(topSpendingCanvas.getContext('2d'), {
      type: 'bar',
      data: {
        labels: topSpendingData.map(d => d.label),
        datasets: [{
          data: topSpendingData.map(d => d.amount.toFixed(2)),
          backgroundColor: '#50c878',
          label: 'Total Amount'
        }]
      },
      options: {
        interaction: {
          intersect: false
        },
        maintainAspectRatio: false,
        tooltips: {
          callbacks: {
            label: function(tooltipItem, data) {
                return `Spent $${data.datasets[0].data[tooltipItem.index]} on ${topSpendingData[tooltipItem.index].count} transactions`
            }
          }
        },
        title: {
          display: true,
          text: 'Top Spending Chart'
        }
      }
    })
  }

  filterTransactions(keyword: string, category: string) {
    keyword = keyword != null && keyword.length > 2 ? keyword.toLowerCase() : null
    if (this.transactionFilter.keyword != keyword ||  this.transactionFilter.category != category) {
      this.transactionFilter.keyword = keyword
      this.transactionFilter.category = category
      this.filterAndPageTransactions(this.transactionPage, this.transactionOrder)
    }
  }

  filterAndPageTransactions(page: number, order: Order) {
    let transactions = this.transactions.slice()

    if (this.transactionFilter.keyword != null) transactions = transactions.filter(t => t.name.toLocaleLowerCase().includes(this.transactionFilter.keyword))
    if (this.transactionFilter.category != null) transactions = transactions.filter(t => t.category == this.transactionFilter.category)

    if (order != null) {
      this.transactionOrder = order
      if (order == Order.DATE_ASC) transactions = transactions.sort((a, b) => new Date(a.date) > new Date(b.date) ? 1 : -1)
      else if (order == Order.DATE_DESC) transactions = transactions.sort((a, b) => new Date(a.date) < new Date(b.date) ? 1 : -1)
      else if (order == Order.AMOUNT_ASC) transactions = transactions.sort((a, b) => parseFloat(a.amount) - parseFloat(b.amount))
      else if (order == Order.AMOUNT_DESC) transactions = transactions.sort((a, b) => parseFloat(b.amount) - parseFloat(a.amount))
    }

    let maxPage =  Math.ceil(transactions.length / 20)
    if (page < 0) page = 0
    else if (page > maxPage - 1) page = maxPage - 1
    this.transactionPage = page

    this.transactionTotal = transactions.length
    this.filteredTransactions = transactions.slice(page*20, (page+1)*20)
  }

  showTransactionModal(transaction: SpendingTransaction) {
    let accounts = this.accountList.filter(a => a.id == transaction.accountId)
    if (accounts.length > 0) this.selectedAccount = accounts[0]
    else this.selectedAccount = new SpendingAccount({name: 'Unknown', identifier: '0000', owner: 'Unknown', icon: 'https://img.icons8.com/ios/500/credit-card-front.png'})
    this.selectedTransaction = transaction
    this.modalRef = this.modalService.open(this.transactionModal, { backdrop: 'static', keyboard: false, centered: true, size: 'lg' })
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
    this.transactionView = false
    this.selectedAccount = account
    this.transactions = []
    this.transactionFiles = new Map<string, string>()
    this.modalRef = this.modalService.open(this.transactionUploadModal,  { backdrop: 'static', keyboard: false, centered: true, size: 'lg'})
  }

  forceDedupTransactions() {
    this.hasDuplicatedTransactions = false
    let dedup = new Map<string, boolean>()
    let counter = 1
    this.transactions.forEach(t => {
      t.error = false
      if (dedup.has(t.identifier)) {
        t.identifier += `#dedup-${counter++}`
      } else {
        dedup.set(t.identifier, true)
      }
    })
  }

  uploadTransactions() {
    this.hasDuplicatedTransactions = false
    let dedup = new Map<string, boolean>()
    this.transactions.forEach(t => {
      if (dedup.has(t.identifier)) {
        this.hasDuplicatedTransactions = true
        t.error = true
      } else {
        dedup.set(t.identifier, true)
        t.error = false
      }
    })

    if (this.hasDuplicatedTransactions) {
      this.notifierService.notify('error', 'Found duplicated record, review before upload')
      return
    }

    this.loading = true
    this.http.post<any>(environment.urlPrefix + `api/spending/transactions`, this.transactions).subscribe(res => {
      this.loading = false
      this.modalRef.close()
    }, error => {
      this.loading = false
      if (error.message) {
        this.transactions.filter(t => t.identifier == error.message).map(t => t.error = true)
        this.notifierService.notify('error', 'Failed to upload, at least one transaction already exists')
      } else {
        this.notifierService.notify('error', 'Failed to upload, unknown error')
        console.log(error.error)
      }
    })
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
          let csv = atob(fileReader.result.toString().replace('data:application/vnd.ms-excel;base64,', ''))
          this.transactionFiles.set(fileName, csv)
        }
        reader.readAsDataURL(file)
      }
    }
  }

  processTransactions() {
    let format = null
    for (const [key, value] of this.transactionFiles.entries()) {
      let matrix = this.csvToArray(value)
      if (format == null) {
        format = matrix[0][0]
      } else {
        if (format != matrix[0][0]) {
          this.notifierService.notify('error', 'Invalid file format for "' + key + '", format column ' + matrix[0][0] + 'is different than ' + format)
          throw new Error('Invalid file format for "' + key + '", format column ' + matrix[0][0] + 'is different than ' + format)
        }
      }
      
      if (format == 'Date') {
        console.log(`Processing "${key}" as AMEX`)
        for (let i = 1; i < matrix.length; i ++) {
          let row = matrix[i]
          if (row[2].startsWith('-')) {
            console.log('Skipping row: ' + row)
            continue
          }

          let transaction = new SpendingTransaction({accountId: this.selectedAccount.id, name: row[1], amount: row[2],
            category: row[10], location: `${row[5]} ${row[6]}, ${row[8]}`, date: row[0]})
          this.transactions.push(this.utils.processTransaction(transaction, 'AMEX'))
        }
      } else if (format == 'Transaction Date') {
        console.log(`Processing "${key}" as Chase`)
        for (let i = 1; i < matrix.length; i ++) {
          let row = matrix[i]
          if (row.length <= 1 || !row[5].startsWith('-')) {
            console.log('Skipping row: ' + row)
            continue
          }

          let transaction = new SpendingTransaction({accountId: this.selectedAccount.id, name: row[2],
            amount: row[5].substring(1), category: row[3], date: row[0]})
          this.transactions.push(this.utils.processTransaction(transaction, 'Chase'))
        }
      } else if (format == 'Posted Date') {
        console.log(`Processing "${key}" as BOA`)
        for (let i = 1; i < matrix.length; i ++) {
          let row = matrix[i]
          if (row.length <= 1 || !row[4].startsWith('-')) {
            console.log('Skipping row: ' + row)
            continue
          }

          let transaction = new SpendingTransaction({accountId: this.selectedAccount.id, name: row[2],
            amount: row[4].substring(1), location: row[3], date: row[0]})
          this.transactions.push(this.utils.processTransaction(transaction, 'BOA'))
        }
      } else {
        console.log("not processed: " + format)
      }
    }
    this.transactionView = true
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

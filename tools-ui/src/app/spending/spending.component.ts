import { AfterViewInit, Component, Inject, OnInit } from '@angular/core'
import { Router, RouterModule, RouterOutlet } from '@angular/router'
import { SpendingAccount } from '../model/spending-account'
import { HttpClient, HttpParams } from '@angular/common/http'
import { Title } from '@angular/platform-browser'
import { environment } from '../../environments/environment'
import { SpendingTransaction } from '../model/spending-transaction'
import { CommonModule } from '@angular/common'
import { Chart, registerables } from 'chart.js'
import { UtilsService, transactionCategories } from '../utils.service'
import { NotificationsService } from 'angular2-notifications'
import { FormsModule } from '@angular/forms'

declare var $: any

enum Order { DATE_ASC='DATE_ASC', DATE_DESC='DATE_DESC', AMOUNT_ASC='AMOUNT_ASC', AMOUNT_DESC='AMOUNT_DESC' }

@Component({
  selector: 'app-spending',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule],
  templateUrl: './spending.component.html',
  styleUrl: './spending.component.css'
})
export class SpendingComponent implements OnInit, AfterViewInit {

  tab = 'reports'
  transactionPage = 0
  transactionTotal = 0
  transactionOrder = Order.DATE_ASC
  transactionFilter: any = {keyword: null, category: null, accountId: null}
  transactionRangeLabel = 'Last Year'
  filteredTransactions: SpendingTransaction[] = []
  filteredTransactionsPage: SpendingTransaction[] = []
  loading = false
  dragOver = false
  hasDuplicatedTransactions = false
  transactionView = false

  monthlySpendingChart: any
  topSpendingChart: any

  selectedAccount: SpendingAccount = new SpendingAccount()
  accountList: SpendingAccount[] = []
  transactionFiles = new Map<string, string>()
  selectedTransaction: SpendingTransaction | undefined
  transactions: SpendingTransaction[] = []

  categories: string[]= []

  constructor(private http: HttpClient, private title: Title, private notifierService: NotificationsService,
    public utils: UtilsService, private router: Router) {
    this.title.setTitle('Finance')
    this.categories = Array.from( transactionCategories.keys() )
    Chart.register(...registerables)
  }

  ngOnInit() { }

  ngAfterViewInit() {
    if (this.router.url == '/spending/manage') {
      this.showTab('manage')
    } else {
      this.showTab('reports')
    }
  }

  showTab(newTab: string) {
    this.tab = newTab
    if (newTab == 'reports') {
      this.loadTransactions(12, 0)

      this.http.get<SpendingAccount[]>(environment.urlPrefix + 'api/finance/spending/accounts').subscribe({
        next: (res: SpendingAccount[]) => {
          this.accountList = res
        },
        error: (error: any) => {
          console.log(error.error)
        }
      })
    } else if (newTab == 'manage') {
      this.loading = true
      this.http.get<SpendingAccount[]>(environment.urlPrefix + 'api/finance/spending/accounts').subscribe({
        next: (res: SpendingAccount[]) => {
          this.accountList = res.sort((a, b) => a.owner!.localeCompare(b.owner!))
          this.loading = false
        },
        error: (error: any) => {
          this.loading = false
          console.log(error.error)
        }
      })
    }
  }

  getTransactionRangeLabel(from: number, to: number) {
    if (from - to == 1) return new Date(new Date().getFullYear(), new Date().getMonth() - from + 1, 1).toISOString().substring(0,7)
    else if (to == 0) return from==24 ? 'Last 2 Years' : from==12 ? 'Last Year' : from==6 ? 'Last 6 months' : from==3 ? 'Last Quarter' : `Last ${from} Months`
    else return 'Custom Range'
  }

  getSelectedAccountName() {
    return this.accountList.find(a => a.id == this.transactionFilter.accountId)?.name
  }

  loadTransactions(from: number, to: number) {
    this.transactionRangeLabel = this.getTransactionRangeLabel(from, to)
    this.loading = true
    this.transactions = []

    let params = new HttpParams().set('from', new Date(new Date().getFullYear(), new Date().getMonth() - from + 1, 1).toISOString().split('T')[0])
    if (to != 0) {
      params = params.set('to', new Date(new Date().getFullYear(), new Date().getMonth() - to + 1, 1).toISOString().split('T')[0])
    }

    this.http.get<SpendingTransaction[]>(environment.urlPrefix + 'api/finance/spending/transactions', {params: params}).subscribe({
      next: (res: SpendingTransaction[]) => {
        this.transactions = res.sort((a, b) => new Date(a.date!) > new Date(b.date!) ? 1 : -1)
        this.filteredTransactions = this.transactions
        this.loading = false
        this.filterAndPageTransactions(0, Order.DATE_ASC)
        this.drawChart()
      },
      error: (error: any) => {
        this.loading = false
        console.log(error.error)
      }
    })
  }

  drawChart() {
    if (this.filteredTransactions.length == 0) return
    let currentMonth = this.filteredTransactions[0].date!.substring(0,7), currentInx = 0
    let spendingByMerchant = new Map<string, any>()
    let monthlySpendingData: any[] = [{label: currentMonth}]
    this.filteredTransactions.forEach(t => {
      if (spendingByMerchant.has(t.name!)) {
        spendingByMerchant.get(t.name!).count ++
        spendingByMerchant.get(t.name!).amount += parseFloat(t.amount!)
      } else {
        spendingByMerchant.set(t.name!, {label: t.name, category: t.category, count: 1, amount: parseFloat(t.amount!)})
      }
      let month = t.date!.substring(0,7)
      if (month != currentMonth) {
        currentMonth = month
        currentInx ++
        monthlySpendingData.push({label: currentMonth})
      }
      if (monthlySpendingData[currentInx].hasOwnProperty(t.category!)) {
        monthlySpendingData[currentInx][t.category!] += parseFloat(t.amount!)
      } else {
        monthlySpendingData[currentInx][t.category!] = parseFloat(t.amount!)
      }
    })

    // If there is only one month, display day chart 2022-01-01
    if (currentInx == 0) {
      let currentDay = this.filteredTransactions[0].date!.substring(8,10)
      monthlySpendingData = [{label: currentDay}]
      this.filteredTransactions.forEach(t => {
        let day = t.date!.substring(8,10)
        if (day != currentDay) {
          currentDay = day
          currentInx ++
          monthlySpendingData.push({label: currentDay})
        }
        if (monthlySpendingData[currentInx].hasOwnProperty(t.category)) {
          monthlySpendingData[currentInx][t.category!] += parseFloat(t.amount!)
        } else {
          monthlySpendingData[currentInx][t.category!] = parseFloat(t.amount!)
        }
      })
    }

    let monthlySpendingCanvas: any = $("#monthlySpending")[0]
    console.log(monthlySpendingCanvas)
    if (this.monthlySpendingChart != null) this.monthlySpendingChart.destroy()
    this.monthlySpendingChart = new Chart(monthlySpendingCanvas.getContext('2d'), {
      type: 'bar',
      data: {
        labels: monthlySpendingData.map(d => d.label),
        datasets: [{
          data: monthlySpendingData.map(d => d.hasOwnProperty('Grocery') ? d['Grocery'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Grocery'), label: "Grocery",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Shopping') ? d['Shopping'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Shopping'), label: "Shopping",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Subscription') ? d['Subscription'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Subscription'), label: "Subscription",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Transportation') ? d['Transportation'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Transportation'), label: "Transportation",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Government') ? d['Government'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Government'), label: "Government",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Utility') ? d['Utility'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Utility'), label: "Utility",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Real Estate') ? d['Real Estate'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Real Estate'), label: "Real Estate",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Restaurant') ? d['Restaurant'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Restaurant'), label: "Restaurant",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Entertainment') ? d['Entertainment'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Entertainment'), label: "Entertainment",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Healthcare') ? d['Healthcare'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Healthcare'), label: "Healthcare",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Travel') ? d['Travel'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Travel'), label: "Travel",
        }, {
          data: monthlySpendingData.map(d => d.hasOwnProperty('Other') ? d['Other'].toFixed(2) : 0),
          backgroundColor: transactionCategories.get('Other'), label: "Other",
        }]
      },
      options: {
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: 'Monthly Spending Chart'
          }
        },
        scales: {
          x: {
            stacked: true
          },
          y: {
            stacked: true
          }
        }
      }
    })

    let topSpendingData = Array.from( spendingByMerchant.values()).sort((a, b) => b.amount - a.amount).slice(0, 15)

    let topSpendingCanvas: any = $("#topSpending")[0]
    if (this.topSpendingChart != null) this.topSpendingChart.destroy()
    this.topSpendingChart = new Chart(topSpendingCanvas.getContext('2d'), {
      type: 'bar',
      data: {
        labels: topSpendingData.map(d => d.label),
        datasets : topSpendingData.map((d, i) => {
          let data = Array(15).fill(0)
          data[i] = d.amount.toFixed(2)
          return {
            data: data,
            backgroundColor: transactionCategories.get(d.category),
            label: d.category
          }
        })
      },
      options: {
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: 'Top Spending Chart'
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                return `Spent $${context.formattedValue} on ${topSpendingData[context.dataIndex].count} transactions`
              }
            }
          }
        },
        scales: {
          x: {
            stacked: true
          },
          y: {
            stacked: true
          }
        }
      }
    })
  }

  getInputValue(target: EventTarget | null) {return (<HTMLTextAreaElement>target).value}

  filterTransactions(keyword: string | undefined, category: string | undefined, accountId: number | undefined) {
    keyword = keyword != undefined && keyword.length > 2 ? keyword.toLowerCase() : undefined
    if (this.transactionFilter.keyword != keyword || this.transactionFilter.category != category || this.transactionFilter.accountId != accountId) {
      this.transactionFilter.keyword = keyword
      this.transactionFilter.category = category
      this.transactionFilter.accountId = accountId
      this.filterAndPageTransactions(this.transactionPage, this.transactionOrder, true)
    }
  }

  filterAndPageTransactions(page: number, order: Order | string | null, reloadChart: boolean = false) {
    let transactions = this.transactions.slice()

    if (this.transactionFilter.keyword != null) transactions = transactions.filter(t => t.name!.toLocaleLowerCase().includes(this.transactionFilter.keyword))
    if (this.transactionFilter.category != null) transactions = transactions.filter(t => t.category == this.transactionFilter.category)
    if (this.transactionFilter.accountId != null) transactions = transactions.filter(t => t.accountId == this.transactionFilter.accountId)

    if (order != null) {
      this.transactionOrder = <Order>order
      if (order == Order.DATE_ASC) transactions = transactions.sort((a, b) => new Date(a.date!) > new Date(b.date!) ? 1 : -1)
      else if (order == Order.DATE_DESC) transactions = transactions.sort((a, b) => new Date(a.date!) < new Date(b.date!) ? 1 : -1)
      else if (order == Order.AMOUNT_ASC) transactions = transactions.sort((a, b) => parseFloat(a.amount!) - parseFloat(b.amount!))
      else if (order == Order.AMOUNT_DESC) transactions = transactions.sort((a, b) => parseFloat(b.amount!) - parseFloat(a.amount!))
    }

    let maxPage =  Math.ceil(transactions.length / 20)
    if (page < 0) page = 0
    else if (page > maxPage - 1) page = maxPage - 1
    this.transactionPage = page

    this.transactionTotal = transactions.length
    this.filteredTransactions = transactions
    this.filteredTransactionsPage = transactions.slice(page*20, (page+1)*20)

    if (reloadChart) this.drawChart()
  }

  showTransactionModal(transaction: SpendingTransaction) {
    let accounts = this.accountList.filter(a => a.id == transaction.accountId)
    if (accounts.length > 0) this.selectedAccount = accounts[0]
    else this.selectedAccount = new SpendingAccount({name: 'Other', identifier: '0000', owner: 'Other', icon: 'https://img.icons8.com/ios/500/credit-card-front.png'})
    this.selectedTransaction = transaction
    $("#transactionModal").modal('show')
  }

  showAccountModal(account: SpendingAccount | null) {
    this.selectedAccount = account == null ? new SpendingAccount() : account
    $("#accountModal").modal('show')
  }

  saveAccount() {
    this.loading = true
    let request = this.selectedAccount!.id == null ?  this.http.post<SpendingAccount>(environment.urlPrefix + 'api/finance/spending/accounts', this.selectedAccount)
      : this.http.put<SpendingAccount>(environment.urlPrefix + 'api/finance/spending/accounts/' + this.selectedAccount!.id, this.selectedAccount)

    request.subscribe({
      next: (res: SpendingAccount) => {
        this.accountList = this.accountList.filter(a => a.id != res.id)
        this.accountList.push(res)
        this.loading = false
        $("#accountModal").modal('hide')
      },
      error: (error: any) => {
        this.loading = false
        console.log(error.error)
      }
    })
  }

  showTransactionUploadModal(account: SpendingAccount) {
    this.transactionView = false
    this.selectedAccount = account
    this.transactions = []
    this.transactionFiles = new Map<string, string>()
    $("#transactionUploadModal").modal('show')
  }

  forceDedupTransactions() {
    this.hasDuplicatedTransactions = false
    let dedup = new Map<string, boolean>()
    let counter = 1
    this.transactions.forEach(t => {
      t.error = false
      if (dedup.has(t.identifier!)) {
        t.identifier += `#dedup-${counter++}`
      } else {
        dedup.set(t.identifier!, true)
      }
    })
  }

  uploadTransactions() {
    this.hasDuplicatedTransactions = false
    let dedup = new Map<string, boolean>()
    this.transactions.forEach(t => {
      if (dedup.has(t.identifier!)) {
        this.hasDuplicatedTransactions = true
        t.error = true
      } else {
        dedup.set(t.identifier!, true)
        t.error = false
      }
    })

    if (this.hasDuplicatedTransactions) {
      this.notifierService.warn('Warning', 'Local check found duplicated record, review before upload')
      return
    }

    this.loading = true
    this.http.post<any>(environment.urlPrefix + `api/finance/spending/transactions`, this.transactions).subscribe({
      next: (res: any) => {
        this.loading = false
        $("#transactionUploadModal").modal('hide')
      },
      error: (error: any) => {
        this.loading = false
        if (error.message) {
          this.transactions.filter(t => t.identifier == error.message).map(t => t.error = true)
          this.notifierService.error('Error', 'Failed to upload, at least one transaction already exists')
        } else {
          this.notifierService.error('Error', 'Failed to upload, unknown error')
          console.log(error.error)
        }
      }
    })
  }

  onDragOver(event: any) {
    event.stopPropagation();
    event.preventDefault();
  }

  onDragEnter(event: any) {
    this.dragOver = true;
    event.preventDefault();
  }

  onDragLeave(event: any) {
    this.dragOver = false;
    event.preventDefault();
  }

  onFilesDropped(event: any) {
    this.dragOver = false;
    event.preventDefault();
    this.loadCSVs(event.dataTransfer.files)

  }

  onFilesSelected(event: any) {
    event.preventDefault();
    this.loadCSVs(event.target.files)
  }

  loadCSVs(files: any) {
    for (let file of files) {
      let fileName = file.name.toLowerCase()
      if (fileName.endsWith('csv')) {
        var reader = new FileReader()
        reader.onload = (event) =>{
          var fileReader = event.target as FileReader
          let csv = atob(fileReader.result!.toString().replace('data:application/vnd.ms-excel;base64,', '').replace('data:text/csv;base64,', ''))
          this.transactionFiles.set(fileName, csv)
        }
        reader.readAsDataURL(file)
      }
    }
  }

  processTransactions() {
    let format: string | null = null
    for (const [key, value] of this.transactionFiles.entries()) {
      let matrix = this.csvToArray(value)
      if (format == null) {
        format = matrix[0][0]
      } else {
        if (format != matrix[0][0]) {
          this.notifierService.error('Error', 'Invalid file format for "' + key + '", format column ' + matrix[0][0] + 'is different than ' + format)
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

          let transaction = new SpendingTransaction({accountId: this.selectedAccount!.id, name: row[1], amount: row[2],
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

          let transaction = new SpendingTransaction({accountId: this.selectedAccount!.id, name: row[2],
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

          let transaction = new SpendingTransaction({accountId: this.selectedAccount!.id, name: row[2],
            amount: row[4].substring(1), location: row[3], date: row[0]})
          this.transactions.push(this.utils.processTransaction(transaction, 'BOA'))
        }
      } else if (format == 'Description') {
        console.log(`Processing "${key}" as BOA checking`)
        for (let i = 1; i < matrix.length; i ++) {
          let row = matrix[i]
          let description = row[1] == null ? null : row[1].toLocaleLowerCase()
          if (description == null || (!row[0].startsWith('0') && !row[0].startsWith('1')) || !row[2].startsWith('-')
            || description.includes('autopay') || description.includes('auto pay') || description.includes('american express')
            || description.includes('bank of a')) {
            console.log('Skipping row: ' + row)
            continue
          }

          let transaction = new SpendingTransaction({accountId: this.selectedAccount!.id, name: row[1],
            amount: row[2].substring(1), date: row[0]})
          this.transactions.push(this.utils.processTransaction(transaction, 'BOA checking'))
        }
      } else if (format == 'Details') {
        console.log(`Processing "${key}" as Chase checking`)
        for (let i = 1; i < matrix.length; i ++) {
          let row = matrix[i]
          let description = row[2] == null ? null : row[2].toLocaleLowerCase()
          if (description == null || (!row[0].startsWith('1') && !row[1].startsWith('1')) || !row[3].startsWith('-')
            || description.includes('autopay') || description.includes('auto pay')) {
            console.log('Skipping row: ' + row)
            continue
          }

          let transaction = new SpendingTransaction({accountId: this.selectedAccount!.id, name: row[2],
            amount: row[3].substring(1), date: row[1]})
          this.transactions.push(this.utils.processTransaction(transaction, 'BOA checking'))
        }
      } else if (format == 'Status') {
        console.log(`Processing "${key}" as citi`)
        for (let i = 1; i < matrix.length; i ++) {
          let row = matrix[i]
          if (row.length <= 1 || row[3] == '') {
            console.log('Skipping row: ' + row)
            continue
          }

          let transaction = new SpendingTransaction({accountId: this.selectedAccount!.id, name: row[2],
            amount: row[3], date: row[1]})
          this.transactions.push(this.utils.processTransaction(transaction, 'BOA'))
        }
      } else {
        console.log("Not processed due to unknown format: " + format)
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

import { HttpClient, HttpParams } from '@angular/common/http'
import { Component, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { Log } from '../model/log'
import { UtilsService } from '../utils.service'
import { NotificationsService } from 'angular2-notifications'
import { environment } from '../../environments/environment'
import { CommonModule } from '@angular/common'
import { FormsModule } from '@angular/forms'
import { RouterOutlet } from '@angular/router'
import { NgbDatepickerModule } from '@ng-bootstrap/ng-bootstrap'

declare var $: any

@Component({
  selector: 'app-log',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, NgbDatepickerModule],
  templateUrl: './log.component.html',
  styleUrl: './log.component.css'
})
export class LogComponent implements OnInit {

  loadingLogs = true
  selectedLog: Log | undefined
  logList: Log[] = []

  message = ''
  fromDate: any
  toDate: any
  level = ''
  service = ''

  currentPage = -1
  totalPages = 0
  totalLogs = 0
  resultPerPage = 50
  math = Math

  constructor(private http: HttpClient, private title: Title, private notifierService: NotificationsService,
    public utils: UtilsService) {
    this.title.setTitle('Logs')
  }

  ngOnInit() {
    this.loadLogs(0)
  }

  loadLogs(page: number) {
    if (page < 0 || (page > this.totalPages && this.totalPages != -1)) return
    this.loadingLogs = true
    this.currentPage = page

    let queryParam = new HttpParams().set('page', page.toString())
    if (this.message.trim() != '') {
      queryParam = queryParam.set('message', this.message.trim());
    }
    if (this.level != '') {
      queryParam = queryParam.set('level', this.level);
    }
    if (this.service != '') {
      queryParam = queryParam.set('service', this.service);
    }

    const httpOptions = {
      params: queryParam,
      observe: 'response' as 'response'
    }
    this.http.get<Log[]>(environment.urlPrefix + 'api/logs', httpOptions).subscribe({
      next: (res: any) => {
        console.log(res.headers.keys())
        this.logList = res.body
        this.totalLogs = Number(res.headers.get('X-Total-Count'))
        this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count')) / this.resultPerPage - 1)
        this.loadingLogs = false
      },
      error: (error: any) => {
        this.loadingLogs = false
        console.log(error)
      }
    })
  }

  onLevelSelected(level: string) {
    this.level = level
    this.loadLogs(0)
  }

  onServiceSelected(service: string) {
    this.service = service
    this.loadLogs(0)
  }

  openDetailsWindow(log: Log) {
    this.selectedLog = log
    $("#logDetailsModal").modal('show')
  }

}

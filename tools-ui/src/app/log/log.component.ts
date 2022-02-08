import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { NotifierService } from 'angular-notifier';
import { environment } from 'src/environments/environment';
import { Log } from '../model/log';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-log',
  templateUrl: './log.component.html',
  styleUrls: ['./log.component.css']
})
export class LogComponent implements OnInit {

  loadingLogs = true
  logList: Log[]

  message = ''
  fromDate: any
  toDate:any
  level = ''

  currentPage = -1
  totalPages = 0
  totalLogs = 0
  resultPerPage = 50
  math = Math

  constructor(private http: HttpClient, private title: Title, private notifierService: NotifierService,
    private modalService: NgbModal, public utils: UtilsService) {
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

    const httpOptions = {
      params: queryParam,
      observe: 'response' as 'response'
    }
    this.http.get<Log[]>(environment.urlPrefix + 'api/logs', httpOptions).subscribe(res => {
      this.logList = res.body
      this.totalLogs = Number(res.headers.get('X-Total-Count'))
      this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count')) / this.resultPerPage - 1)
      this.loadingLogs = false
    }, error => {
      this.loadingLogs = false
      this.notifierService.notify('error', error.message);
    });
  }

  onLevelSelected(level: string) {
    this.level = level
    this.loadLogs(0)
  }

  openDetailsWindow(log: Log) {
    // this.selectedUserLog = userLog
    // this.modalRef = this.modalService.open(this.detailsModal, {
    //   backdrop : 'static',
    //   keyboard : false,
    //   centered: true
    // });
  }

}

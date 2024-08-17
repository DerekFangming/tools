import { CommonModule } from '@angular/common'
import { HttpClient } from '@angular/common/http'
import { AfterViewInit, Component } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { Title } from '@angular/platform-browser'
import { RouterOutlet, RouterModule, Router } from '@angular/router'
import { NotificationsService } from 'angular2-notifications'
import { Chart, registerables } from 'chart.js'
import { environment } from '../../environments/environment'
import { RealEstate } from '../model/real-estate'

declare var $: any

@Component({
  selector: 'app-real-estate',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule],
  templateUrl: './real-estate.component.html',
  styleUrl: './real-estate.component.css'
})
export class RealEstateComponent implements AfterViewInit {

  loading = false
  realEstateList: RealEstate[] = []
  chartList: any[] = []

  constructor(private http: HttpClient, private title: Title, private notifierService: NotificationsService,
    private router: Router) {
    this.title.setTitle('Finance')
    Chart.register(...registerables)
  }

  ngAfterViewInit() {
    this.loading = true
    this.http.get<RealEstate[]>(environment.urlPrefix + 'api/finance/real-estates').subscribe({
      next: (res: RealEstate[]) => {
        this.loading = false
        this.realEstateList = res
        console.log(this.realEstateList)

        setTimeout(() => {
          this.loadCharts()
        }, 200)
      },
      error: (error: any) => {
        this.loading = false
        console.log(error.error)
      }
    })
  }

  loadCharts() {
    let destroyExistingCharts = this.chartList.length > 0
    for (let i = 0; i < this.realEstateList.length; i ++) {
      let canvas: any = $(`#realEstate-${i}`)[0]
      if (destroyExistingCharts) this.chartList[i].destroy()

      let chart = new Chart(canvas.getContext('2d'), {
        type: 'line',
        data: {
          labels: this.realEstateList[i].history?.map(r => r.date?.substring(2,7).replace('-', '/')),
          datasets: [{
            label: 'House Value',
            borderColor: '#ff4040',
            backgroundColor: '#ff4040',
            data: this.realEstateList[i].history?.map(h => h.value),
            tension: 0.4
          },{
            label: 'Debt Balance',
            borderColor: '#003366',
            backgroundColor: '#003366',
            data: this.realEstateList[i].history?.map(h => h.balance),
            tension: 0.4
          }]
        },
        options: {
          maintainAspectRatio: false,
          plugins: {
            title: {
              display: true,
              text: this.realEstateList[i].label
            }
          }
        }
      })
        
      this.chartList.push(chart)
    }
  }

}

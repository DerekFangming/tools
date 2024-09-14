import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { RouterOutlet, RouterModule, Router, ActivatedRoute, NavigationStart, NavigationEnd } from '@angular/router';
import { NotificationsService } from 'angular2-notifications';
import { UtilsService } from '../utils.service';
import { Subscription } from 'rxjs';
import { environment } from '../../environments/environment';
import { Receipt } from '../model/receipt';

@Component({
  selector: 'app-receipt',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule],
  templateUrl: './receipt.component.html',
  styleUrl: './receipt.component.css'
})
export class ReceiptComponent implements OnDestroy, AfterViewInit {

  loading = false
  routerSubscription: Subscription | undefined
  category: string | null = null
  receiptId: string | null = null
  
  receiptList: Receipt[] = []

  constructor(private http: HttpClient, private title: Title, private notifierService: NotificationsService,
    public utils: UtilsService, private route: ActivatedRoute, private router: Router) {
    this.title.setTitle('Receipts')

    this.routerSubscription = this.router.events.subscribe((val) => {
      if (val instanceof NavigationEnd) {
        this.loadData()
      }
    })
  }

  ngAfterViewInit() {
    this.loadData()
  }

  ngOnDestroy(): void {
    this.routerSubscription?.unsubscribe()
  }

  loadData() {
    this.category = this.route.snapshot.paramMap.get('category')
    this.receiptId = this.route.snapshot.paramMap.get('id')
    if (this.category) {
      let params = new HttpParams().set('category', this.category.replace('-', '_').toUpperCase())
      this.loading = true
      this.http.get<Receipt[]>(environment.urlPrefix + `api/receipts`, {params: params}).subscribe({
        next: (res: Receipt[]) => {
          this.loading = false
          this.receiptList = res
        },
        error: (error: any) => {
          this.loading = false
          this.notifierService.error('Error', 'Failed to list')
        }
      })

    } else if (this.receiptId) {
      console.log('Loading receipt with ID ' + this.receiptId)
    }
  }

}

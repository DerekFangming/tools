import { Routes } from '@angular/router'
import { ClipboardComponent } from './clipboard/clipboard.component'
import { HeicComponent } from './heic/heic.component'
import { ImgComponent } from './img/img.component'
import { HomeComponent } from './home/home.component'
import { EmailComponent } from './email/email.component'
import { ImgUploadComponent } from './img-upload/img-upload.component'
import { LogComponent } from './log/log.component'
import { SpendingComponent } from './spending/spending.component'

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'clipboard', component: ClipboardComponent },
  { path: 'heic', component: HeicComponent },
  { path: 's8', component: ImgComponent },
  { path: 'email', component: EmailComponent },
  { path: 'image-upload', component: ImgUploadComponent },
  { path: 'logs', component: LogComponent },
  { path: 'spending', component: SpendingComponent },
  { path: 'spending/manage', component: SpendingComponent },
  { path: '**', redirectTo: '/home', pathMatch: 'full' }
]

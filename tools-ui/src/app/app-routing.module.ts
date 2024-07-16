import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { HeicComponent } from './heic/heic.component';
import { ImgComponent } from './img/img.component';
import { HomeComponent } from './home/home.component';
import { EmailComponent } from './email/email.component';
import { ImgUploadComponent } from './img-upload/img-upload.component';
import { LogComponent } from './log/log.component';


const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'clipboard', component: ClipboardComponent },
  { path: 'heic', component: HeicComponent },
  { path: 's8', component: ImgComponent },
  { path: 'email', component: EmailComponent },
  { path: 'image-upload', component: ImgUploadComponent },
  { path: 'logs', component: LogComponent },
  { path: '**', redirectTo: '/home', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes,
    {
      anchorScrolling: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

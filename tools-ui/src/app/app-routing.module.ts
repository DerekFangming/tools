import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { HeicComponent } from './heic/heic.component';
import { ImgComponent } from './img/img.component';
import { CrlLabComponent } from './crl-lab/crl-lab.component';
import { HomeComponent } from './home/home.component';
import { EmailComponent } from './email/email.component';


const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'clipboard', component: ClipboardComponent },
  { path: 'heic', component: HeicComponent },
  { path: 's8', component: ImgComponent },
  { path: 'crl', component: CrlLabComponent },
  { path: 'email', component: EmailComponent },
  { path: '**', redirectTo: '/home', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

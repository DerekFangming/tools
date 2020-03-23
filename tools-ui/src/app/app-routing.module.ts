import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { HeicComponent } from './heic/heic.component';


const routes: Routes = [
  { path: 'clipboard', component: ClipboardComponent },
  { path: 'heic', component: HeicComponent },
  { path: '**', redirectTo: '/clipboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

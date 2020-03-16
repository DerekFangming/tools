import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ClipboardComponent } from './clipboard/clipboard.component';


const routes: Routes = [
  { path: 'clipboard', component: ClipboardComponent },
  { path: '**', redirectTo: '/clipboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

import { Routes } from '@angular/router'
import { ClipboardComponent } from './clipboard/clipboard.component'
import { HeicComponent } from './heic/heic.component'
import { ImgComponent } from './img/img.component'
import { HomeComponent } from './home/home.component'
import { EmailComponent } from './email/email.component'
import { ImgUploadComponent } from './img-upload/img-upload.component'
import { LogComponent } from './log/log.component'
import { SpendingComponent } from './spending/spending.component'
import { RealEstateComponent } from './real-estate/real-estate.component'
import { DocumentComponent } from './document/document.component'
import { RecipeComponent } from './recipe/recipe.component'

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'clipboard', component: ClipboardComponent },
  { path: 'heic', component: HeicComponent },
  { path: 's8', component: ImgComponent },
  { path: 'email', component: EmailComponent },
  { path: 'image-upload', component: ImgUploadComponent },
  { path: 'logs', component: LogComponent },
  { path: 'real-estate', component: RealEstateComponent },
  { path: 'spending', component: SpendingComponent },
  { path: 'spending/manage', component: SpendingComponent },
  { path: 'document', component: DocumentComponent },
  { path: 'document/manage', component: DocumentComponent },
  { path: 'recipes', redirectTo: 'recipes/categories/chinese-wheaten'},
  { path: 'recipes/categories/:category', component: RecipeComponent },
  { path: 'recipes/:id', component: RecipeComponent },
  { path: '**', redirectTo: '/home', pathMatch: 'full' }
]

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { MonacoEditorModule } from 'ngx-monaco-editor';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HeicComponent } from './heic/heic.component';
import { ImgComponent } from './img/img.component';
import { CrlLabComponent } from './crl-lab/crl-lab.component';
import { HomeComponent } from './home/home.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AuthenticationInterceptor } from './authentication-interceptor';

@NgModule({
  declarations: [
    AppComponent,
    ClipboardComponent,
    HeicComponent,
    ImgComponent,
    CrlLabComponent,
    HomeComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    MonacoEditorModule.forRoot(),
    NgbModule
  ],
  providers: [{
    provide: HTTP_INTERCEPTORS,
    useClass: AuthenticationInterceptor,
    multi: true
  }],
  bootstrap: [AppComponent]
})
export class AppModule { }

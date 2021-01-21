import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HeicComponent } from './heic/heic.component';
import { ImgComponent } from './img/img.component';
import { CrlLabComponent } from './crl-lab/crl-lab.component';
import { HomeComponent } from './home/home.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AuthenticationInterceptor } from './authentication-interceptor';
import { FormsModule } from '@angular/forms';
import { EmailComponent } from './email/email.component';
import { ImgUploadComponent } from './img-upload/img-upload.component';
import { DiscordComponent } from './discord/discord.component';
import { NotifierModule } from 'angular-notifier';
import { APP_BASE_HREF } from '@angular/common';
import { getBaseLocation } from './utils.service';

@NgModule({
  declarations: [
    AppComponent,
    ClipboardComponent,
    HeicComponent,
    ImgComponent,
    CrlLabComponent,
    HomeComponent,
    EmailComponent,
    ImgUploadComponent,
    DiscordComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    NgbModule,
    FormsModule,
    NotifierModule.withConfig({
      behaviour: {
        autoHide: 10000
      }
    })
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthenticationInterceptor,
      multi: true
    },
    {
      provide: APP_BASE_HREF,
      useFactory: getBaseLocation
    }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }

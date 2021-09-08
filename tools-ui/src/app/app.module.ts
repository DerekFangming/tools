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
import { DiscordLogComponent } from './discord-log/discord-log.component';
import { NotifierModule } from 'angular-notifier';
import { DiscordConfigComponent } from './discord-config/discord-config.component';
import { DiscordUserComponent } from './discord-user/discord-user.component';
import { DiscordRoleComponent } from './discord-role/discord-role.component';
import { DiscordAdminComponent } from './discord-admin/discord-admin.component';
import { DiscordChannelComponent } from './discord-channel/discord-channel.component';

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
    DiscordLogComponent,
    DiscordConfigComponent,
    DiscordUserComponent,
    DiscordRoleComponent,
    DiscordAdminComponent,
    DiscordChannelComponent
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
    }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }

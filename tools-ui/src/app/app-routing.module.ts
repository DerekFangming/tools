import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { HeicComponent } from './heic/heic.component';
import { ImgComponent } from './img/img.component';
import { CrlLabComponent } from './crl-lab/crl-lab.component';
import { HomeComponent } from './home/home.component';
import { EmailComponent } from './email/email.component';
import { ImgUploadComponent } from './img-upload/img-upload.component';
import { DiscordLogComponent } from './discord-log/discord-log.component';
import { DiscordUserComponent } from './discord-user/discord-user.component';
import { DiscordRoleComponent } from './discord-role/discord-role.component';
import { DiscordConfigComponent } from './discord-config/discord-config.component';
import { DiscordAdminComponent } from './discord-admin/discord-admin.component';
import { DiscordChannelComponent } from './discord-channel/discord-channel.component';


const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'discord', component: DiscordUserComponent},
  { path: 'discord/log', component: DiscordLogComponent},
  { path: 'discord/role', component: DiscordRoleComponent},
  { path: 'discord/channel', component: DiscordChannelComponent},
  { path: 'discord/config', component: DiscordConfigComponent},
  { path: 'discord/admin', component: DiscordAdminComponent},
  { path: 'clipboard', component: ClipboardComponent },
  { path: 'heic', component: HeicComponent },
  { path: 's8', component: ImgComponent },
  { path: 'crl', component: CrlLabComponent },
  { path: 'email', component: EmailComponent },
  { path: 'image-upload', component: ImgUploadComponent },
  { path: '**', redirectTo: '/home', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

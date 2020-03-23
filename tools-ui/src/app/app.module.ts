import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { MonacoEditorModule } from 'ngx-monaco-editor';
import { HttpClientModule } from '@angular/common/http';
import { ImageConverterComponent } from './image-converter/image-converter.component';

@NgModule({
  declarations: [
    AppComponent,
    ClipboardComponent,
    ImageConverterComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    MonacoEditorModule.forRoot()
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

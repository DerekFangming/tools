import { DOCUMENT } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Image, ImageStatus } from '../model/image';

@Component({
  selector: 'app-img-upload',
  templateUrl: './img-upload.component.html',
  styleUrls: ['./img-upload.component.css']
})
export class ImgUploadComponent implements OnInit {

  uploader = true;
  dragOver = false;
  uploading = false;

  imageList: Image[] = [];
  imageUrlList: string[] = [];

  currentUploadIndex = 0;
  clientId = "Q2xpZW50LUlEIDQzMzQzNWRkNjBmNWQ3OQ==";


  constructor(private title: Title, private http: HttpClient, @Inject(DOCUMENT) private document: Document) {
    this.title.setTitle('Image uploader');
  }

  ngOnInit() {
  }

  onUploadClicked() {
    this.uploader = true;
  }

  onBrowseClicked() {
    this.uploader = false;
  }

  onDragOver(event) {
    event.stopPropagation();
    event.preventDefault();
  }

  onDragEnter(event) {
    this.dragOver = true;
    event.preventDefault();
  }

  onDragLeave(event) {
    this.dragOver = false;
    event.preventDefault();
  }

  onImagesDropped(event) {
    this.dragOver = false;
    event.preventDefault();
    this.loadImages(event.dataTransfer.files);

  }

  onImagesSelected(event) {
    event.preventDefault();
    this.loadImages(event.target.files);
  }

  loadImages(files) {
    for (let file of files) {
      let fileName = file.name.toLowerCase();
      if (fileName.endsWith('jpg') || fileName.endsWith('jpeg') || fileName.endsWith('png') || fileName.endsWith('gif')) {
        var reader = new FileReader();
        reader.onload = (event) =>{
          var fileReader = event.target as FileReader;
    
          let image = new Image({status: ImageStatus.New, data: fileReader.result.toString()});
          this.imageList.push(image);
          console.log(this.imageList);
        };
        reader.readAsDataURL(file);
      }
    }
  }

  onUpload() {
    this.imageUrlList = [];
    this.currentUploadIndex = 0;
    this.uploading = true;

    this.uploadNextImage();
  }

  uploadNextImage() {
    if (this.currentUploadIndex > this.imageList.length - 1) {
      console.log(this.imageUrlList);// TODO
      this.uploading = false;
    } else {
      let image = this.imageList[this.currentUploadIndex];
      this.currentUploadIndex ++;

      if (image.status == ImageStatus.Uploaded) {
        this.uploadNextImage();
      } else {
        image.status = ImageStatus.Uploading;
        let parts = image.data.split(',');
        let data = parts[1];
        this.http.post('https://api.imgur.com/3/image', {image: data}, {headers: {'authorization': atob(this.clientId)}}).subscribe(json => {
          image.url = json['data']['link'];
          image.status = ImageStatus.Uploaded;
          this.imageUrlList.push(image.url);
          this.uploadNextImage();
        }, () => {
          image.status = ImageStatus.Failed;
          this.uploadNextImage();
        });
      }
    }
  }

  onClearClicked() {
    this.imageList = [];
  }

  onCopyClicked(url) {
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = url;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

}

import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Image } from '../model/image';

@Component({
  selector: 'app-img-upload',
  templateUrl: './img-upload.component.html',
  styleUrls: ['./img-upload.component.css']
})
export class ImgUploadComponent implements OnInit {

  uploader = true;
  dragOver = false;

  imageList: Image[] = [];

  constructor(private title: Title) {
    this.title.setTitle('Image uploader');
  }

  ngOnInit() {
    // this.imageList.push(new Image());
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
    
          let image = new Image({id: 0, url: '', data: fileReader.result.toString()});
          this.imageList.push(image);
          console.log(this.imageList);
        };
        reader.readAsDataURL(file);
      }
    }
  }

  onFileSelectClicked() {
    console.log(1);
  }

  onClearClicked() {
    this.imageList = [];
  }

  onCopyClicked() {
    console.log(1);
  }

}

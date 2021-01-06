import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-img-upload',
  templateUrl: './img-upload.component.html',
  styleUrls: ['./img-upload.component.css']
})
export class ImgUploadComponent implements OnInit {

  uploader = true;
  dragOver = false;

  imageList: any[]

  constructor(private title: Title) {
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

  onDrop(event) {
    this.dragOver = false;
    event.preventDefault();
    console.log(event);
    let dt = event.dataTransfer;
    let files = dt.files;
    this.imageList = files;
    console.log(files);
    console.log(typeof files[0]);

    var reader = new FileReader();
    reader.onload = (event) =>{
      console.log('readfiles event ===== ',event);
      // var image = new Image();
      var fileReader = event.target as FileReader;
      console.log(fileReader.result);
      // image.src = fileReader.result;
      // image.width = 50; 
      // this.imageDrop.nativeElement.appendChild(image);
    };
    
    reader.readAsDataURL(files[0]);
  }

}

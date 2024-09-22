import { CommonModule, DOCUMENT } from '@angular/common'
import { HttpClient } from '@angular/common/http'
import { Component, Inject, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { Image, ImageStatus } from '../model/image'
import { UtilsService } from '../utils.service'
import { environment } from '../../environments/environment'
import { FormsModule } from '@angular/forms'
import { RouterOutlet } from '@angular/router'

@Component({
  selector: 'app-img-upload',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule],
  templateUrl: './img-upload.component.html',
  styleUrl: './img-upload.component.css'
})
export class ImgUploadComponent implements OnInit {

  uploader = true
  dragOver = false
  uploading = false
  loadingImages = false

  imageList: Image[] = []
  imageUploadedList: Image[] = []
  imageSavedList: Image[] = []

  currentUploadIndex = 0

  constructor(private title: Title, private http: HttpClient, @Inject(DOCUMENT) private document: Document, public utils: UtilsService) {
    this.title.setTitle('Image uploader')
  }

  ngOnInit() {
  }

  onUploadClicked() {
    this.uploader = true;
  }

  onBrowseClicked() {
    this.uploader = false
    this.imageSavedList = []
    this.loadingImages = true
    this.http.get<Image[]>(environment.urlPrefix + 'api/images').subscribe({
      next: (res: Image[]) => {
        this.imageSavedList = res
        this.loadingImages = false
      },
      error: (error: any) => {
        console.log(error)
        this.loadingImages = false
      }
    })
  }

  onDragOver(event: any) {
    event.stopPropagation();
    event.preventDefault();
  }

  onDragEnter(event: any) {
    this.dragOver = true;
    event.preventDefault();
  }

  onDragLeave(event: any) {
    this.dragOver = false;
    event.preventDefault();
  }

  onImagesDropped(event: any) {
    this.dragOver = false;
    event.preventDefault();
    this.loadImages(event.dataTransfer.files);

  }

  onImagesSelected(event: any) {
    event.preventDefault();
    this.loadImages(event.target.files);
  }

  loadImages(files: any) {
    for (let file of files) {
      let fileName = file.name.toLowerCase();
      if (fileName.endsWith('jpg') || fileName.endsWith('jpeg') || fileName.endsWith('png') || fileName.endsWith('gif')) {
        var reader = new FileReader();
        reader.onload = (event) =>{
          var fileReader = event.target as FileReader;
    
          let image = new Image({status: ImageStatus.New, data: fileReader.result!.toString()});
          this.imageList.push(image);
        };
        reader.readAsDataURL(file);
      }
    }
  }

  onUpload() {
    this.imageUploadedList = [];
    this.currentUploadIndex = 0;
    this.uploading = true;

    this.uploadNextImage();
  }

  uploadNextImage() {
    if (this.currentUploadIndex > this.imageList.length - 1) {
      this.uploading = false
      this.http.post<Image[]>(environment.urlPrefix + 'api/images/bulk', this.imageUploadedList).subscribe({next:() => {}, error:() => {}})
    } else {
      let image = this.imageList[this.currentUploadIndex]
      this.currentUploadIndex ++

      if (image.status == ImageStatus.Uploaded) {
        this.uploadNextImage()
      } else {
        image.status = ImageStatus.Uploading
        let parts = image.data!.split(',')
        let data = parts[1]
        this.http.post('https://api.imgur.com/3/image', {image: data}, {headers: {'authorization': this.utils.getClientId()}}).subscribe({
          next: (res: any) => {
            image.url = res['data']['link']
            image.status = ImageStatus.Uploaded
            this.imageUploadedList.push(image)
            this.uploadNextImage()
          },
          error: (error: any) => {
            console.log(error)
            image.status = ImageStatus.Failed
            this.uploadNextImage()
          }
        })
      }
    }
  }

  onClearClicked() {
    this.imageList = [];
  }

  onCopyClicked(url: any) {
    navigator.clipboard.writeText(url).then().catch(e => console.error(e))
  }

}

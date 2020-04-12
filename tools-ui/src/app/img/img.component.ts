import { Component, OnInit, HostListener } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Post } from '../model/post';

@Component({
  selector: 'app-img',
  templateUrl: './img.component.html',
  styleUrls: ['./img.component.css']
})
export class ImgComponent implements OnInit {

  posts: Post[];
  previewedImgSrc: string;

  loadingNextPage = false;
  showingIFrame = false;
  showingPreview = false;

  constructor(private http: HttpClient, private title: Title) {
    this.title.setTitle('Img');
    this.loadNextPage();
  }

  ngOnInit() {
  }

  loadNextPage() {
    this.loadingNextPage = true;
    this.http.get<Post[]>(environment.urlPrefix + 'api/posts').subscribe(posts => {
      this.loadingNextPage = false;
      this.posts = posts;
    }, error => {
      this.loadingNextPage = false;
      console.log(error.error);
    });
  }

  nextBtnClicked() {
    this.posts = [];
    this.loadingNextPage = true;
    var idList = this.posts.map(p => p.id);
    
    this.http.put(environment.urlPrefix + 'api/posts', idList).subscribe(() => {
      this.loadNextPage();
    }, error => {
      this.loadingNextPage = false;
      console.log(error.error);
    });
  }

  @HostListener('document:keydown.escape', ['$event'])
  onKeydownHandler(event: KeyboardEvent) {
    if (this.showingPreview) {
      this.closePreview();
    } else {
      this.showingIFrame = !this.showingIFrame;
    }
  }

  openPreview(src: string) {
    this.previewedImgSrc = src;
    document.getElementById('s8ShowPreviewButton').click();
    this.showingPreview = true;
  }

  closePreview() {
    document.getElementById('s8HidePreviewButton').click();
    this.showingPreview = false;
  }

  getCategory(forumId: number) {
    switch (forumId) {
      case 798:
        return 'Hua'
      case 96:
        return 'Asian';
      case 427:
        return 'Cloud Fast';
      case 103:
        return 'Cloud';
      case 135:
        return 'U.S.';
      case 136:
        return 'Dong';
      default:
        return 'Unknown id: ' + forumId;
    }
  }

  getRankTheme(rank: number) {
    switch (rank) {
      case 0:
        return 'badge-secondary'
      case 1:
        return 'badge-info';
      case 2:
        return 'badge-warning';
      case 3:
        return 'badge-danger';
      default:
        return 'badge-dark';
    }
  }

  getRankName(rank: number) {
    switch (rank) {
      case 0:
        return 'Normal'
      case 1:
        return 'Day';
      case 2:
        return 'Week';
      case 3:
        return 'Month';
      default:
        return 'Unknown rank: ' + rank;
    }
  }

}

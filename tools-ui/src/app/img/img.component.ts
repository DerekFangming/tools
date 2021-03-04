import { Component, OnInit, HostListener } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Post, PostCatMap } from '../model/post';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-img',
  templateUrl: './img.component.html',
  styleUrls: ['./img.component.css']
})
export class ImgComponent implements OnInit {

  posts: Post[];
  remainingPosts: string;
  previewedImgSrc: string;

  loadingNextPage = false;
  showingIFrame = false;
  showingPreview = false;

  mode = 'all';
  category = PostCatMap.All;
  categories = Object.keys(PostCatMap).filter(value => isNaN(Number(value)));

  reloadBtnText = 'Reload';
  urlPrefix = environment.urlPrefix;

  constructor(private http: HttpClient, private title: Title, private activatedRoute: ActivatedRoute, private router: Router) {
    this.title.setTitle('Images');
    this.mode = this.activatedRoute.snapshot.queryParamMap.get('mode');
    this.category = Number(this.activatedRoute.snapshot.queryParamMap.get('category'));
    this.loadPosts();
  }

  ngOnInit() {
  }

  loadPosts() {
    this.loadingNextPage = true;
    const httpOptions = {
      params: new HttpParams().set('mode', this.mode).set('category', this.category.toString()),
      observe: 'response' as 'response'
    };
    this.http.get<Post[]>(environment.urlPrefix + 'api/posts', httpOptions).subscribe(res => {
      this.loadingNextPage = false;
      this.posts = res.body;
      this.remainingPosts = res.headers.get('X-Total-Count');
    }, error => {
      this.loadingNextPage = false;
      console.log(error.error);
    });
  }

  nextBtnClicked() {
    this.loadingNextPage = true;
    var idList = this.posts.map(p => p.id);
    this.posts = [];
    
    let url = 'api/posts/mark-read'
    if (this.mode == 'saved') {
      url = 'api/posts/mark-unsaved'
    }
    
    this.http.put(environment.urlPrefix + url, idList).subscribe(() => {
      this.loadPosts();
    }, error => {
      this.loadingNextPage = false;
      console.log(error.error);
    });
  }

  reloadBtnClicked() {
    this.http.get(environment.urlPrefix + 'api/posts/reload').subscribe(() => {
      this.reloadBtnText = 'Reloaded'
    }, error => {
      console.log(error.error);
    });
  }

  saveBtnClicked(post: Post) {
    this.http.put(environment.urlPrefix + 'api/posts/mark-saved', post.id).subscribe(() => {
      post.saved = true;
    }, error => {
      console.log(error.error);
    });
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeClicked(event: KeyboardEvent) {
    if (this.showingPreview) {
      this.closePreview();
    } else {
      this.showingIFrame = !this.showingIFrame;
    }
  }
  
  @HostListener('document:keydown.f1', ['$event'])
  onKeydownHandler(event: KeyboardEvent) {
    this.nextBtnClicked();
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
    return PostCatMap[forumId];
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

  getCreatedTime(time: string) {
    return new Date(time).toLocaleString();
  }

  modeChanged(mode: string) {
    if (this.mode != mode) {
      this.mode = mode;
      this.loadPosts();

      this.router.navigate([], {
        relativeTo: this.activatedRoute,
        queryParams: { mode: mode },
        queryParamsHandling: 'merge'
      });
    }
  }

  categoryChanged(categoryString: string) {
    let category = PostCatMap[categoryString];
    if (this.category != category) {
      this.category = category;
      this.loadPosts();

      this.router.navigate([], {
        relativeTo: this.activatedRoute,
        queryParams: { category: category },
        queryParamsHandling: 'merge'
      });
    }
  }

}

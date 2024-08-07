import { Component, OnInit, HostListener } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { HttpClient, HttpParams } from '@angular/common/http'
import { Post, PostCatMap } from '../model/post'
import { ActivatedRoute, Router, RouterModule, RouterOutlet } from '@angular/router'
import { DomSanitizer } from '@angular/platform-browser'
import { environment } from '../../environments/environment'
import { CommonModule } from '@angular/common'
import { FormsModule } from '@angular/forms'

declare var $: any

@Component({
  selector: 'app-img',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule],
  templateUrl: './img.component.html',
  styleUrl: './img.component.css'
})
export class ImgComponent implements OnInit {

  posts: Post[] = []
  remainingPosts: string | undefined
  previewedImgSrc: string | undefined

  loadingNextPage = false
  showingIFrame = false
  showingPreview = false

  postSection = 0
  imgLimit = 12

  mode = 'all'
  category = PostCatMap['All']
  categories = Object.entries(PostCatMap)

  reloadBtnText = 'Reload'
  nextBtnText = 'Next Post'
  urlPrefix = environment.urlPrefix

  constructor(private http: HttpClient, private title: Title, private activatedRoute: ActivatedRoute, private router: Router, public domSanitizer: DomSanitizer) {
    this.title.setTitle('Images')
    this.mode = this.activatedRoute.snapshot.queryParamMap.get('mode') ?? 'all'
    this.category = Number(this.activatedRoute.snapshot.queryParamMap.get('category'))
    this.loadPosts()
  }

  ngOnInit() {
  }

  loadPosts() {
    this.loadingNextPage = true;
    const httpOptions = {
      params: new HttpParams().set('mode', this.mode).set('category', this.category.toString()),
      observe: 'response' as 'response'
    };
    this.http.get<Post[]>(environment.urlPrefix + 'api/posts', httpOptions).subscribe({
      next: (res: any) => {
        this.loadingNextPage = false
        this.posts = res.body
        this.remainingPosts = res.headers.get('X-Total-Count')
  
        this.postSection = 0
        this.router.navigate([], {
          relativeTo: this.activatedRoute,
          queryParams: { mode: this.mode },
          queryParamsHandling: 'merge'
        })
      },
      error: (error: any) => {
        this.loadingNextPage = false
        console.log(error)
      }
    })
  }

  nextPostBtnClicked(forward = true) {
    if (forward && this.postSection + 1 >= this.posts.length) {
      return this.nextPageBtnClicked()
    }

    if (!forward && this.postSection == 0) {
      return
    }

    this.postSection = forward ? this.postSection + 1 : this.postSection - 1

    
    let el = document.getElementById(`post-${this.postSection}`)
    el?.scrollIntoView()
  }

  nextPageBtnClicked() {
    this.loadingNextPage = true;
    var idList = this.posts.map(p => p.id);
    this.posts = [];

    let url = 'api/posts/mark-read'
    if (this.mode == 'saved') {
      url = 'api/posts/mark-unsaved'
    }

    this.http.put(environment.urlPrefix + url, idList).subscribe({
      next: () => {
        this.loadPosts()
      },
      error: (error: any) => {
        this.loadingNextPage = false
        console.log(error)
      }
    })
  }

  reloadBtnClicked() {
    this.http.get(environment.urlPrefix + 'api/posts/reload').subscribe({
      next: () => {
        this.reloadBtnText = 'Reloaded'
      },
      error: (error: any) => {
        console.log(error)
      }
    })
  }

  saveBtnClicked(post: Post) {
    this.http.put(environment.urlPrefix + 'api/posts/mark-saved', post.id).subscribe({
      next: () => {
        post.saved = true
      },
      error: (error: any) => {
        console.log(error)
      } 
    })
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeClicked(event: KeyboardEvent) {
    $("#s8Preview").modal('hide')
    this.showingIFrame = !this.showingIFrame;
  }

  @HostListener('document:keydown.f1', ['$event'])
  onKeydownHandler(event: KeyboardEvent) {
    this.nextPostBtnClicked()
  }

  @HostListener('document:keydown.ArrowLeft', ['$event'])
  onLeftKeydownHandler(event: KeyboardEvent) {
    this.nextPostBtnClicked(false)
  }

  @HostListener('document:keydown.ArrowRight', ['$event'])
  onRightKeydownHandler(event: KeyboardEvent) {
    this.nextPostBtnClicked()
  }


  openPreview(src: string) {
    this.previewedImgSrc = src
    $("#s8Preview").modal('show')
  }

  getCategory(forumId: number) {
    return this.categories.filter(c => c[1] == forumId)[0][0]
  }

  getRankTheme(rank: number) {
    switch (rank) {
      case 0:
        return 'bg-secondary'
      case 1:
        return 'bg-info';
      case 2:
        return 'bg-warning';
      case 3:
        return 'bg-danger';
      default:
        return 'bg-dark';
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

  categoryChanged(category: number) {
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

  getImageNames(post: Post) {
    let host = environment.production ? 'https://simg.fmning.com/images/' : 'http://10.0.1.50:9102/images/'
    let imgs = post.imageNames.length == 0 ? post.imageUrls : post.imageNames.map(n => host + post.id + '/' + n)

	  return post.expanded ? imgs : imgs.slice(0, this.imgLimit)
	}

}

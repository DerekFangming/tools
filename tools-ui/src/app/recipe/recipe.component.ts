import { CommonModule } from '@angular/common'
import { HttpClient, HttpParams } from '@angular/common/http'
import { AfterViewInit, Component, OnDestroy } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { Title } from '@angular/platform-browser'
import { RouterOutlet, RouterModule, Router, ActivatedRoute, NavigationEnd } from '@angular/router'
import { NotificationsService } from 'angular2-notifications'
import { UtilsService } from '../utils.service'
import { Subscription } from 'rxjs'
import { environment } from '../../environments/environment'
import { Recipe, RecipeCategory } from '../model/recipe'
import { MarkdownModule, MarkdownService } from 'ngx-markdown'
import { AngularMarkdownEditorModule, EditorOption } from 'angular-markdown-editor'

declare var $: any

@Component({
  selector: 'app-recipe',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule, RouterModule, MarkdownModule, AngularMarkdownEditorModule],
  templateUrl: './recipe.component.html',
  styleUrl: './recipe.component.css'
})
export class RecipeComponent implements OnDestroy, AfterViewInit {

  loading = false
  editing = false
  isDirty = false
  uploadingImage = false
  routerSubscription: Subscription | undefined
  category: string | null = null
  recipeId: string | null = null
  
  recipeList: Recipe[] = []
  recipe: Recipe = new Recipe()
  recipeCategories: RecipeCategory[] = [{url: 'chinese-wheaten', label: '中式面食', value: 'CHINESE_WHEATEN'}, {url: 'chinese', label: '中餐', value: 'CHINESE'},
    {url: 'korean', label: '韩餐', value: 'KOREAN'}, {url: 'western', label: '西餐', value: 'WESTERN'}, {url: 'other', label: '其他', value: 'OTHER'}]

  editorOptions: EditorOption = {
    autofocus: false,
    iconlibrary: 'fa',
    savable: false,
    height: '700',
    enableDropDataUri: true,
    parser: (val) => this.parse(val),
    onChange: (e) => this.processContent(e),
    onFocus: (e) => this.processContent(e)
  }

  constructor(private http: HttpClient, private title: Title, private notifierService: NotificationsService,
    public utils: UtilsService, private route: ActivatedRoute, private router: Router, private markdownService: MarkdownService) {
    this.title.setTitle('Recipes')

    this.routerSubscription = this.router.events.subscribe((val) => {
      if (val instanceof NavigationEnd) {
        this.loadData()
      }
    })
  }

  ngAfterViewInit() {
    this.loadData()
  }

  ngOnDestroy(): void {
    this.routerSubscription?.unsubscribe()
  }

  loadData() {
    this.editing = false
    this.isDirty = false
    this.category = this.route.snapshot.paramMap.get('category')
    this.recipeId = this.route.snapshot.paramMap.get('id')
    if (this.category) {
      let params = new HttpParams().set('category', this.recipeCategories.find(c => c.url == this.category)?.value ?? '')
      this.loading = true
      this.http.get<Recipe[]>(environment.urlPrefix + `api/recipes`, {params: params}).subscribe({
        next: (res: Recipe[]) => {
          this.loading = false
          this.recipeList = res
        },
        error: (error: any) => {
          this.loading = false
          this.notifierService.error('Error', 'Failed to list')
        }
      })

    } else if (this.recipeId) {
      this.loading = true
      this.http.get<Recipe>(environment.urlPrefix + `api/recipes/${this.recipeId}`).subscribe({
        next: (res: Recipe) => {
          this.loading = false
          this.recipe = res
        },
        error: (error: any) => {
          this.loading = false
          this.notifierService.error('Error', 'Failed to get')
        }
      })
    }
  }

  getCreatedTime(time: string | undefined) {
    return new Date(time ?? '').toLocaleString()
  }

  addRecipe() {
    this.loading = true
    this.http.post<any>(environment.urlPrefix + `api/recipes/editing`, {}).subscribe({
      next: (res: any) => {
        this.loading = false
        this.recipe = new Recipe()
        this.category = null
        this.recipeId = null
        this.editing = true
      },
      error: (error: any) => {
        console.log(error)
        this.loading = false
        this.notifierService.error('Error', 'Do not have access to add recipe')
      }
    })
  }

  editRecipe() {
    this.loading = true
    this.http.post<any>(environment.urlPrefix + `api/recipes/editing`, {}).subscribe({
      next: (res: any) => {
        this.loading = false
        this.category = null
        this.recipeId = null
        this.editing = true
      },
      error: (error: any) => {
        console.log(error)
        this.loading = false
        this.notifierService.error('Error', 'Do not have access to edit recipe')
      }
    })
  }

  parse(inputValue: string) {
    const markedOutput = this.markdownService.parse(inputValue.trim())
    setTimeout(() => {
      this.markdownService.highlight()
    })

    return markedOutput
  }
  
  processContent(e: any) {
    this.isDirty = true
    let content = e.getContent()

    let matches = content.matchAll(/<img src="(.*?)" \/>/gm)
    let next = matches.next()

    if (next.done) {
      return
    }
    
    while (!next.done) {
      let uploadId = `<ImageUploading-${this.utils.uuid()}/>`
      content = content.replace(next.value[0], uploadId)

      // Upload and replace image
      let parts = next.value[1].split(',')
      this.uploadingImage = true
      this.http.post('https://api.imgur.com/3/image', {image: parts[1]}, {headers: {'authorization': this.utils.getClientId()}}).subscribe({
        next: (res: any) => {
          this.uploadingImage = false
          let imgMarkdown = `![Image Description](${res['data']['link']})`

          let latestContent = e.getContent()
          latestContent = latestContent.replace(uploadId, imgMarkdown)
          e.setContent(latestContent)
        },
        error: (error: any) => {
          this.uploadingImage = false
          console.log(error)
          this.notifierService.error('Error', 'Failed to upload thubnail')
        }
      })

      // Check if there are more matches
      next = matches.next()
    }
    
    e.setContent(content)
  }

  onImagesSelected(event: any) {
    event.preventDefault()
    this.isDirty = true
    for (let file of event.target.files) {
      let fileName = file.name.toLowerCase()
      if (fileName.endsWith('jpg') || fileName.endsWith('jpeg') || fileName.endsWith('png') || fileName.endsWith('gif')) {
        var reader = new FileReader()
        reader.onload = (event) =>{
          var fileReader = event.target as FileReader
          this.recipe.thumbnail = fileReader.result!.toString()

          let parts = this.recipe.thumbnail.split(',')
          
          this.uploadingImage = true
          this.http.post('https://api.imgur.com/3/image', {image: parts[1]}, {headers: {'authorization': this.utils.getClientId()}}).subscribe({
            next: (res: any) => {
              this.uploadingImage = false
              this.recipe.thumbnail = res['data']['link']
            },
            error: (error: any) => {
              this.uploadingImage = false
              this.notifierService.error('Error', 'Failed to upload thubnail')
            }
          })
        }
        reader.readAsDataURL(file)
      }
    }
  }

  onCategorySelected(category: RecipeCategory) {
    this.isDirty = true
    this.recipe.category = category.value
  }

  getCategoryLabel() {
    return this.recipeCategories.find(c => c.value == this.recipe.category)?.label
  }

  cancelRecipe() {
    if (this.isDirty) {
      $('#cancelConfirmationModal').modal('show')
    } else {
      this.reloadMainPage()
    }
  }

  reloadMainPage() {
    this.category = this.recipeCategories[0].url
    this.loadData()
  }

  saveRecipe() {
    if (this.uploadingImage) {
      this.notifierService.error('Error', 'Uploading images. Please try again later')
      return
    } else if (this.recipe.name == null) {
      this.notifierService.error('Error', 'Recipe title is required')
      return
    } else if (this.recipe.category == null) {
      this.notifierService.error('Error', 'Recipe category is required')
      return
    } else if (this.recipe.content == null) {
      this.notifierService.error('Error', 'Recipe content is required')
      return
    }

    if (this.recipe.id == null) {
      this.loading = true
      this.http.post<Recipe>(environment.urlPrefix + `api/recipes`, this.recipe).subscribe({
        next: (res: any) => {
          this.loading = false
          this.reloadMainPage()
        },
        error: (error: any) => {
          console.log(error)
          this.loading = false
          this.notifierService.error('Error', 'Failed to create recipe')
        }
      })
    } else {
      this.loading = true
      this.http.put<Recipe>(environment.urlPrefix + `api/recipes/${this.recipe.id}`, this.recipe).subscribe({
        next: (res: any) => {
          this.loading = false
          this.reloadMainPage()
        },
        error: (error: any) => {
          console.log(error)
          this.loading = false
          this.notifierService.error('Error', 'Failed to save recipe')
        }
      })
    }
  }

}

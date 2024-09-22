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
import { Recipe } from '../model/recipe'
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
  routerSubscription: Subscription | undefined
  category: string | null = null
  recipeId: string | null = null
  
  recipeList: Recipe[] = []
  recipe: Recipe = new Recipe()

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
      let params = new HttpParams().set('category', this.category.replace('-', '_').toUpperCase())
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
          console.log(this.recipe.content)
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
    this.recipe = new Recipe()
    this.category = null
    this.recipeId = null
    this.editing = true
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
    if (!/<img src="(.*?)" \/>/gm.test(content)) {
      this.recipe.content = content
      return
    }

    content = content.replace(/<img src="(.*?)" \/>/gm, '<IMAGE IS REPLACED HERE />')
    this.recipe.content = content
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
        }
        reader.readAsDataURL(file)
      }
    }
  }

  cancelRecipe() {
    if (this.isDirty) {
      $('#cancelConfirmationModal').modal('show')
    } else {
      this.confirmCancelRecipe()
    }
  }

  confirmCancelRecipe() {
    this.category = 'chinese-wheaten'
    this.loadData()
  }

}

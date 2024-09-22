export class Recipe {
  id: number | undefined
  name: string | undefined
  category: string | undefined
  thumbnail: string | undefined
  content: string | undefined
  created: string | undefined
}

export class RecipeCategory {
  url!: string
  label!: string
  value!: string
}

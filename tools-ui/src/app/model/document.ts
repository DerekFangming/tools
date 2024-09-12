export class Document {
  id: number | undefined
  name: string | undefined
  owner: string | undefined
  expirationDate: string | undefined
  images: string[] | undefined

  constructor() {
    this.images = []
  }
}
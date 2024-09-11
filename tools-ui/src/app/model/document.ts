export class Document {
  id: string | undefined
  name: string | undefined
  owner: string | undefined
  expirationDate: string | undefined
  images: string[] | undefined

  constructor() {
    this.images = []
  }
}
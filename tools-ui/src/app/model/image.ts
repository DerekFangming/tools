export class Image {
  id: number | undefined
  url: string | undefined
  data: string | undefined
  created: string | undefined
  status: ImageStatus | undefined

  public constructor(init?:Partial<Image>) {
    Object.assign(this, init)
  }
}

export enum ImageStatus {
  New = 0,
  Uploading = 1,
  Uploaded = 2,
  Failed = 3
}
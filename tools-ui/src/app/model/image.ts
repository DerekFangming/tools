export class Image {
  id: number;
  url: string;
  data: string;
  status: ImageStatus;

  public constructor(init?:Partial<Image>) {
    Object.assign(this, init);
  }
}

export enum ImageStatus {
  New = 0,
  Uploading = 1,
  Uploaded = 2,
  Failed = 3
}
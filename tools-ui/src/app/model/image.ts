export class Image {
  id: number;
  url: string;
  data: string;

  public constructor(init?:Partial<Image>) {
    Object.assign(this, init);
  }
}
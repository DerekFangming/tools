export class DiscordObject {
  id: string;
  name: string;

  public constructor(init?:Partial<DiscordObject>) {
    Object.assign(this, init);
}
}
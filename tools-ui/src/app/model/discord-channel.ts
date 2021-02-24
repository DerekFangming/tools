export class DiscordChannel {
  id: string;
  name: string;
  categoryId: string;
  type: string;
  position: number;
  ownerName: string;

  public constructor(init?:Partial<DiscordChannel>) {
    Object.assign(this, init);
  }
}
export class DiscordCategory {
    id: string;
    name: string;
    position: number;
  
    public constructor(init?:Partial<DiscordCategory>) {
      Object.assign(this, init);
    }
  }
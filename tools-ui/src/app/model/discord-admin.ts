export class DiscordAdmin {
  memberId: string;
  roleId: string;
  position: Number;

  public constructor(init?:Partial<DiscordAdmin>) {
    Object.assign(this, init);
  }
}
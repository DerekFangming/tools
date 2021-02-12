export class DiscordAdmin {
  memberId: string;
  roleId: string;

  public constructor(init?:Partial<DiscordAdmin>) {
    Object.assign(this, init);
  }
}
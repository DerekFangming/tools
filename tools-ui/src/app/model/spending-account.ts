export class SpendingAccount {
  id: number
  name: string
  identifier: string
  icon: string
  owner: string;

  public constructor(init?:Partial<SpendingAccount>) {
    Object.assign(this, init);
  }
}
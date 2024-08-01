export class SpendingAccount {
  id: number | undefined
  name: string | undefined
  identifier: string | undefined
  icon: string | undefined
  owner: string | undefined

  public constructor(init?:Partial<SpendingAccount>) {
    Object.assign(this, init)
  }
}
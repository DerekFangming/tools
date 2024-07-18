export class SpendingTransaction {
  id: number
  accountId: number
  identifier: string
  name: string
  amount: string
  category: string
  location: string
  date: string

  public constructor(init?:Partial<SpendingTransaction>) {
    Object.assign(this, init)
  }
}
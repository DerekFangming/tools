export class SpendingTransaction {
  id: number
  accountId: number
  identifier: string
  name: string
  originalName: string
  amount: string
  category: string
  location: string
  date: string

  error: boolean

  public constructor(init?:Partial<SpendingTransaction>) {
    Object.assign(this, init)
  }
}
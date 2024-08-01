export class SpendingTransaction {
  id: number | undefined
  accountId: number | undefined
  identifier: string | undefined
  name: string | undefined
  originalName: string | undefined
  amount: string | undefined
  category: string | undefined
  location: string | undefined
  date: string | undefined

  error: boolean | undefined

  public constructor(init?:Partial<SpendingTransaction>) {
    Object.assign(this, init)
  }
}
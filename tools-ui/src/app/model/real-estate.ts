export class RealEstate {
  zid: string | undefined
  label: string | undefined
  rate: number | undefined
  monthly: number | undefined
  history: RealEstateHistory[] | undefined
}

export class RealEstateHistory {
  date: string | undefined
  value: number | undefined
  balance: number | undefined
}

export class CrlBorrowerLog {
  id: number;
  equipmentId: number;
  name: string;
  utEid: string;
  borrowDate: string;
  returnDate: string;

	constructor(id: number, equipmentId: number, name: string, utEid: string, borrowDate: string, returnDate: string) {
    this.id = id;
    this.equipmentId = equipmentId;
    this.name = name;
    this.utEid = utEid;
    this.borrowDate = borrowDate;
    this.returnDate = returnDate;
  }
    
  static empty() {
    return new CrlBorrowerLog(0, 0, '', '', '', '');
  }

  public trimAll() {
    this.name = this.name.trim();
    this.utEid = this.utEid.trim();
  }
}
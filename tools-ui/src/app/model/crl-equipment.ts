export class CrlEquipment {
  id: number;
  name: string;
  description: string;
  serialNumber: string;
  borrower: string;

  isHidden = false;


	constructor(id: number, name: string, description: string, serialNumber: string, borrower: string) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.serialNumber = serialNumber;
    this.borrower = borrower;
  }
    
  static empty() {
    return new CrlEquipment(0, '', '', '', '');
  }

  public trimAll() {
    this.name = this.name.trim();
    this.description = this.description.trim();
    this.serialNumber = this.serialNumber.trim();
  }
}
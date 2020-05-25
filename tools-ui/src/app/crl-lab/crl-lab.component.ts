import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { CrlEquipment } from '../model/crl-equipment';
import { environment } from '../../environments/environment';
import { NgbModalRef, NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { CrlBorrowerLog } from '../model/crl-borrower-log';

@Component({
  selector: 'app-crl-lab',
  templateUrl: './crl-lab.component.html',
  styleUrls: ['./crl-lab.component.css']
})
export class CrlLabComponent implements OnInit {

  equipmentList: CrlEquipment[];
  newEquipment = CrlEquipment.empty();
  borrowEquipment: CrlEquipment;
  newBorrowerLog = CrlBorrowerLog.empty();

  formError = '';
  loadingEquipment = true;
  addingEquipment = false;
  borrowingEquipment = false;

  modalRef: NgbModalRef;
  @ViewChild('addEquipmentModal', { static: true}) addEquipmentModal: TemplateRef<any>;
  @ViewChild('borrowEquipmentModal', { static: true}) borrowEquipmentModal: TemplateRef<any>;
  ngbModalOptions: NgbModalOptions = {
    backdrop : 'static',
    keyboard : false,
    centered: true
  };

  constructor(private http: HttpClient, private title: Title, private modalService: NgbModal) {
    this.title.setTitle('CRL lab');
    this.http.get<CrlEquipment[]>(environment.urlPrefix + 'api/crl/equipment').subscribe(equipmentList => {
      this.loadingEquipment = false;
      this.equipmentList = equipmentList;
    }, error => {
      this.loadingEquipment = false;
      console.log(error.error);
    });
  }

  ngOnInit() {
  }

  onSearchChange(searchValue: string): void {  
    this.equipmentList.forEach(e => {
      if (searchValue == null || searchValue.trim() == ''){
        e.isHidden = false;
      } else if (e.name.includes(searchValue.trim())) {
        e.isHidden = false;
      } else if (e.borrower != null && e.borrower.includes(searchValue.trim())) {
        e.isHidden = false;
      } else {
        e.isHidden = true;
      }
    })
  }

  onAddBtnClicked() {
    this.formError = '';
    this.newEquipment = CrlEquipment.empty();
    this.modalRef = this.modalService.open(this.addEquipmentModal, this.ngbModalOptions);
  }

  onBorrowBtnClicked(equipment: CrlEquipment) {
    this.formError = '';
    this.borrowEquipment = equipment;
    this.newBorrowerLog = CrlBorrowerLog.empty(); 
    this.modalRef = this.modalService.open(this.borrowEquipmentModal, this.ngbModalOptions);
  }

  onAdd() {
    this.newEquipment.trimAll();
    if (this.newEquipment.name == '') {
      this.formError = 'Equipment name cannot be empty.';
    } else if (this.newEquipment.serialNumber == '') {
      this.formError = 'Serial number cannot be empty.';
    } else {
      this.formError = '';
      this.addingEquipment = true;
      this.http.post<CrlEquipment>(environment.urlPrefix + 'api/crl/equipment', this.newEquipment).subscribe(equipment => {
        this.addingEquipment = false;
        this.equipmentList.push(equipment);
        this.modalRef.close();
      }, error => {
        this.addingEquipment = false;
        console.log(error.error);
        this.formError = error.error;
      });
    }
  }

  onBorrow() {
    this.newBorrowerLog.trimAll();
    if (this.newBorrowerLog.name == '' && this.borrowEquipment.borrower == null)  {
      this.formError = 'Name cannot be empty.';
    } else if (this.newBorrowerLog.utEid == '') {
      this.formError = 'EID cannot be empty.';
    } else {
      console.log(this.newBorrowerLog);
      this.formError = '';
      this.borrowingEquipment = true;
      this.newBorrowerLog.equipmentId = this.borrowEquipment.id;
      this.http.post<CrlBorrowerLog>(environment.urlPrefix + 'api/crl/borrow', this.newBorrowerLog).subscribe(borrowerLog => {
        this.borrowingEquipment = false;
        // this.equipmentList.push(equipment);

        if (this.borrowEquipment.borrower == null) {
          this.equipmentList.filter(e => e.id == this.borrowEquipment.id).map(e => e.borrower = borrowerLog.name);
        } else {
          this.equipmentList.filter(e => e.id == this.borrowEquipment.id).map(e => e.borrower = null);
        }

        this.modalRef.close();
      }, error => {
        this.borrowingEquipment = false;
        console.log(error.error);
        this.formError = error.error;
      });
    }
  }

}

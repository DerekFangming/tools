import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { CrlEquipment } from '../model/cli-equipment';
import { environment } from '../../environments/environment';
import { NgbModalRef, NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-crl-lab',
  templateUrl: './crl-lab.component.html',
  styleUrls: ['./crl-lab.component.css']
})
export class CrlLabComponent implements OnInit {

  equipmentList: CrlEquipment[];
  newEquipment = CrlEquipment.empty();

  formError = '';
  loadingEquipment = true;
  addingEquipment = false;

  modalRef: NgbModalRef;
  @ViewChild('addEquipmentModal', { static: true}) modalContent: TemplateRef<any>;

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
    let ngbModalOptions: NgbModalOptions = {
      backdrop : 'static',
      keyboard : false,
      centered: true
    };
    this.modalRef = this.modalService.open(this.modalContent, ngbModalOptions);
  }


  abc = 'assadasd'

  onAdd() {
    console.log(this.newEquipment);
  }

}

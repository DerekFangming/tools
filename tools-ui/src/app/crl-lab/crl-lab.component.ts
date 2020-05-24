import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { CrlEquipment } from '../model/cli-equipment';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-crl-lab',
  templateUrl: './crl-lab.component.html',
  styleUrls: ['./crl-lab.component.css']
})
export class CrlLabComponent implements OnInit {

  loading = true;
  equipmentList: CrlEquipment[];

  constructor(private http: HttpClient, private title: Title) {
    this.title.setTitle('CRL lab');
    this.http.get<CrlEquipment[]>(environment.urlPrefix + 'api/crl/equipment').subscribe(equipmentList => {
      this.loading = false;
      this.equipmentList = equipmentList;
    }, error => {
      this.loading = false;
      console.log(error.error);
    });
  }

  ngOnInit() {
  }

  onSearchChange(searchValue: string): void {  
    console.log(searchValue);
  }

  onAddBtnClicked() {
    console.log(1);
  }

}

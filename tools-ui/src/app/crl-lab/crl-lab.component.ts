import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-crl-lab',
  templateUrl: './crl-lab.component.html',
  styleUrls: ['./crl-lab.component.css']
})
export class CrlLabComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

  onSearchChange(searchValue: string): void {  
    console.log(searchValue);
  }

  onAddBtnClicked() {
    console.log(1);
  }

}

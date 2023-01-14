import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-info-header',
  templateUrl: './info-header.component.html',
  styleUrls: ['./info-header.component.css']
})
export class InfoHeaderComponent implements OnInit {

  @Input() title : String = "";
  @Input() addRole : string = "";
  @Input() addRouterLink = ['']
  @Input() addButtonRouting: boolean = true;
  @Output() onAddButtonClick: EventEmitter<any> = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {
  }

  handleAddClick(event) {
    this.onAddButtonClick.emit(event);
  }
}

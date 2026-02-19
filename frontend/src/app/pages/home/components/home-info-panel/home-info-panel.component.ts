import {Component, Input} from '@angular/core';
import {EmployeeDto} from "../../../../shared/dtos/employee-dto.model";

@Component({
  selector: 'app-home-info-panel',
  templateUrl: './home-info-panel.component.html',
  styleUrls: ['./home-info-panel.component.css'],
  standalone: false
})
export class HomeInfoPanelComponent {
  @Input({ required: true }) username!: string;
  @Input({ required: true }) role!: string;
  @Input({ required: true }) employee!: EmployeeDto;
}

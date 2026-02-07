import {Component, Input} from '@angular/core';
import {EmployeeDto} from "../../../../shared/dtos/employee-dto.model";

@Component({
  selector: 'app-home-header',
  templateUrl: './home-header.component.html',
  styleUrls: ['./home-header.component.css'],
  standalone: false
})
export class HomeHeaderComponent {
  @Input({ required: true }) employee!: EmployeeDto;
}

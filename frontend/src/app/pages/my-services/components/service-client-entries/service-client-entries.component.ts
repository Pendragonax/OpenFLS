import {Component, Input} from '@angular/core';
import {ClientAndDateServiceDto} from "../../../../shared/dtos/client-and-date-response-dto.model";

@Component({
  selector: 'app-service-client-entries',
  templateUrl: './service-client-entries.component.html',
  styleUrls: ['./service-client-entries.component.css'],
  standalone: false
})
export class ServiceClientEntriesComponent {
  @Input() clientSelected = false;
  @Input() dateDisplay = '';
  @Input() entriesLoading = false;
  @Input() entries: ClientAndDateServiceDto[] = [];
}

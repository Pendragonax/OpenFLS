import {Component, Input} from '@angular/core';
import {PermissionRow} from "../../models/permission-row.model";

@Component({
  selector: 'app-home-permissions-table',
  templateUrl: './home-permissions-table.component.html',
  styleUrls: ['./home-permissions-table.component.css'],
  standalone: false
})
export class HomePermissionsTableComponent {
  @Input({ required: true }) permissions: PermissionRow[] = [];

  readonly tableColumns: ReadonlyArray<string> = ['name', 'lead', 'write', 'read', 'affiliated'];
}

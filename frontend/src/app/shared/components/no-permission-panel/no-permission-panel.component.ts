import {Component, Input} from '@angular/core';

/**
 * Shown instead of a dashboard section the signed in employee may not read, so a
 * missing permission is never mistaken for missing documentation.
 */
@Component({
  selector: 'app-no-permission-panel',
  templateUrl: './no-permission-panel.component.html',
  styleUrls: ['./no-permission-panel.component.css'],
  standalone: false
})
export class NoPermissionPanelComponent {
  @Input() title: string = 'Keine Berechtigung';
  @Input() description: string = 'Sie haben keine Berechtigung, diese Informationen zu sehen.';
}

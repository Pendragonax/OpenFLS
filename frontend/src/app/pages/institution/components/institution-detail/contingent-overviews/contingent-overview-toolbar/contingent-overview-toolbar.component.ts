import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-contingent-overview-toolbar',
  templateUrl: './contingent-overview-toolbar.component.html',
  styleUrls: ['./contingent-overview-toolbar.component.css'],
  standalone: false
})
export class ContingentOverviewToolbarComponent {
  @Input() years: number[] = [];
  @Input() selectedYear: number = new Date(Date.now()).getFullYear();
  @Input() hourTypes: { name: string; value: number }[] = [];
  @Input() selectedHourType: number = 0;
  @Input() exportDisabled: boolean = true;
  @Input() loading: boolean = false;

  @Output() yearChanged = new EventEmitter<number>();
  @Output() hourTypeChanged = new EventEmitter<number>();
  @Output() exportClicked = new EventEmitter<void>();
  @Output() infoClicked = new EventEmitter<Event>();

  onYearSelection(value: number) {
    this.yearChanged.emit(value);
  }

  onHourTypeSelection(value: number) {
    this.hourTypeChanged.emit(value);
  }
}

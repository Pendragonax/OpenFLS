import {Component} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {ServiceBetaNewComponent} from "../service-beta-new/service-beta-new.component";

@Component({
  selector: 'app-service-beta-edit',
  templateUrl: './service-beta-edit.component.html',
  styleUrls: ['./service-beta-edit.component.css'],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    {
      provide: DateAdapter,
      useClass: NativeDateAdapter,
      deps: [MAT_DATE_LOCALE],
    },
    { provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS },
  ],
  standalone: false
})
export class ServiceBetaEditComponent extends ServiceBetaNewComponent {
  pageTitle = 'Eintrag bearbeiten';

  get isLoading(): boolean {
    return this.editMode && !this.clientSelected;
  }
}

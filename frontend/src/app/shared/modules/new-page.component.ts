import { Component, OnInit } from '@angular/core';
import {Location} from "@angular/common";
import {ReplaySubject} from "rxjs";
import {HelperService} from "../../services/helper.service";

@Component({
  selector: 'app-new-page',
  template: ``,
  styles: [
  ]
})
export abstract class NewPageComponent<T> implements OnInit {

  // STATES
  isSubmitting = false;

  // VARs
  value: T = this.getNewValue();
  value$: ReplaySubject<T> = new ReplaySubject<T>(1);

  protected constructor(
    protected helperService: HelperService,
    public location: Location
  ) {
  }

  ngOnInit(): void {
    this.value$.next(this.value);
    this.loadReferenceValues();
  }

  abstract getNewValue(): T;

  abstract loadReferenceValues();

  abstract create();

  abstract initFormSubscriptions();

  handleSuccess(message: string, routeBack: boolean = false, reloadWindow: boolean = false) {
    this.isSubmitting = false;
    this.helperService.openSnackBar(message);

    if (routeBack) {
      this.location.back();
    }

    if (reloadWindow) {
      window.location.reload();
    }
  }

  handleFailure(message: string, routeBack: boolean = false) {
    this.isSubmitting = false;
    this.helperService.openSnackBar(message);

    if (routeBack)
      this.location.back();
  }
}
